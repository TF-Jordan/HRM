import "server-only";

import type { NextRequest } from "next/server";

import { serverEnv } from "@/env";
import { readSession } from "@/server/session";

const TYPES = ["CNPS", "DIPE", "IRPP_CAC"];

/**
 * Streams the statutory declaration CSV from payroll-core, preserving the
 * Content-Disposition so the browser downloads it with the right filename.
 */
export async function GET(request: NextRequest) {
  const session = await readSession();
  if (!session) {
    return new Response("Not authenticated", { status: 401 });
  }
  if (!serverEnv) {
    return new Response("server-only env not loaded", { status: 500 });
  }
  const type = request.nextUrl.searchParams.get("type");
  const runId = request.nextUrl.searchParams.get("runId");
  if (!type || !TYPES.includes(type) || !runId) {
    return new Response("Invalid 'type' or 'runId'", { status: 400 });
  }

  const params = new URLSearchParams({ type, runId });
  const upstream = await fetch(
    `${serverEnv.KSM_BASE_URL}/api/v1/payroll/declarations/csv?${params}`,
    {
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
    },
  );
  if (!upstream.ok) {
    return new Response(`Upstream ${upstream.status}`, { status: upstream.status });
  }
  const headers = new Headers();
  headers.set("content-type", upstream.headers.get("content-type") ?? "text/csv; charset=UTF-8");
  headers.set(
    "content-disposition",
    upstream.headers.get("content-disposition") ??
      `attachment; filename="declaration-${type}-${runId}.csv"`,
  );
  return new Response(upstream.body, { status: 200, headers });
}
