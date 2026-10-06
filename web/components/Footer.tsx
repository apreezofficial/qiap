import { Logo } from "./Brand";
import { Waitlist } from "./Waitlist";
import { IconInstagram, IconX, IconGithub } from "./Icons";

const pages = [
  ["Home", "#top"],
  ["Features", "#features"],
  ["Exercises", "#exercises"],
  ["How it works", "#how"],
  ["FAQ", "#faq"],
] as const;

const utility = [
  ["Privacy policy", "#"],
  ["Terms & conditions", "#"],
  ["Contact", "mailto:hello@qiap.app"],
] as const;

export function Footer() {
  return (
    <footer className="border-t border-line bg-white">
      <div className="container-qiap grid gap-10 py-14 md:grid-cols-[1.3fr_0.7fr_0.7fr_1.4fr]">
        <div>
          <Logo />
          <p className="mt-4 max-w-[280px] text-[14px] leading-relaxed text-ink-2">
            The alarm that only shuts up after you do the reps. On-device, private, built for everyday Android phones.
          </p>
          <div className="mt-5 flex gap-2">
            {[
              [IconInstagram, "Instagram"],
              [IconX, "X"],
              [IconGithub, "GitHub"],
            ].map(([I, label]) => {
              const Icon = I as typeof IconX;
              return (
                <a key={label as string} href="#" aria-label={label as string} className="grid size-9 place-items-center rounded-full bg-surface-muted text-ink transition-colors hover:bg-wash-2">
                  <Icon size={16} />
                </a>
              );
            })}
          </div>
        </div>

        <nav aria-label="Pages">
          <p className="text-[15px] font-medium">Pages</p>
          <ul className="mt-4 space-y-3 text-[14px] text-ink-2">
            {pages.map(([l, h]) => (
              <li key={l}><a href={h} className="hover:text-ink">{l}</a></li>
            ))}
          </ul>
        </nav>

        <nav aria-label="Utility">
          <p className="text-[15px] font-medium">Utility</p>
          <ul className="mt-4 space-y-3 text-[14px] text-ink-2">
            {utility.map(([l, h]) => (
              <li key={l}><a href={h} className="hover:text-ink">{l}</a></li>
            ))}
          </ul>
        </nav>

        <div className="rounded-[20px] bg-surface-muted p-6">
          <p className="text-[17px] font-medium tracking-[-0.01em]">Get launch news</p>
          <p className="mb-4 mt-1.5 text-[13px] leading-relaxed text-ink-2">One email when early access opens. Nothing else.</p>
          <Waitlist />
        </div>
      </div>
      <div className="container-qiap flex flex-col justify-between gap-2 border-t border-line py-6 text-[13px] text-ink-2 sm:flex-row">
        <span>© 2026 Qiap. All rights reserved.</span>
        <span>起 · rise</span>
      </div>
    </footer>
  );
}
