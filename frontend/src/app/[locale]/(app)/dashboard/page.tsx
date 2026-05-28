import { setRequestLocale } from "next-intl/server";

import { DashboardContent } from "@/components/dashboard/dashboard-content";

export default async function DashboardPage({ params }: PageProps<"/[locale]/dashboard">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <DashboardContent />;
}
