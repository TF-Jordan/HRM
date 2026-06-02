import { setRequestLocale } from "next-intl/server";

import { SkillDetail } from "@/components/skills/skill-detail";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/skills/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <SkillDetail skillId={id} />;
}
