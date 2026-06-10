import { setRequestLocale } from "next-intl/server";

import { PayrollEmployees } from "@/components/payroll/payroll-employees";

export default async function Page({ params }: { params: Promise<{ locale: string }> }) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <PayrollEmployees />;
}
