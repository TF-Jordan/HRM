import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewMissionForm } from "@/components/missions/new-mission-form";
import { readSession } from "@/server/session";

export default async function NewMissionOrderPage({
  params,
}: PageProps<"/[locale]/mission-orders/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!session.user.permissions.includes("hrm:mission:create")) {
    redirect("/mission-orders");
  }
  return <NewMissionForm />;
}
