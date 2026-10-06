# Qiap — project memory for Claude Code

Qiap is an Android alarm app that only stops ringing after the user completes a camera-tracked workout (on-device pose detection, 52 exercises, optional video proof).

## Source of truth
- `details.md` — product, stack, architecture, alarm engine, pose pipeline, exercise catalog, phases, acceptance criteria.
- `design.md` — brand, tokens, components, screens, landing page. **If it conflicts with the UI section of `details.md`, `design.md` wins** (light "Dawn" theme by default; Night only for ringing + workout).

Read both fully before writing code. Work phase by phase as laid out in `details.md` §15. Do not jump ahead.

## Hard rules
- Kotlin + Jetpack Compose + CameraX + MediaPipe Pose (behind a `PoseEngine` interface). Manual DI, no Hilt. No extra libraries without a stated reason.
- Alarm reliability beats every other feature. If a change risks the alarm firing, don't ship it.
- No allocations in the per-frame analysis path. No blur or glass over the camera preview.
- `exercise/` package stays pure Kotlin (no Android imports). Thresholds live only in `ExerciseCatalog`.
- Tune exercise thresholds from recorded landmark fixtures, never by guessing.
- Every UI element comes from the design system (`core/designsystem`). Missing component? Add it there first.
- No network permission in v1. Everything on-device.
- Check current Android docs for any API you are unsure about (full-screen intents, foreground service types, CameraX use-case combinations).

## Definition of done for each phase
Unit tests pass, release build with R8 succeeds, and you report APK size plus any measured FPS/jank numbers.

## Working style
Restate the plan in a few lines at the start of each phase, then implement. Commit small and often.
