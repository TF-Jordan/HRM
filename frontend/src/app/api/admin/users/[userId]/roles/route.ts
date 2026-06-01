import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as adminApi from "@/server/ksm/modules/admin";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ userId: string }> },
) {
  const { userId } = await params;
  return requirePermissionRoute(
    ["administration:roles:read", "administration:roles:write"],
    async (session) => {
      const data = await adminApi.listUserRoles(userId, session);
      return Response.json({ ok: true, data });
    },
  );
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ userId: string }> },
) {
  const { userId } = await params;
  return requirePermissionRoute("administration:roles:write", async (session) => {
    const body = (await request.json()) as {
      roleId: string;
      scope: string;
      scopeType?: string;
      scopeId?: string | null;
    };
    const data = await adminApi.assignRole(userId, body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
