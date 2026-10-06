import { PhoneFrame } from "./Phone";
import { HomeScreen, WorkoutScreen, LibraryScreen, HistoryScreen, EditorScreen } from "./Screens";
import { IconAndroid, IconApple } from "./Icons";
import { Reveal } from "./ui";

export function PhoneCarousel() {
  return (
    <section className="overflow-hidden pb-16 pt-4 md:pb-24">
      <div className="relative mx-auto max-w-[1200px]">
        <div className="fade-x fade-b flex h-[360px] items-end justify-center gap-5 overflow-hidden md:h-[470px]">
          <div className="hidden lg:block opacity-60"><PhoneFrame scale={0.62}><EditorScreen /></PhoneFrame></div>
          <div className="hidden sm:block opacity-80"><PhoneFrame scale={0.74}><HomeScreen /></PhoneFrame></div>
          <div className="relative z-10"><PhoneFrame scale={0.9}><WorkoutScreen pose="stand" /></PhoneFrame></div>
          <div className="hidden sm:block opacity-80"><PhoneFrame scale={0.74}><LibraryScreen /></PhoneFrame></div>
          <div className="hidden lg:block opacity-60"><PhoneFrame scale={0.62}><HistoryScreen /></PhoneFrame></div>
        </div>
      </div>

      <Reveal className="container-qiap -mt-4 text-center">
        <h2 className="h-display text-[28px] md:text-[34px]">Rise with purpose, not snooze</h2>
        <p className="mx-auto mt-4 max-w-[520px] text-[15px] leading-relaxed text-ink-2">
          Set it once, then let it do the nagging. Real-time rep counting, form feedback and a streak of seals that
          makes skipping feel like losing.
        </p>
        <div className="mt-7 flex flex-wrap items-center justify-center gap-3">
          <a href="#get" className="btn btn-primary btn-sm">
            <IconAndroid size={16} /> Android early access
          </a>
          <span className="btn btn-glass btn-sm cursor-not-allowed opacity-70" aria-disabled="true" title="Android first. iOS restricts alarm apps.">
            <IconApple size={16} /> iOS: not planned yet
          </span>
        </div>
      </Reveal>
    </section>
  );
}
