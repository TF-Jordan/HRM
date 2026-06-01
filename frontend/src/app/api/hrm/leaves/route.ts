import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as leavesApi from "@/server/ksm/modules/leaves";

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:leave:create", async (session) => {
    const body = (await request.json()) as leavesApi.SubmitLeaveRequest;
    const data = await leavesApi.submitLeave(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
