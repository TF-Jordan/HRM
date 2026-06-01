import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as medicalApi from "@/server/ksm/modules/medical";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:medical:read", async (session) => {
    const sp = request.nextUrl.searchParams;
    const employeeId = sp.get("employeeId");
    if (employeeId) {
      const data = await medicalApi.listVisitsByEmployee(employeeId, session);
      return Response.json({ ok: true, data });
    }
    const data = await medicalApi.listVisits(session, sp.get("organizationId") ?? undefined);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:medical:create", async (session) => {
    const body = (await request.json()) as medicalApi.CreateMedicalVisitRequest;
    const data = await medicalApi.createVisit(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
