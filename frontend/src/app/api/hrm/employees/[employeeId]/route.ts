import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:employee:read", async (session) => {
    const data = await employeesApi.getEmployee(employeeId, session);
    return Response.json({ ok: true, data });
  });
}

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:employee:update", async (session) => {
    const body = (await request.json()) as employeesApi.UpdateEmployeeRequest;
    const data = await employeesApi.updateEmployee(employeeId, body, session);
    return Response.json({ ok: true, data });
  });
}
