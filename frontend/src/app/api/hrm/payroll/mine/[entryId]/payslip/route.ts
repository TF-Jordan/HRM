import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ entryId: string }> },
) {
  const { entryId } = await params;
  return authenticatedRoute(async (session) => {
    const data = await payrollApi.getMyPayslipLines(entryId, session);
    return Response.json({ ok: true, data });
  });
}
