import { setRequestLocale } from "next-intl/server";
import { MissionOrdersClient } from "./mission-orders-client";

export default async function MissionOrdersPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MissionOrdersClient />;
}
