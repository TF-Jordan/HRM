import { setRequestLocale } from "next-intl/server";

import { ContractsList } from "@/components/contracts/contracts-list";

export default async function Page({ params }: PageProps<"/[locale]/controller/contracts">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ContractsList />;
}
