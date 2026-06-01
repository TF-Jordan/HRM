import { setRequestLocale } from "next-intl/server";

import { MyPayslips } from "@/components/payroll/my-payslips";

export default async function PayslipsPage({ params }: PageProps<"/[locale]/payslips">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyPayslips />;
}
