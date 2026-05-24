import { setRequestLocale } from "next-intl/server";
import { ContractDetailClient } from "./contract-detail-client";

export default async function ContractDetailPage({
  params,
}: {
  params: Promise<{ locale: string; id: string }>;
}) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <ContractDetailClient id={id} />;
}
