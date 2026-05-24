import { NextResponse } from "next/server";
import { getKsmContext } from "@/server/ksm/context";
import { serverEnv } from "@/env";
import { logger } from "@/lib/log";

export async function GET(
  _request: Request,
  ctx: RouteContext<"/api/hrm/files/[fileId]">,
) {
  try {
    const { fileId } = await ctx.params;
    const kctx = await getKsmContext();
    const base = serverEnv.KSM_BASE_URL.replace(/\/$/, "");

    const res = await fetch(`${base}/api/files/${fileId}`, {
      method: "GET",
      headers: {
        "X-Client-Id": serverEnv.KSM_CLIENT_ID,
        "X-Api-Key": serverEnv.KSM_API_KEY,
        "X-Tenant-Id": kctx.tenantId,
        "X-Organization-Id": kctx.organizationId,
        Authorization: `Bearer ${kctx.bearer}`,
      },
    });

    if (!res.ok) {
      return NextResponse.json(
        { success: false, message: `File not found (${res.status})`, errorCode: "NOT_FOUND" },
        { status: res.status >= 400 && res.status < 500 ? res.status : 502 },
      );
    }

    const contentType = res.headers.get("content-type") ?? "application/octet-stream";
    const contentDisposition = res.headers.get("content-disposition") ?? "inline";
    const body = res.body;

    return new NextResponse(body, {
      status: 200,
      headers: {
        "Content-Type": contentType,
        "Content-Disposition": contentDisposition,
      },
    });
  } catch (err) {
    if (err instanceof Error && err.message === "Not authenticated") {
      return NextResponse.json(
        { success: false, message: "Unauthorized", errorCode: "UNAUTHORIZED" },
        { status: 401 },
      );
    }
    logger.error({ err: String(err) }, "GET /api/hrm/files/[fileId] failed");
    return NextResponse.json(
      { success: false, message: "Download failed", errorCode: "INTERNAL" },
      { status: 500 },
    );
  }
}
