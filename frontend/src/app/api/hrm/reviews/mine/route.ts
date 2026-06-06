import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as reviewsApi from "@/server/ksm/modules/reviews";
import type { ObjectiveResponse } from "@/server/ksm/modules/reviews";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/**
 * Self-service: the current employee's performance reviews, each enriched with its objectives so the
 * page can render the goal breakdown without a per-card round-trip. Objectives are fetched in
 * parallel and degrade gracefully (a failed review contributes an empty objective list).
 */
export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, reviews: [], objectives: {} } });
    }
    const reviews = await reviewsApi.listReviewsByEmployee(employee.id, session);
    const results = await Promise.allSettled(
      reviews.map((r) => reviewsApi.listObjectives(r.id, session)),
    );
    const objectives: Record<string, ObjectiveResponse[]> = {};
    reviews.forEach((r, i) => {
      const res = results[i];
      objectives[r.id] = res.status === "fulfilled" ? res.value : [];
    });
    return Response.json({ ok: true, data: { employee, reviews, objectives } });
  });
}
