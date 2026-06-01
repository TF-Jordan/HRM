import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { PayrollList } from "@/components/payroll/payroll-list";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function PayrollIndexPage({
  params,
}: PageProps<"/[locale]/payroll">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:payroll:read")) redirect("/dashboard");
  return <PayrollList />;
}
