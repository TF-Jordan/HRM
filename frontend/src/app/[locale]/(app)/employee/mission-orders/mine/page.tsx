import { setRequestLocale } from "next-intl/server";

import { MissionsInbox } from "@/components/missions/missions-inbox";

export default async function Page({ params }: PageProps<"/[locale]/employee/mission-orders/mine">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MissionsInbox />;
}
