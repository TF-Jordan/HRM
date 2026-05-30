import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as medicalApi from "@/server/ksm/modules/medical";

export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:medical:read", async (session) => {
    const data = await medicalApi.getCertificate(id, session);
    return Response.json({ ok: true, data });
  });
}
