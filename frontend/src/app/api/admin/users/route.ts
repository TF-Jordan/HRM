import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as adminApi from "@/server/ksm/modules/admin";
import { createUserOrchestrated, type CreateUserInput } from "@/server/orchestration/create-user";

export async function GET() {
  return requirePermissionRoute(
    ["administration:roles:read", "administration:roles:write"],
    async (session) => {
      const data = await adminApi.listTenantUsers(session);
      return Response.json({ ok: true, data });
    },
  );
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("tenant:admin", async (session) => {
    const body = (await request.json()) as CreateUserInput;
    if (!body.firstName || !body.lastName || !body.email) {
      return Response.json(
        { ok: false, status: 400, errorCode: "VALIDATION_ERROR", message: "firstName, lastName and email are required" },
        { status: 400 },
      );
    }
    const result = await createUserOrchestrated(body, session);
    return Response.json({ ok: true, data: result }, { status: 201 });
  });
}
