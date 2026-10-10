import Link from "next/link";
import { PhoneFrame } from "./Phone";
import { LibraryScreen, RingingScreen, WorkoutScreen } from "./Screens";
import { SiteNav } from "./SiteNav";

/** A flat half-sun. Single colour, no gradient. */
export function Sun({ className = "" }: { className?: string }) {
  return (
    <svg viewBox="0 0 100 100" className={className} aria-hidden>
      <circle cx="50" cy="50" r="50" fill="#F2542D" />
    </svg>
  );
}

export function Hero() {
  return (
    <section className="mx-auto max-w-[1280px] p-2 sm:p-4">
      <div className="panel sky-wash relative overflow-hidden text-ink">
        <div className="px-2 pt-2 sm:px-4 sm:pt-4">
          <SiteNav tone="page" />
        </div>

        <div className="relative z-10 mx-auto max-w-[920px] px-5 pb-6 pt-14 text-center sm:pt-20">
          <h1 className="h-display text-[60px] sm:text-[104px] lg:text-[128px]">
            Wake up.
            <br />
            Prove it.
          </h1>
          <p className="mx-auto mt-6 max-w-[520px] text-[18px] leading-relaxed text-ink-2 sm:text-[20px]">
            An Android alarm that keeps ringing until your camera sees you do the reps.
          </p>
          <div className="mt-9 flex flex-wrap items-center justify-center gap-3">
            <Link href="/#early" className="btn btn-dark">
              Get early access
            </Link>
            <Link href="/exercises" className="btn btn-outline">
              See the 52 exercises
            </Link>
          </div>
          <p className="mt-5 text-[14px] text-ink-2">Android first. Nothing leaves your phone.</p>
        </div>

        {/* A sun rising behind three phones, cropped by the panel edge. */}
        <div className="relative mx-auto h-[360px] w-full max-w-[1000px] overflow-hidden sm:h-[560px]">
          <Sun className="absolute left-1/2 top-[34%] w-[760px] -translate-x-1/2 sm:top-[28%] sm:w-[1040px]" />

          <div className="absolute left-1/2 top-0 hidden origin-bottom sm:block" style={{ transform: "translateX(-150%) translateY(56px) rotate(-8deg)" }}>
            <PhoneFrame scale={0.86}>
              <LibraryScreen />
            </PhoneFrame>
          </div>
          <div className="absolute left-1/2 top-0 hidden origin-bottom sm:block" style={{ transform: "translateX(50%) translateY(56px) rotate(8deg)" }}>
            <PhoneFrame scale={0.86}>
              <WorkoutScreen />
            </PhoneFrame>
          </div>
          <div className="absolute left-1/2 top-0 z-10 -translate-x-1/2 scale-[0.82] origin-top sm:scale-100">
            <PhoneFrame>
              <RingingScreen />
            </PhoneFrame>
          </div>
        </div>
      </div>
    </section>
  );
}
