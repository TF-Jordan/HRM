import { setRequestLocale } from "next-intl/server";

import { EmployeeCreateForm } from "@/components/employees/employee-create-form";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/employees/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <EmployeeCreateForm />;
}
