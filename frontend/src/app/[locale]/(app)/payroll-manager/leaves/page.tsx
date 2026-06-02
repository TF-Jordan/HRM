import { setRequestLocale } from "next-intl/server";

import { LeavesQueue } from "@/components/leaves/leaves-queue";

export default async function Page({ params }: PageProps<"/[locale]/payroll-manager/leaves">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <LeavesQueue />;
}
