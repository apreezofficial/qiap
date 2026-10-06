import { Heading, Reveal, SectionBadge } from "./ui";
import { Seal } from "./Brand";
import { Skeleton } from "./Skeleton";
import { IconAlarm, IconCheck } from "./Icons";

export function HowItWorks() {
  return (
    <section id="how" className="section">
      <div className="container-qiap text-center">
        <Reveal>
          <SectionBadge>How it works</SectionBadge>
          <Heading className="mt-4" lead="From ringing to done" accent="in three steps." />
          <p className="mx-auto mt-4 max-w-[520px] text-[15px] leading-relaxed text-ink-2">
            No account, no cloud, no tricks. Set it up once and it handles the rest.
          </p>
        </Reveal>

        <div className="mt-12 grid gap-6 text-left md:grid-cols-3">
          {/* 1 */}
          <Reveal>
            <div className="h-[260px] overflow-hidden rounded-[var(--radius-card)] bg-gradient-to-b from-wash to-wash-2/70 p-5">
              <div className="space-y-2.5">
                <div className="flex items-center gap-3 rounded-2xl bg-white p-3 shadow-soft">
                  <span className="grid size-9 place-items-center rounded-full bg-wash text-sky"><IconAlarm size={18} /></span>
                  <div>
                    <p className="tnum text-[18px] font-semibold leading-none">06:30</p>
                    <p className="mt-1 text-[11px] text-ink-2">Mon – Fri</p>
                  </div>
                </div>
                <div className="flex items-center justify-between rounded-2xl bg-white p-3 text-[12px] shadow-soft">
                  <span className="font-medium">Exercise</span><span className="text-ink-2">Squat × 15</span>
                </div>
                <div className="flex items-center justify-between rounded-2xl bg-white p-3 text-[12px] shadow-soft">
                  <span className="font-medium">Video proof</span><span className="text-ink-2">On</span>
                </div>
              </div>
            </div>
            <h3 className="mt-5 text-[17px] font-semibold">1 · Set your alarm</h3>
            <p className="mt-1.5 text-[14px] leading-relaxed text-ink-2">Pick the time, the days and the exercise (or a random pool). A 10-second test run checks it all works.</p>
          </Reveal>

          {/* 2 */}
          <Reveal delay={100}>
            <div className="relative h-[260px] overflow-hidden rounded-[var(--radius-card)] bg-night">
              <div className="absolute inset-0 bg-[radial-gradient(80%_70%_at_50%_30%,#2b2b33_0%,#0d0d0f_80%)]" />
              <Skeleton pose="squat" className="absolute left-1/2 top-4 h-[200px] -translate-x-1/2" />
              <div className="absolute bottom-3 left-4 flex items-end gap-1.5 text-paper">
                <span className="tnum font-app text-[44px] font-semibold leading-[0.85] tracking-[-0.05em]">12</span>
                <span className="tnum mb-0.5 text-[13px] text-paper/60">/ 15</span>
              </div>
              <span className="absolute bottom-4 right-4 inline-flex items-center gap-1 rounded-full bg-jade/20 px-2.5 py-1 text-[11px] font-medium text-jade">
                <IconCheck size={12} /> Good depth
              </span>
            </div>
            <h3 className="mt-5 text-[17px] font-semibold">2 · Do the reps</h3>
            <p className="mt-1.5 text-[14px] leading-relaxed text-ink-2">The alarm keeps ringing. Stand back, let the camera see you, and hit your target. Shallow or rushed reps don&apos;t count.</p>
          </Reveal>

          {/* 3 */}
          <Reveal delay={200}>
            <div className="grid h-[260px] place-items-center overflow-hidden rounded-[var(--radius-card)] bg-gradient-to-b from-wash to-wash-2/70">
              <Seal size={120} animate />
            </div>
            <h3 className="mt-5 text-[17px] font-semibold">3 · Get your seal</h3>
            <p className="mt-1.5 text-[14px] leading-relaxed text-ink-2">Alarm off, seal stamped. Your history becomes a grid of seals, and you won&apos;t want to break the streak.</p>
          </Reveal>
        </div>
      </div>
    </section>
  );
}
