"use client";

import { useState } from "react";
import { Heading, Reveal, SectionBadge } from "./ui";
import { Skeleton } from "./Skeleton";

const modes = [
  {
    name: "Wake-up Lite",
    blurb: "Gentle on a groggy brain. Twists, bends and a few light moves to get the blood going.",
    moves: ["Standing torso twist", "Side bend", "Jumping jack", "Squat", "Crunch"],
    pose: "arms-up" as const,
  },
  {
    name: "Cardio Blast",
    blurb: "Heart rate up fast. For people who need to be properly shocked awake.",
    moves: ["Jumping jack", "Mountain climber", "High knees", "Burpee", "Jog in place"],
    pose: "squat" as const,
  },
  {
    name: "Strength",
    blurb: "Classic bodyweight work with form checking on every rep.",
    moves: ["Push-up", "Squat", "Forward lunge", "Plank", "Tricep dip"],
    pose: "plank" as const,
  },
  {
    name: "Core Crusher",
    blurb: "Sit-ups, twists and holds that make sure you're fully, painfully awake.",
    moves: ["Sit-up", "Bicycle crunch", "Leg raise", "Plank", "V-up"],
    pose: "plank" as const,
  },
  {
    name: "Quiet",
    blurb: "Apartment-friendly. No jumping, so the neighbours (and the floor) stay asleep.",
    moves: ["Squat", "Push-up", "Glute bridge", "Wall sit", "Plank"],
    pose: "stand" as const,
  },
];

export function Modes({ initial = 0 }: { initial?: number }) {
  const [i, setI] = useState(initial);

  return (
    <section className="section bg-gradient-to-b from-white via-wash/60 to-white">
      <div className="container-qiap text-center">
        <Reveal>
          <SectionBadge>Built for every morning</SectionBadge>
          <Heading className="mx-auto mt-4 max-w-[760px]" lead="Pick a routine," accent="or let it surprise you." />
          <p className="mx-auto mt-4 max-w-[540px] text-[15px] leading-relaxed text-ink-2">
            Use one fixed exercise, chain up to three, or let Qiap pick at random from a pool you choose.
          </p>
        </Reveal>

        <div
          role="tablist"
          aria-label="Routines"
          className="no-scrollbar mx-auto mt-8 flex max-w-full gap-1 overflow-x-auto rounded-full border border-line bg-white p-1 shadow-soft sm:w-fit"
        >
          {modes.map((x, idx) => (
            <button
              key={x.name}
              role="tab"
              aria-selected={idx === i}
              onClick={() => setI(idx)}
              className={`shrink-0 rounded-full px-4 py-2 text-[13px] font-medium transition-colors duration-200 ${
                idx === i ? "bg-sky text-white" : "text-ink hover:bg-surface-muted"
              }`}
            >
              {x.name}
            </button>
          ))}
        </div>

        {/* All panels are rendered up front (good for SEO, no layout shift); only the active one is visible. */}
        {modes.map((m, idx) => (
          <div
            key={m.name}
            role="tabpanel"
            hidden={idx !== i}
            className="relative mx-auto mt-6 grid max-w-[920px] overflow-hidden rounded-[var(--radius-shell)] bg-night text-left text-white md:grid-cols-[1.2fr_1fr]"
          >
            <div className="relative z-10 p-7 md:p-10">
              <p className="text-[13px] font-medium text-paper/60">Routine</p>
              <h3 className="mt-1 text-[28px] font-medium tracking-[-0.03em]">{m.name}</h3>
              <p className="mt-3 max-w-[380px] text-[15px] leading-relaxed text-white/75">{m.blurb}</p>
              <ul className="mt-6 flex flex-wrap gap-2">
                {m.moves.map((mv) => (
                  <li key={mv} className="rounded-full border border-white/15 bg-white/10 px-3 py-1.5 text-[12px] font-medium">
                    {mv}
                  </li>
                ))}
              </ul>
              <p className="tnum mt-8 text-[44px] font-semibold leading-none tracking-[-0.04em]">
                5 <span className="text-[14px] font-medium tracking-normal text-white/60">moves in this pool</span>
              </p>
            </div>
            <div className="relative grid min-h-[220px] place-items-center bg-[radial-gradient(70%_70%_at_50%_40%,#2b2b33_0%,#0d0d0f_75%)]">
              <Skeleton pose={m.pose} className="w-[230px]" />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
