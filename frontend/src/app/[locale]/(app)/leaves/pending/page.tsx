import { setRequestLocale } from "next-intl/server";
import { PendingLeavesClient } from "./pending-leaves-client";

export default async function PendingLeavesPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <PendingLeavesClient />;
}
