import { setRequestLocale } from "next-intl/server";
import { NewContractClient } from "./new-contract-client";

export default async function NewContractPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewContractClient />;
}
