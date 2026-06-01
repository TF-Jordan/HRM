import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const orgId = request.nextUrl.searchParams.get("organizationId") ?? undefined;
    const data = await payrollApi.listPayrollRuns(session, orgId);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const body = (await request.json()) as payrollApi.RunPayrollRequest;
    const data = await payrollApi.runPayroll(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
