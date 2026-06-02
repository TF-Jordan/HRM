import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { TrainingsCatalog } from "@/components/trainings/trainings-catalog";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function TrainingsCatalogPage({
  params,
}: PageProps<"/[locale]/trainings/catalog">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:training:read")) redirect("/dashboard");
  return <TrainingsCatalog />;
}
