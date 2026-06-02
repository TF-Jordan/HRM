import { setRequestLocale } from "next-intl/server";

import { MedicalOverview } from "@/components/medical/medical-overview";

export default async function Page({ params }: PageProps<"/[locale]/doctor/medical">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MedicalOverview />;
}
