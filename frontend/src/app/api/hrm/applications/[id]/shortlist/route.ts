import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as recruitmentApi from "@/server/ksm/modules/recruitment";

export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:recruitment:manage", async (session) => {
    const data = await recruitmentApi.transitionApplication(id, "shortlist", session);
    return Response.json({ ok: true, data });
  });
}
