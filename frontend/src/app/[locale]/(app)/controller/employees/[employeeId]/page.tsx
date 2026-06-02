import { setRequestLocale } from "next-intl/server";

import { EmployeeDetail } from "@/components/employees/employee-detail";

export default async function Page({ params }: PageProps<"/[locale]/controller/employees/[employeeId]">) {
  const { locale, employeeId } = await params;
  setRequestLocale(locale);
  return <EmployeeDetail employeeId={employeeId} />;
}
