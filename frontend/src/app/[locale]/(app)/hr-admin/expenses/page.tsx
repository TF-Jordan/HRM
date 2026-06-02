import { setRequestLocale } from "next-intl/server";

import { ExpensesQueue } from "@/components/expenses/expenses-queue";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/expenses">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ExpensesQueue />;
}
