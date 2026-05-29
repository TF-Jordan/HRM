import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as missionsApi from "@/server/ksm/modules/missions";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ missionOrderId: string }> },
) {
  const { missionOrderId } = await params;
  return requirePermissionRoute("hrm:mission:accept", async (session) => {
    const body = (await request.json()) as { reason?: string };
    const reason = (body.reason ?? "").trim();
    if (!reason) {
      return Response.json(
        { ok: false, status: 400, errorCode: "BAD_REQUEST", message: "reason is required" },
        { status: 400 },
      );
    }
    const data = await missionsApi.declineMissionOrder(missionOrderId, reason, session);
    return Response.json({ ok: true, data });
  });
}
