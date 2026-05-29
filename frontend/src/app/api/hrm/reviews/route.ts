import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as reviewsApi from "@/server/ksm/modules/reviews";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:review:read", async (session) => {
    const sp = request.nextUrl.searchParams;
    const periode = sp.get("periode");
    if (!periode) {
      return Response.json(
        { ok: false, status: 400, errorCode: "BAD_REQUEST", message: "periode is required" },
        { status: 400 },
      );
    }
    const data = await reviewsApi.listReviewsByOrgAndPeriode(
      session,
      periode,
      sp.get("organizationId") ?? undefined,
    );
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:review:create", async (session) => {
    const body = (await request.json()) as reviewsApi.CreateReviewRequest;
    const data = await reviewsApi.createReview(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
