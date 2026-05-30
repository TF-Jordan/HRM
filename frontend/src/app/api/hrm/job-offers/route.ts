import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as recruitmentApi from "@/server/ksm/modules/recruitment";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:recruitment:read", async (session) => {
    const orgId = request.nextUrl.searchParams.get("organizationId") ?? undefined;
    const data = await recruitmentApi.listJobOffers(session, orgId);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:recruitment:create", async (session) => {
    const body = (await request.json()) as recruitmentApi.CreateJobOfferRequest;
    const data = await recruitmentApi.createJobOffer(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
