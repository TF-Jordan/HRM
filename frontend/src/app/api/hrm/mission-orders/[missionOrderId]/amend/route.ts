import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as missionsApi from "@/server/ksm/modules/missions";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ missionOrderId: string }> },
) {
  const { missionOrderId } = await params;
  return requirePermissionRoute("hrm:mission:manage", async (session) => {
    const body = (await request.json()) as missionsApi.AmendMissionOrderRequest;
    const data = await missionsApi.amendMissionOrder(missionOrderId, body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
