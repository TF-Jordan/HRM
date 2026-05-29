import { setRequestLocale } from "next-intl/server";

import { ExpenseDetail } from "@/components/expenses/expense-detail";

export default async function ExpenseDetailPage({
  params,
}: PageProps<"/[locale]/expenses/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <ExpenseDetail expenseReportId={id} />;
}
