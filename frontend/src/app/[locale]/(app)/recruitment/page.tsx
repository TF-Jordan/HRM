import { setRequestLocale } from "next-intl/server";
import { RecruitmentClient } from "./recruitment-client";

export default async function RecruitmentPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <RecruitmentClient />;
}
