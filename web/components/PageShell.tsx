import type { ReactNode } from "react";
import { SiteFooter } from "./SiteFooter";
import { SiteNav } from "./SiteNav";

/** Frame for every inner page: floating nav, a readable column, the footer. */
export function PageShell({ children, wide = false }: { children: ReactNode; wide?: boolean }) {
  return (
    <>
      <div className="px-2 pt-2 sm:px-4 sm:pt-4">
        <SiteNav tone="panel" />
      </div>
      <main className={`mx-auto px-5 pb-24 pt-16 sm:px-8 sm:pt-24 ${wide ? "max-w-[1200px]" : "max-w-[820px]"}`}>
        {children}
      </main>
      <SiteFooter />
    </>
  );
}

export function PageTitle({ children, note }: { children: ReactNode; note?: ReactNode }) {
  return (
    <header>
      <h1 className="h-display text-[48px] sm:text-[84px]">{children}</h1>
      {note ? <p className="mt-6 max-w-[620px] text-[19px] leading-relaxed text-fg-2">{note}</p> : null}
    </header>
  );
}
