import { NextResponse } from "next/server";
import { ksmLogin } from "@/server/ksm/modules/auth";
import { setSession, ensureCsrfToken } from "@/server/session";
import { loginSchema } from "@/lib/validation/auth.schema";
import { HttpError } from "@/lib/types/api";
import { logger } from "@/lib/log";

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
    const result = await ksmLogin({
      principal: parsed.data.email,
      password: parsed.data.password,
    });

    if (result.kind === "mfa") {
      return NextResponse.json({
        success: true,
        kind: "mfa",
        mfaToken: result.mfaToken,
        channel: result.channel,
        redirectTo: "/mfa",
      });
    }

    if (result.contexts.length > 1) {
      // Multi-org user: persist preliminary token and bounce to context selection.
      // For Phase 0 we store the basic session and let the user re-select.
      const sessionTtlMs = result.expiresInSeconds * 1000;
      await setSession({
        user: result.user,
        // Will be overridden once a context is chosen.
        context: {
          tenantId: result.contexts[0]!.tenantId,
          organizationId: result.contexts[0]!.organizationId,
          agencyId: result.contexts[0]!.agencyId ?? null,
        },
        permissions: result.permissions,
        accessToken: result.accessToken,
        refreshToken: result.refreshToken ?? null,
        expiresAt: Date.now() + sessionTtlMs,
      });
      await ensureCsrfToken();
      return NextResponse.json({
        success: true,
        kind: "select-context",
        contexts: result.contexts,
        redirectTo: "/select-context",
      });
    }

    const ctx = result.contexts[0];
    await setSession({
      user: result.user,
      context: {
        tenantId: ctx?.tenantId ?? "",
        organizationId: ctx?.organizationId ?? "",
        agencyId: ctx?.agencyId ?? null,
      },
      permissions: result.permissions,
      accessToken: result.accessToken,
      refreshToken: result.refreshToken ?? null,
      expiresAt: Date.now() + result.expiresInSeconds * 1000,
    });
    await ensureCsrfToken();

    return NextResponse.json({
      success: true,
      kind: "success",
      user: result.user,
      redirectTo: "/dashboard",
    });
  } catch (err) {
    if (err instanceof HttpError) {
      logger.warn(
        { status: err.status, errorCode: err.errorCode, message: err.message },
        "login failed (KSM error)",
      );
      return NextResponse.json(
        { success: false, message: err.message, errorCode: err.errorCode ?? "AUTH_FAILED" },
        { status: err.status === 401 ? 401 : 400 },
      );
    }
    logger.error({ err: String(err) }, "login failed (unexpected)");
    return NextResponse.json(
      { success: false, message: "Unexpected error", errorCode: "INTERNAL" },
      { status: 500 },
    );
  }
}
