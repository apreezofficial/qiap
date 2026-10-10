import Link from "next/link";
import type { ReactNode } from "react";
import { Seal } from "./Brand";
import { PhoneFrame } from "./Phone";
import { HistoryScreen, RingingScreen, WorkoutScreen } from "./Screens";
import { Reveal } from "./Reveal";
import { Skeleton } from "./Skeleton";
import { exercises } from "@/lib/exercises";

/* Three numbers that are measured or counted, not claimed. */
export function Proof() {
  const holds = exercises.filter((e) => e.kind === "hold").length;
  const facts = [
    { n: String(exercises.length), short: "exercises", long: ` the camera can count, ${holds} of them timed holds` },
    { n: "0", short: "network permissions", long: ". The app cannot send anything anywhere" },
    { n: "27", unit: "MB", short: "for the whole app", long: ", pose model included" },
  ];
  return (
    <section aria-label="In numbers" className="mx-auto max-w-[1200px] px-3 py-8 sm:px-8 sm:py-24">
      <Reveal>
        <dl className="grid grid-cols-3 gap-px border border-hair bg-hair sm:gap-8 sm:border-0 sm:bg-transparent">
          {facts.map((f) => (
            <div key={f.short} className="bg-page px-2 py-6 text-center sm:p-0 sm:text-left">
              <dt className="h-display tnum whitespace-nowrap text-[46px] leading-none sm:text-[104px]">
                {f.n}
                {"unit" in f ? <span className="ml-1 text-[0.42em] text-fg-2">{f.unit}</span> : null}
              </dt>
              <dd className="mt-2 text-[12px] leading-snug text-fg-2 sm:mt-3 sm:max-w-[260px] sm:text-[16px] sm:leading-relaxed">
                {f.short}
                <span className="hidden sm:inline">{f.long}</span>
              </dd>
            </div>
          ))}
        </dl>
      </Reveal>
    </section>
  );
}

/* ───────── four big rows ───────── */

function Row({
  id,
  title,
  children,
  visual,
  flip = false,
}: {
  id: string;
  title: string;
  children: ReactNode;
  visual: ReactNode;
  flip?: boolean;
}) {
  return (
    <section id={id} className="mx-auto max-w-[1200px] scroll-mt-6 px-5 py-10 sm:px-8 sm:py-16">
      <div className={`grid items-center gap-10 md:grid-cols-2 md:gap-16 ${flip ? "md:[&>*:first-child]:order-2" : ""}`}>
        <Reveal>
          <h2 className="h-display text-[44px] sm:text-[60px]">{title}</h2>
          <div className="mt-6 max-w-[480px] space-y-4 text-[18px] leading-relaxed text-fg-2">{children}</div>
        </Reveal>
        <Reveal delay={120}>{visual}</Reveal>
      </div>
    </section>
  );
}

