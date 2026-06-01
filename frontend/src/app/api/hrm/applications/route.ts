import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as recruitmentApi from "@/server/ksm/modules/recruitment";

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:recruitment:create", async (session) => {
    const body = (await request.json()) as recruitmentApi.CreateApplicationRequest;
    const data = await recruitmentApi.createApplication(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
