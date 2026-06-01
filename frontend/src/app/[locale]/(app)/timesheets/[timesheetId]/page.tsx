import { setRequestLocale } from "next-intl/server";

import { TimesheetDetail } from "@/components/timesheets/timesheet-detail";

export default async function TimesheetDetailPage({
  params,
}: PageProps<"/[locale]/timesheets/[timesheetId]">) {
  const { locale, timesheetId } = await params;
  setRequestLocale(locale);
  return <TimesheetDetail timesheetId={timesheetId} />;
}
