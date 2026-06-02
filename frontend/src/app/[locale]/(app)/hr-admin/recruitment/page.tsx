import { setRequestLocale } from "next-intl/server";

import { RecruitmentOverview } from "@/components/recruitment/recruitment-overview";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/recruitment">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <RecruitmentOverview />;
}
