import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as adminApi from "@/server/ksm/modules/admin";

export async function DELETE(
  _request: NextRequest,
  { params }: { params: Promise<{ userId: string; assignmentId: string }> },
) {
  const { userId, assignmentId } = await params;
  return requirePermissionRoute("administration:roles:write", async (session) => {
    await adminApi.revokeRole(userId, assignmentId, session);
    return Response.json({ ok: true, data: { ok: true } });
  });
}
