import { setRequestLocale } from "next-intl/server";

import { LoansQueue } from "@/components/loans/loans-queue";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/loans">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <LoansQueue />;
}
