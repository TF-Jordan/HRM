import { setRequestLocale } from "next-intl/server";

import { ExpenseDetail } from "@/components/expenses/expense-detail";

export default async function Page({ params }: PageProps<"/[locale]/employee/expenses/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <ExpenseDetail expenseReportId={id} />;
}
