import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as recruitmentApi from "@/server/ksm/modules/recruitment";
import { hasPermission } from "@/server/permissions";

/**
 * Hire-and-provision: the recruiter supplies the missing HR fields (catégorie,
 * département, dates de contrat, etc.) and KSM provisions an Actor + Employee
 * + active Contract while marking the application HIRED. Both permissions
 * (recruitment:manage AND employee:create) are required because the call
 * touches both aggregates.
 */
export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return authenticatedRoute(async (session) => {
    if (!hasPermission(session, ["hrm:recruitment:manage"]) ||
        !hasPermission(session, ["hrm:employee:create"])) {
      return Response.json(
        {
          ok: false,
          status: 403,
          errorCode: "FORBIDDEN",
          message: "Missing required permission(s)",
        },
        { status: 403 },
      );
    }
    const body = (await request.json()) as recruitmentApi.ConvertApplicationRequest;
    const data = await recruitmentApi.convertApplicationToEmployee(id, body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
