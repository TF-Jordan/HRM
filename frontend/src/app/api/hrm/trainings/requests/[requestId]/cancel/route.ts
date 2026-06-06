import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as trainingsApi from "@/server/ksm/modules/trainings";

/** Employee withdraws their own still-pending training request. */
export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ requestId: string }> },
) {
  const { requestId } = await params;
  return requirePermissionRoute("hrm:training:request", async (session) => {
    const data = await trainingsApi.cancelTrainingRequest(requestId, session);
    return Response.json({ ok: true, data });
  });
}
