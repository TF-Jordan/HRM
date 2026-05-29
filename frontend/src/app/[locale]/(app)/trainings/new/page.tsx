import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewTrainingForm } from "@/components/trainings/new-training-form";
import { readSession } from "@/server/session";

export default async function NewTrainingPage({ params }: PageProps<"/[locale]/trainings/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!session.user.permissions.includes("hrm:training:create")) {
    redirect("/trainings");
  }
  return <NewTrainingForm />;
}
