import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { ExpensesQueue } from "@/components/expenses/expenses-queue";
import { readSession } from "@/server/session";

export default async function ExpensesIndexPage({ params }: PageProps<"/[locale]/expenses">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");

  // Accountant / DAF / admin → validation queue. Employee → self-service.
  const canManage = session.user.permissions.includes("hrm:expense:manage");
  if (!canManage) redirect("/expenses/mine");
  return <ExpensesQueue />;
}
