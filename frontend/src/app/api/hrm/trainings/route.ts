import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as trainingsApi from "@/server/ksm/modules/trainings";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:training:read", async (session) => {
    const orgId = request.nextUrl.searchParams.get("organizationId") ?? undefined;
    const data = await trainingsApi.listTrainings(session, orgId);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:training:create", async (session) => {
    const body = (await request.json()) as trainingsApi.PlanTrainingRequest;
    const data = await trainingsApi.planTraining(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
