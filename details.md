# details.md — "Rise" (working name): Workout-to-Dismiss Alarm App

> Spec for Claude Code. Read fully before writing code. Build in the phases at the bottom. Do not skip the performance and reliability rules — they are the product.

---

## 1. Product summary

An Android alarm app that keeps ringing until the user proves they're awake by completing a workout. The phone camera tracks the body with on-device pose detection, counts reps (or hold time) automatically, optionally records a short video as proof, and only then stops the alarm.

**Core promise:** the alarm never fails to ring, never lags, and can't be dismissed without doing the work.

### Goals
- 50+ exercises, each auto-counted by pose detection.
- Rock-solid alarm reliability (Doze, reboot, battery killers, lock screen).
- Smooth UI: 60 fps, no jank, instant interactions.
- Fully on-device. No account, no server, no network required.
- Light: small APK, low RAM, low battery drain.

### Non-goals (v1)
- iOS (platform restrictions make this impossible to do properly; revisit later).
- Cloud sync, social features, accounts.
- Server-side video analysis.

---

## 2. Tech stack (fixed — don't substitute without asking)

| Area | Choice |
|---|---|
| Language | Kotlin (latest stable) |
| UI | Jetpack Compose + Material 3 (heavily customized theme) |
| Min / Target SDK | minSdk 26, targetSdk = latest required by Play |
| Camera | CameraX (`Preview` + `ImageAnalysis` + `VideoCapture`) |
| Pose | MediaPipe Tasks Vision — Pose Landmarker (`lite` default, `full` optional), behind a `PoseEngine` interface so ML Kit can be swapped in |
| Alarm | `AlarmManager.setAlarmClock()` + foreground service + full-screen intent |
| Audio | `MediaPlayer`/ExoPlayer on `USAGE_ALARM`, looping |
| DB | Room (alarms, sessions) |
| Settings | DataStore (Preferences) |
| Async | Coroutines + Flow |
| DI | Manual `AppContainer` (no Hilt/Dagger — keep build + startup light) |
| Navigation | Navigation Compose (type-safe routes) |
| Build | Gradle KTS, version catalog, R8 + resource shrinking in release, Baseline Profiles |
| Testing | JUnit5, Turbine, Robolectric (light), Macrobenchmark |

**Dependency rule:** every new library needs a reason. No Lottie, no Glide/Coil unless truly needed, no analytics SDKs, no Firebase in v1.

Verify current stable versions via Gradle before pinning.

---

## 3. Performance budgets (hard requirements)

| Metric | Target |
|---|---|
| Cold start to alarm list | < 600 ms on mid-range device |
| Pose inference | ≥ 20 FPS effective on a budget device (3 GB RAM, Helio G-class or similar), ≥ 30 FPS on mid-range |
| UI jank | < 1% janky frames in Macrobenchmark on all main flows |
| APK/AAB download | < 40 MB (lite model is ~5–6 MB) |
| RAM during workout | < 300 MB |
| Alarm fire latency | Rings within 1 s of scheduled time |
| Workout screen battery | Camera session capped (default 10 min max, then fallback) |

**Test on budget hardware** (Tecno/Infinix/itel/Samsung A-series class devices), not just a flagship. If it's smooth there it's smooth everywhere.

### Rules to hit these numbers
- `ImageAnalysis` with `STRATEGY_KEEP_ONLY_LATEST`, resolution ~640×480, on a dedicated single-thread executor. Never analyze on the main thread.
- MediaPipe in `LIVE_STREAM` mode with GPU delegate, **fallback to CPU** if GPU init fails.
- Reuse buffers/bitmaps. No allocation in the per-frame hot path (use primitive arrays, preallocated landmark holders).
- Landmark state exposed as a single immutable snapshot via `StateFlow`, collected with `collectAsStateWithLifecycle`.
- Skeleton overlay drawn in **one `Canvas`** (`drawWithCache`/`DrawScope`), not as many composables. Only the overlay and counter recompose per frame, nothing else.
- Mark UI state classes `@Immutable`/`@Stable`; use `key` in lazy lists; no lambdas allocated in hot composables (hoist/remember them).
- Exercise library uses `LazyVerticalGrid` with stable keys and lightweight vector icons, no bitmap decoding on scroll.
- Ship Baseline Profile + Startup Profile. Run R8 full mode.
- Keep `Application.onCreate` nearly empty. Lazy-init the pose model only when the workout screen opens (but **pre-warm it** when the alarm starts ringing so the workout screen opens instantly).

