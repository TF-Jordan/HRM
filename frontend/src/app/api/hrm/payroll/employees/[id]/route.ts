import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as api from "@/server/ksm/modules/payroll-employees";

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const body = (await request.json()) as api.UpsertPayrollEmployeeRequest;
    const data = await api.updatePayrollEmployee(
      id,
      { ...body, organizationId: session.workspace?.organizationId ?? body.organizationId },
      session,
    );
    return Response.json({ ok: true, data });
  });
}

export async function DELETE(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const data = await api.deactivatePayrollEmployee(id, null, session);
    return Response.json({ ok: true, data });
  });
}
