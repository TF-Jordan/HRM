import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as skillsApi from "@/server/ksm/modules/skills";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:skill:read", async (session) => {
    const data = await skillsApi.listEmployeeSkillsEnriched(employeeId, session);
    return Response.json({ ok: true, data });
  });
}
