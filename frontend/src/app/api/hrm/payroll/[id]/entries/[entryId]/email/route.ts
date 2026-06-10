import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import { emailSinglePayslip } from "@/server/orchestration/email-payslip";

/**
 * Send a single employee their signed payslip PDF. Permission gate: `hrm:payroll:run`.
 * Business gate: enforced inside `emailSinglePayslip` — the parent cycle must be
 * VALIDATED (or beyond). Errors are returned with stable codes so the UI can map them
 * to user-facing toasts.
 */
export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string; entryId: string }> },
) {
  const { id, entryId } = await params;
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const { runStatus, outcome } = await emailSinglePayslip(id, entryId, session);
    if (outcome.reason === "RUN_NOT_VALIDATED") {
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
    if (outcome.reason === "ENTRY_NOT_FOUND") {
      return Response.json(
        { ok: false, status: 404, errorCode: "ENTRY_NOT_FOUND", message: "Payslip entry not found." },
        { status: 404 },
      );
    }
    if (!outcome.ok) {
      return Response.json(
        {
          ok: false,
          status: 422,
          errorCode: outcome.reason ?? "EMAIL_FAILED",
          message: emailErrorMessage(outcome.reason),
        },
        { status: 422 },
      );
    }
    return Response.json({ ok: true, data: { runStatus, ...outcome } });
  });
}

function emailErrorMessage(reason: string | undefined): string {
  switch (reason) {
    case "NO_EMAIL":
      return "Cet employé n'a pas d'adresse email enregistrée.";
    case "PDF_UNAVAILABLE":
      return "Impossible de générer ou récupérer le bulletin PDF.";
    default:
      return "L'envoi de l'email a échoué.";
  }
}
