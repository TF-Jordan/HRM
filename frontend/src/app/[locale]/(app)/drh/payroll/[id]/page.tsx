import { setRequestLocale } from "next-intl/server";

import { PayrollRunDetail } from "@/components/payroll/payroll-run-detail";

export default async function Page({ params }: PageProps<"/[locale]/drh/payroll/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <PayrollRunDetail runId={id} />;
}
