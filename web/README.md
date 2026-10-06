# Qiap landing page

Next.js (App Router, static export) + Tailwind v4. Design system comes from `../design.md`.

## Run
```bash
npm install
npm run dev        # http://localhost:3000
npm run build      # static site in ./out
npx serve out      # preview the build
```

## Structure
- `app/globals.css` — all design tokens (`@theme`), pills, glass, sky wash, marquee, reveal.
- `components/Screens.tsx` — the in-app screens (Home, Ringing, Workout, Library, History, Editor) drawn in HTML/CSS so the page needs no screenshots. Same tokens as the Android app.
- `components/Phone.tsx` — device frame; content is authored at 280×580 and scaled.
- `components/Skeleton.tsx` — pose-landmark figure (also used for exercise tiles).
- `components/Brand.tsx` — logo glyph and the 醒 seal.
- Sections: `Nav`, `Hero`, `FeatureMarquee`, `Statement`, `PhoneCarousel`, `Bento`, `Modes`, `HowItWorks`, `Exercises`, `Faq`, `CtaBanner`, `Footer`.

## TODO before launch
- [ ] **Wire the waitlist** in `components/Waitlist.tsx` (it currently validates the email and says early access isn't open; nothing is sent anywhere).
- [ ] Replace `hello@qiap.app`, the social links and the privacy / terms links in `Footer.tsx` and `Faq.tsx` with real ones.
- [x] 醒 seal is now a vector path (`SEAL_PATH` in `Brand.tsx`, from Noto Serif CJK SC Bold, OFL). Reuse it for the Android app icon and stamp.
- [ ] Add real Play Store link when it exists; add real testimonials only once real users exist (none are faked here).
- [x] Favicon (`app/icon.svg`), share image (`app/opengraph-image.png`), `robots` and `sitemap` added. Change `https://qiap.app` in `app/robots.ts` and `app/sitemap.ts` if your domain differs.
- [ ] Run Lighthouse on a mid-range phone profile. Targets are in `../design.md` §9.
