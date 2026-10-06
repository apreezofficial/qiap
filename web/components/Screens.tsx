import { StatusBar } from "./Phone";
import { Seal } from "./Brand";
import { Skeleton } from "./Skeleton";
import { IconAlarm, IconGrid, IconCalendar, IconSearch, IconFlame, IconPlay } from "./Icons";

/* All screens are authored for a 266×566 viewport (inside PhoneFrame). They use the same tokens as the Android app. */

function FloatingNav({ active = 0, dark = false }: { active?: number; dark?: boolean }) {
  const items = [IconAlarm, IconGrid, IconCalendar];
  return (
    <div
      className={`absolute bottom-3 left-1/2 z-20 flex -translate-x-1/2 items-center gap-1 rounded-full p-1.5 shadow-[0_8px_20px_rgba(17,17,20,0.14)] ${dark ? "bg-night-2" : "bg-white"}`}
    >
      {items.map((I, i) => (
        <span
          key={i}
          className={`grid size-9 place-items-center rounded-full ${
            i === active ? (dark ? "bg-paper text-ink" : "bg-ink text-white") : dark ? "text-paper/60" : "text-ink-3"
          }`}
        >
          <I size={16} />
        </span>
      ))}
    </div>
  );
}

function Toggle({ on = true }: { on?: boolean }) {
  return (
    <span className={`relative h-[22px] w-[38px] rounded-[8px] ${on ? "bg-ink" : "bg-line"}`}>
      <span
        className={`absolute top-[3px] size-4 rounded-[5px] bg-white transition-all ${on ? "left-[19px]" : "left-[3px]"}`}
      />
    </span>
  );
}

/* ───────── Home (Dawn) ───────── */
export function HomeScreen() {
  return (
    <div className="relative h-full bg-white">
      <div className="sky-wash absolute inset-x-0 top-0 h-56" />
      <StatusBar />
      <div className="relative px-4 pt-14">
        <p className="text-[11px] font-medium text-ink-2">Good morning</p>
        <p className="mt-0.5 text-[11px] text-ink-3">Next alarm in</p>
        <p className="tnum text-[34px] font-semibold leading-none tracking-[-0.03em]">7h 32m</p>

        <div className="mt-4 space-y-2.5">
          {[
            { t: "06:30", d: "Mon – Fri", e: "Squats × 15", on: true },
            { t: "07:45", d: "Sat", e: "Burpees × 10", on: true },
            { t: "08:30", d: "Sun", e: "Plank 45s", on: false },
          ].map((a) => (
            <div key={a.t} className="rounded-[18px] border border-line bg-white p-3 shadow-soft">
              <div className="flex items-center justify-between">
                <span className={`tnum text-[28px] font-semibold leading-none tracking-[-0.03em] ${a.on ? "" : "text-ink-3"}`}>
                  {a.t}
                </span>
                <Toggle on={a.on} />
              </div>
              <div className="mt-2 flex items-center gap-1.5">
                <span className="rounded-full bg-wash px-2 py-0.5 text-[9px] font-medium">{a.d}</span>
                <span className="rounded-full bg-surface-muted px-2 py-0.5 text-[9px] font-medium">{a.e}</span>
              </div>
            </div>
          ))}
        </div>

        <div className="mt-3 flex items-center gap-2 rounded-[14px] bg-surface-muted px-3 py-2 text-[10px] font-medium">
          <span className="size-1.5 rounded-full bg-jade" />
          Reliability: all checks passed
        </div>
      </div>
      <div className="absolute bottom-3 left-1/2 z-20 -translate-x-1/2">
        <span className="inline-flex h-10 items-center gap-1.5 rounded-full bg-ink px-4 text-[12px] font-medium text-white shadow-pill">
          + New alarm
        </span>
      </div>
    </div>
  );
}

