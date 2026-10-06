import { PhoneFrame } from "./Phone";
import { WorkoutScreen } from "./Screens";
import { Seal } from "./Brand";
import { Heading, Reveal, SectionBadge } from "./ui";
import { IconCheck, IconAlarm, IconBolt } from "./Icons";

function Glass({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return (
    <div className={`rounded-2xl border border-white/70 bg-white/85 p-4 text-ink shadow-[0_10px_30px_rgba(0,0,0,0.12)] ${className}`}>
      {children}
    </div>
  );
}

export function Bento() {
  return (
    <section id="features" className="section">
      <div className="container-qiap">
        <Reveal className="grid items-end gap-6 md:grid-cols-[1.3fr_1fr]">
          <div>
            <SectionBadge>Our features</SectionBadge>
            <Heading className="mt-4" lead="Everything you need to" accent="actually get up." />
          </div>
          <p className="text-[15px] leading-relaxed text-ink-2">
            Qiap brings together on-device pose tracking, an alarm engine built to survive Doze and reboots, and a
            library of 52 exercises, so the only way to silence it is to move.
          </p>
        </Reveal>

        <div className="mt-10 grid gap-4 md:grid-cols-3 md:grid-rows-[auto_auto]">
          {/* A — pose tracking */}
          <Reveal className="md:row-span-2">
            <div className="relative h-full min-h-[440px] overflow-hidden rounded-[var(--radius-card)] bg-gradient-to-b from-[#3b7fd4] to-[#8dbdf3] p-6 text-white">
              <h3 className="text-[20px] font-semibold tracking-[-0.01em]">Pose-tracked reps</h3>
              <p className="mt-2 max-w-[260px] text-[14px] leading-relaxed text-white/90">
                Your camera maps 33 body points, counts every rep and tells you when your form slips.
              </p>
              <div className="absolute inset-x-0 bottom-0 flex justify-center">
                <div className="translate-y-[28%]">
                  <PhoneFrame scale={0.82}>
                    <WorkoutScreen />
                  </PhoneFrame>
                </div>
              </div>
            </div>
          </Reveal>

          {/* B — reliability */}
          <Reveal className="md:col-span-2" delay={80}>
            <div className="relative h-full min-h-[240px] overflow-hidden rounded-[var(--radius-card)] bg-night p-6 text-white">
              <div
                className="pointer-events-none absolute inset-0 opacity-30"
                style={{
                  backgroundImage:
                    "repeating-linear-gradient(115deg, rgba(255,255,255,0.08) 0 1px, transparent 1px 9px)",
                }}
                aria-hidden
              />
              <div className="relative grid gap-6 md:grid-cols-[1fr_1.1fr]">
                <div>
                  <h3 className="text-[20px] font-semibold tracking-[-0.01em]">Alarms that don&apos;t flake</h3>
                  <p className="mt-2 max-w-[300px] text-[14px] leading-relaxed text-white/75">
                    Exact scheduling, a foreground service and a lock-screen takeover, re-armed after every reboot.
                  </p>
                </div>
                <div className="space-y-3">
                  <Glass className="-rotate-1">
                    <div className="flex items-center gap-3">
                      <span className="grid size-9 place-items-center rounded-full bg-cinnabar/15 text-cinnabar"><IconAlarm size={18} /></span>
                      <div>
                        <p className="text-[13px] font-semibold">06:30 · Rise</p>
                        <p className="text-[12px] text-ink-2">15 squats to silence it</p>
                      </div>
                    </div>
                  </Glass>
                  <Glass className="ml-6 rotate-1">
                    <div className="flex items-center gap-3">
                      <span className="grid size-9 place-items-center rounded-full bg-jade/15 text-jade"><IconBolt size={18} /></span>
                      <div>
                        <p className="text-[13px] font-semibold">Restart detected</p>
                        <p className="text-[12px] text-ink-2">3 alarms re-armed</p>
                      </div>
                    </div>
                  </Glass>
                </div>
              </div>
            </div>
          </Reveal>

          {/* C — exercise library */}
          <Reveal delay={120}>
            <div className="relative h-full min-h-[300px] overflow-hidden rounded-[var(--radius-card)] bg-gradient-to-b from-[#1d5b43] to-[#3a9a72] p-6 text-white">
              <h3 className="text-[20px] font-semibold tracking-[-0.01em]">52 exercises</h3>
              <p className="mt-2 text-[14px] leading-relaxed text-white/85">Squats to burpees to yoga holds, all auto-counted.</p>
              <Glass className="mt-5">
                <ul className="space-y-2 text-[12px]">
                  {[
                    ["Lower body", "13"],
                    ["Upper body", "9"],
                    ["Core", "14"],
                    ["Full body", "10"],
                    ["Mobility", "6"],
                  ].map(([k, v]) => (
                    <li key={k} className="flex items-center justify-between">
                      <span className="inline-flex items-center gap-2 font-medium"><IconCheck size={14} className="text-jade" />{k}</span>
                      <span className="tnum text-ink-2">{v}</span>
                    </li>
                  ))}
                </ul>
              </Glass>
            </div>
          </Reveal>

          {/* D — proof & seals */}
          <Reveal delay={160}>
            <div className="relative h-full min-h-[300px] overflow-hidden rounded-[var(--radius-card)] bg-gradient-to-b from-[#4a90e2] to-[#a9cdf7] p-6 text-white">
              <h3 className="text-[20px] font-semibold tracking-[-0.01em]">Proof, then a seal</h3>
              <p className="mt-2 text-[14px] leading-relaxed text-white/90">Optional video proof stays on your phone. Finish and a seal lands in your history.</p>
              <Glass className="mt-5 flex items-center gap-4">
                <Seal size={52} />
                <div className="text-[12px]">
                  <p className="font-semibold">15 / 15 reps</p>
                  <p className="text-ink-2">Form checked · saved on device</p>
                </div>
              </Glass>
            </div>
          </Reveal>
        </div>
      </div>
    </section>
  );
}
