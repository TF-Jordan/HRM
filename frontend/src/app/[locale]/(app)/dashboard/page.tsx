import { setRequestLocale } from "next-intl/server";
import { DashboardClient } from "./dashboard-client";
import { requireSession } from "@/server/session";
import { getProfileSummary } from "@/server/profile";

export default async function DashboardPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  const session = await requireSession();
  const profile = await getProfileSummary();
  return (
    <DashboardClient
      firstName={profile?.firstName || session.user.displayName}
      roleCode={profile?.roleCode ?? "EMPLOYE"}
      employeeId={profile?.employeeId ?? null}
    />
  );
}
