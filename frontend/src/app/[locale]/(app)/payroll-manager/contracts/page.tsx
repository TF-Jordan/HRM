import { setRequestLocale } from "next-intl/server";

import { ContractsList } from "@/components/contracts/contracts-list";

export default async function Page({ params }: PageProps<"/[locale]/payroll-manager/contracts">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ContractsList />;
}
