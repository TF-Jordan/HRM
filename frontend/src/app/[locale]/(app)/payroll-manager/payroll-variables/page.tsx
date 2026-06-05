import { setRequestLocale } from "next-intl/server";

import { PayVariables } from "@/components/payroll/pay-variables";

export default async function Page({ params }: PageProps<"/[locale]/payroll-manager/payroll-variables">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <PayVariables />;
}
