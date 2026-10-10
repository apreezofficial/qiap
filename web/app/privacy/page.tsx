import type { Metadata } from "next";
import { PageShell, PageTitle } from "@/components/PageShell";

export const metadata: Metadata = {
  title: "Privacy · Qiap",
  description: "Qiap runs entirely on your phone. No account, no ads, no analytics, and no internet permission.",
};

const UPDATED = "7 October 2026";

const sections: { title: string; body: string[] }[] = [
  {
    title: "The short version",
    body: [
      "Qiap works entirely on your phone. It has no account, no ads and no analytics, and the app does not even ask Android for permission to use the internet, so it cannot send your data anywhere.",
    ],
  },
  {
    title: "The camera",
    body: [
      "Qiap uses the front camera to see your body position and count your reps. This happens on your phone. Camera frames are analysed and thrown away. They are not saved and not sent anywhere.",
    ],
  },
  {
    title: "Video proof (optional)",
    body: [
      "If you turn on video proof for an alarm, Qiap records your workout to a video file in the app's private storage on your phone. It is off by default and has no sound. You choose how long it is kept (7, 14 or 30 days, 14 by default) and then Qiap deletes it. Other apps cannot read it. It is only shared if you tap Share and pick where it goes.",
    ],
  },
  {
    title: "What is stored on your phone",
    body: [
      "Your alarms and settings, a history of which mornings you finished (date, exercise, reps, time taken), and any video proofs you chose to record. All of it stays in the app's private storage and is removed when you uninstall Qiap. Android backup is switched off for the app, so it is not copied to your Google account either.",
    ],
  },
  {
    title: "Permissions and why",
    body: [
      "Camera: count your reps and, if you ask for it, record video proof.",
      "Notifications and full-screen alert: show the ringing screen over your lock screen.",
      "Exact alarms and start at boot: ring at the right minute, including after a restart.",
      "Foreground service, wake lock and vibration: keep the alarm sounding until you finish.",
      "Motion sensor: notice when the phone is being shaken instead of propped up. Readings are used in the moment and not stored.",
    ],
  },
  {
    title: "Third-party code",
    body: [
      "Rep counting uses Google's MediaPipe pose model, which is bundled inside the app and runs offline. That library contains code that can report usage statistics. Qiap removes the internet permission it asks for, so on a Qiap install that code cannot connect to anything.",
    ],
  },
  {
    title: "Crash notes",
    body: [
      "If the app crashes, a short error note is saved on your phone. It is never sent automatically. You can choose to share it with us from Setup if you want help.",
    ],
  },
  {
    title: "This website",
    body: [
      "This site sets no cookies, runs no analytics and loads no third-party scripts. If you email us, we only have what you wrote.",
    ],
  },
  {
    title: "Your health",
    body: [
      "Exercise carries some risk. Qiap is not medical advice. Only do what is comfortable for you. The emergency exit on the ringing screen is always there if you are hurt or cannot exercise.",
    ],
  },
  {
    title: "Children",
    body: [
      "Qiap is a general alarm and exercise app and is not designed for children under 13. It collects no personal information from anyone.",
    ],
  },
  {
    title: "Changes and contact",
    body: [
      "If this policy changes, the new version will be posted here with a new date. Questions: hello@qiap.app.",
    ],
  },
];

export default function Privacy() {
  return (
    <PageShell>
      <p className="text-[14px] text-fg-2">Last updated {UPDATED}</p>
      <div className="mt-3">
        <PageTitle note="Short, because there is very little to tell.">Privacy.</PageTitle>
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
