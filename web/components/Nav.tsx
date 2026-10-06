import { Logo } from "./Brand";
import { IconAndroid, IconApple } from "./Icons";
import { IconTile } from "./ui";

const links = [
  ["Features", "#features"],
  ["Exercises", "#exercises"],
  ["How it works", "#how"],
  ["FAQ", "#faq"],
] as const;

export function Nav() {
  return (
    <header className="sticky top-3 z-50 px-3 pt-3">
      <nav
        aria-label="Main"
        className="mx-auto flex h-[60px] max-w-[1120px] items-center justify-between rounded-2xl border border-line bg-white/90 px-3 shadow-soft md:h-[68px] md:px-4"
      >
        <a href="#top" aria-label="Qiap home" className="pl-1">
          <Logo />
        </a>

        <ul className="hidden items-center gap-8 text-[14px] text-ink-2 md:flex">
          {links.map(([label, href]) => (
            <li key={href}>
              <a href={href} className="transition-colors hover:text-ink">
                {label}
              </a>
            </li>
          ))}
        </ul>

        <div className="flex items-center gap-2">
          <span className="hidden items-center gap-2 sm:flex">
            <IconTile label="iOS: not available (Android first)">
              <IconApple size={20} />
            </IconTile>
            <IconTile label="Android: early access soon">
              <IconAndroid size={20} />
            </IconTile>
          </span>
          <a href="#get" className="btn btn-primary btn-sm sm:hidden">
            Get access
          </a>
        </div>
      </nav>
    </header>
  );
}
