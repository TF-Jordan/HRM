import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as reviewsApi from "@/server/ksm/modules/reviews";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:review:read", async (session) => {
    const data = await reviewsApi.getReview(id, session);
    return Response.json({ ok: true, data });
  });
}
