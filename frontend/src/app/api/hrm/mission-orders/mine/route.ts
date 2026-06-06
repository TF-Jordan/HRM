import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as expensesApi from "@/server/ksm/modules/expenses";
import * as missionsApi from "@/server/ksm/modules/missions";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

function num(value: number | string | null | undefined): number {
  if (value == null) return 0;
  const n = typeof value === "string" ? Number(value) : value;
  return Number.isFinite(n) ? n : 0;
}

export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, orders: [] } });
    }

    const [orders, reports] = await Promise.all([
      missionsApi.listMissionOrdersByEmployee(employee.id, session),
      expensesApi.listExpenseReportsByEmployee(employee.id, session),
    ]);

    // Group the employee's expense reports by the mission they regularize.
    const byMission = new Map<string, expensesApi.ExpenseReportResponse[]>();
    for (const r of reports) {
      if (!r.missionOrderId) continue;
      const list = byMission.get(r.missionOrderId) ?? [];
      list.push(r);
      byMission.set(r.missionOrderId, list);
    }

    const enriched = orders.map((o) => {
      const linked = byMission.get(o.id) ?? [];
      const advance = num(o.montantAvance);
      let approvedExpenses = 0;
      let pendingExpenses = 0;
      for (const r of linked) {
        const amount = num(r.totalMontant);
        if (r.status === "APPROVED" || r.status === "REIMBURSED") approvedExpenses += amount;
        else if (r.status === "SUBMITTED" || r.status === "DRAFT") pendingExpenses += amount;
      }
      return {
        ...o,
        reconciliation: {
          advance,
          approvedExpenses,
          pendingExpenses,
          reportCount: linked.length,
          // > 0 → employer still owes the employee; < 0 → employee must return the surplus.
          balance: approvedExpenses - advance,
        },
      };
    });

    return Response.json({ ok: true, data: { employee, orders: enriched } });
  });
}
