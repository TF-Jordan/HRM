import { setRequestLocale } from "next-intl/server";

import { TimesheetsAdmin } from "@/components/timesheets/timesheets-admin";

export default async function Page({ params }: PageProps<"/[locale]/manager/timesheets">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <TimesheetsAdmin />;
}
