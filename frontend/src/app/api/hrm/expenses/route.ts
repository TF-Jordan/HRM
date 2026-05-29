import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as expensesApi from "@/server/ksm/modules/expenses";

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:expense:create", async (session) => {
    const body = (await request.json()) as expensesApi.CreateExpenseReportRequest;
    const data = await expensesApi.createExpenseReport(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:expense:read", async (session) => {
    const sp = request.nextUrl.searchParams;
    const employeeId = sp.get("employeeId");
    if (employeeId) {
      const data = await expensesApi.listExpenseReportsByEmployee(employeeId, session);
      return Response.json({ ok: true, data });
    }
    const status = sp.get("status") ?? undefined;
    const data = await expensesApi.listAllExpenseReports(session, {
      organizationId: sp.get("organizationId") ?? undefined,
      status: status as expensesApi.ExpenseReportStatus | undefined,
    });
    return Response.json({ ok: true, data });
  });
}
