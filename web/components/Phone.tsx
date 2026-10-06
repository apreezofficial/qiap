import type { CSSProperties, ReactNode } from "react";

export const PHONE_W = 280;
export const PHONE_H = 580;

/**
 * Device frame. Content is laid out at a fixed 280×580 and scaled with a transform,
 * so every screen looks identical at any size (no per-size CSS).
 */
export function PhoneFrame({
  children,
  scale = 1,
  dim = 0,
  className = "",
  style,
}: {
  children: ReactNode;
  scale?: number;
  /** 0–1 white veil, used for the faded side phones */
  dim?: number;
  className?: string;
  style?: CSSProperties;
}) {
  return (
    <div
      className={`relative shrink-0 ${className}`}
      style={{ width: PHONE_W * scale, height: PHONE_H * scale, ...style }}
    >
      <div
        className="absolute left-0 top-0 origin-top-left"
        style={{ width: PHONE_W, height: PHONE_H, transform: `scale(${scale})` }}
      >
        <div
          className="relative h-full w-full rounded-[44px] bg-[#15151a] p-[7px]"
          style={{ boxShadow: "var(--shadow-phone), inset 0 0 0 1.5px #3a3a42" }}
        >
          <div className="relative h-full w-full overflow-hidden rounded-[37px] bg-white font-app text-ink">
            {children}
            {/* dynamic island */}
            <div className="absolute left-1/2 top-[9px] z-30 h-[22px] w-[78px] -translate-x-1/2 rounded-full bg-black" />
            {dim > 0 && (
              <div className="absolute inset-0 z-40 bg-white" style={{ opacity: dim }} />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export function StatusBar({ light = false, time = "6:42" }: { light?: boolean; time?: string }) {
  return (
    <div
      className={`absolute inset-x-0 top-0 z-20 flex h-[44px] items-center justify-between px-6 pt-1 text-[11px] font-semibold ${light ? "text-white" : "text-ink"}`}
    >
      <span className="tnum">{time}</span>
      <span className="flex items-center gap-1 opacity-90">
        <svg width="14" height="9" viewBox="0 0 14 9" fill="currentColor"><rect x="0" y="6" width="2.2" height="3" rx=".6"/><rect x="3.6" y="4" width="2.2" height="5" rx=".6"/><rect x="7.2" y="2" width="2.2" height="7" rx=".6"/><rect x="10.8" y="0" width="2.2" height="9" rx=".6"/></svg>
        <svg width="20" height="9" viewBox="0 0 20 9" fill="none" stroke="currentColor"><rect x=".5" y=".5" width="16" height="8" rx="2.2"/><rect x="2" y="2" width="11" height="5" rx="1" fill="currentColor" stroke="none"/><rect x="17.5" y="3" width="1.6" height="3" rx=".8" fill="currentColor" stroke="none"/></svg>
      </span>
    </div>
  );
}
