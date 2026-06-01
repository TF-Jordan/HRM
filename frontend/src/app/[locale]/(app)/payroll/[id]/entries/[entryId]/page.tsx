import { setRequestLocale } from "next-intl/server";
import { redirect } from "next/navigation";

import { PayslipPreview } from "@/components/payroll/payslip-preview";
import { hasPermission } from "@/server/permissions";
import { readSession } from "@/server/session";

export default async function PayslipPage({
  params,
}: PageProps<"/[locale]/payroll/[id]/entries/[entryId]">) {
  const { locale, id, entryId } = await params;
  setRequestLocale(locale);

  const session = await readSession();
  if (!session) redirect("/login");
  if (!hasPermission(session, "hrm:payroll:read")) redirect("/dashboard");
  return <PayslipPreview runId={id} entryId={entryId} />;
}
