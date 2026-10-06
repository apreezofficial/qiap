import { PhoneFrame } from "./Phone";
import { RingingScreen } from "./Screens";
import { Waitlist } from "./Waitlist";
import { Reveal } from "./ui";

export function CtaBanner() {
  return (
    <section id="get" className="px-3 pb-16 md:pb-24">
      <Reveal>
        <div className="relative mx-auto max-w-[1120px] overflow-hidden rounded-[var(--radius-shell)] border border-line bg-gradient-to-b from-wash to-white">
          <div className="pointer-events-none absolute -right-24 -top-24 size-[460px] rounded-full bg-wash-2/60" aria-hidden />
          <div className="relative grid items-center gap-8 px-6 pt-10 md:grid-cols-[1.1fr_0.9fr] md:px-14 md:pt-14">
            <div className="pb-10 md:pb-14">
              <h2 className="h-display text-[32px] md:text-[44px]">Take your wake-up anywhere.</h2>
              <p className="mt-4 max-w-[430px] text-[15px] leading-relaxed text-ink-2">
                Qiap is being built for Android first. Leave your email and you&apos;ll hear when the first build is ready.
              </p>
              <div className="mt-7">
                <Waitlist />
              </div>
            </div>
            <div className="fade-b -mb-24 flex justify-center md:justify-end md:self-end">
              <PhoneFrame scale={0.9}>
                <RingingScreen />
              </PhoneFrame>
            </div>
          </div>
        </div>
      </Reveal>
    </section>
  );
}
