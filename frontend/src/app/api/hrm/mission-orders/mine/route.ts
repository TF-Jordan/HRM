import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as missionsApi from "@/server/ksm/modules/missions";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, orders: [] } });
    }
    const orders = await missionsApi.listMissionOrdersByEmployee(employee.id, session);
    return Response.json({ ok: true, data: { employee, orders } });
  });
}
