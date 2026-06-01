import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as expensesApi from "@/server/ksm/modules/expenses";

export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:expense:manage", async (session) => {
    const data = await expensesApi.rejectExpenseReport(id, session);
    return Response.json({ ok: true, data });
  });
}
