import { setRequestLocale } from "next-intl/server";
import { AnalyticsClient } from "./analytics-client";

export default async function AnalyticsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <AnalyticsClient />;
}
