import { setRequestLocale } from "next-intl/server";

import { PayElements } from "@/components/payroll/pay-elements";

export default async function Page({ params }: PageProps<"/[locale]/payroll-manager/pay-elements">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <PayElements />;
}
