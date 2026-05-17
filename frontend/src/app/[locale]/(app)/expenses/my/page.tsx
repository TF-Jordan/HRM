import { setRequestLocale } from "next-intl/server";
import { MyExpensesClient } from "./my-expenses-client";

export default async function MyExpensesPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyExpensesClient />;
}
