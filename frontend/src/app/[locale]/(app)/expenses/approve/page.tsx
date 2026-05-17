import { setRequestLocale } from "next-intl/server";
import { ApproveExpensesClient } from "./approve-expenses-client";

export default async function ApproveExpensesPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ApproveExpensesClient />;
}
