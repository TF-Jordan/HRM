import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewOfferForm } from "@/components/recruitment/new-offer-form";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function NewOfferPage({ params }: PageProps<"/[locale]/recruitment/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:recruitment:create")) redirect("/recruitment");
  return <NewOfferForm />;
}
