import type { MetadataRoute } from "next";

export const dynamic = "force-static";

export default function sitemap(): MetadataRoute.Sitemap {
  return [
    { url: "https://qiap.app", lastModified: new Date("2026-10-06") },
    { url: "https://qiap.app/privacy", lastModified: new Date("2026-10-07") },
  ];
}
