import "server-only";

import type { NextRequest } from "next/server";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const settlementId = request.nextUrl.searchParams.get("settlementId");
    if (!settlementId) {
      return fail(400, "INVALID_QUERY", "'settlementId' is required.");
    }
    const data = await payrollApi.generateFinalSettlementDocument(settlementId, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
