import { setRequestLocale } from "next-intl/server";

import { CandidateDetail } from "@/components/recruitment/candidate-detail";

export default async function CandidatePage({
  params,
}: PageProps<"/[locale]/recruitment/candidates/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <CandidateDetail applicationId={id} />;
}
