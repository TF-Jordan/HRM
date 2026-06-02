import { setRequestLocale } from "next-intl/server";

import { CertificateDetail } from "@/components/medical/certificate-detail";

export default async function Page({ params }: PageProps<"/[locale]/drh/medical/certificates/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <CertificateDetail certificateId={id} />;
}
