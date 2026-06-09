import "server-only";

import type { NextRequest } from "next/server";

import { handleRoute } from "@/server/api-response";
import {
  buildSession,
  buildSessionFromContextual,
  decideAfterDiscover,
  enrichSessionWithActorName,
  logAuthEvent,
} from "@/server/auth-flow";
import { callKsm } from "@/server/ksm/client";
import * as authApi from "@/server/ksm/modules/auth";
import { writeSession } from "@/server/session";

export async function POST(request: NextRequest) {
  return handleRoute(async () => {
    const body = (await request.json()) as { email?: string; password?: string };
    const principal = String(body.email ?? "").trim().toLowerCase();
    const password = String(body.password ?? "");
    if (!principal || !password) {
      return Response.json(
        { ok: false, status: 400, errorCode: "VALIDATION_ERROR", message: "email and password are required" },
        { status: 400 },
      );
    }

    const discovery = await authApi.discoverContexts({ principal, password });
    const decision = decideAfterDiscover(discovery.selectionToken, discovery.contexts);

    if (decision.kind === "noAccess") {
      logAuthEvent("login_no_access", { principal });
      return Response.json(
        { ok: false, status: 403, errorCode: "NO_ACCESSIBLE_CONTEXT", message: "No accessible workspace found." },
        { status: 403 },
      );
    }

    if (decision.kind === "needsSelection") {
      logAuthEvent("login_needs_selection", { principal, contextCount: decision.contexts.length });
      return Response.json({
        ok: true,
        data: {
          step: "select_context" as const,
          selectionToken: decision.selectionToken,
          contexts: decision.contexts,
        },
      });
    }

    // Auto-select the single context
    const contextual = await authApi.selectContext({
      selectionToken: discovery.selectionToken,
      contextId: decision.contextId,
      organizationId: decision.organizationId,
    });

    const appSession = buildSessionFromContextual(contextual);
    // Fallback workspace resolution: the discovery payload only carries an
    // explicit organization when the user holds a business_actor_profile in
    // that org. For scoped-only users (e.g. EMPLOYEE in org X) we still know
    // the org because every granted permission ends with
    // `#ORGANIZATION:<uuid>`. Try the catalog API first (works for tenant
    // admins), then fall back to parsing scoped permissions.
    if (!appSession.workspace?.organizationId) {
      try {
        const orgs = await callKsm<
          Array<{ id: string; shortName?: string; longName?: string; code?: string }>
        >("/api/organizations", {}, { session: appSession });
        const first = orgs[0];
        if (first?.id) {
          appSession.workspace = {
            tenantId: appSession.user.tenantId,
            organizationId: first.id,
            organizationName: first.shortName ?? first.longName ?? first.code,
          };
        }
      } catch {
        // Catalog not accessible — fall through to permission-scope parsing.
      }
    }
    if (!appSession.workspace?.organizationId) {
      const orgIdFromPerms = (appSession.user.permissions ?? [])
        .map((p) => /#ORGANIZATION:([0-9a-f-]{36})/.exec(p)?.[1])
        .find((id): id is string => !!id);
      if (orgIdFromPerms) {
        appSession.workspace = {
          tenantId: appSession.user.tenantId,
          organizationId: orgIdFromPerms,
        };
      }
    }
    await enrichSessionWithActorName(appSession);
    await writeSession(appSession);

    logAuthEvent("login_success", {
      userId: appSession.user.userId,
      tenantId: appSession.user.tenantId,
      organizationId: appSession.workspace?.organizationId,
      forcePasswordChange: appSession.forcePasswordChange,
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
