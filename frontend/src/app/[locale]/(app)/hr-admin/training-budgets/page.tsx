import { setRequestLocale } from "next-intl/server";

import { BudgetOverview } from "@/components/budget/budget-overview";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/training-budgets">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <BudgetOverview />;
}
