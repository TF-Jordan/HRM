import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as recruitmentApi from "@/server/ksm/modules/recruitment";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:recruitment:read", async (session) => {
    const data = await recruitmentApi.getJobOffer(id, session);
    return Response.json({ ok: true, data });
  });
}
