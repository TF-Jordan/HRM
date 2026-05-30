import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as recruitmentApi from "@/server/ksm/modules/recruitment";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:onboarding:read", async (session) => {
    const data = await recruitmentApi.listOnboardingTasks(employeeId, session);
    return Response.json({ ok: true, data });
  });
}
