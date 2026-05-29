import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as leavesApi from "@/server/ksm/modules/leaves";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:leave:approve", async (session) => {
    const sp = request.nextUrl.searchParams;
    const data = await leavesApi.listPendingLeaves(
      session,
      sp.get("organizationId") ?? undefined,
      sp.get("agencyId") ?? undefined,
    );
    return Response.json({ ok: true, data });
  });
}
