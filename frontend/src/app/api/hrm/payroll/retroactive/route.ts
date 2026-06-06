import "server-only";

import type { NextRequest } from "next/server";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const employeeId = request.nextUrl.searchParams.get("employeeId") ?? undefined;
    const data = await payrollApi.listRetroactiveAdjustments(session, { employeeId });
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const body = (await request.json()) as Partial<payrollApi.CalculateRetroactiveRequest>;
    if (!body.employeeId || !body.originPeriod || !body.targetPeriod || body.newBaseSalary == null) {
      return fail(
        400,
        "INVALID_BODY",
        "'employeeId', 'originPeriod', 'targetPeriod' and 'newBaseSalary' are required.",
      );
    }
    const payload: payrollApi.CalculateRetroactiveRequest = {
      employeeId: body.employeeId,
      originPeriod: body.originPeriod,
      newBaseSalary: body.newBaseSalary,
      targetPeriod: body.targetPeriod,
      reason: body.reason ?? null,
    };
    const data = await payrollApi.calculateRetroactive(payload, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
