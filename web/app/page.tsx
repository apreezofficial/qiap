import { Hero } from "@/components/Hero";
import { Early, Promises, Proof, Rows, Status, Steps } from "@/components/HomeSections";
import { SiteFooter } from "@/components/SiteFooter";

export default function Page() {
  return (
    <>
      <main>
        <Hero />
        <Proof />
        <Rows />
        <Steps />
        <Promises />
        <Status />
        <Early />
      </main>
      <SiteFooter />
    </>
  );
}
