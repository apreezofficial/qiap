import { Nav } from "@/components/Nav";
import { Hero } from "@/components/Hero";
import { FeatureMarquee } from "@/components/FeatureMarquee";
import { Statement } from "@/components/Statement";
import { PhoneCarousel } from "@/components/PhoneCarousel";
import { Bento } from "@/components/Bento";
import { Modes } from "@/components/Modes";
import { HowItWorks } from "@/components/HowItWorks";
import { Exercises } from "@/components/Exercises";
import { Faq } from "@/components/Faq";
import { CtaBanner } from "@/components/CtaBanner";
import { Footer } from "@/components/Footer";

export default function Page() {
  return (
    <>
      <Nav />
      <main>
        <Hero />
        <FeatureMarquee />
        <Statement />
        <PhoneCarousel />
        <Bento />
        <Modes />
        <HowItWorks />
        <Exercises />
        <Faq />
        <CtaBanner />
      </main>
      <Footer />
    </>
  );
}
