import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string; contractId: string }> },
) {
  const { employeeId, contractId } = await params;
  return requirePermissionRoute("hrm:contract:read", async (session) => {
    const data = await employeesApi.getContract(employeeId, contractId, session);
    return Response.json({ ok: true, data });
  });
}
