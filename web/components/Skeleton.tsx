type Pose = "stand" | "squat" | "arms-up" | "plank";

type Pt = [number, number];
type Joints = Record<
  "head" | "neck" | "hip" | "lSh" | "rSh" | "lEl" | "rEl" | "lWr" | "rWr" | "lKn" | "rKn" | "lAn" | "rAn",
  Pt
>;

const POSES: Record<Pose, Joints> = {
  stand: {
    head: [100, 28], neck: [100, 50], hip: [100, 112],
    lSh: [82, 54], rSh: [118, 54], lEl: [76, 84], rEl: [124, 84], lWr: [74, 112], rWr: [126, 112],
    lKn: [90, 152], rKn: [110, 152], lAn: [88, 194], rAn: [112, 194],
  },
  squat: {
    head: [100, 62], neck: [100, 84], hip: [100, 140],
    lSh: [82, 88], rSh: [118, 88], lEl: [66, 100], rEl: [134, 100], lWr: [56, 84], rWr: [144, 84],
    lKn: [72, 150], rKn: [128, 150], lAn: [74, 194], rAn: [126, 194],
  },
  "arms-up": {
    head: [100, 34], neck: [100, 56], hip: [100, 118],
    lSh: [82, 60], rSh: [118, 60], lEl: [66, 36], rEl: [134, 36], lWr: [62, 10], rWr: [138, 10],
    lKn: [84, 156], rKn: [116, 156], lAn: [70, 194], rAn: [130, 194],
  },
  plank: {
    head: [30, 92], neck: [48, 100], hip: [118, 122],
    lSh: [52, 102], rSh: [52, 102], lEl: [52, 134], rEl: [52, 134], lWr: [52, 168], rWr: [52, 168],
    lKn: [158, 138], rKn: [158, 138], lAn: [196, 154], rAn: [196, 154],
  },
};

const BONES: [keyof Joints, keyof Joints][] = [
  ["neck", "hip"], ["lSh", "rSh"], ["lSh", "lEl"], ["lEl", "lWr"], ["rSh", "rEl"], ["rEl", "rWr"],
  ["hip", "lKn"], ["lKn", "lAn"], ["hip", "rKn"], ["rKn", "rAn"],
];

/** Pose-landmark style figure. Paper-white bones, jade (good) / saffron (off) joints. Matches the in-app skeleton overlay. */
export function Skeleton({
  pose = "stand",
  bone = "#F4EFE6",
  className = "",
  flag = [],
}: {
  pose?: Pose;
  bone?: string;
  className?: string;
  /** joint keys to render as "off form" (saffron) */
  flag?: (keyof Joints)[];
}) {
  const j = POSES[pose];
  const joints = Object.keys(j) as (keyof Joints)[];
  return (
    <svg viewBox="0 0 200 210" className={className} fill="none" aria-hidden>
      {BONES.map(([a, b], i) => (
        <line
          key={i}
          x1={j[a][0]} y1={j[a][1]} x2={j[b][0]} y2={j[b][1]}
          stroke={bone} strokeWidth="4" strokeLinecap="round" opacity="0.92"
        />
      ))}
      <circle cx={j.head[0]} cy={j.head[1]} r="13" stroke={bone} strokeWidth="4" />
      <line x1={j.head[0]} y1={j.head[1] + 13} x2={j.neck[0]} y2={j.neck[1]} stroke={bone} strokeWidth="4" strokeLinecap="round" />
      {joints
        .filter((k) => k !== "head" && k !== "neck")
        .map((k) => (
          <circle
            key={k}
            cx={j[k][0]} cy={j[k][1]} r="5.5"
            fill={flag.includes(k) ? "#F5B83D" : "#34C38F"}
            stroke="#0D0D0F" strokeWidth="1.5"
          />
        ))}
    </svg>
  );
}
