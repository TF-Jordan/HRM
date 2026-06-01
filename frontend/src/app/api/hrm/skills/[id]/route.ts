import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as skillsApi from "@/server/ksm/modules/skills";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:skill:read", async (session) => {
    const data = await skillsApi.getSkill(id, session);
    return Response.json({ ok: true, data });
  });
}
