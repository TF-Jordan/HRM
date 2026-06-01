import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as declarationsApi from "@/server/ksm/modules/declarations";

export async function POST(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:declaration:manage", async (session) => {
    const data = await declarationsApi.submitDeclaration(id, session);
    return Response.json({ ok: true, data });
  });
}
