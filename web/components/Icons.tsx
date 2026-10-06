import type { SVGProps } from "react";

type P = SVGProps<SVGSVGElement> & { size?: number };

function base({ size = 18, ...rest }: P) {
  return {
    width: size,
    height: size,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.75,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
    "aria-hidden": true,
    ...rest,
  };
}

export const IconStar = (p: P) => (
  <svg {...base(p)} fill="currentColor" stroke="none">
    <path d="M12 2.5l2.9 6.1 6.6.8-4.9 4.6 1.3 6.6L12 17.3 6.1 20.6l1.3-6.6L2.5 9.4l6.6-.8L12 2.5z" />
  </svg>
);
export const IconApple = (p: P) => (
  <svg {...base(p)} fill="currentColor" stroke="none">
    <path d="M16.4 12.6c0-2.3 1.9-3.4 2-3.5-1.1-1.6-2.8-1.8-3.4-1.8-1.4-.1-2.8.8-3.5.8-.7 0-1.9-.8-3.1-.8-1.6 0-3 .9-3.9 2.3-1.7 2.9-.4 7.2 1.2 9.5.8 1.1 1.7 2.4 3 2.4 1.2 0 1.7-.8 3.1-.8s1.8.8 3.1.8c1.3 0 2.1-1.2 2.9-2.3.9-1.3 1.3-2.6 1.3-2.7 0 0-2.5-1-2.5-3.9zM14.1 5.7c.6-.8 1.1-1.9 1-3-1 0-2.1.6-2.8 1.4-.6.7-1.1 1.8-1 2.9 1.1.1 2.1-.5 2.8-1.3z" />
  </svg>
);
export const IconAndroid = (p: P) => (
  <svg {...base(p)} fill="currentColor" stroke="none">
    <path d="M7 9.5h10v7.2a1.3 1.3 0 01-1.3 1.3H14v2.2a1.1 1.1 0 01-2.2 0V18h-1.6v2.2a1.1 1.1 0 01-2.2 0V18H6.3A1.3 1.3 0 015 16.7V9.5h2zm-2.9.2a1.1 1.1 0 012.2 0v5a1.1 1.1 0 01-2.2 0v-5zm14.6 0a1.1 1.1 0 012.2 0v5a1.1 1.1 0 01-2.2 0v-5zM7.3 8.6A4.8 4.8 0 0112 4a4.8 4.8 0 014.7 4.6H7.3zm2.2-1.9a.5.5 0 100-1 .5.5 0 000 1zm5 0a.5.5 0 100-1 .5.5 0 000 1z" />
    <path d="M8.4 3.2l1 1.5M15.6 3.2l-1 1.5" stroke="currentColor" strokeWidth="1" />
  </svg>
);
export const IconPlay = (p: P) => (
  <svg {...base(p)} fill="currentColor" stroke="none">
    <path d="M8 5.2v13.6a.8.8 0 001.2.7l11-6.8a.8.8 0 000-1.4L9.2 4.5A.8.8 0 008 5.2z" />
  </svg>
);
export const IconArrow = (p: P) => (
  <svg {...base(p)}>
    <path d="M5 12h14M13 6l6 6-6 6" />
  </svg>
);
export const IconCheck = (p: P) => (
  <svg {...base(p)} strokeWidth={2.25}>
    <path d="M5 12.5l4.5 4.5L19 7.5" />
  </svg>
);
export const IconChevron = (p: P) => (
  <svg {...base(p)}>
    <path d="M6 9l6 6 6-6" />
  </svg>
);
export const IconChevronLeft = (p: P) => (
  <svg {...base(p)}>
    <path d="M15 6l-6 6 6 6" />
  </svg>
);
export const IconChevronRight = (p: P) => (
  <svg {...base(p)}>
    <path d="M9 6l6 6-6 6" />
  </svg>
);
export const IconAlarm = (p: P) => (
  <svg {...base(p)}>
    <circle cx="12" cy="13" r="7.5" />
    <path d="M12 9v4l2.5 1.5M4 5.5L7 3M20 5.5L17 3" />
  </svg>
);
export const IconCamera = (p: P) => (
  <svg {...base(p)}>
    <path d="M3 8.5A2.5 2.5 0 015.5 6h2L9 4h6l1.5 2h2A2.5 2.5 0 0121 8.5v8a2.5 2.5 0 01-2.5 2.5h-13A2.5 2.5 0 013 16.5v-8z" />
    <circle cx="12" cy="12.5" r="3.5" />
  </svg>
);
export const IconBolt = (p: P) => (
  <svg {...base(p)}>
    <path d="M13 3L5 13.5h6L10 21l8-10.5h-6L13 3z" />
  </svg>
);
export const IconShield = (p: P) => (
  <svg {...base(p)}>
    <path d="M12 3l7.5 3v5.5c0 4.5-3.1 7.7-7.5 9.5-4.4-1.8-7.5-5-7.5-9.5V6L12 3z" />
    <path d="M9 12l2.2 2.2L15.5 10" />
  </svg>
);
export const IconOffline = (p: P) => (
  <svg {...base(p)}>
    <path d="M3 3l18 18M8.5 8.7A9 9 0 003 11.5M5.5 14.5a6 6 0 013.3-1.9M12 20h.01M15.5 12.2A9 9 0 0121 11.5M16.5 15.5a6 6 0 00-1.4-1" />
  </svg>
);
export const IconVideo = (p: P) => (
  <svg {...base(p)}>
    <rect x="3" y="6.5" width="12.5" height="11" rx="2.5" />
    <path d="M15.5 10.5l5.5-3v9l-5.5-3" />
  </svg>
);
export const IconFlame = (p: P) => (
  <svg {...base(p)}>
    <path d="M12 3c.5 3.2 4.5 4.8 4.5 9.2A4.5 4.5 0 0112 17a4.5 4.5 0 01-4.5-4.4c0-1.8.9-3 1.9-4C10.4 10 11.5 10 12 8.5 12.3 6.8 12 5 12 3z" />
    <path d="M12 21a6.5 6.5 0 006.5-6.5" />
  </svg>
);
export const IconDumbbell = (p: P) => (
  <svg {...base(p)}>
    <path d="M6.5 6.5v11M17.5 6.5v11M3.5 9v6M20.5 9v6M6.5 12h11" />
  </svg>
);
export const IconLock = (p: P) => (
  <svg {...base(p)}>
    <rect x="5" y="10.5" width="14" height="10" rx="2.5" />
    <path d="M8 10.5V8a4 4 0 118 0v2.5" />
  </svg>
);
export const IconClock = (p: P) => (
  <svg {...base(p)}>
    <circle cx="12" cy="12" r="8.5" />
    <path d="M12 7.5V12l3 2" />
  </svg>
);
export const IconSearch = (p: P) => (
  <svg {...base(p)}>
    <circle cx="11" cy="11" r="6.5" />
    <path d="M20 20l-4-4" />
  </svg>
);
export const IconHome = (p: P) => (
  <svg {...base(p)}>
    <path d="M4 11l8-6.5 8 6.5v8a1.5 1.5 0 01-1.5 1.5h-3V14h-7v6.5h-3A1.5 1.5 0 014 19v-8z" />
  </svg>
);
export const IconGrid = (p: P) => (
  <svg {...base(p)}>
    <rect x="4" y="4" width="6.5" height="6.5" rx="1.5" />
    <rect x="13.5" y="4" width="6.5" height="6.5" rx="1.5" />
    <rect x="4" y="13.5" width="6.5" height="6.5" rx="1.5" />
    <rect x="13.5" y="13.5" width="6.5" height="6.5" rx="1.5" />
  </svg>
);
export const IconCalendar = (p: P) => (
  <svg {...base(p)}>
    <rect x="4" y="5.5" width="16" height="15" rx="2.5" />
    <path d="M4 10h16M8.5 3.5v4M15.5 3.5v4" />
  </svg>
);
export const IconInstagram = (p: P) => (
  <svg {...base(p)}>
    <rect x="3.5" y="3.5" width="17" height="17" rx="5" />
    <circle cx="12" cy="12" r="4" />
    <circle cx="17" cy="7" r="0.6" fill="currentColor" />
  </svg>
);
export const IconX = (p: P) => (
  <svg {...base(p)} fill="currentColor" stroke="none">
    <path d="M17.8 3h3L14.2 10.5 22 21h-6.1l-4.8-6.3L5.6 21h-3l7.1-8.1L2 3h6.2l4.3 5.8L17.8 3zm-1 16.2h1.7L7.3 4.7H5.5l11.3 14.5z" />
  </svg>
);
export const IconGithub = (p: P) => (
  <svg {...base(p)}>
    <path d="M9 19c-4.3 1.4-4.3-2.5-6-3m12 5v-3.5c0-1 .1-1.4-.5-2 2.8-.3 5.5-1.4 5.5-6a4.6 4.6 0 00-1.3-3.2 4.2 4.2 0 00-.1-3.2s-1.1-.3-3.5 1.3a12.3 12.3 0 00-6.2 0C6.5 2.8 5.4 3.1 5.4 3.1a4.2 4.2 0 00-.1 3.2A4.6 4.6 0 004 9.5c0 4.6 2.7 5.7 5.5 6-.6.6-.6 1.2-.5 2V21" />
  </svg>
);
export const IconMenu = (p: P) => (
  <svg {...base(p)}>
    <path d="M4 7h16M4 12h16M4 17h16" />
  </svg>
);
export const IconClose = (p: P) => (
  <svg {...base(p)}>
    <path d="M6 6l12 12M18 6L6 18" />
  </svg>
);
