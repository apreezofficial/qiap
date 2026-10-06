import type { Metadata, Viewport } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Qiap — the alarm that only stops when you do the reps",
  description:
    "Qiap is an Android alarm that keeps ringing until your camera confirms you've done the workout. On-device pose tracking, 52 exercises, no account needed.",
  openGraph: {
    title: "Qiap — Wake up. Prove it.",
    description: "The alarm that only shuts up after you do the reps.",
    type: "website",
  },
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#ffffff",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <noscript>
          <style>{`.reveal{opacity:1!important;transform:none!important}`}</style>
        </noscript>
      </head>
      <body>{children}</body>
    </html>
  );
}
