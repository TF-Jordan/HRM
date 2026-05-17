import { setRequestLocale } from "next-intl/server";
import { PayrollRunDetail } from "./payroll-run-detail";

export default async function PayrollRunPage({
  params,
}: {
  params: Promise<{ locale: string; runId: string }>;
}) {
  const { locale, runId } = await params;
  setRequestLocale(locale);
  return <PayrollRunDetail runId={runId} />;
}
