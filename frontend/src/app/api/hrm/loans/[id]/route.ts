import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as loansApi from "@/server/ksm/modules/loans";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:loan:read", async (session) => {
    const data = await loansApi.getLoanAdvance(id, session);
    return Response.json({ ok: true, data });
  });
}
