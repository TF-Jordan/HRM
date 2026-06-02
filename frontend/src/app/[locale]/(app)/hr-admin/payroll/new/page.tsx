import { setRequestLocale } from "next-intl/server";

import { NewPayrollForm } from "@/components/payroll/new-payroll-form";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/payroll/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewPayrollForm />;
}
