import { setRequestLocale } from "next-intl/server";

import { ContractsList } from "@/components/contracts/contracts-list";

export default async function ContractsPage({ params }: PageProps<"/[locale]/contracts">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ContractsList />;
}
