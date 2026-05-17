import { setRequestLocale } from "next-intl/server";
import { PayslipClient } from "./payslip-client";

export default async function PayslipPage({
  params,
}: {
  params: Promise<{ locale: string; entryId: string }>;
}) {
  const { locale, entryId } = await params;
  setRequestLocale(locale);
  return <PayslipClient entryId={entryId} />;
}
