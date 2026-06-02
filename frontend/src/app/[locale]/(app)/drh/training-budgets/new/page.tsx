import { setRequestLocale } from "next-intl/server";

import { NewBudgetForm } from "@/components/budget/new-budget-form";

export default async function Page({ params }: PageProps<"/[locale]/drh/training-budgets/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewBudgetForm />;
}
