import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as trainingsApi from "@/server/ksm/modules/trainings";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ enrollmentId: string }> },
) {
  const { enrollmentId } = await params;
  return requirePermissionRoute("hrm:training:manage", async (session) => {
    const body = (await request.json()) as { note: number | string; attestationId?: string | null };
    const data = await trainingsApi.completeEnrollment(enrollmentId, body, session);
    return Response.json({ ok: true, data });
  });
}
