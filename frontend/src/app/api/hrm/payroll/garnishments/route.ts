import "server-only";

import type { NextRequest } from "next/server";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const employeeId = request.nextUrl.searchParams.get("employeeId") ?? undefined;
    const data = await payrollApi.listGarnishments(session, { employeeId });
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const body = (await request.json()) as Partial<payrollApi.CreateGarnishmentRequest>;
    const organizationId = session.workspace?.organizationId;
    if (!organizationId) {
      return fail(400, "NO_ORGANIZATION", "No organization in the current workspace.");
    }
    if (!body.employeeId || !body.type || !body.beneficiary || body.totalAmount == null || body.monthlyAmount == null) {
      return fail(
        400,
        "INVALID_BODY",
        "'employeeId', 'type', 'beneficiary', 'totalAmount' and 'monthlyAmount' are required.",
      );
    }
    const payload: payrollApi.CreateGarnishmentRequest = {
      organizationId,
      employeeId: body.employeeId,
      type: body.type,
      beneficiary: body.beneficiary,
      reference: body.reference ?? null,
      totalAmount: body.totalAmount,
      monthlyAmount: body.monthlyAmount,
    };
    const data = await payrollApi.createGarnishment(payload, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
