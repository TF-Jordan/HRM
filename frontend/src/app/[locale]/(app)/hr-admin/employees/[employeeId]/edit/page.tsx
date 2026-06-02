import { setRequestLocale } from "next-intl/server";

import { EmployeeEditForm } from "@/components/employees/employee-edit-form";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/employees/[employeeId]/edit">) {
  const { locale, employeeId } = await params;
  setRequestLocale(locale);
  return <EmployeeEditForm employeeId={employeeId} />;
}
