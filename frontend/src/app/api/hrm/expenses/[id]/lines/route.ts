import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as expensesApi from "@/server/ksm/modules/expenses";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:expense:read", async (session) => {
    const data = await expensesApi.listExpenseLines(id, session);
    return Response.json({ ok: true, data });
  });
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:expense:create", async (session) => {
    const body = (await request.json()) as expensesApi.AddExpenseLineRequest;
    const data = await expensesApi.addExpenseLine(id, body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
