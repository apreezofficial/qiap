"use client";

import { useEffect, useRef, type ReactNode } from "react";
import { IconStar } from "./Icons";

/** Fades/slides children in when scrolled into view. Content stays visible without JS (see layout noscript). */
export function Reveal({
  children,
  className = "",
  delay = 0,
}: {
  children: ReactNode;
  className?: string;
  delay?: number;
}) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    if (typeof IntersectionObserver === "undefined") {
      el.classList.add("in");
      return;
    }
    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((e) => {
          if (e.isIntersecting) {
            el.classList.add("in");
            io.disconnect();
          }
        });
      },
      { threshold: 0.12, rootMargin: "0px 0px -40px 0px" },
    );
    io.observe(el);
    return () => io.disconnect();
  }, []);

  return (
    <div ref={ref} className={`reveal ${className}`} style={{ transitionDelay: `${delay}ms` }}>
      {children}
    </div>
  );
}

export function SectionBadge({ children }: { children: ReactNode }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full bg-wash px-3 py-1.5 text-[13px] font-medium text-ink">
      <IconStar size={13} className="text-sky" />
      {children}
    </span>
  );
}

/** Two-tone section heading: black lead + gray accent phrase (design.md §2.2). */
export function Heading({
  lead,
  accent,
  className = "",
  as: Tag = "h2",
}: {
  lead: ReactNode;
  accent?: ReactNode;
  className?: string;
  as?: "h1" | "h2" | "h3";
}) {
  return (
    <Tag className={`h-display text-[34px] md:text-[48px] ${className}`}>
      {lead}
      {accent ? (
        <>
          {" "}
          <span className="accent">{accent}</span>
        </>
      ) : null}
    </Tag>
  );
}

export function IconTile({ children, label }: { children: ReactNode; label: string }) {
  return (
    <span
      aria-label={label}
      title={label}
      className="grid size-12 place-items-center rounded-xl bg-surface-muted text-ink"
    >
      {children}
    </span>
  );
}
