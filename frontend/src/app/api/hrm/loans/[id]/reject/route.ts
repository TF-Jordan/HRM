import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as loansApi from "@/server/ksm/modules/loans";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:loan:approve", async (session) => {
    const body = (await request.json().catch(() => ({}))) as { motif?: string };
    const motif = (body.motif ?? "").trim() || "—";
    const data = await loansApi.rejectLoanAdvance(id, motif, session);
    return Response.json({ ok: true, data });
  });
}
