import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as loansApi from "@/server/ksm/modules/loans";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ loanId: string }> },
) {
  const { loanId } = await params;
  return authenticatedRoute(async (session) => {
    const data = await loansApi.listMyLoanRepayments(loanId, session);
    return Response.json({ ok: true, data });
  });
}
