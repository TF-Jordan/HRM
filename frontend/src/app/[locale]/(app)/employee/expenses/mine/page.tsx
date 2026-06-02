import { setRequestLocale } from "next-intl/server";

import { MyExpenses } from "@/components/expenses/my-expenses";

export default async function Page({ params }: PageProps<"/[locale]/employee/expenses/mine">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyExpenses />;
}
