import { setRequestLocale } from "next-intl/server";
import { MyPayslipsClient } from "./my-payslips-client";

export default async function MyPayslipsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyPayslipsClient />;
}