/* ───────── Ringing (Cinnabar) ───────── */
export function RingingScreen() {
  return (
    <div className="relative h-full overflow-hidden bg-cinnabar text-ink">
      <StatusBar light time="6:30" />
      <div className="sunpulse absolute left-1/2 top-[84px] size-[210px] -translate-x-1/2 rounded-full bg-white/25" />
      <div className="sunpulse absolute left-1/2 top-[114px] size-[150px] -translate-x-1/2 rounded-full bg-white/30 [animation-delay:400ms]" />
      <div className="relative flex h-full flex-col items-center px-5 pt-[120px] text-center">
        <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-ink/70">Rise</p>
        <p className="tnum mt-1 text-[78px] font-semibold leading-none tracking-[-0.05em]">6:30</p>
        <p className="mt-2 text-[13px] font-medium text-ink/80">15 squats to silence me.</p>
        <div className="mt-auto pb-8">
          <span className="inline-flex h-12 items-center rounded-full bg-white px-8 text-[14px] font-semibold shadow-[0_10px_24px_rgba(0,0,0,0.2)]">
            Start workout
          </span>
          <p className="mt-3 text-[9px] font-medium text-ink/60">Can&apos;t do it today? Hold for backup</p>
        </div>
      </div>
    </div>
  );
}

/* ───────── Workout (Night) ───────── */
export function WorkoutScreen({ pose = "squat" as "squat" | "stand" }) {
  return (
    <div className="relative h-full overflow-hidden bg-night text-paper">
      {/* faux camera feed: dim room, no blur */}
      <div className="absolute inset-0 bg-[radial-gradient(90%_60%_at_50%_30%,#2b2b33_0%,#121215_70%)]" />
      <div className="absolute inset-x-0 bottom-0 h-1/2 bg-gradient-to-t from-black/70 to-transparent" />
      <StatusBar light time="6:31" />

      <div className="absolute left-3 top-12 z-10 flex items-center gap-1.5 rounded-full bg-black/45 px-2.5 py-1 text-[9px] font-semibold">
        <span className="size-1.5 rounded-full bg-crimson" /> REC
      </div>
      <div className="absolute right-3 top-12 z-10 rounded-full bg-black/45 px-2.5 py-1 text-[9px] font-semibold text-jade">
        Full body ✓
      </div>

      <Skeleton pose={pose} className="absolute left-1/2 top-[120px] w-[210px] -translate-x-1/2" />

      <div className="absolute inset-x-0 bottom-0 z-10 px-4 pb-5">
        <div className="mb-2 inline-flex rounded-full bg-black/55 px-3 py-1 text-[10px] font-medium">Lower… then push!</div>
        <div className="flex items-end gap-2">
          <span className="tnum text-[96px] font-semibold leading-[0.8] tracking-[-0.05em]">12</span>
          <span className="tnum mb-1 text-[20px] font-medium text-paper/60">/ 15</span>
        </div>
        <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-white/15">
          <div className="h-full w-[80%] rounded-full bg-jade" />
        </div>
      </div>
    </div>
  );
}

