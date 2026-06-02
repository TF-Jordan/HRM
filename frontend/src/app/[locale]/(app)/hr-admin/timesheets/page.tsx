import { setRequestLocale } from "next-intl/server";

import { TimesheetsAdmin } from "@/components/timesheets/timesheets-admin";

export default async function Page({ params }: PageProps<"/[locale]/hr-admin/timesheets">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <TimesheetsAdmin />;
}
