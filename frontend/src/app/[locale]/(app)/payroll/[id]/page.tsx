import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { PayrollRunDetail } from "@/components/payroll/payroll-run-detail";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function PayrollRunPage({
  params,
}: PageProps<"/[locale]/payroll/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:payroll:read")) redirect("/dashboard");
  return <PayrollRunDetail runId={id} />;
}
