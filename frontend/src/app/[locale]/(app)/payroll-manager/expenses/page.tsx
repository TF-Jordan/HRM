import { setRequestLocale } from "next-intl/server";

import { ExpensesQueue } from "@/components/expenses/expenses-queue";

export default async function Page({ params }: PageProps<"/[locale]/payroll-manager/expenses">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ExpensesQueue />;
}
