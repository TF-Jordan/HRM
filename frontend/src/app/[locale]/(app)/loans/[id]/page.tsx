import { setRequestLocale } from "next-intl/server";

import { LoanDetail } from "@/components/loans/loan-detail";

export default async function LoanDetailPage({
  params,
}: PageProps<"/[locale]/loans/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <LoanDetail loanId={id} />;
}
