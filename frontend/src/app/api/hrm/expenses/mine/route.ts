import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as expensesApi from "@/server/ksm/modules/expenses";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, reports: [] } });
    }
    const reports = await expensesApi.listExpenseReportsByEmployee(employee.id, session);
    return Response.json({ ok: true, data: { employee, reports } });
  });
}
