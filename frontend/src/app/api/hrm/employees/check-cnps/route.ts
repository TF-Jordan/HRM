import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import { checkCnpsAvailability } from "@/server/ksm/modules/employees";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:employee:create", async (session) => {
    const value = request.nextUrl.searchParams.get("value") ?? "";
    if (!value.trim()) {
      return Response.json({ ok: true, data: true });
    }
    const available = await checkCnpsAvailability(value, session);
    return Response.json({ ok: true, data: available });
  });
}
