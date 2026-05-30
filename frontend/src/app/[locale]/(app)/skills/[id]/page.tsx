import { setRequestLocale } from "next-intl/server";

import { SkillDetail } from "@/components/skills/skill-detail";

export default async function SkillDetailPage({
  params,
}: PageProps<"/[locale]/skills/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <SkillDetail skillId={id} />;
}
