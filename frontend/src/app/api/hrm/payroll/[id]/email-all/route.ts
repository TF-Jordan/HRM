import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import { emailAllPayslipsForRun } from "@/server/orchestration/email-payslip";

/**
 * Bulk email: dispatch every employee their signed payslip PDF for the given payroll cycle.
 * Permission gate: `hrm:payroll:run` (the payroll manager owns the dispatch action).
 * Business gate: enforced inside `emailAllPayslipsForRun` — the cycle must be VALIDATED
 * (or beyond). The response carries one outcome per entry so the UI can list failures.
 */
export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const { runStatus, results } = await emailAllPayslipsForRun(id, session);
    if (results.length === 0) {
      // Either the run isn't emailable yet, or it has no entries — surface a 409 so the
      // UI shows a meaningful toast instead of "0 emails sent" success.
      return Response.json(
        {
          ok: false,
          status: 409,
          errorCode: "RUN_NOT_EMAILABLE",
          message: `Run is ${runStatus}; validate it before emailing payslips.`,
        },
        { status: 409 },
      );
    }
    const sent = results.filter((r) => r.ok).length;
    const failed = results.length - sent;
    return Response.json({
      ok: true,
      data: { runStatus, sent, failed, total: results.length, results },
    });
  });
}
