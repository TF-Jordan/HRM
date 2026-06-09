import "server-only";

import type { NextRequest } from "next/server";

import { handleRoute } from "@/server/api-response";
import { buildSessionFromContextual, enrichSessionWithActorName, logAuthEvent } from "@/server/auth-flow";
import * as authApi from "@/server/ksm/modules/auth";
import { writeSession } from "@/server/session";

export async function POST(request: NextRequest) {
  return handleRoute(async () => {
    const body = (await request.json()) as {
      selectionToken?: string;
      contextId?: string;
      organizationId?: string;
    };
    const selectionToken = String(body.selectionToken ?? "");
    const contextId = String(body.contextId ?? "");
    if (!selectionToken || !contextId) {
      return Response.json(
        { ok: false, status: 400, errorCode: "VALIDATION_ERROR", message: "selectionToken and contextId are required" },
        { status: 400 },
      );
    }

    const contextual = await authApi.selectContext({
      selectionToken,
      contextId,
      organizationId: body.organizationId,
    });
    const appSession = buildSessionFromContextual(contextual);
    await enrichSessionWithActorName(appSession);
    await writeSession(appSession);

    logAuthEvent("context_selected", {
      userId: appSession.user.userId,
      tenantId: appSession.user.tenantId,
      organizationId: appSession.workspace?.organizationId,
    });

    return Response.json({
      ok: true,
      data: {
        step: appSession.forcePasswordChange ? ("change_password" as const) : ("authenticated" as const),
        user: appSession.user,
        workspace: appSession.workspace,
        forcePasswordChange: appSession.forcePasswordChange,
      },
    });
  });
}
