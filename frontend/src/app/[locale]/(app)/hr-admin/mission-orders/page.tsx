import { setRequestLocale } from "next-intl/server";

import { MissionsQueue } from "@/components/missions/missions-queue";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/mission-orders">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MissionsQueue />;
}
