import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as reviewsApi from "@/server/ksm/modules/reviews";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, reviews: [] } });
    }
    const reviews = await reviewsApi.listReviewsByEmployee(employee.id, session);
    return Response.json({ ok: true, data: { employee, reviews } });
  });
}
