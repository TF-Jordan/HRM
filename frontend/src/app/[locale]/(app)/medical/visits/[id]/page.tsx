import { setRequestLocale } from "next-intl/server";

import { VisitDetail } from "@/components/medical/visit-detail";

export default async function VisitDetailPage({
  params,
}: PageProps<"/[locale]/medical/visits/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <VisitDetail visitId={id} />;
}
