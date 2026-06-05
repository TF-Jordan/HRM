import { setRequestLocale } from "next-intl/server";

import { TaxBrackets } from "@/components/payroll/tax-brackets";

export default async function Page({ params }: { params: Promise<{ locale: string }> }) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <TaxBrackets />;
}
