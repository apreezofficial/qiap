import { Reveal } from "./ui";

const tags = ["Heavy sleepers", "Students", "Night-shift workers", "Gym beginners", "Runners"];

export function Statement() {
  return (
    <section className="relative overflow-hidden py-20 md:py-28">
      <div className="sky-wash pointer-events-none absolute inset-x-0 bottom-0 h-[70%] opacity-90" aria-hidden />
      <div className="container-qiap relative text-center">
        <Reveal>
          <h2 className="h-display mx-auto max-w-[820px] text-[30px] md:text-[44px]">
            Build a stronger morning with an alarm that makes{" "}
            <span className="accent">your body, not your thumb, turn it off.</span>
          </h2>
        </Reveal>
        <Reveal delay={120}>
          <p className="mt-10 text-[13px] text-ink-2">Who it&apos;s for</p>
          <ul className="mt-4 flex flex-wrap items-center justify-center gap-2.5">
            {tags.map((t) => (
              <li key={t} className="chip"># {t}</li>
            ))}
          </ul>
        </Reveal>
      </div>
    </section>
  );
}
