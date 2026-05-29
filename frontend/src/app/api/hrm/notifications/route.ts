import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as expensesApi from "@/server/ksm/modules/expenses";
import * as missionsApi from "@/server/ksm/modules/missions";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/**
 * In-app notification counts driven from real KSM data:
 *   - pendingAcceptance : mission orders awaiting the current employee's decision
 *   - declined          : mission orders the current user (manager) needs to amend
 *   - expensesToApprove : expense reports SUBMITTED, awaiting the accountant/DAF
 * Each bucket is zero for users that lack the underlying permission.
 */
export async function GET() {
  return authenticatedRoute(async (session) => {
    const owned = new Set(session.user.permissions ?? []);
    const canAccept = owned.has("hrm:mission:accept");
    const canManageMissions = owned.has("hrm:mission:manage");
    const canManageExpenses = owned.has("hrm:expense:manage");

    let pendingAcceptance = 0;
    let declined = 0;
    let expensesToApprove = 0;

    if (canAccept) {
      const me = await findMyEmployee(session);
      if (me) {
        try {
          const orders = await missionsApi.listMissionOrdersByEmployee(me.id, session);
          pendingAcceptance = orders.filter((o) => o.status === "PENDING_ACCEPTANCE").length;
        } catch {
          // surface as 0 rather than break the topbar
        }
      }
    }

    if (canManageMissions) {
      try {
        const orders = await missionsApi.listDeclined(session);
        declined = orders.length;
      } catch {
        // surface as 0 rather than break the topbar
      }
    }

    if (canManageExpenses) {
      try {
        const reports = await expensesApi.listAllExpenseReports(session, { status: "SUBMITTED" });
        expensesToApprove = reports.length;
      } catch {
        // surface as 0 rather than break the topbar
      }
    }

    return Response.json({
      ok: true,
      data: {
        total: pendingAcceptance + declined + expensesToApprove,
        buckets: { pendingAcceptance, declined, expensesToApprove },
      },
    });
  });
}
