import { setRequestLocale } from "next-intl/server";

import { NewSkillForm } from "@/components/skills/new-skill-form";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/skills/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewSkillForm />;
}
