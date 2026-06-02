import { setRequestLocale } from "next-intl/server";

import { NewMissionForm } from "@/components/missions/new-mission-form";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/mission-orders/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewMissionForm />;
}
