import { setRequestLocale } from "next-intl/server";

import { MyTimesheets } from "@/components/timesheets/my-timesheets";

export default async function Page({ params }: PageProps<"/[locale]/employee/timesheets">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyTimesheets />;
}
