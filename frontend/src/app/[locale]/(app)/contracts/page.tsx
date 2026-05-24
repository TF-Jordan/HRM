import { setRequestLocale } from "next-intl/server";
import { ContractsClient } from "./contracts-client";

export default async function ContractsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <ContractsClient />;
}
