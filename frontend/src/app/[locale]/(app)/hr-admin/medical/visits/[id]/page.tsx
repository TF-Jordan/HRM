import { setRequestLocale } from "next-intl/server";

import { VisitDetail } from "@/components/medical/visit-detail";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/medical/visits/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <VisitDetail visitId={id} />;
}
