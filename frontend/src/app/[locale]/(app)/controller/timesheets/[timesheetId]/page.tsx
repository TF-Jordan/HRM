import { setRequestLocale } from "next-intl/server";

import { TimesheetDetail } from "@/components/timesheets/timesheet-detail";

export default async function Page({ params }: PageProps<"/[locale]/controller/timesheets/[timesheetId]">) {
  const { locale, timesheetId } = await params;
  setRequestLocale(locale);
  return <TimesheetDetail timesheetId={timesheetId} />;
}
