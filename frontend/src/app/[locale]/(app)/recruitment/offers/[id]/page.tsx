import { setRequestLocale } from "next-intl/server";

import { OfferDetail } from "@/components/recruitment/offer-detail";

export default async function OfferDetailPage({
  params,
}: PageProps<"/[locale]/recruitment/offers/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <OfferDetail offerId={id} />;
}
