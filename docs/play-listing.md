# Play Console answers for Qiap

Copy these into the Play Console. Everything below matches what the app really does as of
Phase 7; if you change behaviour (for example add a network feature), update this file first.

## Store listing

**App name:** Qiap

**Short description (80 max):** The alarm that only stops after you do the reps. Private, on-device.

**Full description (draft):**
> Qiap is an alarm that keeps ringing until you prove you are awake by doing a workout. Your phone's
> camera counts your reps, right on the phone. No account, no ads, and no internet permission.
>
> - Pick a workout, a routine of up to three, or a random surprise from a preset
> - 52 exercises, counted with on-device pose detection (some are still being tuned)
> - Rings reliably: exact alarms, over the lock screen, after a reboot
> - Optional video proof, saved only on your phone and deleted after 7, 14 or 30 days
> - Snooze off by default; optionally earn a snooze with a mini set
> - An emergency exit (three quick sums) so you are never stuck if you are hurt or the camera fails
> - A seal for every morning you win, and a streak to protect
>
> Exercise carries some risk. Qiap is not medical advice. Do what is comfortable for you.

**Category:** Tools (alternative: Health & Fitness)
**Contact email:** hello@qiap.app (make this a real inbox)
**Privacy policy URL:** https://qiap.app/privacy

**Screenshots to capture (phone, 2 to 8):** Home with two alarms; the ringing screen (full cinnabar);
the workout screen mid-rep; the exercise library; the History seal calendar; the alarm editor.
Take them from the debug or release build on a real phone.

## App content

| Question | Answer |
|---|---|
| Ads | No |
| App access | All functionality available without login (no accounts) |
| Target audience | 13 and over (not designed for children; avoids the Families policy) |
| Content rating (IARC) | No violence, sexual content, language, gambling or user-generated content: expect Everyone |
| Health apps declaration | Fitness/wellness alarm; not a medical device; no health data is collected |
| Government / financial / news | No |

## Data safety

| Section | Answer |
|---|---|
| Does the app collect or share user data? | **No data collected, no data shared** |
| Camera | Used on the device to count reps; frames are not stored or transmitted. Not "collected" because it never leaves the device |
| Video proof | Optional, stored only in app-private storage, user-controlled retention; not transmitted |
| Data encrypted in transit | Not applicable: the app has no INTERNET permission |
| Can users request deletion | Everything is local: uninstalling deletes it (Android backup is off) |
| Third-party SDKs | Google MediaPipe (on-device model). Its telemetry cannot run because the app strips the INTERNET permission, and CI fails the build if INTERNET ever reappears |

## Permission declarations

| Permission | Why (use this wording) |
|---|---|
| `USE_EXACT_ALARM` | Core function: Qiap is an alarm clock. It must ring at the exact minute the user chose |
| `USE_FULL_SCREEN_INTENT` | Core function: shows the ringing screen over the lock screen when an alarm fires, as an alarm clock app does |
| `FOREGROUND_SERVICE` + `..._SYSTEM_EXEMPTED` | Plays the alarm sound until the user finishes the workout. System-exempted is allowed because the app holds exact-alarm permission |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Fallback type if exact alarms are revoked on Android 12/12L. Subtype: "Alarm clock: plays the alarm sound until the user dismisses it by completing a workout." |
| `CAMERA` | Count exercise reps with on-device pose detection; optional video proof. `camera.front` is not required, so the alarm still installs without it |
| `POST_NOTIFICATIONS` | Show the alarm notification that launches the ringing screen |
| `RECEIVE_BOOT_COMPLETED` | Re-arm alarms after a restart |
| `WAKE_LOCK` | Keep the CPU on while an alarm is ringing |
| `VIBRATE` | Vibrate with the alarm |
| `SCHEDULE_EXACT_ALARM` (API 31-32 only) | Same as `USE_EXACT_ALARM` on versions that predate it |

**Foreground service video (if Play asks):** record the alarm firing over the lock screen, the sound
continuing while the workout runs, and the alarm stopping when the reps are done.

## Release notes (first test)
> First public test. Alarms, 52 exercises (several still being tuned), routines, snooze options,
> optional video proof, and an emergency exit. Please tell us which exercises miscount on your phone.
