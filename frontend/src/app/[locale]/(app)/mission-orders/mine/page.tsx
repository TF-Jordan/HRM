import { setRequestLocale } from "next-intl/server";

import { MissionsInbox } from "@/components/missions/missions-inbox";

export default async function MyMissionOrdersPage({
  params,
}: PageProps<"/[locale]/mission-orders/mine">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MissionsInbox />;
}
