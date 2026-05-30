import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as recruitmentApi from "@/server/ksm/modules/recruitment";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:recruitment:manage", async (session) => {
    const body = (await request.json()) as recruitmentApi.CompleteInterviewRequest;
    const data = await recruitmentApi.completeInterview(id, body, session);
    return Response.json({ ok: true, data });
  });
}