---

## 4. Architecture

Single Gradle module to start (fast builds), strict package boundaries so it can split later.

```
app/src/main/java/app/rise/
├── RiseApp.kt                  // Application, builds AppContainer
├── AppContainer.kt             // manual DI
├── core/
│   ├── designsystem/           // theme, tokens, components
│   ├── util/                   // time, math, permissions helpers
│   └── model/
├── alarm/
│   ├── AlarmScheduler.kt       // setAlarmClock, rescheduling logic
│   ├── AlarmReceiver.kt        // BroadcastReceiver -> starts service
│   ├── BootReceiver.kt         // BOOT_COMPLETED, TIME_SET, TIMEZONE_CHANGED, MY_PACKAGE_REPLACED
│   ├── AlarmService.kt         // foreground service: sound, vibration, notification
│   ├── AlarmSoundPlayer.kt
│   └── RingingActivity.kt      // lock-screen activity
├── pose/
│   ├── PoseEngine.kt           // interface
│   ├── MediaPipePoseEngine.kt
│   ├── PoseFrame.kt            // 33 landmarks + visibility, timestamp
│   ├── LandmarkFilter.kt       // One Euro filter
│   └── PoseMath.kt             // angles, distances, normalization
├── exercise/
│   ├── ExerciseDefinition.kt   // data-driven definition
│   ├── ExerciseCatalog.kt      // all 50+ exercises
│   ├── RepCounter.kt           // generic state-machine engine
│   ├── HoldTimer.kt
│   ├── FormChecker.kt
│   └── AntiCheat.kt
├── camera/
│   ├── CameraController.kt     // CameraX binding, analysis, recording
│   └── VideoRecorder.kt
├── data/
│   ├── db/ (Room: AlarmEntity, SessionEntity, DAOs)
│   ├── SettingsStore.kt
│   └── Repositories
├── ui/
│   ├── home/ editor/ library/ workout/ ringing/ history/ settings/ onboarding/
│   └── navigation/
└── debug/
    └── LandmarkRecorder.kt     // debug-only: export landmark sequences to JSON for tests
```

Layering: `ui → domain(exercise/alarm) → pose/camera/data`. The `exercise` package is **pure Kotlin** (no Android imports) so it's trivially unit-testable.

---

## 5. Alarm engine (reliability is priority #1)

### Scheduling
- Use `AlarmManager.setAlarmClock()` — exempt from Doze, shows the system alarm icon, and doesn't need the `SCHEDULE_EXACT_ALARM` permission.
- Store alarms in Room. Compute `nextTriggerTime` from time + repeat days + timezone. After each fire, schedule the next occurrence immediately.
- Reschedule everything on: `BOOT_COMPLETED`, `LOCKED_BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`, `MY_PACKAGE_REPLACED`.
- Use `directBootAware` receiver so alarms ring after reboot before first unlock.

### Ringing flow
1. `AlarmReceiver` fires → starts `AlarmService` as a foreground service (type `mediaPlayback`) immediately.
2. Service: acquire partial wake lock, start looping alarm audio on `USAGE_ALARM`, vibrate, post a high-priority notification with `fullScreenIntent` → `RingingActivity`.
3. `RingingActivity`: `setShowWhenLocked(true)`, `setTurnScreenOn(true)`, request keyguard dismiss, keep screen on. Shows time + "Start workout" button.
4. Volume: force alarm stream to the user's configured level; ramp up gradually if "gentle wake" is enabled (default off). Re-assert volume if the user lowers it while ringing.
5. If the user leaves the activity, the alarm **keeps ringing** and the notification re-launches the activity. Persist ringing state in DB so a process death restarts it (`START_STICKY`, restore on service restart).

