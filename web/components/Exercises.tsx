import { Heading, Reveal, SectionBadge } from "./ui";
import { Skeleton } from "./Skeleton";

const cats = [
  { name: "Lower body", n: 13, ex: ["Squat", "Lunge", "Wall sit", "Glute bridge"], pose: "squat" as const },
  { name: "Upper body", n: 9, ex: ["Push-up", "Pike push-up", "Tricep dip", "Arm circles"], pose: "plank" as const },
  { name: "Core", n: 14, ex: ["Sit-up", "Plank", "Bicycle crunch", "V-up"], pose: "plank" as const },
  { name: "Full body & cardio", n: 10, ex: ["Burpee", "Jumping jack", "Mountain climber", "Inchworm"], pose: "arms-up" as const },
  { name: "Mobility & yoga", n: 6, ex: ["Chair pose", "Warrior II", "Downward dog", "Tree pose"], pose: "stand" as const },
];

export function Exercises() {
  return (
    <section id="exercises" className="section pt-0">
      <div className="container-qiap">
        <Reveal className="text-center">
          <SectionBadge>The library</SectionBadge>
          <Heading className="mt-4" lead="52 exercises." accent="Every one auto-counted." />
          <p className="mx-auto mt-4 max-w-[560px] text-[15px] leading-relaxed text-ink-2">
            Each exercise is tuned from real recorded movement and checks range and tempo, so a lazy half-rep
            doesn&apos;t sneak through.
          </p>
        </Reveal>

        <div className="mt-12 grid gap-4 sm:grid-cols-2 lg:grid-cols-6">
          {cats.map((c, idx) => (
            <Reveal
              key={c.name}
              delay={idx * 70}
              className={idx < 3 ? "lg:col-span-2" : "lg:col-span-3"}
            >
              <div className="card flex h-full items-stretch gap-4 overflow-hidden p-5">
                <div className="grid w-[96px] shrink-0 place-items-center rounded-2xl bg-night">
                  <Skeleton pose={c.pose} className="h-[84px]" />
                </div>
                <div className="min-w-0 py-1">
                  <p className="tnum text-[28px] font-semibold leading-none tracking-[-0.03em]">{c.n}</p>
                  <p className="mt-1 text-[14px] font-semibold">{c.name}</p>
                  <p className="mt-2 text-[12px] leading-relaxed text-ink-2">{c.ex.join(" · ")}…</p>
                </div>
              </div>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  );
}
