import { setRequestLocale } from "next-intl/server";

import { NewExpenseForm } from "@/components/expenses/new-expense-form";

export default async function Page({ params }: PageProps<"/[locale]/employee/expenses/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewExpenseForm />;
}
