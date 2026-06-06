import "server-only";

import type { NextRequest } from "next/server";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

const TYPES = ["CNPS", "DIPE", "IRPP_CAC"] as const;

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const type = request.nextUrl.searchParams.get("type");
    const runId = request.nextUrl.searchParams.get("runId");
    if (!type || !TYPES.includes(type as (typeof TYPES)[number])) {
      return fail(400, "INVALID_QUERY", "'type' must be one of CNPS, DIPE, IRPP_CAC.");
    }
    if (!runId) {
      return fail(400, "INVALID_QUERY", "'runId' is required.");
    }
    const data = await payrollApi.generateDeclaration(
      session,
      type as payrollApi.DeclarationType,
      runId,
    );
    return Response.json({ ok: true, data });
  });
}
