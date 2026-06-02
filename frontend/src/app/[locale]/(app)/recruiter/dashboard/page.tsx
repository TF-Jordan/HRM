import { setRequestLocale } from "next-intl/server";

import { DashboardContent } from "@/components/dashboard/dashboard-content";

export default async function Page({ params }: PageProps<"/[locale]/recruiter/dashboard">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <DashboardContent />;
}
