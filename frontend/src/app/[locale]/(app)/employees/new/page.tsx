import { setRequestLocale } from "next-intl/server";
import { CreateEmployeeForm } from "./create-employee-form";

export default async function NewEmployeePage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <CreateEmployeeForm />;
}
