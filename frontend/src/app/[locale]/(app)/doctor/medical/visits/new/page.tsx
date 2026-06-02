import { setRequestLocale } from "next-intl/server";

import { NewVisitForm } from "@/components/medical/new-visit-form";

export default async function Page({ params }: PageProps<"/[locale]/doctor/medical/visits/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewVisitForm />;
}
