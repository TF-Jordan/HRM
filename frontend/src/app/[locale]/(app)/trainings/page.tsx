import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { TrainingsCatalog } from "@/components/trainings/trainings-catalog";
import { readSession } from "@/server/session";

export default async function TrainingsIndexPage({ params }: PageProps<"/[locale]/trainings">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");

  // DRH / manager / admin → catalog view. Plain employee → /mine.
  const canRead = session.user.permissions.includes("hrm:training:read");
  const canManage =
    session.user.permissions.includes("hrm:training:manage") ||
    session.user.permissions.includes("hrm:training:create");
  if (canRead && !canManage) redirect("/trainings/mine");
  return <TrainingsCatalog />;
}
