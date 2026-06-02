import { setRequestLocale } from "next-intl/server";

import { NewTimesheetForm } from "@/components/timesheets/new-timesheet-form";

export default async function Page({
  params,
  searchParams,
}: PageProps<"/[locale]/employee/timesheets/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  const sp = await searchParams;
  const periodeRaw = sp.periode;
  const periode = Array.isArray(periodeRaw) ? periodeRaw[0] : periodeRaw;
  return <NewTimesheetForm initialPeriode={periode} />;
}
