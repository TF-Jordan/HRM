import { setRequestLocale } from "next-intl/server";
import { PayrollClient } from "./payroll-client";

export default async function PayrollPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <PayrollClient />;
}
