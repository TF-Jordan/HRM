import { setRequestLocale } from "next-intl/server";

import { OfferDetail } from "@/components/recruitment/offer-detail";

export default async function Page({ params }: PageProps<"/[locale]/controller/recruitment/offers/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <OfferDetail offerId={id} />;
}
