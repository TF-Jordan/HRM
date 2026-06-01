import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:employee:terminate", async (session) => {
    const body = (await request.json()) as employeesApi.TerminateEmployeeRequest;
    const data = await employeesApi.terminateEmployee(employeeId, body, session);
    return Response.json({ ok: true, data });
  });
}
