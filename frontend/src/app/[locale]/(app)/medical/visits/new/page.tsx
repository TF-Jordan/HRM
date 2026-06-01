import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewVisitForm } from "@/components/medical/new-visit-form";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function NewVisitPage({
  params,
}: PageProps<"/[locale]/medical/visits/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:medical:create")) redirect("/medical");
  return <NewVisitForm />;
}
