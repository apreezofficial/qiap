// Reads the Android app's ExerciseCatalog.kt and writes lib/exercises.generated.json, so the
// website's exercise pages can never drift from what the app actually counts.
// Runs before `dev`, `build` and `typecheck`. Fails loudly if the catalog stops parsing.
import { readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const source = join(here, "../../app/src/main/java/app/qiap/exercise/ExerciseCatalog.kt");
const target = join(here, "../lib/exercises.generated.json");

const text = readFileSync(source, "utf8").replace(/\r\n/g, "\n");

// One block per `val Name = ExerciseSpec(` up to its closing `\n    )` (4-space indent inside the object).
const blocks = [...text.matchAll(/\n    val (\w+) = ExerciseSpec\(\n([\s\S]*?)\n    \)\n/g)];

const pick = (block, re, fallback) => (re.exec(block)?.[1] ?? fallback);

const exercises = blocks.map(([, , body]) => ({
  id: pick(body, /\bid = "([^"]+)"/),
  name: pick(body, /\bname = "([^"]+)"/),
  unit: pick(body, /\bunit = "([^"]+)"/),
  category: pick(body, /category = Category\.(\w+)/, "LOWER").toLowerCase(),
  kind: pick(body, /kind = Kind\.(\w+)/, "CYCLE").toLowerCase(),
  view: pick(body, /view = CameraView\.(\w+)/, "FRONT").toLowerCase(),
  difficulty: Number(pick(body, /difficulty = (\d)/, "1")),
  target: Number(pick(body, /defaultTarget = (\d+)/, "12")),
  needsFloor: /needsFloor = true/.test(body),
  jumping: /jumping = true/.test(body),
  tip: pick(body, /tip = "((?:[^"\\]|\\.)*)"/, ""),
}));

if (exercises.length !== 52 || exercises.some((e) => !e.id || !e.name || !e.unit)) {
  console.error(`gen-exercises: expected 52 complete exercises, parsed ${exercises.length}.`);
  process.exit(1);
}
if (new Set(exercises.map((e) => e.id)).size !== exercises.length) {
  console.error("gen-exercises: duplicate exercise ids.");
  process.exit(1);
}

writeFileSync(target, JSON.stringify(exercises, null, 2) + "\n");
console.log(`gen-exercises: wrote ${exercises.length} exercises.`);
