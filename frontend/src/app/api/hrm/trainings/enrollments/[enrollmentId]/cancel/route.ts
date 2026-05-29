import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as trainingsApi from "@/server/ksm/modules/trainings";

export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ enrollmentId: string }> },
) {
  const { enrollmentId } = await params;
  return requirePermissionRoute("hrm:training:manage", async (session) => {
    const data = await trainingsApi.cancelEnrollment(enrollmentId, session);
    return Response.json({ ok: true, data });
  });
}
