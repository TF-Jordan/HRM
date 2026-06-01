import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as loansApi from "@/server/ksm/modules/loans";

export async function PUT(
  _request: NextRequest,
  { params }: { params: Promise<{ loanId: string }> },
) {
  const { loanId } = await params;
  return requirePermissionRoute("hrm:loan:approve", async (session) => {
    const data = await loansApi.approveLoan(loanId, session);
    return Response.json({ ok: true, data });
  });
}
