import { PhoneFrame } from "./Phone";
import { HomeScreen, RingingScreen, WorkoutScreen, LibraryScreen } from "./Screens";
import { Glyph } from "./Brand";
import { IconArrow, IconPlay, IconAndroid } from "./Icons";

export function Hero() {
  return (
    <section id="top" className="relative overflow-hidden pb-0 pt-10 md:pt-16">
      <div className="dotgrid pointer-events-none absolute inset-x-0 top-0 h-[560px]" aria-hidden />

      <div className="container-qiap relative text-center">
        {/* top chip */}
        <a
          href="#get"
          className="mx-auto inline-flex items-center gap-2 rounded-full border border-line bg-white py-1.5 pl-3 pr-1.5 text-[13px] font-medium shadow-soft"
        >
          <Glyph size={16} />
          Android early access
          <span className="inline-flex items-center gap-1 rounded-full bg-sky px-2.5 py-1 text-[12px] font-medium text-white">
            Get access <IconArrow size={12} />
          </span>
        </a>

        <h1 className="h-display mx-auto mt-6 max-w-[860px] text-[46px] sm:text-[64px] md:text-[84px]">
          Wake up.
          <br />
          Prove it.
        </h1>
        <p className="mx-auto mt-5 max-w-[560px] text-[16px] leading-relaxed text-ink-2 md:text-[18px]">
          The alarm that only shuts up after you do the reps. Your camera counts them, your bed loses, and nobody
          gets to snooze their way out.
        </p>

        <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
          <a href="#get" className="btn btn-primary">
            <IconAndroid size={18} /> Get early access
          </a>
          <a href="#how" className="btn btn-glass">
            <IconPlay size={14} /> See how it works
          </a>
        </div>
      </div>

      {/* phone trio on the sky arch */}
      <div className="relative mt-14 md:mt-20">
        <div
          className="pointer-events-none absolute left-1/2 top-[8%] h-[520px] w-[1100px] -translate-x-1/2 rounded-[50%] bg-wash"
          aria-hidden
        />
        <div className="fade-b relative mx-auto flex h-[330px] max-w-[1000px] items-end justify-center gap-0 overflow-hidden px-4 md:h-[470px]">
          <div className="hidden md:block" style={{ transform: "rotate(-5deg) translateY(36px)" }}>
            <PhoneFrame scale={0.74} dim={0.25}>
              <LibraryScreen />
            </PhoneFrame>
          </div>
          <div className="relative z-10 -mb-2 md:hidden">
            <PhoneFrame scale={0.78}>
              <WorkoutScreen />
            </PhoneFrame>
          </div>
          <div className="relative z-10 hidden md:block">
            <PhoneFrame scale={0.9}>
              <WorkoutScreen />
            </PhoneFrame>
          </div>
          <div className="hidden md:block" style={{ transform: "rotate(5deg) translateY(36px)" }}>
            <PhoneFrame scale={0.74} dim={0.25}>
              <RingingScreen />
            </PhoneFrame>
          </div>
        </div>
      </div>
    </section>
  );
}

