import { setRequestLocale } from "next-intl/server";

import { MyTimesheets } from "@/components/timesheets/my-timesheets";

export default async function MyTimesheetsPage({ params }: PageProps<"/[locale]/timesheets/my">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyTimesheets />;
}
