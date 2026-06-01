import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as declarationsApi from "@/server/ksm/modules/declarations";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:declaration:read", async (session) => {
    const orgId = request.nextUrl.searchParams.get("organizationId") ?? undefined;
    const data = await declarationsApi.listDeclarations(session, orgId);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:declaration:create", async (session) => {
    const body = (await request.json()) as declarationsApi.CreateSocialDeclarationRequest;
    const data = await declarationsApi.createDeclaration(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
