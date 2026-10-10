import type { Metadata } from "next";
import { PageShell, PageTitle } from "@/components/PageShell";
import { Skeleton } from "@/components/Skeleton";
import { categories, exercises, poseFor, unitLabel, viewLabel } from "@/lib/exercises";

export const metadata: Metadata = {
  title: "All 52 exercises · Qiap",
  description: "Every exercise the Qiap camera can count, with where to put the phone and what to aim for.",
};

export default function ExercisesPage() {
  const beta = exercises.length; // every exercise is Beta until a recorded fixture backs it
  return (
    <PageShell wide>
      <PageTitle
        note={
          <>
            Each one is counted by your phone&apos;s camera, on the phone. All {beta} are still marked Beta in the
            app while we tune them against real recordings, so start with squats, push-ups and planks.
          </>
        }
      >
        {exercises.length} exercises.
        <br />
        One camera.
      </PageTitle>

      <nav aria-label="Jump to a group" className="mt-12 flex flex-wrap gap-x-8 gap-y-3 text-[17px] text-fg-2">
        {categories.map((c) => (
          <a key={c.id} href={`#${c.id}`} className="link-under hover:text-fg">
            {c.label}
          </a>
        ))}
      </nav>

      {categories.map((c) => {
        const list = exercises.filter((e) => e.category === c.id);
        return (
          <section key={c.id} id={c.id} className="mt-24 scroll-mt-6">
            <div className="flex flex-wrap items-baseline justify-between gap-4">
              <h2 className="h-display text-[40px] sm:text-[56px]">{c.label}</h2>
              <p className="tnum text-[16px] text-fg-2">{list.length} exercises</p>
            </div>
            <p className="mt-3 max-w-[560px] text-[17px] leading-relaxed text-fg-2">{c.blurb}</p>

            <ul className="mt-10 grid gap-px border border-hair bg-hair sm:grid-cols-2 lg:grid-cols-3">
              {list.map((e) => (
                <li key={e.id} className="flex flex-col bg-page p-6 sm:p-8">
                  <Skeleton pose={poseFor(e)} className="h-[200px] w-auto self-start" />
                  <h3 className="mt-6 text-[24px] font-medium tracking-[-0.02em]">{e.name}</h3>
                  <p className="tnum mt-1 text-[16px] text-fg-2">Aim for {unitLabel(e)}</p>
                  <p className="mt-4 text-[15px] leading-relaxed text-fg-2">{e.tip}</p>
                  <p className="mt-auto pt-6 text-[14px] text-fg-2">
                    {viewLabel[e.view]}
                    {e.needsFloor ? " · needs floor space" : ""}
                    {e.jumping ? " · jumping" : " · quiet"}
                  </p>
                </li>
              ))}
            </ul>
          </section>
        );
      })}
    </PageShell>
  );
}
