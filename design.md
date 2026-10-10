# design.md — Qiap brand & UI system

> Companion to `details.md`. **Where this file and §11 (UI/UX) of `details.md` disagree, this file wins.** Specifically: the app is now **Dawn (light) by default**, with Night (dark) only for the ringing and workout screens. `details.md` said dark-first; that is superseded.

Reference: Dribbble "AI Travel Assistant App Landing Page" (Travexa). We copy its *system* (layout rhythm, type treatment, pill/chip language, soft sky backdrops, phone showcases, calm motion), **not its content, copy, imagery, logo or brand**. Everything below is re-themed for Qiap.

Colors in this file are estimated from screenshots. Treat them as starting values and tune by eye.

---

## 1. Brand

- **Name:** Qiap (起 qǐ = rise / get up, fused with "ap").
- **Tagline options:** "Wake up. Prove it." (default) / "Rise, then ring off." / "No reps, no off switch."
- **Wordmark:** lowercase `qiap`, same grotesk as headings, medium weight, tight tracking. Left of it: a small glyph, a half-sun arc rising over a short horizon line (reads as dawn and as a squat curve). Single color, works at 16px.
- **Signature move: the seal.** Finishing a workout stamps a red 醒 seal (Cinnabar) with a haptic. The same seal is the **app icon** (red seal on paper-white). Draw the character as a vector path so no CJK font is bundled. History screen = a grid of stamps.
- **Voice:** short, direct, a little cheeky. Never preachy about fitness. Examples: "Your bed lost." / "12 reps to silence me." / "Seal earned."

---

## 2. Design principles (from the reference)

1. **Calm, airy, lots of white.** Large radii, hairline borders, soft shadows. Nothing shouts except the one dark CTA.
2. **Two-tone headlines.** Every section heading is black with one phrase in mid-gray: "Everything you need for a **Smarter Journey.**" For Qiap: "Everything you need to **actually get up.**"
3. **One dark pill is the action.** Primary CTA is a near-black pill. Secondary is a pale glass pill. Color accents are tiny (badges, active tab, dots).
4. **Soft sky backdrop, used sparingly.** Pale blue arch/cloud washes behind phones and section headers. Not a hero gradient, not decoration everywhere.
5. **Pills for everything small.** Chips, tabs, badges, toggles, nav, inputs are all fully rounded.
6. **Phones are the hero.** Show the real UI, center phone large, side phones smaller and faded.
7. **Faded edges.** Marquees, carousels and long lists fade out at the edges/bottom with a mask instead of a hard cut.

---

## 3. Color tokens

