import "server-only";

import type { NextRequest } from "next/server";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const entryId = request.nextUrl.searchParams.get("entryId");
    if (!entryId) {
      return fail(400, "INVALID_QUERY", "'entryId' is required.");
    }
    const data = await payrollApi.generatePayslipDocument(entryId, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
