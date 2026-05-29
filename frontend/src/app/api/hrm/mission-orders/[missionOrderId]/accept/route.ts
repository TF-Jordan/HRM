import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as missionsApi from "@/server/ksm/modules/missions";

export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ missionOrderId: string }> },
) {
  const { missionOrderId } = await params;
  return requirePermissionRoute("hrm:mission:accept", async (session) => {
    const data = await missionsApi.acceptMissionOrder(missionOrderId, session);
    return Response.json({ ok: true, data });
  });
}
