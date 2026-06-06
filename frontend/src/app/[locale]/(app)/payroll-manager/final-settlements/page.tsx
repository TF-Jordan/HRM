import { setRequestLocale } from "next-intl/server";

import { FinalSettlements } from "@/components/payroll/final-settlements";

export default async function Page({ params }: { params: Promise<{ locale: string }> }) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <FinalSettlements />;
}
