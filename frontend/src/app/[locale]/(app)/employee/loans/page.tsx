import { setRequestLocale } from "next-intl/server";

import { MyLoans } from "@/components/loans/my-loans";

export default async function Page({ params }: PageProps<"/[locale]/employee/loans">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyLoans />;
}
