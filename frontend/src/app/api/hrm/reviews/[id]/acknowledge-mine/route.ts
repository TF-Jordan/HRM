import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as reviewsApi from "@/server/ksm/modules/reviews";

/**
 * Self-service: the authenticated employee acknowledges their OWN performance review.
 * Backed by the KSM self-service endpoint, which scopes the action to the caller's employee record.
 */
export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return authenticatedRoute(async (session) => {
    const data = await reviewsApi.acknowledgeMyReview(id, session);
    return Response.json({ ok: true, data });
  });
}
