import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as leavesApi from "@/server/ksm/modules/leaves";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/**
 * Returns the leave history of the currently authenticated employee.
 * Resolves the employeeId from session.actorId behind the scenes so the
 * client never needs to know the underlying HRM id.
 */
export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({
        ok: true,
        data: { employee: null, leaves: [] },
      });
    }
    const leaves = await leavesApi.listLeavesByEmployee(employee.id, session);
    return Response.json({ ok: true, data: { employee, leaves } });
  });
}
