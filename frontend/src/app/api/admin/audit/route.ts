import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as adminApi from "@/server/ksm/modules/admin";

export async function GET(request: NextRequest) {
  const limit = Number(request.nextUrl.searchParams.get("limit") ?? "50");
  return requirePermissionRoute("administration:audit:read", async (session) => {
    const data = await adminApi.listAudit(session, Math.min(200, Math.max(1, limit)));
    return Response.json({ ok: true, data });
  });
}
