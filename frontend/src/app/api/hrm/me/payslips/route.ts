import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { getProfileSummary } from "@/server/profile";
import { ksmListPayrollRuns, ksmListPayrollEntries } from "@/server/ksm/modules/payroll";
import type { PayrollEntry } from "@/lib/types/hrm/payroll";

export type MyPayslip = {
  entryId: string;
  runId: string;
  periode: string;
  status: string;
  brut: number;
  net: number;
  paymentStatus: string;
};

/** Payslip entries for the signed-in employee, aggregated across payroll runs. */
export async function GET() {
  return withKsmHandler(async () => {
    const profile = await getProfileSummary();
    const employeeId = profile?.employeeId;
    if (!employeeId) return [] as MyPayslip[];

    const ctx = await getKsmContext();
    const runs = await ksmListPayrollRuns(ctx);
    const perRun = await Promise.all(
      runs.map(async (r) => {
        const entries = await ksmListPayrollEntries(r.id, ctx).catch(() => [] as PayrollEntry[]);
        return entries
          .filter((e) => e.employeeId === employeeId)
          .map(
            (e): MyPayslip => ({
              entryId: e.id,
              runId: r.id,
              periode: r.periode,
              status: r.status,
              brut: Number(e.brut),
              net: Number(e.net),
              paymentStatus: e.paymentStatus,
            }),
          );
      }),
    );
    return perRun.flat().sort((a, b) => b.periode.localeCompare(a.periode));
  });
}
