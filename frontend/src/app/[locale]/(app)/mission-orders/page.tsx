import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { MissionsQueue } from "@/components/missions/missions-queue";
import { readSession } from "@/server/session";

export default async function MissionOrdersIndexPage({
  params,
}: PageProps<"/[locale]/mission-orders">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");

  const canManage = session.user.permissions.includes("hrm:mission:manage");
  if (!canManage) redirect("/mission-orders/mine");
  return <MissionsQueue />;
}
