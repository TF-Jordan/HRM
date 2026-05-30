import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as medicalApi from "@/server/ksm/modules/medical";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/**
 * Self-service: medical visits and certificates for the current employee.
 */
export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, visits: [], certificates: [] } });
    }
    const [visits, certificates] = await Promise.all([
      medicalApi.listVisitsByEmployee(employee.id, session),
      medicalApi.listCertificatesByEmployee(employee.id, session),
    ]);
    return Response.json({ ok: true, data: { employee, visits, certificates } });
  });
}
