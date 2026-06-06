import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as expensesApi from "@/server/ksm/modules/expenses";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, reports: [], lines: [] } });
    }
    const reports = await expensesApi.listExpenseReportsByEmployee(employee.id, session);

    // Fetch every report's lines in parallel so the UI can render a category
    // breakdown and per-report line counts without N round-trips client-side.
    const lineResults = await Promise.allSettled(
      reports.map((r) => expensesApi.listExpenseLines(r.id, session)),
    );
    const lines = lineResults.flatMap((res) =>
      res.status === "fulfilled" ? res.value : [],
    );

    return Response.json({ ok: true, data: { employee, reports, lines } });
  });
}
