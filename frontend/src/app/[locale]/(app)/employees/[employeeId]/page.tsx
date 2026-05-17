import { setRequestLocale } from "next-intl/server";
import { EmployeeDetailClient } from "./employee-detail-client";

export default async function EmployeeDetailPage({
  params,
}: {
  params: Promise<{ locale: string; employeeId: string }>;
}) {
  const { locale, employeeId } = await params;
  setRequestLocale(locale);
  return <EmployeeDetailClient employeeId={employeeId} />;
}
