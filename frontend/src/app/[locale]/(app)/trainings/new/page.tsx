import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewTrainingForm } from "@/components/trainings/new-training-form";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function NewTrainingPage({ params }: PageProps<"/[locale]/trainings/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:training:create")) redirect("/trainings");
  return <NewTrainingForm />;
}
