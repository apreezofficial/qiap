import { IconCamera, IconDumbbell, IconAlarm, IconOffline, IconVideo, IconShield, IconFlame, IconBolt } from "./Icons";

const items = [
  [IconDumbbell, "52 exercises"],
  [IconCamera, "Camera rep counting"],
  [IconAlarm, "Alarms that don't miss"],
  [IconOffline, "Works fully offline"],
  [IconVideo, "Video proof"],
  [IconShield, "Anti-cheat checks"],
  [IconFlame, "Streak seals"],
  [IconBolt, "Light & fast"],
] as const;

export function FeatureMarquee() {
  const row = items.map(([I, label]) => (
    <span key={label} className="chip">
      <I size={15} />
      {label}
    </span>
  ));
  return (
    <section aria-label="Highlights" className="pt-6 md:pt-10">
      <p className="mb-4 text-center text-[13px] text-ink-2">Our top-notch features</p>
      <div className="marquee fade-x overflow-hidden py-2">
        <div className="marquee-track">
          {row}
          <span aria-hidden className="contents">
            {row}
          </span>
        </div>
      </div>
    </section>
  );
}
