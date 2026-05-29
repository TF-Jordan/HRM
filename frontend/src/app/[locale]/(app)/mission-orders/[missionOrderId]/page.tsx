import { setRequestLocale } from "next-intl/server";

import { MissionDetail } from "@/components/missions/mission-detail";

export default async function MissionOrderDetailPage({
  params,
}: PageProps<"/[locale]/mission-orders/[missionOrderId]">) {
  const { locale, missionOrderId } = await params;
  setRequestLocale(locale);
  return <MissionDetail missionOrderId={missionOrderId} />;
}
