import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as skillsApi from "@/server/ksm/modules/skills";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/**
 * Self-service: the current employee's competency assessments (current vs expected level), enriched
 * with the skill referential names/categories so the page can render a personal competency matrix
 * without a per-row round-trip.
 */
export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, skills: [] } });
    }
    const skills = await skillsApi.listEmployeeSkillsEnriched(employee.id, session);
    return Response.json({ ok: true, data: { employee, skills } });
  });
}