### Dawn (light), default
| Token | Hex | Use |
|---|---|---|
| `bg` | `#FFFFFF` | page / screen background |
| `bg-wash` | `#EEF5FF` | sky wash behind hero, phone arch, CTA banner |
| `bg-wash-2` | `#DCEBFF` | deeper sky for cards (how-it-works, FAQ card) |
| `surface` | `#FFFFFF` | cards |
| `surface-muted` | `#F5F5F6` | pricing header, newsletter card, icon tiles |
| `border` | `#ECECEE` | 1px hairlines, card outlines |
| `ink` | `#111114` | headlines, primary text, dark pill |
| `ink-2` | `#6B6B72` | body text, headline accent phrase |
| `ink-3` | `#74747E` | captions, dates, placeholders (was #9A9AA2; deepened to pass contrast, Phase 6) |
| `sky` | `#2B74D6` | active tab, small links ("Get Access"), toggles (was #4A90E2; white text on it was 3.3:1, now 4.6:1, Phase 6) |
| `sky-strong` | `#1A56F0` | "Most Popular"-type badge only |
| `cinnabar` | `#F2542D` | **ringing screen, seal stamp, alarm-on state**. Not a general accent. |
| `jade` | `#34C38F` | good form, checks, success |
| `saffron` | `#F5B83D` | off form, streak, "Save 15%"-style highlight chip |
| `crimson` | `#D7263D` | errors only |

### Night (dark), ringing + workout screens only
| Token | Hex | Use |
|---|---|---|
| `night-bg` | `#0D0D0F` | background |
| `night-surface` | `#17171A` / `#202024` | cards |
| `night-ink` | `#F4EFE6` | text |
| `night-ink-2` | `#A09B92` | secondary text |

Rules:
- Ringing screen = full-bleed **Cinnabar** with ink-black giant time. It is the only loud screen, on purpose.
- Workout screen = Night, because it sits on a camera feed.
- Light theme is the default for Home, Editor, Library, History, Settings. Follow system dark mode there by mapping to Night tokens.
- Keep sky washes as one reusable asset (see §9). Behind a single token (`wash.enabled`) so it can be switched off.

---

## 4. Typography

- **Headings / UI:** Inter (or Inter Tight). The reference looks like Inter with tight tracking. Weight 500, **letter-spacing about −2% to −3%** on large sizes, line-height about 1.05–1.1.
- **App-screen UI text:** Plus Jakarta Sans (the reference's phone UI looks like it). Friendly, rounder. Use for in-app labels and the big counters.
- **Numbers (alarm time, rep counter, stats):** tabular numerals (`tnum`) always, so digits never jitter.
- Bundle TTFs, subsetted to used weights (400/500/600). No downloadable fonts in the Android app (offline, faster).
- Website: `next/font` self-hosted, `display: swap`.

### Scale (web / Android sp)
| Role | Web | Android | Notes |
|---|---|---|---|
| Display (alarm time, rep count) | 96–120 | 96 | Plus Jakarta, tnum, 600 |
| H1 hero | 72 / 48 mobile | n/a | Inter 500, −3% |
| H2 section | 48 / 32 mobile | 28 | two-tone |
| H3 card title | 20 | 20 | 600 |
| Body | 16–18 | 16 | `ink-2` |
| Label / chip | 13–14 | 13 | 500 |
| Caption | 12 | 12 | `ink-3` |

---

## 5. Shape, spacing, elevation

- **Spacing scale (only these):** 4, 8, 12, 16, 24, 32, 48, 64, 96 (web sections use 96 between).
- **Radius:** chips/pills = full; small cards = 16; large cards/bento = 24; phone-showcase containers = 32; inputs = full.
- **Borders:** 1px `border`. Cards are white with a hairline plus a very soft shadow.
- **Shadow:** one soft level: `0 8px 24px rgba(17,17,20,0.06)`. Dark primary pill gets a tighter, stronger shadow: `0 6px 16px rgba(17,17,20,0.25)`.
- **Glass:** secondary pill and floating overlay cards = white at 70–80% opacity + 1px white border + faint blue-white gradient. On Android, **do not use real-time blur** (expensive on budget phones). Fake glass with a flat translucent fill.
- **Background texture:** faint dot grid fading out behind the hero (web only), 4% opacity.

---

## 6. Components

Build each once, reuse everywhere. No one-off styles.

| Component | Spec |
|---|---|
| **PillButton / primary** | `ink` fill, white text, full radius, 48–52px tall, 24px h-padding, strong shadow. Optional leading icon. |
| **PillButton / secondary** | glass pill, `ink` text, light blue-white gradient, hairline. Optional play icon ("Watch demo"). |
| **IconTile** | 48px square, radius 12, `surface-muted`, centered icon. Used for store buttons / quick actions. |
| **Chip** | full radius, white, hairline, 13px label, optional leading icon (feature marquee, audience hashtags, categories). |
| **SectionBadge** | small pill above each H2: ★ icon in `sky` + label ("Our Features"). Light fill. |
| **SegmentedTabs** | pill container; active segment filled `sky` with white text, others plain. Sliding indicator, 200ms. |
| **FeatureCard (bento)** | radius 24, photo/sky background, title + 1–2 lines on top, overlay glass UI card at bottom. |
| **Stat card** | big number (`94%`) + small label over a dark image with bottom scrim. |
| **Testimonial card** | white, radius 20, avatar + name + role, small circular icon button top-right, bold title, gray body, date caption. Masonry 3 columns, fade at bottom, "Show more" glass pill. |
| **Accordion** | hairline dividers, 16px label, chevron rotates, answer in `ink-2`. |
| **PriceCard** | muted header (icon, name, price right-aligned), white body with check list + hairline dividers, bottom pill CTA. Middle card has "Most popular" `sky-strong` badge and dark CTA. |
| **Toggle (billing)** | two pills; active = `ink` fill; saffron "Save 15%" chip tilted slightly above the second. |
| **CarouselControls** | two 44px circles: prev (gray), next (`ink`). |
| **PhoneFrame** | rounded device frame, soft drop shadow, bottom fade-out. Center large; side phones 85% scale, 60–70% opacity. |
| **ExerciseTile** | square tile, 3D-ish or flat vector pictogram of the exercise, name, difficulty dots. |
| **SealStamp** | Cinnabar 醒 inside a rounded square, slight rotation (−4°), used on success + history. |

---

## 7. App screens (Android, Compose)

Same tokens as the web. Reference phone UI gives the pattern: big greeting line, rounded search, horizontal "insight" tiles with illustrated icons, pill filter chips, rounded image cards, floating bottom nav pill.

| Screen | Theme | Layout |
|---|---|---|
| **Home** | Dawn | Top: "Good morning" small + next alarm countdown big ("in 7h 32m"). Alarm cards: huge `tnum` time, day dots, exercise chip, square toggle. Reliability card (pass/fail rows). Floating black pill "+ New alarm". |
| **Alarm editor** | Dawn | Time wheel, day chips (pills), exercise picker row, target stepper, sound/volume rows, snooze policy, video-proof toggle. Sticky bottom primary pill. |
| **Exercise library** | Dawn | Search pill, category SegmentedTabs (All / Lower / Upper / Core / Cardio / Mobility), **bento grid** of ExerciseTiles with illustrated pictograms from the skeleton renderer, favorites, "no jumping" / "needs floor" chips. Tap = detail sheet with ghost-skeleton demo loop + camera-placement diagram + "Try it now". |
| **Ringing** | Cinnabar | Full-bleed Cinnabar, giant ink time, slow-pulsing sun circle, one big white pill "Start workout". Fallback hidden behind long-press / timeout. |
| **Workout** | Night | Full camera, paper-white skeleton, jade/saffron joints, giant rep number bottom-left, thick progress bar, small form-feedback pill. **No blur or glass over the camera.** |
| **Success** | Dawn | SealStamp slams in (under 1.5s), streak number, "Alarm off". |
| **History** | Dawn | Calendar grid of seal stamps (missed = empty dashed square, fallback = saffron-edged), streak card, per-exercise totals, video list. |
| **Settings / Onboarding** | Dawn | Onboarding uses the phone-trio hero idea: big two-tone headline, step chips, permission rows, "Test alarm in 10s" primary pill. |

Floating bottom nav: white pill, 3 icons (Alarms, Library, History), active icon has `ink` filled circle. Hidden during ringing/workout.

---

## 8. Landing page (Next.js + Tailwind, static)

Follows the reference's section order, adapted to Qiap. Build in `web/` inside the repo (or a separate repo). Static export, no heavy JS, no runtime blur.

1. **Nav:** floating rounded bar, logo left, links center (Features, Exercises, How it works, FAQ, Download), right: two IconTiles (App Store disabled "soon" / Play Store).
2. **Hero:** top chip with avatar stack "Join the early wake-ups" + `sky` "Get access →" pill. H1 two lines centered, two-tone: **"Wake up. Prove it."** / sub: "The alarm that only shuts up after you do the reps." Buttons: dark "Download for Android" + glass "Watch demo". Dot-grid texture behind.
3. **Phone trio** (workout / ringing / library) on the pale sky arch, bottom faded.
4. **Feature chip marquee** ("Our top-notch features"): 52 Exercises · Camera rep counting · Works offline · Reliable alarms · Video proof · Anti-cheat · Streak seals. Edge fade, slow auto-scroll, pause on hover/reduced-motion.
5. **Two-tone statement** + cloud wash + audience hashtags (#Heavy sleepers #Students #Night-shift #Gym beginners #Runners).
6. **Phone carousel** (5 phones, side ones faded) + "Rise with purpose, not snooze" + download pills.
7. **Bento features:** (a) Pose-tracked reps, (b) Alarm that never fails (dark card, notification-style overlay), (c) 52 exercises (grid of tiles), (d) Video proof & seals. Use illustrated/skeleton visuals, not stock photos, to keep it on-brand and light.
8. **Persona/Mode tabs:** Wake-up Lite / Cardio Blast / Strength / Core Crusher / Quiet. Active tab swaps big card (image + one-line + stat).
9. **How it works:** three soft-blue cards: 1 Set your alarm → 2 Do the reps → 3 Get your seal.
10. **Social proof:** center-focus filmstrip carousel (grayscale sides, color center with glass quote). Only use real user quotes once they exist. **Do not ship fake testimonials.** Use placeholders clearly marked until real.
11. **Reviews masonry:** same rule, real reviews only.
12. **Pricing (optional):** Free (core alarm + a handful of exercises) / Pro (full 52, video proof, history). Delete this section if the app is fully free at launch.
13. **FAQ:** two-column, left sky card "Still stuck? Contact us". Questions: Does it work offline? What if I can't do it (injury)? Is my video uploaded? Does it work with the screen locked? Which phones?
14. **CTA banner:** rounded pale-blue container, text left, cropped phone right.
15. **Footer:** logo + one-liner + social circles, link columns, newsletter card (pill input + dark Subscribe), bottom bar.

Copy rules: short, concrete, no hype numbers you can't back up.

---

## 9. Assets & performance

- **Sky wash:** one pre-rendered, compressed WebP/AVIF cloud-arch image (about 20–40 KB) reused as a CSS background. On Android, a single vector/WebP asset drawn once. No runtime blur anywhere.
- **Icons:** one icon set (Lucide or Phosphor), stroke 1.75, subset to used icons.
- **Illustrations:** exercise pictograms generated from the same skeleton renderer (vector). Keeps APK small and the style consistent.
- **Photos (web only):** if used, AVIF/WebP, lazy, with explicit width/height. Only licensed or original images.
- **Web budget:** LCP under 2s on mid-range mobile on 4G, JS under 100 KB gzipped for the landing page, CLS about 0.
- **Android budget:** unchanged from `details.md` §3 (APK under 40 MB, UI jank under 1%). Glass/shadows must not cause overdraw on the workout screen.

---

## 10. Motion

- Default: **200 ms**, one easing curve everywhere (`cubic-bezier(0.2, 0.8, 0.2, 1)`).
- SegmentedTabs: sliding indicator. Accordion: height + chevron rotate. Carousels: center item scales up, sides desaturate and fade. Marquee: slow linear.
- Seal stamp: scale 1.6 → 1.0 with a tiny overshoot, haptic on contact, 400 ms total.
- Ringing sun circle: 3s pulse, opacity only.
- Respect reduced-motion: marquee stops, carousel snaps, stamp appears without scale.

---

## 11. Do / don't

- **Do** keep one dark pill as the action on every screen.
- **Do** use two-tone headlines everywhere on the web.
- **Do** keep Cinnabar rare so the ringing screen and seal stay special.
- **Don't** copy Travexa's name, copy, photos, 3D icons or logo.
- **Don't** put blur or glass over the camera feed.
- **Don't** add gradients as decoration. The sky wash is the only soft background effect.
- **Don't** invent testimonials, member counts or stats. Leave placeholders until real.

---

## 12. Instructions for Claude Code

1. Read `details.md` and this file together. If they conflict on visuals, this file wins.
2. Phase 0 (Android): implement §3–§6 as the Compose design system first (`core/designsystem`): color tokens (Dawn + Night), type scale, spacing/radius objects, then PillButton, Chip, SegmentedTabs, IconTile, Card, SealStamp, FloatingNavPill.
3. Build screens only from those components. If something is missing, add it to the design system first.
4. Landing page is a separate track (Next.js + Tailwind, `web/`), started after the Android Phase 1 pose prototype works. Use the same tokens as Tailwind theme values.
5. Check every screen at 360dp width and on a budget device before moving on.