export function Rows() {
  return (
    <>
      <Row
        id="alarm"
        title="It rings like it means it."
        visual={
          <div className="panel relative flex h-[460px] items-end justify-center overflow-hidden bg-cinnabar sm:h-[560px]">
            <svg viewBox="0 0 400 400" className="sunpulse absolute left-1/2 top-[8%] w-[420px] -translate-x-1/2 sm:w-[520px]" aria-hidden>
              <circle cx="200" cy="200" r="190" fill="none" stroke="#fff" strokeOpacity=".5" strokeWidth="3" />
              <circle cx="200" cy="200" r="140" fill="none" stroke="#fff" strokeOpacity=".5" strokeWidth="3" />
              <circle cx="200" cy="200" r="90" fill="#fff" fillOpacity=".35" />
            </svg>
            <div className="relative translate-y-24 sm:translate-y-16">
              <PhoneFrame>
                <RingingScreen />
              </PhoneFrame>
            </div>
          </div>
        }
      >
        <p>
          Exact alarms, over your lock screen, back after a restart. If the app is swiped away or killed halfway
          through a ring, it picks the ring back up.
        </p>
        <p>
          Thirty minutes in, it gives up and writes the morning down as missed, so a stuck alarm never drains your
          battery all day.
        </p>
      </Row>

      <Row
        id="reps"
        flip
        title="The camera does the counting."
        visual={
          <div className="panel relative flex h-[460px] items-center justify-center overflow-hidden bg-panel-2 sm:h-[560px]">
            <Skeleton pose="squat" className="h-[360px] w-auto sm:h-[440px]" />
            <span className="h-display tnum absolute right-8 top-6 text-[120px] text-fg sm:text-[168px]">12</span>
            <span className="absolute bottom-6 left-8 text-[15px] text-fg-2">
              Joints turn amber when the rep is too shallow.
            </span>
          </div>
        }
      >
        <p>
          The front camera finds your joints and counts reps on the phone itself. Squats, push-ups, planks,
          burpees: 52 in all, each one a few lines of data.
        </p>
        <p>
          Some are still being tuned against real recordings, and the app labels those Beta so you know which to
          trust first.
        </p>
      </Row>

      <Row
        id="check"
        title="Hard to fake at six a.m."
        visual={
          <div className="panel sky-wash relative flex h-[540px] flex-col items-center justify-end overflow-hidden text-ink sm:h-[560px]">
            <p className="h-display absolute left-8 right-8 top-8 text-[40px] sm:text-[52px]">Raise your right hand.</p>
            <Skeleton pose="arms-up" bone="#111114" className="h-[300px] w-auto sm:h-[400px]" />
          </div>
        }
      >
        <p>
          Before the first rep you raise a hand or touch your head, a different move each morning. After the last
          rep you stand still, then do it again.
        </p>
        <p>
          Wave the phone around, or stand half out of frame, and nothing counts. Hurt, or the camera is broken?
          Three quick sums are always there as a way out.
        </p>
      </Row>

      <Row
        id="seal"
        flip
        title="Win the morning, get stamped."
        visual={
          <div className="panel relative flex h-[460px] items-center justify-center overflow-hidden bg-panel-2 sm:h-[560px]">
            <div className="absolute left-6 top-6 sm:left-12">
              <Seal size={170} animate />
            </div>
            <div className="absolute left-1/2 top-[230px] -translate-x-1/2 sm:left-auto sm:right-14 sm:top-auto sm:-bottom-24 sm:translate-x-0">
              <PhoneFrame scale={0.95}>
                <HistoryScreen />
              </PhoneFrame>
            </div>
          </div>
        }
      >
        <p>
          Every finished morning stamps a red 醒 seal on your calendar. Skip one and the square stays empty.
        </p>
        <p>
          Want a recording too? Turn on video proof. It lives on your phone for a week, two or a month, then it is
          deleted.
        </p>
      </Row>
    </>
  );
}

/* ───────── four steps ───────── */

const steps = [
  { n: "1", pose: "stand", title: "Set it", short: "Time, days, workout. Or a routine, or a surprise.", body: "A time, the days, and a workout. Or a routine of three, or a surprise from a preset." },
  { n: "2", pose: "squat", title: "It rings", short: "Volume climbs over the lock screen.", body: "The volume climbs over the lock screen. Tap start workout." },
  { n: "3", pose: "arms-up", title: "Move", short: "A hand gesture, then reps, skeleton live.", body: "A hand gesture first, then the reps, with the skeleton drawn live on top of you." },
  { n: "4", pose: "plank", title: "Stamp", short: "Stand still, one last gesture. Seal earned.", body: "Stand still, one last gesture, and the alarm is off. Seal earned." },
] as const;

