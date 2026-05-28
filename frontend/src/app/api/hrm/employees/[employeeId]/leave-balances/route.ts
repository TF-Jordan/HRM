import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ employeeId: string }> },
) {
  const { employeeId } = await params;
  const annee = Number(request.nextUrl.searchParams.get("annee") ?? new Date().getFullYear());
  return requirePermissionRoute("hrm:leave:read", async (session) => {
    const data = await employeesApi.listLeaveBalances(employeeId, annee, session);
    return Response.json({ ok: true, data });
  });
}
