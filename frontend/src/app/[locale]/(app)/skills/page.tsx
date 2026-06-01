import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { SkillsOverview } from "@/components/skills/skills-overview";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function SkillsIndexPage({ params }: PageProps<"/[locale]/skills">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:skill:read")) redirect("/dashboard");
  return <SkillsOverview />;
}
