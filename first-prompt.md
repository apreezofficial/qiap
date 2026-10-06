# First prompt for Claude Code (paste this)

Read `CLAUDE.md`, `details.md` and `design.md` fully before doing anything.

We're building **Qiap**, starting with **Phase 0** from `details.md` §15, plus the design-system part of `design.md`.

Do this, in order:

1. **Project setup.** New Android project in this folder: Kotlin, Jetpack Compose, Material 3, Gradle KTS with a version catalog, minSdk 26. Package `app.qiap`. Set up R8 + resource shrinking for release, and a Baseline Profile module placeholder. Check current stable versions before pinning anything.
2. **Folder structure.** Create the package layout from `details.md` §4 (empty packages are fine), a manual `AppContainer`, and a nearly empty `Application` class.
3. **Design system** in `core/designsystem`, from `design.md` §3–§6:
   - Color tokens for Dawn and Night, and a theme that switches between them.
   - Type scale (bundle Inter and Plus Jakarta Sans as TTFs, tabular numerals for time/counters).
   - Spacing, radius and shadow objects (only the values in the spec).
   - Components: `PillButton` (primary + secondary), `Chip`, `SegmentedTabs`, `IconTile`, `QiapCard`, `SectionBadge`, `SealStamp` (draw 醒 as a vector path, no CJK font), `FloatingNavPill`.
4. **Navigation shell** with placeholder screens: Home, Editor, Library, Ringing, Workout, History, Settings. Floating bottom nav on Home/Library/History. Ringing uses full-bleed Cinnabar, Workout uses Night.
5. **A single "Design gallery" debug screen** that shows every component and token so I can check consistency by eye.

Rules: no extra libraries without telling me why. No blur effects. Every screen built only from design-system components.

When done, run the unit tests and a release build, then tell me: what you built, APK size, and anything in the spec you found unclear or wanted to change. Don't start Phase 1 until I say so.
