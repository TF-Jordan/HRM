import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ employeeId: string; contractId: string }> },
) {
  const { employeeId, contractId } = await params;
  return requirePermissionRoute("hrm:contract:update", async (session) => {
    const body = (await request.json()) as employeesApi.TerminateContractRequest;
    const data = await employeesApi.terminateContract(employeeId, contractId, body, session);
    return Response.json({ ok: true, data });
  });
}
