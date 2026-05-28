import { setRequestLocale } from "next-intl/server";

import { EmployeesList } from "@/components/employees/employees-list";

export default async function EmployeesPage({ params }: PageProps<"/[locale]/employees">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <EmployeesList />;
}
