import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as loansApi from "@/server/ksm/modules/loans";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, loans: [] } });
    }
    const loans = await loansApi.listLoanAdvancesByEmployee(employee.id, session);
    return Response.json({ ok: true, data: { employee, loans } });
  });
}
