import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as missionsApi from "@/server/ksm/modules/missions";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:mission:read", async (session) => {
    const orgId = request.nextUrl.searchParams.get("organizationId") ?? undefined;
    const data = await missionsApi.listDeclined(session, orgId);
    return Response.json({ ok: true, data });
  });
}
