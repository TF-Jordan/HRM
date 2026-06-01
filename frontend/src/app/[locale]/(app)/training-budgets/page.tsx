import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { BudgetOverview } from "@/components/budget/budget-overview";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function BudgetIndexPage({
  params,
}: PageProps<"/[locale]/training-budgets">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:budget:read")) redirect("/dashboard");
  return <BudgetOverview />;
}
