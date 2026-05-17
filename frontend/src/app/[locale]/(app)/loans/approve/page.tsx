import { setRequestLocale } from "next-intl/server";
import { ApproveLoansClient } from "./approve-loans-client";

export default async function ApproveLoansPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ApproveLoansClient />;
}
