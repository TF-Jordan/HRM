import { setRequestLocale } from "next-intl/server";
import { DashboardClient } from "./dashboard-client";
import { requireSession } from "@/server/session";

export default async function DashboardPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const session = await requireSession();
  return <DashboardClient displayName={session.user.displayName} />;
}
