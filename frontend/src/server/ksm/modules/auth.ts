import "server-only";

import { callKsm } from "../client";

/**
 * Thin typed wrappers around KSM auth endpoints. Shapes are inferred from
 * the auth-core controllers — they will be refined once we plug
 * openapi-typescript in Phase 0+.
 */

export type KsmLoginRequest = {
  principal: string;
  password: string;
};

export type KsmLoginResponse =
  | {
      kind: "mfa";
      mfaToken: string;
      channel: string;
    }
  | {
      kind: "success";
      accessToken: string;
      refreshToken?: string;
      expiresInSeconds: number;
      user: {
        userId: string;
        actorId: string;
        email: string;
        displayName: string;
        preferredLanguage?: string;
      };
      contexts: KsmContext[];
      permissions: string[];
    };

export type KsmContext = {
  contextId: string;
  tenantId: string;
  tenantName: string;
  organizationId: string;
  organizationName: string;
  agencyId?: string | null;
  agencyName?: string | null;
  roles: string[];
};

/**
 * Calls KSM /api/auth/login. The exact response shape is bound to evolve as
 * we wire up real auth-core endpoints — this is the Phase 0 stub.
 */
export async function ksmLogin(input: KsmLoginRequest): Promise<KsmLoginResponse> {
  return callKsm<KsmLoginResponse>("/api/auth/login", {
    method: "POST",
    body: { principal: input.principal, password: input.password },
  });
}

export async function ksmMfaConfirm(input: {
  mfaToken: string;
  code: string;
}): Promise<KsmLoginResponse> {
  return callKsm<KsmLoginResponse>("/api/auth/login/mfa/confirm", {
    method: "POST",
    body: input,
  });
}

export async function ksmSelectContext(input: {
  bearer: string;
  tenantId: string;
  organizationId: string;
  agencyId?: string | null;
}): Promise<KsmLoginResponse> {
  return callKsm<KsmLoginResponse>("/api/auth/select-context", {
    method: "POST",
    bearer: input.bearer,
    body: {
      tenantId: input.tenantId,
      organizationId: input.organizationId,
      agencyId: input.agencyId ?? null,
    },
  });
}
