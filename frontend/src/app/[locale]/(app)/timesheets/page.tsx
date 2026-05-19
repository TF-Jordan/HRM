import { setRequestLocale } from "next-intl/server";
import { TimesheetsClient } from "./timesheets-client";

export default async function TimesheetsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <TimesheetsClient />;
}
