# Device checklist

Things unit tests and CI cannot prove. Run these on a real phone with the **debug** APK from the
latest GitHub Actions run (`qiap-debug-apk`). Tick them off in order; the alarm ones matter most
(`CLAUDE.md`: alarm reliability beats every other feature).

Use a spare minute to read each result aloud-style: write down pass/fail and the phone model.

## 0. Install
- [ ] Settings > Apps > Qiap: allow notifications, allow the camera when asked.
- [ ] Open Qiap > Setup (gear icon). Every row should show **On**. Fix any that do not.

## 1. Alarm rings (priority one)
Use **Setup > Test alarm in 10 s** for quick runs, then repeat once with a real alarm.
- [ ] Screen on, app open: rings within 1 s, ringing screen shows.
- [ ] Screen locked: screen wakes, ringing screen shows over the lock screen, sound plays.
- [ ] App swiped away from recents before it fires: still rings.
- [ ] Phone in Battery saver: still rings.
- [ ] Phone rebooted after saving the alarm: it still rings at the right time (unlock not required).
- [ ] Silent / do-not-disturb on: the alarm stream still plays (alarms are exempt on most phones).

## 2. Can it be dismissed without doing the work?
- [ ] Back button / home / recents on the ringing screen: alarm keeps ringing, screen comes back.
- [ ] Tapping the notification re-opens the ringing screen.
- [ ] Hold "Can't do it today?" for 3 s: lands on **three sums**, not on a dismissed alarm.
- [ ] Wrong answer on the sums restarts from step 1. Three right answers end the alarm.

## 3. Kill and restart (the Phase 3 acceptance test)
Run while the alarm is ringing, once on the ringing screen and once mid-workout:
```bash
adb shell am force-stop app.qiap
```

- [ ] Within a few seconds the alarm rings again and the ringing screen returns.
- [ ] After 30 min total it stops by itself and the day shows as missed in History.

## 4. Snooze
Set an alarm with Snooze 2x.
- [ ] Ringing screen shows "Snooze 5 min - 2 left". Tap it: silence for 5 min, then rings again with 1 left.
- [ ] Reboot during the 5 minutes: it still rings afterwards.
- [ ] With "Earn each snooze with a mini set" on: tapping snooze opens a short workout; finishing it snoozes.
- [ ] With snooze off: no snooze button at all.

## 5. Routine and random
- [ ] Routine of 3: finishing move 1 opens move 2 (fresh counter), then 3, then the seal screen.
- [ ] Random pool alarm: ring twice (test alarm cannot do this; use two real alarms a minute apart) and see it can pick different exercises.

## 6. Camera and pose
- [ ] Debug HUD (top of the workout screen) shows `fps` and `inference ms` and `GPU` or `CPU`.
      **Write the fps down.** Target is 20 or more on a budget phone, 30 or more on a mid-range one.
- [ ] Time from tapping Start workout to the camera showing (pre-warm): should feel instant, under 1 s.
- [ ] Deny the camera permission: the alarm goes to the sums challenge, never to a free pass.
- [ ] Cover the camera or stand in the dark for 10 s: "Step back" cue appears; nothing counts.

## 7. Record fixtures so the thresholds can be tuned
For each exercise below, in the debug workout screen tap **Record**, do **10 clean reps**, tap **Stop**.
Then do it again with **5 sloppy reps** (too shallow, too fast, bad form). Files are saved in
`Android/data/app.qiap/files/fixtures/`. Copy them to the repo's
`app/src/test/resources/fixtures/` and send them over.

Priority order (the ones most likely to be used in an alarm):
1. squat  2. push-up  3. plank (hold 30 s)  4. jumping jack  5. lunge  6. sit-up
7. high knees  8. mountain climber  9. burpee  10. jump squat

Camera placement for each is on the exercise's detail sheet in the Library.

## Where to write results
Add a short note to the pull request: phone model, Android version, pass/fail per section, the fps number.

## 8. Anti-cheat (real alarms only; Library "Try it now" skips these)
- [ ] After "Start workout" the camera asks for a gesture ("Raise your right hand" etc). Doing it starts the count; reps before it do not count.
- [ ] Doing the wrong hand does not pass. After 20 s without the gesture it lets you start anyway (so a bad camera angle cannot trap you).
- [ ] Wave the phone about while exercising: the cue "Prop the phone up and keep it still" appears and reps stop counting. Prop it again: counting resumes.
- [ ] Stand so only your upper half is in frame: "Step back so I can see all of you", nothing counts.
- [ ] After the last rep: "Stand still for a moment", then a different gesture, then the seal screen. Moving around keeps restarting the 3 s stillness.

## 9. Video proof
Turn on Video proof in an alarm, then ring it (test alarm cannot, it has no saved setting; use a real alarm one minute away).
- [ ] The workout screen shows the red REC dot and "Proof - m:ss". If your phone cannot record and count together, the dot is absent and the workout still works. Note which.
- [ ] After the seal screen: History shows a Video proof row. Play works and the scrubber moves.
- [ ] Share opens the share sheet with an MP4 attached.
- [ ] Setup > Keep video proof for: pick 7 days. Videos older than that disappear the next time Home opens.
- [ ] The video file is not in the phone's Gallery (it is private to Qiap).

## 10. Haptics and look
- [ ] A small tick on every rep, a firmer buzz on every 5th.
- [ ] Setup shows a battery guide for your phone's brand. Tick "Done" after following it.
- [ ] Text is readable at the largest font size (Settings > Display > Font size): nothing cut off on Home, the editor, or the ringing screen.
- [ ] TalkBack on: the nav pill, alarm toggle, day chips, and "Start workout" all read out sensible names.
