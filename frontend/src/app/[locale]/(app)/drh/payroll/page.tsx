import { setRequestLocale } from "next-intl/server";

import { PayrollList } from "@/components/payroll/payroll-list";

export default async function Page({ params }: PageProps<"/[locale]/drh/payroll">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <PayrollList />;
}
