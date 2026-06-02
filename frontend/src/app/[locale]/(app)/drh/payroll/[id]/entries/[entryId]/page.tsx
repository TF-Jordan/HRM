import { setRequestLocale } from "next-intl/server";

import { PayslipPreview } from "@/components/payroll/payslip-preview";

export default async function Page({
  params,
}: PageProps<"/[locale]/drh/payroll/[id]/entries/[entryId]">) {
  const { locale, id, entryId } = await params;
  setRequestLocale(locale);
  return <PayslipPreview runId={id} entryId={entryId} />;
}
