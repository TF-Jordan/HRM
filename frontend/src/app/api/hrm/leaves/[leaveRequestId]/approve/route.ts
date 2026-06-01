import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as leavesApi from "@/server/ksm/modules/leaves";

export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ leaveRequestId: string }> },
) {
  const { leaveRequestId } = await params;
  return requirePermissionRoute("hrm:leave:approve", async (session) => {
    const data = await leavesApi.approveLeave(leaveRequestId, session);
    return Response.json({ ok: true, data });
  });
}
