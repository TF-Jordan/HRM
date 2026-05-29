import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { BudgetOverview } from "@/components/budget/budget-overview";
import { readSession } from "@/server/session";

export default async function BudgetIndexPage({
  params,
}: PageProps<"/[locale]/training-budgets">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!session.user.permissions.includes("hrm:budget:read")) {
    redirect("/dashboard");
  }
  return <BudgetOverview />;
}
