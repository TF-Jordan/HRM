import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as declarationsApi from "@/server/ksm/modules/declarations";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:declaration:read", async (session) => {
    const data = await declarationsApi.getDeclaration(id, session);
    return Response.json({ ok: true, data });
  });
}
