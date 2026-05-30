import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const data = await payrollApi.listPayrollEntries(id, session);
    return Response.json({ ok: true, data });
  });
}
