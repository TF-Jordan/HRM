import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as leavesApi from "@/server/ksm/modules/leaves";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ leaveRequestId: string }> },
) {
  const { leaveRequestId } = await params;
  return requirePermissionRoute("hrm:leave:approve", async (session) => {
    const body = (await request.json()) as { commentaire?: string };
    const data = await leavesApi.rejectLeave(leaveRequestId, body.commentaire ?? "", session);
    return Response.json({ ok: true, data });
  });
}
