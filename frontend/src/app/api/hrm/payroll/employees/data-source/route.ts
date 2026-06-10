import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as api from "@/server/ksm/modules/payroll-employees";

export async function GET() {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const data = await api.getPayrollDataSource(session);
    return Response.json({ ok: true, data });
  });
}

export async function PUT(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const body = (await request.json()) as { source?: string };
    const source = body.source === "LOCAL" ? "LOCAL" : "HRM";
    const data = await api.setPayrollDataSource(source, session);
    return Response.json({ ok: true, data });
  });
}
