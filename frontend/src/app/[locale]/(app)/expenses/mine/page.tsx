import { setRequestLocale } from "next-intl/server";

import { MyExpenses } from "@/components/expenses/my-expenses";

export default async function MyExpensesPage({ params }: PageProps<"/[locale]/expenses/mine">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyExpenses />;
}
