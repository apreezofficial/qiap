import type { Metadata } from "next";
import { PageShell, PageTitle } from "@/components/PageShell";

export const metadata: Metadata = {
  title: "Questions · Qiap",
  description: "Straight answers about offline use, injuries, video, the lock screen and which phones work.",
};

const qa: [string, string][] = [
  [
    "Does it need the internet?",
    "No. The app does not even hold the internet permission, so it could not connect if it wanted to. Pose detection runs on your phone from a model bundled inside the app.",
  ],
  [
    "What if I am hurt, or the camera breaks?",
    "Hold the small button on the ringing screen for three seconds, then answer three quick sums. The alarm stops and the day gets a saffron-edged seal instead of a red one. A broken camera or a missing camera permission sends you to the same exit on its own. Nobody gets stuck.",
  ],
  [
    "Does it ring with the screen locked?",
    "That is what it is built for: exact alarms, a full-screen alert over the lock screen, and a restart-proof schedule. We are testing on real phones now, so if yours misbehaves, tell us the model.",
  ],
  [
    "Is my video uploaded?",
    "Never. Video proof is off unless you turn it on for an alarm. It records without sound into the app's private storage, and the app deletes it after the 7, 14 or 30 days you choose. It only leaves the phone if you tap Share.",
  ],
  [
    "Can I just wave the phone about?",
    "The app watches for that. It asks for a random hand gesture before counting starts and another after the last rep, ignores reps while the phone is being shaken, and ignores them when you are mostly out of frame. A determined person can still fool any on-device check, but half asleep is a different story.",
  ],
  [
    "Can I snooze?",
    "Only if you switch it on. Snooze is off by default. You can allow up to three five-minute snoozes, and you can make each one cost a short mini set.",
  ],
  [
    "How accurate is the counting?",
    "Honestly: squats, push-ups and planks are where we started, and every exercise is still marked Beta while we tune it against recordings from real phones. Sloppy reps are meant not to count. Tell us which exercise miscounts on your phone.",
  ],
  [
    "Which phones?",
    "Android 8.0 and up with a front camera. We are building for everyday and budget phones, not just flagships, so a Tecno or a Galaxy A should be fine. Battery savers on some brands kill alarms, so the app has a step-by-step guide for your phone's brand.",
  ],
  [
    "Why no iPhone version?",
    "iOS does not let a third-party alarm ring and take over the lock screen the way this needs to. We would rather say so than ship something that fails at 6 a.m.",
  ],
];

export default function HelpPage() {
  return (
    <PageShell>
      <PageTitle note="The things people ask before they trust an alarm with their morning.">
        Questions you would ask.
      </PageTitle>

      <div className="mt-16 border-t border-hair">
        {qa.map(([q, a]) => (
          <details key={q} className="q border-b border-hair">
            <summary className="flex items-start justify-between gap-6 py-7 text-[22px] leading-snug tracking-[-0.01em] sm:text-[26px]">
              <span>{q}</span>
              <span className="q-plus mt-1 text-[32px] leading-none text-fg-2" aria-hidden>
                +
              </span>
            </summary>
            <p className="max-w-[680px] pb-8 text-[18px] leading-relaxed text-fg-2">{a}</p>
          </details>
        ))}
      </div>

      <section className="mt-24">
        <h2 className="h-display text-[36px] sm:text-[48px]">Still stuck?</h2>
        <p className="mt-4 max-w-[520px] text-[18px] leading-relaxed text-fg-2">
          Write to us with your phone model and what happened. A real person reads it.
        </p>
        <a href="mailto:hello@qiap.app" className="btn btn-cta mt-8">
          hello@qiap.app
        </a>
      </section>
    </PageShell>
  );
}
