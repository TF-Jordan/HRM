import "server-only";

import type { NextRequest } from "next/server";

import { handleRoute } from "@/server/api-response";
import { logAuthEvent } from "@/server/auth-flow";
import * as authApi from "@/server/ksm/modules/auth";
import { patchSession, readSession } from "@/server/session";

export async function POST(request: NextRequest) {
  return handleRoute(async () => {
    const session = await readSession();
    if (!session) {
      return Response.json(
        { ok: false, status: 401, errorCode: "UNAUTHORIZED", message: "Not authenticated" },
        { status: 401 },
      );
    }

    const body = (await request.json()) as { currentPassword?: string; newPassword?: string };
    const currentPassword = String(body.currentPassword ?? "");
    const newPassword = String(body.newPassword ?? "");
    if (!currentPassword || !newPassword) {
      return Response.json(
        { ok: false, status: 400, errorCode: "VALIDATION_ERROR", message: "currentPassword and newPassword are required" },
        { status: 400 },
      );
    }

    await authApi.changePassword(
      session.accessToken,
      session.user.tenantId,
      { currentPassword, newPassword },
      session.workspace?.organizationId,
    );

    // Clear the flag from the session
    await patchSession({ forcePasswordChange: false });

    logAuthEvent("password_changed", { userId: session.user.userId });

    return Response.json({ ok: true, data: { ok: true } });
  });
}
