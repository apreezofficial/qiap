import type { MetadataRoute } from "next";

export const dynamic = "force-static";

const pages = ["", "/exercises", "/help", "/privacy", "/terms"];

export default function sitemap(): MetadataRoute.Sitemap {
  return pages.map((p) => ({ url: `https://qiap.app${p}`, lastModified: new Date("2026-10-10") }));
}