### Dismissal rules
- Alarm stops **only** when `WorkoutSession.success == true`, or via emergency fallback.
- **Snooze:** configurable per alarm: off (default) / allowed N times / "snooze = do a mini set of reps". 
- **Emergency fallback (mandatory, safety):** after X minutes of failed attempts (default 10) or on "I can't do this" tap, offer a fallback dismissal: a 3-step math challenge or type-a-phrase challenge. Log it as `fallbackUsed`. Never let the user get truly stuck (injury, bad lighting, broken camera).
- **Camera failure:** if camera can't open or no pose model, go straight to the fallback.
- **Auto-silence cap:** stop ringing after 30 min (configurable) and log as missed, so a stuck alarm doesn't drain the battery all day.

### Permissions & onboarding checklist
- `POST_NOTIFICATIONS` (Android 13+)
- `USE_FULL_SCREEN_INTENT` (grant status check on Android 14+; deep-link to settings if denied)
- `CAMERA`
- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `WAKE_LOCK`, `VIBRATE`, `RECEIVE_BOOT_COMPLETED`
- Request ignore-battery-optimizations guidance (do not hard-require)
- Onboarding wizard walks through each, with a **"Test alarm in 10 seconds"** button that runs the full real flow.

### OEM battery-killer handling
Budget Android skins (Tecno HiOS, Infinix XOS, itel, Xiaomi MIUI/HyperOS, Oppo/Realme ColorOS, Vivo, Samsung) aggressively kill background apps. Include:
- A detection of `Build.MANUFACTURER` and an in-app guide with screenshots-free step text: "Enable Autostart", "Lock app in recents", "Set battery to Unrestricted".
- A "Reliability score" card on Home showing which checks pass (notifications, full-screen, battery, autostart-acknowledged). Tapping a failing item opens the relevant settings screen.
- Re-arm check: on every app open, verify the next alarm is actually registered with `AlarmManager`.

---

## 6. Pose pipeline

```
CameraX ImageAnalysis (640x480, YUV)
   → PoseEngine.detect(frame)           // MediaPipe, 33 landmarks + visibility
   → LandmarkFilter (One Euro, per landmark)
   → PoseFrame (normalized coords + world coords + timestamp)
   → ExerciseEngine.process(frame)      // RepCounter / HoldTimer / FormChecker / AntiCheat
   → WorkoutUiState (StateFlow)         // reps, phase, feedback, skeleton
```

### Details
- `numPoses = 1`. Use `minPoseDetectionConfidence ≈ 0.5`, `minTrackingConfidence ≈ 0.5`.
- Smooth with a **One Euro filter** (tunable `minCutoff`, `beta`) so counts don't jitter but fast motion still tracks.
- **Normalize by torso length** (mid-shoulder → mid-hip distance) so thresholds work at any distance from camera. Never use raw pixel thresholds.
- Compute angles in 2D image space for most exercises; use **world landmarks (3D)** for rotation-based ones (twists) and depth-sensitive side views.
- A landmark is "usable" only if `visibility ≥ 0.5`. Each exercise declares required landmarks; if any are unusable, show "Move back so we can see your [body part]" and pause counting.
- Mirror the front-camera preview and overlay so it feels natural.
- Pre-flight "setup" step per exercise: shows a pictogram of where to place the phone (front-facing / side view / on floor), a 5-second countdown, and a "full body in frame" check with a green outline when ready.

### Camera orientation per exercise
Each exercise has `cameraView`:
- `FRONT_FACING` — user faces camera (standing front-on).
- `SIDE_VIEW` — user is side-on (push-ups, lunges, planks, sit-ups). 
- `FLOOR_PROP` — phone propped on floor several steps away (floor exercises).

Default camera: front (selfie) so the user can see the screen. Allow rear camera in settings.

---

## 7. Rep counting engine

Data-driven: **every exercise is a definition, not custom code**, so adding exercise #51 is just data. Only a few complex ones (burpee, inchworm, jump-based) use a multi-phase state machine.

### Definition schema

```kotlin
data class ExerciseDefinition(
    val id: String,
    val name: String,
    val category: Category,          // LOWER, UPPER, CORE, CARDIO, MOBILITY
    val type: Type,                  // REPS or HOLD
    val cameraView: CameraView,
    val bodyPosition: Position,      // STANDING, FLOOR_PRONE, FLOOR_SUPINE, SEATED, QUADRUPED
    val requiredLandmarks: Set<Landmark>,
    val signal: Signal,              // how to compute the tracked value from a PoseFrame
    val phases: List<Phase>,         // ordered state machine: name, enter-condition, min duration
    val alternating: Boolean,        // left/right counted per side
    val gates: List<Gate>,           // extra conditions that must hold (body line straight, stance wide, etc.)
    val minRepMs: Long,              // reject reps faster than this
    val minRangeOfMotion: Float,     // reject shallow reps
    val holdBand: ClosedFloatingPointRange<Float>? = null,
    val difficulty: Int,             // 1..5 (used for target scaling)
    val defaultTarget: Int,          // reps or seconds
    val metEstimate: Float,          // for optional calorie estimate
    val tips: List<String>
)
```

