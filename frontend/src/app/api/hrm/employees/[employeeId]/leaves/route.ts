import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as leavesApi from "@/server/ksm/modules/leaves";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:leave:read", async (session) => {
    const data = await leavesApi.listLeavesByEmployee(employeeId, session);
    return Response.json({ ok: true, data });
  });
}
