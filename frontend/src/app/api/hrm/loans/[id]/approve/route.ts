import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as loansApi from "@/server/ksm/modules/loans";

export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:loan:approve", async (session) => {
    const data = await loansApi.approveLoanAdvance(id, session);
    return Response.json({ ok: true, data });
  });
}
