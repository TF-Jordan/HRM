import { getKsmContext } from "@/server/ksm/context";
import { withKsmHandler } from "@/server/ksm/handler";
import { ksmListEmployeeLoans } from "@/server/ksm/modules/loans";
import { ksmListEmployees } from "@/server/ksm/modules/employees";
import type { LoanAdvance } from "@/lib/types/hrm/loan-advance";

/**
 * Aggregates loans across all org employees and filters PENDING.
 * KSM has no /loan-advances/pending endpoint yet, so we fan out via the
 * employee list. Cost is O(N employees) — acceptable on small orgs.
 */
export async function GET() {
  return withKsmHandler(async () => {
    const ctx = await getKsmContext();
    const employees = await ksmListEmployees(ctx);
    const all = await Promise.all(
      employees.map((e) => ksmListEmployeeLoans(e.id, ctx).catch(() => [] as LoanAdvance[])),
    );
    return all.flat().filter((l) => l.status === "PENDING");
  });
}
