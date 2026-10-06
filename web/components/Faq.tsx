import { Heading, Reveal, SectionBadge } from "./ui";
import { IconChevron } from "./Icons";

const faqs = [
  {
    q: "What if I can't do the workout (injury, bad lighting, broken camera)?",
    a: "You're never stuck. After several failed attempts, or if the camera can't open, Qiap offers a backup way to turn the alarm off, such as a short maths challenge. It's logged, but it always works.",
  },
  {
    q: "Does it work offline?",
    a: "Yes. Pose detection runs on your phone and v1 doesn't need an internet connection, so there's nothing to upload and nothing to sign into.",
  },
  {
    q: "Is my video uploaded anywhere?",
    a: "No. Optional video proof is saved in the app's private storage on your phone and auto-deleted after a retention period you choose.",
  },
  {
    q: "Will it ring on a locked screen, or after a restart?",
    a: "That's the whole design goal. Qiap uses exact alarm scheduling, a foreground service and a lock-screen takeover, and re-arms your alarms after a reboot. On some phone brands you'll also need to allow auto-start and unrestricted battery. The app walks you through it and tests it.",
  },
  {
    q: "Can I cheat it by waving the phone around?",
    a: "It's built to make that hard: full-body detection, minimum range and tempo per rep, shake detection and a quick \"raise your hand\" check. No on-device system is cheat-proof against someone determined, but it's designed to stop sleepy shortcuts.",
  },
  {
    q: "Which phones does it work on?",
    a: "Android 8.0 and up, aimed at everyday phones, not just flagships. iOS isn't planned yet because Apple doesn't let third-party alarm apps take over the screen like Android does.",
  },
  {
    q: "When can I get it?",
    a: "Qiap is in development. Join the early-access list below and we'll let you know when the first Android build is ready.",
  },
];

export function Faq() {
  return (
    <section id="faq" className="section">
      <div className="container-qiap grid gap-12 md:grid-cols-[0.85fr_1.15fr]">
        <Reveal>
          <SectionBadge>FAQ</SectionBadge>
          <Heading className="mt-4" lead="Common questions," accent="quick answers." />
          <p className="mt-4 max-w-[340px] text-[15px] leading-relaxed text-ink-2">
            Everything you need to know before you let an app take your snooze button away.
          </p>
          <div className="relative mt-8 hidden overflow-hidden rounded-[var(--radius-card)] border border-line bg-gradient-to-b from-wash-2 to-white p-6 md:block">
            <h3 className="text-[20px] font-medium tracking-[-0.02em]">Still wondering?</h3>
            <p className="mt-1 text-[13px] text-ink-2">Ask us anything about Qiap.</p>
            <a href="mailto:hello@qiap.app" className="btn btn-primary btn-sm mt-8">
              Contact us
            </a>
          </div>
        </Reveal>

        <Reveal delay={100}>
          <div className="divide-y divide-line border-y border-line">
            {faqs.map((f, i) => (
              <details key={f.q} className="group py-5" open={i === 0}>
                <summary className="flex cursor-pointer list-none items-center justify-between gap-4 text-[15px] font-medium [&::-webkit-details-marker]:hidden">
                  {f.q}
                  <IconChevron size={18} className="shrink-0 text-ink-2 transition-transform duration-200 group-open:rotate-180" />
                </summary>
                <p className="mt-3 pr-8 text-[14px] leading-relaxed text-ink-2">{f.a}</p>
              </details>
            ))}
          </div>
        </Reveal>
      </div>
    </section>
  );
}
