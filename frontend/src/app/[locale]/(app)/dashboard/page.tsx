import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { DashboardContent } from "@/components/dashboard/dashboard-content";
import { roleHomePath } from "@/lib/roles";
import { readSession } from "@/server/session";

export default async function DashboardPage({ params }: PageProps<"/[locale]/dashboard">) {
  const { locale } = await params;
  setRequestLocale(locale);

  // Single dispatcher: send users whose role has a dedicated namespace to their
  // own dashboard. Non-migrated roles keep rendering the shared dashboard here.
  const session = await readSession();
  if (!session) redirect("/login");
  const home = roleHomePath(session.user.roles, session.user.permissions);
  if (home !== "/dashboard") redirect(home);

  return <DashboardContent />;
}
