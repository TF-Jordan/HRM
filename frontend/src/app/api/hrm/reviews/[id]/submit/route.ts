import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as reviewsApi from "@/server/ksm/modules/reviews";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:review:manage", async (session) => {
    const body = (await request.json()) as reviewsApi.SubmitReviewRequest;
    const data = await reviewsApi.submitReview(id, body, session);
    return Response.json({ ok: true, data });
  });
}
