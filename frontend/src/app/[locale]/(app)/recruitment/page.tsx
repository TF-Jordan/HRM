import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { RecruitmentOverview } from "@/components/recruitment/recruitment-overview";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function RecruitmentIndexPage({
  params,
}: PageProps<"/[locale]/recruitment">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:recruitment:read")) redirect("/dashboard");
  return <RecruitmentOverview />;
}