### Generic rep state machine (pseudo)

```
state = UP
on each frame (if all required landmarks usable and gates pass):
    v = signal(frame)                       // smoothed
    when state:
        UP   : if v <= downThreshold            -> state = DOWN, tDown = now
        DOWN : if v >= upThreshold:
                   if now - tDown >= minRepMs and rangeReached >= minRangeOfMotion:
                        reps++ ; haptic tick ; feedback "Good"
                   else: feedback "Go deeper" / "Slow down"
                   state = UP
```

Rules:
- **Hysteresis**: down and up thresholds are separated (e.g. 100° / 160°) so jitter can't double-count.
- Track `minValueInRep` to verify depth was actually reached.
- Alternating exercises track left and right as separate counters; a rep counts only when sides alternate (or just count both, configurable).
- Hold exercises: timer runs only while the signal is inside `holdBand` **and** gates pass; pauses (doesn't reset) when form breaks for < 1.5 s, resets after longer.
- Count sound/haptic tick on each rep; larger milestone feedback every 5 reps.

### Signals glossary
- `kneeAngle` = angle(hip, knee, ankle)
- `elbowAngle` = angle(shoulder, elbow, wrist)
- `hipAngle` = angle(shoulder, hip, knee)
- `bodyLine` = angle(shoulder, hip, ankle) — 180° = straight plank
- `torsoLean` = angle of (mid-hip → mid-shoulder) vs vertical
- `hipY`, `wristY`, `ankleY`, `kneeY` = vertical position normalized by torso length vs a calibrated standing baseline
- `dist(a,b)` = distance / torso length
- `shoulderWidth` = |L shoulder − R shoulder|

---

## 8. Exercise catalog (52)

All thresholds are **starting defaults**. They must be tuned using recorded landmark fixtures (see §13). `rep` = counted reps, `hold` = seconds. "alt" = alternating sides.

### Lower body (13)

| ID | Exercise | Type | View | Detection logic |
|---|---|---|---|---|
| L01 | Squat | rep | front | kneeAngle: down < 100°, up > 160° |
| L02 | Sumo squat | rep | front | squat logic + gate: ankle distance > 1.5× shoulderWidth |
| L03 | Jump squat | rep | front | knee < 120° then airborne: hipY rises > 0.25 torso, then landing |
| L04 | Forward lunge | rep alt | side | front knee < 100° then > 160°, count per leg |
| L05 | Reverse lunge | rep alt | side | same, with trailing foot moving behind hip |
| L06 | Side lunge | rep alt | front | hip shifts laterally, bent knee < 110° while other leg > 160° |
| L07 | Wall sit | hold | side | kneeAngle in 80–110°, hipY stable |
| L08 | Glute bridge | rep | side/floor | hipAngle: low < 130°, up > 165° (hips raised) |
| L09 | Calf raise | rep | side | knees > 165°, hipY rises 0.04–0.10 torso then returns |
| L10 | High knees | rep alt | front | knee lifts to ≥ hip height (kneeY ≥ hipY − 0.1) per leg |
| L11 | Butt kicks | rep alt | side | heel within 0.25 torso of hip, kneeAngle < 60° |
| L12 | Skater hop | rep alt | front | hipX swings ±0.5 shoulderWidth, alternating |
| L13 | Standing side leg raise | rep alt | front | ankle abduction > 30° from vertical, per leg |

### Upper body (9)

| ID | Exercise | Type | View | Detection logic |
|---|---|---|---|---|
| U01 | Push-up | rep | side | elbowAngle: down < 90°, up > 155°; gate bodyLine > 160° |
| U02 | Wide push-up | rep | side/front | push-up logic + wrist distance > 1.6× shoulderWidth |
| U03 | Diamond push-up | rep | front | push-up logic + wrist distance < 0.4× shoulderWidth |
| U04 | Knee push-up | rep | side | elbow same as U01; bodyLine measured shoulder-hip-knee > 155° |
| U05 | Pike push-up | rep | side | gate: hipAngle < 110° (inverted V); elbow down < 100°, up > 150° |
| U06 | Tricep dip | rep | side | elbow down < 100°, up > 155°, hips off the floor |
| U07 | Plank shoulder tap | rep alt | front/floor | bodyLine > 160°; wrist reaches opposite shoulder (dist < 0.35 torso) |
| U08 | Arm circles | rep | front | wrist orbits shoulder through 360°, elbow > 150°; one orbit = 1 rep |
| U09 | Shadow overhead press | rep | front | elbow < 90° with wrist at shoulder → > 165° with wrist above head |

### Core (14)

| ID | Exercise | Type | View | Detection logic |
|---|---|---|---|---|
| C01 | Sit-up | rep | side | hipAngle: down > 140° (flat), up < 70° |
| C02 | Crunch | rep | side | torso lifts > 25° from floor, returns < 10° |
| C03 | Bicycle crunch | rep alt | front/floor | elbow to opposite knee dist < 0.3 torso, alternating |
| C04 | Leg raise | rep | side | legs straight (knee > 160°); hipAngle up < 100°, down > 165° |
| C05 | Russian twist | rep alt | front | seated lean-back gate; wrists cross hip line ±0.3 shoulderWidth alternately |
| C06 | Flutter kick | rep alt | side | alternating ankleY amplitude > 0.15 torso, each lift = 1 |
| C07 | Plank | hold | side | bodyLine 160–185°, hips not sagging/piking |
| C08 | Side plank | hold | front/floor | bodyLine > 160°, hip raised, one shoulder over the other |
| C09 | Reverse crunch | rep | side | knees to chest (dist < 0.6 torso), hipAngle < 70° → > 120° |
| C10 | Standing toe touch | rep | front | wrist near ankle (dist < 0.25 torso), hipAngle < 90° → > 165° |
| C11 | Standing oblique crunch | rep alt | front | elbow to same-side knee dist < 0.3 torso |
| C12 | V-up | rep | side | hipAngle < 70° (hands meet feet) → > 150° |
| C13 | Dead bug | rep alt | side/floor | opposite wrist and ankle extend alternately (distance peak) |
| C14 | Bird dog | rep alt | side | opposite wrist–hip–ankle line > 165° held ≥ 1 s = 1 rep |

### Full body & cardio (10)

| ID | Exercise | Type | View | Detection logic |
|---|---|---|---|---|
| F01 | Jumping jack | rep | front | open: wristY above nose & ankle dist > 1.6× shoulderWidth; closed: wrists below shoulders & dist < 1.0 |
| F02 | Burpee | rep | side | state machine: STAND → SQUAT → PLANK (torsoLean > 60° from vertical, bodyLine > 150°) → SQUAT → STAND+jump |
| F03 | Mountain climber | rep alt | side | bodyLine > 150°; hipAngle < 80° per leg alternately |
| F04 | Star jump | rep | front | jumping-jack logic + airborne gate |
| F05 | Squat thrust | rep | side | burpee machine without push-up or jump |
| F06 | Shadow boxing | rep alt | front | elbow > 150° with wrist-to-shoulder distance spike and velocity threshold, per hand |
| F07 | Jump rope (imaginary) | rep | front | hipY oscillation peaks 0.04–0.20 torso (airborne) |
| F08 | Jog in place | rep alt | front | ankleY alternation with knee lift ≥ 0.3 torso |
| F09 | Seal jack | rep | front | arms horizontal open/close + feet wide/together |
| F10 | Inchworm | rep | side | STAND → FOLD (hipAngle < 70°) → PLANK → FOLD → STAND |

### Mobility & yoga holds (6) — "gentle wake" pool

| ID | Exercise | Type | View | Detection logic |
|---|---|---|---|---|
| M01 | Chair pose | hold | side | kneeAngle 100–140°, arms overhead (wristY > noseY) |
| M02 | Warrior II | hold | front | front knee 80–110°, back knee > 165°, arms horizontal (wristY ≈ shoulderY ± 0.15) |
| M03 | Tree pose | hold | front | one foot raised above standing knee, hipX stable, hands together |
| M04 | Downward dog | hold | side | hipAngle 60–110° (inverted V), arms and legs > 155° |
| M05 | Standing torso twist | rep alt | front | world-coords shoulder yaw > 35° each side, alternating |
| M06 | Side bend | rep alt | front | lateral torsoLean > 20° each side, arms overhead |

### Presets / pools
- **Wake-up Lite:** M05, M06, F01, L01, C02
- **Cardio Blast:** F01, F03, L10, F02, F08
- **Strength:** U01, L01, L04, C07, U06
- **Core Crusher:** C01, C03, C04, C07, C12
- **No-floor (small room):** all standing exercises only
- **Quiet (apartment-friendly):** no jumping exercises (excludes L03, F01, F04, F07, F02)

Each alarm can use: one fixed exercise, an ordered routine (up to 3), or **random from a chosen pool** (surprise mode).

---

## 9. Anti-cheat & "are you actually awake" checks

The point is proof of waking, so make it hard to fake without being annoying.

- **Full-body gate:** required landmarks visible (visibility ≥ 0.5) and body bounding box ≥ 40% of frame height.
- **Range-of-motion gate:** per-rep minimum depth/amplitude; shallow reps flash "Go deeper" and don't count.
- **Tempo gate:** `minRepMs` per exercise; blocks waving the phone/arms rapidly.
- **Phone-shake gate:** if the whole skeleton (including supposedly static joints like hips in an upper-body move) moves in unison with high jitter, reject. Use accelerometer: sustained high acceleration = phone is being shaken.
- **Liveness challenge:** at workout start, a random 1-second action ("raise your right hand", "touch your head") verified by pose. Defeats replaying a pre-recorded video of someone exercising.
- **Single-person gate:** if a second person is detected, pause. (Run a second pass or `numPoses = 2` only when suspicious, to save compute.)
- **Final "I'm awake" check:** after the last rep, user must stand still in frame for ~3 seconds and do one more liveness gesture. Then success.
- **Optional face-present check:** nose/eye landmarks visible at start and at end.

Honest caveat: a determined cheater can still fool any on-device system. The goal is to defeat sleepy half-asleep shortcuts, not a motivated attacker.

---

## 10. Video proof (optional per alarm)

- CameraX `VideoCapture` alongside `Preview` + `ImageAnalysis`. This 3-use-case combo isn't supported on every device; **query supported combinations** and fall back to: (a) record at lower analysis resolution, or (b) skip video and mark `videoProof = unavailable`.
- Record 720p, ~1.5–2.5 Mbps, H.264, capped at 60 s (rolling: keep the last portion if the workout is longer).
- Save to **app-private storage**. Never upload anywhere. Auto-delete after N days (default 14, configurable).
- History screen: playback with a tiny scrubber; "Export/Share" via `FileProvider`.
- Show a visible REC indicator while recording.
- v2 idea: burn the skeleton overlay and rep counter into the saved video.

---

## 11. UI / UX

### Design principles
- Dark-first, high contrast, big type. Things must be readable half-asleep at 6 AM.
- **Absolute consistency**: one spacing scale, one radius scale, one type scale, one set of components. No one-off styles inside screens. Every screen is built only from `designsystem` components.
- Motion is purposeful and short (150–250 ms), never blocking input.
- Every interaction gives immediate feedback (ripple + haptic).

### Design tokens (put in `core/designsystem`)
- **Spacing:** 4, 8, 12, 16, 24, 32, 48 dp. Nothing else.
- **Radius:** 8 (chips), 16 (cards), 28 (sheets/buttons), full (pills).
- **Type scale:** Display (alarm time), Headline, Title, Body, Label. Use a variable font with tabular numerals for times and counters (so digits don't jitter).
- **Colors:** one accent (energetic, not generic purple gradient), neutral surfaces, semantic success/warning/error. Define light + dark, but design dark first. No gradient backgrounds as decoration.
- **Elevation:** use tonal surfaces instead of heavy shadows.
- **Components:** `RiseButton`, `RiseCard`, `RiseSwitch`, `DayChip`, `TimeDisplay`, `ExerciseTile`, `ProgressRing`, `RepCounterBig`, `PermissionRow`, `BottomSheet`.

### Screens
1. **Onboarding** — value pitch → permissions wizard → OEM battery guide → test alarm.
2. **Home** — list of alarms (time large, days, exercise chip, toggle), next-alarm countdown, reliability card, FAB to add.
3. **Alarm editor** — time wheel, repeat day chips, exercise picker (fixed / routine / random pool), target count/seconds with difficulty presets, sound picker, volume + gentle ramp, vibration, snooze policy, video proof toggle, label.
4. **Exercise library** — searchable grid, category filter chips, favorites, difficulty badge, "needs floor space" / "jumping" tags, tap for detail sheet: camera-placement diagram, tips, **demo animation** (a ghost skeleton looping through 2–3 keyframe poses using the same skeleton renderer — no video assets), and "Try it now" practice mode.
5. **Ringing screen** — huge time, pulsing subtle indicator, "Start workout" primary button, fallback hidden behind a long-press or after-timeout.
6. **Workout screen** — full-screen camera, skeleton overlay with color-coded joints (green = good form, amber = off), giant rep counter (tabular digits), progress ring toward target, phase hint text ("Lower…", "Push!"), small form-feedback line, exit disabled while alarm active. Haptic + sound per rep.
7. **Success screen** — short celebration (< 1.5 s), streak counter, "Alarm off". Not blocking.
8. **History/Stats** — calendar heatmap, streak, total reps per exercise, videos list, missed/fallback markers.
9. **Settings** — default camera, sound defaults, theme, units, video retention, reliability checklist, debug tools (debug builds only).

### Performance-sensitive UI rules
- Camera preview via `PreviewView` in `AndroidView` with `COMPATIBLE`/`PERFORMANCE` mode chosen for smoothness; overlay is a sibling Canvas, **not** a recomposing tree.
- Rep counter text uses `animateContentTransition` sparingly; avoid heavy animation on the workout screen so GPU stays free for inference.
- No blur or large shadows over the camera preview.
- Screens use `collectAsStateWithLifecycle`; no business logic inside composables.

---

## 12. Data model

```kotlin
@Entity AlarmEntity(
  id, hour, minute, repeatMask /*bit per weekday*/, label, enabled,
  mode /*FIXED|ROUTINE|RANDOM_POOL*/, exerciseIds /*ordered list*/, poolId,
  targetScale /*difficulty multiplier*/, soundUri, volume, gentleRamp, vibrate,
  snoozePolicy, snoozeMax, videoProof, createdAt
)

@Entity SessionEntity(
  id, alarmId, firedAt, startedAt, completedAt, exerciseId,
  targetValue, achievedValue, success, fallbackUsed, snoozeCount,
  videoPath?, avgFps?, notes?
)
```

DataStore keys: default camera, theme, video retention days, gentle wake default, onboarding done, OEM guide acknowledged.

---

## 13. Testing strategy

### Unit tests (pure Kotlin, `exercise/` package)
- **Fixture-based golden tests:** debug build has a `LandmarkRecorder` that exports a pose-frame sequence to JSON while a human performs an exercise (record each exercise ≥ 3 times, correct + sloppy variants). Tests replay fixtures through `RepCounter` and assert exact rep counts. This is how thresholds get tuned. **Do not tune by guessing.**
- Tests for math: angles, normalization, One Euro filter.
- Tests for alarm next-trigger computation (DST, timezone change, repeat masks).
- Anti-cheat tests: shaky/shallow/too-fast fixtures must NOT count.

### Instrumented / manual
- Alarm firing: locked screen, Doze, after reboot, after app swiped from recents, with battery saver on.
- Device matrix: 1 low-end, 1 mid, 1 flagship, plus at least 2 OEM skins.
- Low-light test, backlit test, loose clothing, different heights.
- Macrobenchmark: cold start, library scroll, workout screen open → first frame.

---

## 14. Privacy, Play Store & safety

- Everything on-device. No network permission needed (don't declare `INTERNET` in v1).
- Privacy policy page (required): camera used for pose detection only; video stays on device.
- Play declarations: full-screen intent (alarm app category), foreground service type, camera.
- In-app health disclaimer on first workout: exercise is optional-risk; "Skip/swap exercise" always available; injury-safe "Gentle" pool.
- Never penalize the user for stopping if they're hurt: fallback dismissal is always available.

---

## 15. Build phases (follow in order; each phase ends runnable + tested)

### Phase 0 — Skeleton
- [ ] Project setup, version catalog, theme/tokens, navigation shell, AppContainer, R8/Baseline Profile config.
- **Done when:** empty app builds, themed, navigates between placeholder screens.

### Phase 1 — Pose + one exercise
- [ ] CameraX preview + analysis, `PoseEngine` + MediaPipe lite, One Euro filter, skeleton overlay Canvas.
- [ ] `ExerciseDefinition` + `RepCounter` + squat (L01) with form feedback.
- [ ] `LandmarkRecorder` debug tool and first fixtures.
- **Done when:** squats count accurately at ≥ 20 FPS on a budget device.

### Phase 2 — Alarm core
- [ ] Room + repositories, `AlarmScheduler` (`setAlarmClock`), receivers, `AlarmService`, `RingingActivity`, sound/vibration, reboot rescheduling.
- [ ] Alarm editor + Home list. Onboarding permissions + "test alarm in 10 s".
- **Done when:** alarm reliably rings on lock screen, survives reboot and Doze.

### Phase 3 — Lock alarm behind workout
- [ ] Ringing → workout → success flow, session logging, snooze policy, emergency fallback, auto-silence cap.
- [ ] Pre-warm pose model on ring.
- **Done when:** alarm can only be stopped via reps or fallback; kill-and-restart test passes.

### Phase 4 — Catalog expansion
- [ ] Implement all 52 exercises as data. Add the few complex state machines (burpee, inchworm, jump-based, circular motion).
- [ ] Record fixtures per exercise and tune thresholds until golden tests pass.
- [ ] Library UI, filters, detail sheet, practice mode, presets/pools, random mode.
- **Done when:** every exercise has ≥ 3 passing fixtures and works on-device.

### Phase 5 — Anti-cheat + video proof
- [ ] Gates, tempo, shake detection, liveness gesture, final awake check.
- [ ] CameraX video recording with fallback logic, History playback, retention cleanup.

### Phase 6 — Polish & performance
- [ ] Demo skeleton animations, success/streak screen, stats, haptics/sounds.
- [ ] Macrobenchmarks, Baseline Profile generation, jank fixes, APK size audit.
- [ ] OEM battery guides, reliability score card.
- [ ] Full device-matrix pass, accessibility pass (TalkBack labels, font scaling, contrast).

### Phase 7 — Release prep
- [ ] Privacy policy, Play listing declarations, signed AAB, crash-safe logging (local only).

---

## 16. Working rules for Claude Code

1. Work phase by phase. At the start of each phase, restate the plan in a few lines, then implement. Don't jump ahead.
2. Keep `exercise/` free of Android imports. All thresholds live in `ExerciseCatalog`, never hardcoded in logic.
3. No per-frame allocations in the analysis path. Profile before and after any change touching it.
4. Never tune thresholds by guesswork. Use recorded fixtures and update tests.
5. Every UI element must use design-system components and tokens. If something is missing, add it to the design system first.
6. Keep dependencies minimal; justify any addition in the PR/commit message.
7. After each phase: run unit tests, build release with R8, and report APK size and any measured FPS/jank numbers.
8. Alarm reliability beats everything. If a feature risks the alarm firing, don't ship it.
9. When uncertain about a platform API behavior (full-screen intent rules, FGS types, CameraX use-case combos), check the current Android docs rather than assuming.
10. Commit small and often with clear messages.

---

## 17. Acceptance criteria (v1 ship checklist)

- [ ] Alarm rings on time in 20/20 trials across lock screen, Doze, post-reboot, and swiped-from-recents on at least two OEM skins.
- [ ] Cannot be dismissed without completing target or using fallback.
- [ ] All 52 exercises count correctly in golden fixtures and in live testing.
- [ ] ≥ 20 FPS pose on the budget test device; UI jank < 1%.
- [ ] Cold start < 600 ms on mid-range device; AAB < 40 MB.
- [ ] Video proof records and plays back, or gracefully falls back where unsupported.
- [ ] Camera failure, low light, and injury cases all have a working escape path.
- [ ] No network permission, no data leaves the device.
