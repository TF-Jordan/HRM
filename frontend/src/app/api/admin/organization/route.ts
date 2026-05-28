import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as orgApi from "@/server/ksm/modules/organization";

export async function GET() {
  return requirePermissionRoute("organizations:write", async (session) => {
    const orgId = session.workspace?.organizationId;
    if (!orgId) {
      return Response.json(
        { ok: false, status: 400, errorCode: "ORGANIZATION_CONTEXT_REQUIRED", message: "No organization in context" },
        { status: 400 },
      );
    }
    const data = await orgApi.getOrganization(orgId, session);
    return Response.json({ ok: true, data });
  });
}

export async function PATCH(request: NextRequest) {
  return requirePermissionRoute("organizations:write", async (session) => {
    const orgId = session.workspace?.organizationId;
    if (!orgId) {
      return Response.json(
        { ok: false, status: 400, errorCode: "ORGANIZATION_CONTEXT_REQUIRED", message: "No organization in context" },
        { status: 400 },
      );
    }
    const body = (await request.json()) as orgApi.UpdateOrganizationRequest;
    const data = await orgApi.updateOrganization(orgId, body, session);
    return Response.json({ ok: true, data });
  });
}
