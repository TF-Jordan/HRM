import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as missionsApi from "@/server/ksm/modules/missions";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/**
 * In-app notification counts driven from real KSM data:
 *   - pendingAcceptance : mission orders awaiting the current employee's decision
 *   - declined          : mission orders the current user (manager) needs to amend
 * Both buckets are zero for users that lack the underlying permission.
 */
export async function GET() {
  return authenticatedRoute(async (session) => {
    const owned = new Set(session.user.permissions ?? []);
    const canAccept = owned.has("hrm:mission:accept");
    const canManage = owned.has("hrm:mission:manage");

    let pendingAcceptance = 0;
    let declined = 0;

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

    if (canManage) {
      try {
        const orders = await missionsApi.listDeclined(session);
        declined = orders.length;
      } catch {
        // surface as 0 rather than break the topbar
      }
    }

    return Response.json({
      ok: true,
      data: {
        total: pendingAcceptance + declined,
        buckets: { pendingAcceptance, declined },
      },
    });
  });
}
