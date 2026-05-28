import { setRequestLocale } from "next-intl/server";

import { EmployeeCreateForm } from "@/components/employees/employee-create-form";

export default async function NewEmployeePage({ params }: PageProps<"/[locale]/employees/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <EmployeeCreateForm />;
}
