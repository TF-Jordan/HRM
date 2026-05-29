import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewBudgetForm } from "@/components/budget/new-budget-form";
import { readSession } from "@/server/session";

export default async function NewBudgetPage({
  params,
}: PageProps<"/[locale]/training-budgets/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!session.user.permissions.includes("hrm:budget:create")) {
    redirect("/training-budgets");
  }
  return <NewBudgetForm />;
}
