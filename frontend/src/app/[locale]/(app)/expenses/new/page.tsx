import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewExpenseForm } from "@/components/expenses/new-expense-form";
import { readSession } from "@/server/session";

export default async function NewExpensePage({ params }: PageProps<"/[locale]/expenses/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!session.user.permissions.includes("hrm:expense:create")) {
    redirect("/expenses");
  }
  return <NewExpenseForm />;
}
