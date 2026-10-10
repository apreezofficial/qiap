import Link from "next/link";
import { Logo } from "./Brand";

export function SiteFooter() {
  return (
    <footer className="mx-auto max-w-[1200px] px-5 pb-12 pt-10 sm:px-8">
      <div className="grid gap-12 border-t border-hair pt-14 md:grid-cols-[1.5fr_1fr_1fr]">
        <div>
          <Logo />
          <p className="mt-5 max-w-[320px] text-[16px] leading-relaxed text-fg-2">
            An alarm for people who negotiate with their alarm. Android first, private, no account.
          </p>
          <a href="mailto:hello@qiap.app" className="link-under mt-5 inline-block text-[16px]">
            hello@qiap.app
          </a>
        </div>

        <nav aria-label="Look around">
          <p className="text-[13px] uppercase tracking-[0.12em] text-fg-2">Look around</p>
          <ul className="mt-5 space-y-3 text-[16px]">
            <li><Link href="/#alarm" className="hover:text-fg-2">The alarm</Link></li>
            <li><Link href="/#reps" className="hover:text-fg-2">The reps</Link></li>
            <li><Link href="/exercises" className="hover:text-fg-2">All 52 exercises</Link></li>
            <li><Link href="/help" className="hover:text-fg-2">Questions</Link></li>
          </ul>
        </nav>

        <nav aria-label="Small print">
          <p className="text-[13px] uppercase tracking-[0.12em] text-fg-2">Small print</p>
          <ul className="mt-5 space-y-3 text-[16px]">
            <li><Link href="/privacy" className="hover:text-fg-2">Privacy</Link></li>
            <li><Link href="/terms" className="hover:text-fg-2">Terms</Link></li>
            <li><a href="mailto:hello@qiap.app" className="hover:text-fg-2">Contact</a></li>
          </ul>
        </nav>
      </div>
      <p className="mt-14 max-w-[640px] text-[13px] leading-relaxed text-fg-2">
        © 2026 Qiap. The 醒 seal is drawn from Noto Serif CJK SC Bold, used under the SIL Open Font License 1.1.
      </p>
    </footer>
  );
}
