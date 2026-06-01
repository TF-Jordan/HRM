import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewCertificateForm } from "@/components/medical/new-certificate-form";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function NewCertificatePage({
  params,
}: PageProps<"/[locale]/medical/certificates/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:medical:create")) redirect("/medical");
  return <NewCertificateForm />;
}