export function Steps() {
  return (
    <section aria-labelledby="steps-h" className="mx-auto max-w-[1280px] p-2 sm:p-4">
      <div className="panel bg-panel px-4 py-12 sm:px-12 sm:py-24">
        <h2 id="steps-h" className="h-display mx-auto max-w-[760px] text-center text-[34px] sm:text-[64px]">
          From alarm to seal in four moves.
        </h2>
        <ol className="mt-10 grid grid-cols-2 gap-x-4 gap-y-10 sm:mt-16 lg:grid-cols-4 lg:gap-8">
          {steps.map((s) => (
            <li key={s.n}>
              <Skeleton pose={s.pose} className="h-[96px] w-auto sm:h-[190px]" />
              <div className="mt-3 flex items-baseline gap-2 sm:mt-6 sm:block">
                <p className="h-display tnum text-[40px] leading-none text-fg-2 sm:text-[96px]">{s.n}</p>
                <h3 className="text-[19px] font-medium tracking-[-0.02em] sm:mt-2 sm:text-[26px]">{s.title}</h3>
              </div>
              <p className="mt-2 text-[13px] leading-snug text-fg-2 sm:mt-3 sm:max-w-[280px] sm:text-[16px] sm:leading-relaxed">
                <span className="sm:hidden">{s.short}</span>
                <span className="hidden sm:inline">{s.body}</span>
              </p>
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}

/* ───────── what we do and do not do ───────── */

export function Promises() {
  const we = [
    "Run everything on your phone.",
    "Keep the emergency exit open, always.",
    "Say which exercises are still being tuned.",
    "Delete your video proof on the schedule you pick.",
  ];
  const wont = [
    "Ask you for an account.",
    "Request the internet permission.",
    "Show ads, or sell anything about you.",
    "Print reviews we did not get.",
  ];
  return (
    <section className="mx-auto max-w-[1200px] px-5 py-20 sm:px-8 sm:py-32">
      <h2 className="h-display text-[44px] sm:text-[64px]">The small print, out loud.</h2>
      <div className="mt-14 grid gap-px border border-hair bg-hair md:grid-cols-2">
        <div className="bg-page p-8 sm:p-12">
          <p className="text-[15px] uppercase tracking-[0.12em] text-fg-2">We will</p>
          <ul className="mt-6 space-y-5 text-[22px] leading-snug tracking-[-0.01em] sm:text-[26px]">
            {we.map((t) => (
              <li key={t}>{t}</li>
            ))}
          </ul>
        </div>
        <div className="bg-page p-8 sm:p-12">
          <p className="text-[15px] uppercase tracking-[0.12em] text-fg-2">We will not</p>
          <ul className="mt-6 space-y-5 text-[22px] leading-snug tracking-[-0.01em] text-fg-2 sm:text-[26px]">
            {wont.map((t) => (
              <li key={t}>{t}</li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}

/* ───────── honest status ───────── */

export function Status() {
  const rows = [
    ["Android", "In testing. Android 8.0 and up, with a front camera."],
    ["Play Store", "Not yet. Early access comes first."],
    ["Counting", "Squats, push-ups and planks first. Others are marked Beta in the app."],
    ["iPhone", "Not possible: Apple does not let third-party alarms ring over the lock screen like this."],
  ];
  return (
    <section aria-labelledby="status-h" className="mx-auto max-w-[1200px] px-5 pb-20 sm:px-8 sm:pb-32">
      <h2 id="status-h" className="h-display text-[44px] sm:text-[64px]">
        Not finished, and we say so.
      </h2>
      <dl className="mt-12 border-t border-hair">
        {rows.map(([k, v]) => (
          <div key={k} className="grid gap-2 border-b border-hair py-7 sm:grid-cols-[220px_1fr] sm:gap-8">
            <dt className="text-[22px] tracking-[-0.01em]">{k}</dt>
            <dd className="max-w-[640px] text-[18px] leading-relaxed text-fg-2">{v}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}

/* ───────── the loud panel ───────── */

export function Early() {
  return (
    <section id="early" className="mx-auto max-w-[1280px] scroll-mt-6 p-2 sm:p-4">
      <div className="panel overflow-hidden bg-cinnabar px-6 py-20 text-center text-ink sm:px-12 sm:py-32">
        <h2 className="h-display mx-auto max-w-[820px] text-[48px] sm:text-[88px]">Set the alarm you cannot argue with.</h2>
        <p className="mx-auto mt-8 max-w-[480px] text-[19px] leading-relaxed">
          Early access is not open yet. Send us a line and we will write back the day it is.
        </p>
        <div className="mt-10 flex flex-wrap items-center justify-center gap-3">
          <a href="mailto:hello@qiap.app?subject=Early%20access" className="btn btn-dark">
            Email for early access
          </a>
          <Link href="/exercises" className="btn btn-outline">
            Browse the exercises first
          </Link>
        </div>
      </div>
    </section>
  );
}
