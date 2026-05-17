import { setRequestLocale } from "next-intl/server";
import { MyLoansClient } from "./my-loans-client";

export default async function MyLoansPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyLoansClient />;
}
