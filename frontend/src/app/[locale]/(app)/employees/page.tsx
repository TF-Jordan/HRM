import { setRequestLocale } from "next-intl/server";
import { EmployeesPageClient } from "./employees-page-client";

export default async function EmployeesPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <EmployeesPageClient />;
}
