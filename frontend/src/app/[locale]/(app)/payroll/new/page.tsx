import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { NewPayrollForm } from "@/components/payroll/new-payroll-form";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";
import { listPayrollRuns } from "@/server/ksm/modules/payroll";
import { getPayrollWindowState } from "@/lib/payroll-status";

export default async function NewPayrollPage({
  params,
}: PageProps<"/[locale]/payroll/new">) {
  const { locale } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:payroll:run")) redirect("/payroll");

  // Guard: only allow access during the 5-day activation window
  try {
    const runs = await listPayrollRuns(session);
    const window = getPayrollWindowState(runs);
    if (!window.canRun) redirect("/payroll");
  } catch {
    // If we can't check (network error), let the form handle it
  }

  return <NewPayrollForm />;
}
