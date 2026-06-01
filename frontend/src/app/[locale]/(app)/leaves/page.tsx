import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { LeavesQueue } from "@/components/leaves/leaves-queue";
import { readSession } from "@/server/session";

export default async function LeavesIndexPage({ params }: PageProps<"/[locale]/leaves">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");

  // Manager / admin → pending queue. Employee → self-service.
  const canApprove = session.user.permissions.includes("hrm:leave:approve");
  if (!canApprove) redirect("/leaves/my");
  return <LeavesQueue />;
}
