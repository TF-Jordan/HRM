import { setRequestLocale } from "next-intl/server";
import { MissionDetailClient } from "./mission-detail-client";

export default async function MissionDetailPage({
  params,
}: {
  params: Promise<{ locale: string; id: string }>;
}) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <MissionDetailClient id={id} />;
}
