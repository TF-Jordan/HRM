import { setRequestLocale } from "next-intl/server";

import { MyLoans } from "@/components/loans/my-loans";

export default async function MyLoansPage({ params }: PageProps<"/[locale]/loans/mine">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyLoans />;
}
