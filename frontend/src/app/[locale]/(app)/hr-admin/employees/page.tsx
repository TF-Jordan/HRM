import { setRequestLocale } from "next-intl/server";

import { EmployeesList } from "@/components/employees/employees-list";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/employees">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <EmployeesList />;
}
