// Dev helper: renders each Modes tab to static HTML so the shareable preview can swap panels without React.
import { renderToStaticMarkup } from "react-dom/server";
import { writeFileSync } from "node:fs";
import { Modes } from "../components/Modes";

for (let i = 0; i < 5; i++) {
  const html = renderToStaticMarkup(<Modes initial={i} />);
  writeFileSync(`/tmp/modes-${i}.html`, html);
}
console.log("rendered 5 modes");
