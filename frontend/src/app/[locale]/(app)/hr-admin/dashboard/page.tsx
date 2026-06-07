import { setRequestLocale } from "next-intl/server";

import { HrAdminDashboard } from "@/components/dashboard/hr-admin-dashboard";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/dashboard">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <HrAdminDashboard />;
}
