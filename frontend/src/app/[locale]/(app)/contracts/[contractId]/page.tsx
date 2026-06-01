import { setRequestLocale } from "next-intl/server";

import { ContractDetail } from "@/components/contracts/contract-detail";

export default async function ContractDetailPage({
  params,
  searchParams,
}: PageProps<"/[locale]/contracts/[contractId]">) {
  const { locale, contractId } = await params;
  const sp = await (searchParams as Promise<{ employeeId?: string; from?: string }>);
  setRequestLocale(locale);
  return (
    <ContractDetail
      contractId={contractId}
      employeeId={sp.employeeId ?? ""}
      fromEmployee={sp.from === "employee"}
    />
  );
}
