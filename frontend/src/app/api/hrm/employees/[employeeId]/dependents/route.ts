import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:dependent:read", async (session) => {
    const data = await employeesApi.listDependents(employeeId, session);
    return Response.json({ ok: true, data });
  });
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:dependent:create", async (session) => {
    const body = (await request.json()) as employeesApi.AddDependentRequest;
    const data = await employeesApi.addDependent(employeeId, body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
