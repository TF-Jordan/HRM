import "server-only";

import type { NextRequest } from "next/server";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const period = request.nextUrl.searchParams.get("period");
    if (!period) {
      return fail(400, "MISSING_PERIOD", "Query parameter 'period' (YYYY-MM) is required.");
    }
    const data = await payrollApi.listPayVariables(session, period);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const body = (await request.json()) as Partial<payrollApi.CapturePayVariableRequest>;
    const organizationId = session.workspace?.organizationId;
    if (!organizationId) {
      return fail(400, "MISSING_ORGANIZATION", "No active organization in the current workspace.");
    }
    if (!body.employeeId || !body.period) {
      return fail(400, "INVALID_BODY", "'employeeId' and 'period' are required.");
    }
    const payload: payrollApi.CapturePayVariableRequest = {
      organizationId,
      employeeId: body.employeeId,
      period: body.period,
      overtimeHoursDay: body.overtimeHoursDay ?? 0,
      overtimeHoursNight: body.overtimeHoursNight ?? 0,
      overtimeHoursSundayHoliday: body.overtimeHoursSundayHoliday ?? 0,
      bonuses: body.bonuses ?? 0,
      unpaidAbsenceDays: body.unpaidAbsenceDays ?? 0,
      advances: body.advances ?? 0,
      workedDaysOverride: body.workedDaysOverride ?? null,
    };
    const data = await payrollApi.capturePayVariable(payload, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
