import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";
import {
  createEmployeeOrchestrated,
  type CreateEmployeeOrchestratedInput,
} from "@/server/orchestration/create-employee";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:employee:read", async (session) => {
    const sp = request.nextUrl.searchParams;
    const data = await employeesApi.listEmployees(session, {
      organizationId: sp.get("organizationId") ?? undefined,
      agencyId: sp.get("agencyId") ?? undefined,
    });
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:employee:create", async (session) => {
    const body = (await request.json()) as CreateEmployeeOrchestratedInput;
    const result = await createEmployeeOrchestrated(body, session);
    return Response.json({ ok: true, data: result }, { status: 201 });
  });
}
