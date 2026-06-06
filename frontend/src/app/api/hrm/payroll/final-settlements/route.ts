import "server-only";

import type { NextRequest } from "next/server";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const employeeId = request.nextUrl.searchParams.get("employeeId") ?? undefined;
    const data = await payrollApi.listFinalSettlements(session, { employeeId });
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const body = (await request.json()) as Partial<payrollApi.CalculateFinalSettlementRequest>;
    if (!body.employeeId || !body.departureDate || !body.reason) {
      return fail(
        400,
        "INVALID_BODY",
        "'employeeId', 'departureDate' and 'reason' are required.",
      );
    }
    const payload: payrollApi.CalculateFinalSettlementRequest = {
      employeeId: body.employeeId,
      departureDate: body.departureDate,
      reason: body.reason,
      unusedLeaveDays: body.unusedLeaveDays ?? 0,
      noticeMonths: body.noticeMonths ?? 0,
      accruedGratification: body.accruedGratification ?? 0,
    };
    const data = await payrollApi.calculateFinalSettlement(payload, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
