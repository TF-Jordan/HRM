import { NextResponse } from "next/server";
import { z } from "zod";
import { ksmSelectContext } from "@/server/ksm/modules/auth";
import { setSession, ensureCsrfToken } from "@/server/session";
import { HttpError } from "@/lib/types/api";
import { logger } from "@/lib/log";

const schema = z.object({
  selectionToken: z.string().min(1),
  contextId: z.string().min(1),
  organizationId: z.uuid().nullish(),
});

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
  const parsed = schema.safeParse(body);
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
    const contextual = await ksmSelectContext({
      selectionToken: parsed.data.selectionToken,
      contextId: parsed.data.contextId,
      organizationId: parsed.data.organizationId ?? null,
    });

    await setSession({
      user: {
        userId: contextual.session.id,
        actorId: contextual.session.actorId,
        email: contextual.session.email,
        displayName: contextual.session.username,
      },
      context: {
        tenantId: contextual.selectedTenantId,
        organizationId: contextual.selectedOrganizationId ?? "",
        agencyId: null,
      },
      accessToken: contextual.session.accessToken,
      refreshToken: null,
      expiresAt: Date.now() + contextual.session.expiresInSeconds * 1000,
    });
    await ensureCsrfToken();

    return NextResponse.json({
      success: true,
      user: {
        userId: contextual.session.id,
        email: contextual.session.email,
        displayName: contextual.session.username,
      },
      redirectTo: "/dashboard",
    });
  } catch (err) {
    if (err instanceof HttpError) {
      logger.warn(
        { status: err.status, errorCode: err.errorCode, message: err.message },
        "select-context failed",
      );
      return NextResponse.json(
        { success: false, message: err.message, errorCode: err.errorCode ?? "AUTH_FAILED" },
        { status: err.status === 401 ? 401 : err.status >= 500 ? 502 : 400 },
      );
    }
    logger.error({ err: String(err) }, "select-context failed (unexpected)");
    return NextResponse.json(
      { success: false, message: "Unexpected error", errorCode: "INTERNAL" },
      { status: 500 },
    );
  }
}