/* ───────── Library (Dawn, bento tiles) ───────── */
export function LibraryScreen() {
  const tiles = [
    { n: "Squat", d: 2, p: "squat" as const },
    { n: "Jumping jack", d: 2, p: "arms-up" as const },
    { n: "Plank", d: 3, p: "plank" as const },
    { n: "Lunge", d: 3, p: "stand" as const },
  ];
  return (
    <div className="relative h-full bg-white">
      <div className="sky-wash absolute inset-x-0 top-0 h-44" />
      <StatusBar />
      <div className="relative px-4 pt-14">
        <p className="text-[19px] font-semibold leading-tight tracking-[-0.02em]">
          Pick your <span className="text-ink-2">poison.</span>
        </p>
        <div className="mt-3 flex h-9 items-center gap-2 rounded-full border border-line bg-white px-3 text-[10px] text-ink-3">
          <IconSearch size={14} /> Search 52 exercises
        </div>
        <div className="no-scrollbar mt-3 flex gap-1.5 overflow-hidden">
          {["All", "Lower", "Upper", "Core"].map((c, i) => (
            <span
              key={c}
              className={`rounded-full px-3 py-1 text-[10px] font-medium ${i === 0 ? "bg-sky text-white" : "border border-line bg-white"}`}
            >
              {c}
            </span>
          ))}
        </div>
        <div className="mt-3 grid grid-cols-2 gap-2.5">
          {tiles.map((t) => (
            <div key={t.n} className="rounded-[18px] border border-line bg-white p-2.5 shadow-soft">
              <div className="grid h-[84px] place-items-center rounded-[12px] bg-night">
                <Skeleton pose={t.p} className="h-[72px]" />
              </div>
              <p className="mt-2 text-[11px] font-semibold">{t.n}</p>
              <div className="mt-1 flex gap-0.5">
                {[1, 2, 3, 4, 5].map((i) => (
                  <span key={i} className={`size-1.5 rounded-full ${i <= t.d ? "bg-ink" : "bg-line"}`} />
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
      <FloatingNav active={1} />
    </div>
  );
}

/* ───────── History (Dawn, seals) ───────── */
export function HistoryScreen() {
  // 5 weeks × 7 days. 1 = seal, 0 = missed, 2 = fallback used
  const cells = [1, 1, 1, 0, 1, 1, 1, 1, 1, 2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1];
  return (
    <div className="relative h-full bg-white">
      <div className="sky-wash absolute inset-x-0 top-0 h-44" />
      <StatusBar />
      <div className="relative px-4 pt-14">
        <p className="text-[19px] font-semibold leading-tight tracking-[-0.02em]">
          Your <span className="text-ink-2">seals.</span>
        </p>
        <div className="mt-3 flex items-center gap-3 rounded-[18px] border border-line bg-white p-3 shadow-soft">
          <span className="grid size-10 place-items-center rounded-full bg-saffron/20 text-[#b57d00]">
            <IconFlame size={20} />
          </span>
          <div>
            <p className="tnum text-[22px] font-semibold leading-none">12 days</p>
            <p className="mt-0.5 text-[9px] text-ink-3">Current streak</p>
          </div>
        </div>
        <div className="mt-3 grid grid-cols-7 gap-1.5">
          {cells.map((c, i) =>
            c === 0 ? (
              <Seal key={i} size={30} dashed />
            ) : (
              <span key={i} className={c === 2 ? "rounded-[22%] ring-2 ring-saffron" : ""}>
                <Seal size={30} />
              </span>
            ),
          )}
        </div>
        <p className="mt-3 text-[9px] text-ink-3">Dashed = missed · Gold ring = backup used</p>
      </div>
      <FloatingNav active={2} />
    </div>
  );
}

/* ───────── Editor (Dawn) ───────── */
export function EditorScreen() {
  return (
    <div className="relative h-full bg-white">
      <div className="sky-wash absolute inset-x-0 top-0 h-44" />
      <StatusBar />
      <div className="relative px-4 pt-14">
        <p className="text-[19px] font-semibold tracking-[-0.02em]">
          New <span className="text-ink-2">alarm</span>
        </p>
        <p className="tnum mt-3 text-center text-[58px] font-semibold leading-none tracking-[-0.04em]">06:30</p>
        <div className="mt-3 flex justify-center gap-1">
          {["M", "T", "W", "T", "F", "S", "S"].map((d, i) => (
            <span
              key={i}
              className={`grid size-7 place-items-center rounded-full text-[10px] font-semibold ${i < 5 ? "bg-ink text-white" : "border border-line"}`}
            >
              {d}
            </span>
          ))}
        </div>
        <div className="mt-4 space-y-2">
          {[
            ["Exercise", "Squat"],
            ["Target", "15 reps"],
            ["Snooze", "Off"],
            ["Video proof", "On"],
          ].map(([k, v]) => (
            <div key={k} className="flex items-center justify-between rounded-[14px] border border-line px-3 py-2.5 text-[11px]">
              <span className="font-medium">{k}</span>
              <span className="text-ink-2">{v}</span>
            </div>
          ))}
        </div>
      </div>
      <div className="absolute inset-x-4 bottom-4">
        <span className="flex h-11 items-center justify-center gap-1.5 rounded-full bg-ink text-[12px] font-medium text-white shadow-pill">
          <IconPlay size={12} /> Save &amp; test in 10s
        </span>
      </div>
    </div>
  );
}

