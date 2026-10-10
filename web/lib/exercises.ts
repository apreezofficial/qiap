import data from "./exercises.generated.json";

export type Exercise = {
  id: string;
  name: string;
  unit: string;
  category: "lower" | "upper" | "core" | "cardio" | "mobility";
  kind: "cycle" | "alternating" | "hold" | "sequence";
  view: "front" | "side" | "floor";
  difficulty: number;
  target: number;
  needsFloor: boolean;
  jumping: boolean;
  tip: string;
};

/** Straight from the app's ExerciseCatalog.kt (see scripts/gen-exercises.mjs). */
export const exercises = data as Exercise[];

export const categories: { id: Exercise["category"]; label: string; blurb: string }[] = [
  { id: "lower", label: "Lower body", blurb: "Squats, lunges and hops that wake the big muscles first." },
  { id: "upper", label: "Upper body", blurb: "Push-ups and their cousins, plus arm work you can do in a small room." },
  { id: "core", label: "Core", blurb: "Sit-ups, planks and twists. Mostly on the floor." },
  { id: "cardio", label: "Cardio", blurb: "Jacks, burpees and boxing for the mornings you need your heart racing." },
  { id: "mobility", label: "Mobility", blurb: "Gentle holds and bends for slow starts." },
];

export const viewLabel: Record<Exercise["view"], string> = {
  front: "Face the phone",
  side: "Stand side-on",
  floor: "Phone on the floor",
};

export type Pose = "stand" | "squat" | "arms-up" | "plank";

/** Which of the four drawn figures reads closest to the exercise. */
export function poseFor(e: Exercise): Pose {
  if (e.needsFloor && (e.category === "upper" || e.category === "core" || e.id === "burpee" || e.id === "inchworm")) {
    return "plank";
  }
  if (e.category === "lower" && !e.jumping) return "squat";
  if (e.jumping || e.category === "cardio" || e.category === "mobility") return "arms-up";
  return "stand";
}

export const unitLabel = (e: Exercise) => (e.kind === "hold" ? `${e.target} seconds` : `${e.target} ${e.unit}`);
