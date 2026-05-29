import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as profileApi from "@/server/ksm/modules/employee-profile";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:employee:read", async (session) => {
    const data = await profileApi.getEmployeeTimeline(employeeId, session);
    return Response.json({ ok: true, data });
  });
}
