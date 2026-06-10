import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:payroll:validate", async (session) => {
    const body = (await request.json().catch(() => ({}))) as { reason?: string };
    const reason = String(body.reason ?? "").trim();
    if (!reason) {
      return Response.json(
        { ok: false, status: 400, errorCode: "VALIDATION_ERROR", message: "reason is required" },
        { status: 400 },
      );
    }
    const data = await payrollApi.rejectPayroll(id, reason, session);
    return Response.json({ ok: true, data });
  });
}
