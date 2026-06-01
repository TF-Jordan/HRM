import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as declarationsApi from "@/server/ksm/modules/declarations";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:declaration:manage", async (session) => {
    const body = (await request.json()) as { fichierId?: string };
    const fileId = (body.fichierId ?? "").trim();
    if (!fileId) {
      return Response.json(
        { ok: false, status: 400, errorCode: "BAD_REQUEST", message: "fichierId is required" },
        { status: 400 },
      );
    }
    const data = await declarationsApi.generateDeclaration(id, fileId, session);
    return Response.json({ ok: true, data });
  });
}
