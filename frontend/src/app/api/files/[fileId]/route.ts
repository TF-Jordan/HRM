import "server-only";

import type { NextRequest } from "next/server";

import { serverEnv } from "@/env";
import { readSession } from "@/server/session";

/**
 * Stream a file binary from file-core back to the browser. Keeps the original
 * Content-Type and Content-Disposition so PDFs render inline and downloads
 * use the right filename.
 */
export async function GET(
  _request: NextRequest,
  { params }: { params: Promise<{ fileId: string }> },
) {
  const { fileId } = await params;
  const session = await readSession();
  if (!session) {
    return Response.json(
      { ok: false, status: 401, errorCode: "UNAUTHORIZED", message: "Not authenticated" },
      { status: 401 },
    );
  }
  if (!serverEnv) {
    return new Response("server-only env not loaded", { status: 500 });
  }

  const upstream = await fetch(`${serverEnv.KSM_BASE_URL}/api/files/${fileId}`, {
    headers: {
      "X-Client-Id": serverEnv.KSM_CLIENT_ID,
      "X-Api-Key": serverEnv.KSM_API_KEY,
      Authorization: `Bearer ${session.accessToken}`,
      "X-Tenant-Id": session.user.tenantId,
      ...(session.workspace?.organizationId
        ? { "X-Organization-Id": session.workspace.organizationId }
        : {}),
    },
    cache: "no-store",
  });
  if (!upstream.ok) {
    return new Response(`Upstream ${upstream.status}`, { status: upstream.status });
  }
  const headers = new Headers();
  const ct = upstream.headers.get("content-type");
  if (ct) headers.set("content-type", ct);
  const cd = upstream.headers.get("content-disposition");
  if (cd) headers.set("content-disposition", cd);
  return new Response(upstream.body, { status: 200, headers });
}
