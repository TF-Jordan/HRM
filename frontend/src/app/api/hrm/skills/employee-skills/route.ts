import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as skillsApi from "@/server/ksm/modules/skills";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:skill:read", async (session) => {
    const orgId = request.nextUrl.searchParams.get("organizationId") ?? undefined;
    const data = await skillsApi.listAllEmployeeSkills(session, orgId);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:skill:create", async (session) => {
    const body = (await request.json()) as skillsApi.CreateEmployeeSkillRequest;
    const data = await skillsApi.createEmployeeSkill(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
