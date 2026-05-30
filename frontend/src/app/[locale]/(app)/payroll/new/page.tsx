import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewPayrollForm } from "@/components/payroll/new-payroll-form";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function NewPayrollPage({
  params,
}: PageProps<"/[locale]/payroll/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:payroll:run")) redirect("/payroll");
  return <NewPayrollForm />;
}
