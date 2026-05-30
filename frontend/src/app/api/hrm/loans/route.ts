import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as loansApi from "@/server/ksm/modules/loans";

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:loan:create", async (session) => {
    const body = (await request.json()) as loansApi.RequestLoanAdvanceRequest;
    const data = await loansApi.requestLoanAdvance(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:loan:read", async (session) => {
    const sp = request.nextUrl.searchParams;
    const employeeId = sp.get("employeeId");
    if (employeeId) {
      const data = await loansApi.listLoanAdvancesByEmployee(employeeId, session);
      return Response.json({ ok: true, data });
    }
    const status = sp.get("status") ?? undefined;
    const data = await loansApi.listLoanAdvancesByOrganization(session, {
      status: status as loansApi.LoanAdvanceStatus | undefined,
    });
    return Response.json({ ok: true, data });
  });
}
