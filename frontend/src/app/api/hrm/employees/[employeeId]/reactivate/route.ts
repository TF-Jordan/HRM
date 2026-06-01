import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";

export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:employee:reactivate", async (session) => {
    const data = await employeesApi.reactivateEmployee(employeeId, session);
    return Response.json({ ok: true, data });
  });
}
