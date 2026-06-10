import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as api from "@/server/ksm/modules/payroll-employees";

export async function GET() {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const data = await api.listPayrollEmployees(session);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const body = (await request.json()) as api.UpsertPayrollEmployeeRequest;
    const data = await api.createPayrollEmployee(
      { ...body, organizationId: session.workspace?.organizationId ?? body.organizationId },
      session,
    );
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
