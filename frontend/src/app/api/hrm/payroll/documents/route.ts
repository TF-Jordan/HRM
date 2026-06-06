import "server-only";

import type { NextRequest } from "next/server";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const employeeId = request.nextUrl.searchParams.get("employeeId");
    if (!employeeId) {
      return fail(400, "INVALID_QUERY", "'employeeId' is required.");
    }
    const data = await payrollApi.listEmployeeDocuments(session, employeeId);
    return Response.json({ ok: true, data });
  });
}
