import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { MedicalOverview } from "@/components/medical/medical-overview";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function MedicalIndexPage({
  params,
}: PageProps<"/[locale]/medical">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:medical:read")) redirect("/dashboard");

  // Plain employees → self-service view. HR / médecin → org-wide overview.
  const canCreate = hasPermission(session, "hrm:medical:create");
  if (!canCreate) redirect("/medical/mine");
  return <MedicalOverview />;
}
