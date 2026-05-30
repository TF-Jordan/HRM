import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewSkillForm } from "@/components/skills/new-skill-form";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function NewSkillPage({ params }: PageProps<"/[locale]/skills/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:skill:create")) redirect("/skills");
  return <NewSkillForm />;
}
