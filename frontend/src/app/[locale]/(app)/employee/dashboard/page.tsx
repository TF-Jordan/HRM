import { setRequestLocale } from "next-intl/server";

import { EmployeeDashboard } from "@/components/dashboard/employee-dashboard";

export default async function Page({ params }: PageProps<"/[locale]/employee/dashboard">) {
  const { locale } = await params;
  setRequestLocale(locale);
  // The employee workspace always shows the personal dashboard, regardless of any elevated
  // permissions the account may also hold (those drive the manager/HR workspaces instead).
  return <EmployeeDashboard />;
}
