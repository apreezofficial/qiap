import type { Metadata } from "next";
import { Nav } from "@/components/Nav";
import { Footer } from "@/components/Footer";

export const metadata: Metadata = {
  title: "Privacy policy · Qiap",
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
      "If you turn on Video proof for an alarm, Qiap records your workout to a video file in the app's private storage on your phone. It is off by default and has no sound. You choose how long it is kept (7, 14 or 30 days, 14 by default) and then Qiap deletes it. Other apps cannot read it. It is only shared if you tap Share and pick where it goes.",
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
    title: "Crash logs",
    body: [
      "If the app crashes, a short error note is saved on your phone. It is never sent automatically. You can choose to share it with us from Setup if you want help.",
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
    <>
      <Nav />
      <main className="container-qiap max-w-[760px] py-16">
        <p className="text-[13px] text-ink-3">Last updated {UPDATED}</p>
        <h1 className="mt-2 text-[40px] font-medium leading-[1.05] tracking-[-0.03em] md:text-[56px]">
          Privacy policy. <span className="text-ink-2">Short, because there is little to tell.</span>
        </h1>
        <div className="mt-12 space-y-10">
          {sections.map((s) => (
            <section key={s.title}>
              <h2 className="text-[22px] font-medium tracking-[-0.02em]">{s.title}</h2>
              <div className="mt-3 space-y-3 text-[16px] leading-relaxed text-ink-2">
                {s.body.map((p) => (
                  <p key={p}>{p}</p>
                ))}
              </div>
            </section>
          ))}
        </div>
      </main>
      <Footer />
    </>
  );
}
