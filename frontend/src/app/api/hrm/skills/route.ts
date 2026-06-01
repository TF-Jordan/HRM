import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as skillsApi from "@/server/ksm/modules/skills";

export async function GET() {
  return requirePermissionRoute("hrm:skill:read", async (session) => {
    const data = await skillsApi.listSkills(session);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:skill:create", async (session) => {
    const body = (await request.json()) as skillsApi.CreateSkillRequest;
    const data = await skillsApi.createSkill(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
