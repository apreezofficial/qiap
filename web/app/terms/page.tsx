import type { Metadata } from "next";
import { PageShell, PageTitle } from "@/components/PageShell";

export const metadata: Metadata = {
  title: "Terms · Qiap",
  description: "Plain-language terms for using Qiap.",
};

const UPDATED = "10 October 2026";

const sections: { title: string; body: string[] }[] = [
  {
    title: "Using Qiap",
    body: [
      "Qiap is an alarm and exercise app for Android. By installing and using it you agree to these terms. If you do not agree, please do not use it. Qiap is in early testing, so things will change and some parts will be rough.",
    ],
  },
  {
    title: "Do not rely on it alone",
    body: [
      "We work hard to make the alarm ring on time, but phones differ and some makers close background apps to save battery. Do not make Qiap your only alarm for anything you cannot afford to miss, such as a flight, an exam or a shift. Follow the battery steps in Setup for your phone and test the alarm yourself.",
    ],
  },
  {
    title: "Your health",
    body: [
      "Exercise carries some risk. Qiap is not medical advice and does not know your health. Only do what is comfortable for you, in a safe space, and stop if something hurts. The emergency exit on the ringing screen is always available.",
    ],
  },
  {
    title: "Your things stay yours",
    body: [
      "Your alarms, history and any video proofs are stored on your phone and belong to you. We do not receive them. You can delete them at any time by removing them in the app or uninstalling it.",
    ],
  },
  {
    title: "Fair use",
    body: [
      "Please do not copy, resell or reverse engineer the app in ways the law does not allow, and do not use it to harm anyone. Qiap includes open-source components that stay under their own licences.",
    ],
  },
  {
    title: "No guarantees",
    body: [
      "Qiap is provided as it is, without promises that it will be error free, that every exercise will be counted correctly, or that an alarm will always ring. To the extent the law allows, we are not liable for missed alarms, miscounted reps or injuries. Nothing here limits rights you have under the law of your country that cannot be limited.",
    ],
  },
  {
    title: "Changes",
    body: [
      "We may update these terms as Qiap grows. The current version will always be on this page with its date. Questions go to hello@qiap.app.",
    ],
  },
];

export default function Terms() {
  return (
    <PageShell>
      <p className="text-[14px] text-fg-2">Last updated {UPDATED}</p>
      <div className="mt-3">
        <PageTitle note="Plain words. The short version: do not trust any single alarm with your life, and do not hurt yourself.">
          Terms.
        </PageTitle>
      </div>
      <div className="mt-16 space-y-14">
        {sections.map((s) => (
          <section key={s.title}>
            <h2 className="text-[26px] font-medium tracking-[-0.02em]">{s.title}</h2>
            <div className="mt-4 space-y-3 text-[18px] leading-relaxed text-fg-2">
              {s.body.map((p) => (
                <p key={p}>{p}</p>
              ))}
            </div>
          </section>
        ))}
      </div>
    </PageShell>
  );
}
