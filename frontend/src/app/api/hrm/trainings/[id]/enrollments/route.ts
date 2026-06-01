import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as trainingsApi from "@/server/ksm/modules/trainings";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:training:read", async (session) => {
    const data = await trainingsApi.listEnrollmentsByTraining(id, session);
    return Response.json({ ok: true, data });
  });
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:training:manage", async (session) => {
    const body = (await request.json()) as { employeeId: string };
    const data = await trainingsApi.enrollEmployee(id, body.employeeId, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
