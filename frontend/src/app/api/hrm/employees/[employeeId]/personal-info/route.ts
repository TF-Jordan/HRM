import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as profileApi from "@/server/ksm/modules/employee-profile";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:employee:read", async (session) => {
    const data = await profileApi.getPersonalInfo(employeeId, session);
    return Response.json({ ok: true, data });
  });
}

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  return requirePermissionRoute("hrm:employee:update", async (session) => {
    const body = (await request.json()) as profileApi.UpsertPersonalInfoRequest;
    const data = await profileApi.upsertPersonalInfo(employeeId, body, session);
    return Response.json({ ok: true, data });
  });
}
