import { setRequestLocale } from "next-intl/server";

import { SkillsOverview } from "@/components/skills/skills-overview";

export default async function Page({ params }: PageProps<"/[locale]/controller/skills">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <SkillsOverview />;
}
