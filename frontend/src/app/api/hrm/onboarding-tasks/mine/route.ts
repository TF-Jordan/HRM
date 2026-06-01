import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as recruitmentApi from "@/server/ksm/modules/recruitment";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/** Self-service: the current employee's onboarding tasks. */
export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, tasks: [] } });
    }
    const tasks = await recruitmentApi.listOnboardingTasks(employee.id, session);
    return Response.json({ ok: true, data: { employee, tasks } });
  });
}
