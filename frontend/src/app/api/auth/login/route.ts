import { NextResponse } from "next/server";
import {
  ksmDiscoverContexts,
  ksmSelectContext,
  type KsmDiscoveredContext,
} from "@/server/ksm/modules/auth";
import { setSession, ensureCsrfToken } from "@/server/session";
import { loginSchema } from "@/lib/validation/auth.schema";
import { HttpError } from "@/lib/types/api";
import { logger } from "@/lib/log";

/**
 * BFF login endpoint. Drives the KSM multi-tenant auth flow:
 *  1. discover-contexts -> list of tenants × organizations
 *  2. if exactly one context with one organization -> auto select-context + create session
 *  3. otherwise -> return the discovery payload so the client can route to /select-context
 */
export async function POST(request: Request) {
  let body: unknown;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json(
      { success: false, message: "Invalid JSON body", errorCode: "BAD_REQUEST" },
      { status: 400 },
    );
  }

  const parsed = loginSchema.safeParse(body);
  if (!parsed.success) {
    return NextResponse.json(
      {
        success: false,
        message: "Validation failed",
        errorCode: "VALIDATION",
        details: parsed.error.issues,
      },
      { status: 400 },
    );
  }

  try {
    const discovery = await ksmDiscoverContexts(parsed.data.email, parsed.data.password);

    if (discovery.contexts.length === 0) {
      return NextResponse.json(
        {
          success: false,
          message: "No tenant access found for this account.",
          errorCode: "NO_CONTEXT",
        },
        { status: 401 },
      );
    }

    const autoSelected = pickAutoContext(discovery.contexts);
    if (autoSelected) {
      const { context, organization } = autoSelected;
      const contextual = await ksmSelectContext({
        selectionToken: discovery.selectionToken,
        contextId: context.contextId,
        organizationId: organization.organizationId,
      });

      await persistSession(contextual);
      await ensureCsrfToken();

      return NextResponse.json({
        success: true,
        kind: "success",
        user: {
          userId: contextual.session.id,
          actorId: contextual.session.actorId,
          email: contextual.session.email,
          displayName: contextual.session.username,
        },
        redirectTo: "/dashboard",
      });
    }

    // Multiple tenants or multiple organisations — defer to client.
    return NextResponse.json({
      success: true,
      kind: "select-context",
      selectionToken: discovery.selectionToken,
      expiresInSeconds: discovery.expiresInSeconds,
      contexts: discovery.contexts,
      redirectTo: "/select-context",
    });
  } catch (err) {
    if (err instanceof HttpError) {
      logger.warn(
        { status: err.status, errorCode: err.errorCode, message: err.message },
        "login failed (KSM error)",
      );
      return NextResponse.json(
        { success: false, message: err.message, errorCode: err.errorCode ?? "AUTH_FAILED" },
        { status: err.status === 401 ? 401 : err.status >= 500 ? 502 : 400 },
      );
    }
    logger.error({ err: String(err) }, "login failed (unexpected)");
    return NextResponse.json(
      { success: false, message: "Unexpected error", errorCode: "INTERNAL" },
      { status: 500 },
    );
  }
}

function pickAutoContext(contexts: KsmDiscoveredContext[]):
  | { context: KsmDiscoveredContext; organization: KsmDiscoveredContext["organizations"][number] }
  | null {
  if (contexts.length !== 1) return null;
  const context = contexts[0]!;
  if (context.organizations.length !== 1) return null;
  return { context, organization: context.organizations[0]! };
}

async function persistSession(contextual: Awaited<ReturnType<typeof ksmSelectContext>>) {
  const session = contextual.session;
  await setSession({
    user: {
      userId: session.id,
      actorId: session.actorId,
      email: session.email,
      displayName: session.username,
    },
    context: {
      tenantId: contextual.selectedTenantId,
      organizationId: contextual.selectedOrganizationId ?? "",
      agencyId: null,
    },
    accessToken: session.accessToken,
    refreshToken: null,
    expiresAt: Date.now() + session.expiresInSeconds * 1000,
  });
}
