import { setRequestLocale } from "next-intl/server";

import { CandidateDetail } from "@/components/recruitment/candidate-detail";

export default async function Page({ params }: PageProps<"/[locale]/recruiter/recruitment/candidates/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <CandidateDetail applicationId={id} />;
}
