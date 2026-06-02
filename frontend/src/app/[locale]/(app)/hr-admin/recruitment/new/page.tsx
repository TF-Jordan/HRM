import { setRequestLocale } from "next-intl/server";

import { NewOfferForm } from "@/components/recruitment/new-offer-form";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/recruitment/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewOfferForm />;
}
