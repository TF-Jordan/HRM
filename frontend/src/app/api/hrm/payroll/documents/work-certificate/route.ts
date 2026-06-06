import "server-only";

import type { NextRequest } from "next/server";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const employeeId = request.nextUrl.searchParams.get("employeeId");
    if (!employeeId) {
      return fail(400, "INVALID_QUERY", "'employeeId' is required.");
    }
    const position = request.nextUrl.searchParams.get("position");
    const data = await payrollApi.generateWorkCertificate(employeeId, position, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
