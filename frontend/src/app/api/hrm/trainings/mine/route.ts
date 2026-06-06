import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as trainingsApi from "@/server/ksm/modules/trainings";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/**
 * Self-service: the current employee's enrollments and training requests joined
 * with the catalog of training sessions. Sessions are fetched once and indexed
 * locally so the client can render title / dates / location without a per-row
 * round-trip.
 */
export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({
        ok: true,
        data: { employee: null, enrollments: [], requests: [], trainings: [] },
      });
    }
    const [enrollments, requests, trainings] = await Promise.all([
      trainingsApi.listEnrollmentsByEmployee(employee.id, session),
      trainingsApi.listTrainingRequestsByEmployee(employee.id, session),
      trainingsApi.listTrainings(session),
    ]);
    return Response.json({ ok: true, data: { employee, enrollments, requests, trainings } });
  });
}
