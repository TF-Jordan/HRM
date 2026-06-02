import { setRequestLocale } from "next-intl/server";

import { NewCertificateForm } from "@/components/medical/new-certificate-form";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/medical/certificates/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewCertificateForm />;
}
