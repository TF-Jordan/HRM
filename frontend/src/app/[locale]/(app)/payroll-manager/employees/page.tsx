import { setRequestLocale } from "next-intl/server";

import { EmployeesList } from "@/components/employees/employees-list";

export default async function Page({ params }: PageProps<"/[locale]/payroll-manager/employees">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <EmployeesList />;
}
