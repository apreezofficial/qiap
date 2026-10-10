import Link from "next/link";
import { Logo } from "./Brand";

const links = [
  ["The alarm", "/#alarm"],
  ["The reps", "/#reps"],
  ["Exercises", "/exercises"],
  ["Questions", "/help"],
] as const;

/**
 * Floating pill nav. Solid fill, no blur. `tone` picks the fill so it reads on the pale hero
 * panel (near-black) and on the dark pages (a step lighter).
 */
export function SiteNav({ tone = "page" }: { tone?: "page" | "panel" }) {
  return (
    <nav
      aria-label="Main"
      className={`relative z-30 mx-auto flex h-[64px] w-full max-w-[1120px] items-center justify-between rounded-full pl-5 pr-2.5 text-fg ${
        tone === "page" ? "bg-page" : "bg-panel"
      }`}
    >
      <Link href="/" aria-label="Qiap home">
        <Logo />
      </Link>

      <ul className="hidden items-center gap-9 text-[15px] text-fg-2 md:flex">
        {links.map(([label, href]) => (
          <li key={href}>
            <Link href={href} className="transition-colors hover:text-fg">
              {label}
            </Link>
          </li>
        ))}
      </ul>

      <div className="flex items-center gap-2">
        <Link href="/#early" className="btn btn-cta btn-sm">
          Early access
        </Link>
        <details className="menu relative md:hidden">
          <summary
            aria-label="Menu"
            className="flex h-11 items-center rounded-full border border-hair px-4 text-[14px] text-fg-2"
          >
            Menu
          </summary>
          <ul className="panel absolute right-0 top-14 w-56 border border-hair bg-panel p-3 text-[16px]">
            {links.map(([label, href]) => (
              <li key={href}>
                <Link href={href} className="block px-3 py-2.5 text-fg">
                  {label}
                </Link>
              </li>
            ))}
          </ul>
        </details>
      </div>
    </nav>
  );
}
