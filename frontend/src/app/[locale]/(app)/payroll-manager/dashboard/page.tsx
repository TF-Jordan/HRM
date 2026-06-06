import { setRequestLocale } from "next-intl/server";

import { PayrollDashboard } from "@/components/payroll/payroll-dashboard";

export default async function Page({ params }: PageProps<"/[locale]/payroll-manager/dashboard">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <PayrollDashboard />;
}
