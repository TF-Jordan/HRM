import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:contract:read", async (session) => {
    const data = await employeesApi.listContracts(employeeId, session);
    return Response.json({ ok: true, data });
  });
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:contract:create", async (session) => {
    const body = (await request.json()) as employeesApi.AddContractRequest;
    const data = await employeesApi.addContract(employeeId, body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
