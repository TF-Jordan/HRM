import "server-only";

import { callKsm } from "../client";

/**
 * Typed wrappers around KSM auth-core endpoints.
 * Flow:
 *   1. POST /api/auth/discover-contexts { principal, password }
 *      -> selectionToken + list of tenants × organizations
 *   2. POST /api/auth/select-context  { selectionToken, contextId, organizationId? }
 *      -> accessToken (JWT) + user + authorities
 *   (MFA path: discover-contexts can return mfa challenge, then /api/auth/login/mfa/confirm)
 */

export type KsmUserOrganizationAccess = {
  organizationId: string;
  organizationCode: string;
  shortName: string | null;
  longName: string | null;
  displayName: string | null;
  legalName: string | null;
  services: string[];
};

export type KsmDiscoveredContext = {
  contextId: string;
  tenantId: string;
  userId: string;
  actorId: string;
  organizations: KsmUserOrganizationAccess[];
};

export type KsmDiscoverContextsResponse = {
  selectionToken: string;
  expiresInSeconds: number;
  contexts: KsmDiscoveredContext[];
};

export type KsmLoginResponse = {
  id: string;
  tenantId: string;
  actorId: string;
  username: string;
  email: string;
  phoneNumber: string | null;
  authProvider: string;
  status: string;
  plan: string;
  onboardingStatus: string;
  accountType: string;
  emailVerified: boolean;
  mfaEnabled: boolean;
  mfaChannel: string | null;
  accessToken: string;
  sessionToken: string;
  tokenType: string;
  expiresInSeconds: number;
  authorities: string[];
  organizations: KsmUserOrganizationAccess[];
};

export type KsmContextualLoginResponse = {
  selectedTenantId: string;
  selectedOrganizationId: string | null;
  session: KsmLoginResponse;
};

export async function ksmDiscoverContexts(
  principal: string,
  password: string,
): Promise<KsmDiscoverContextsResponse> {
  return callKsm<KsmDiscoverContextsResponse>("/api/auth/discover-contexts", {
    method: "POST",
    body: { principal, password },
  });
}

export async function ksmSelectContext(input: {
  selectionToken: string;
  contextId: string;
  organizationId?: string | null;
}): Promise<KsmContextualLoginResponse> {
  return callKsm<KsmContextualLoginResponse>("/api/auth/select-context", {
    method: "POST",
    body: {
      selectionToken: input.selectionToken,
      contextId: input.contextId,
      organizationId: input.organizationId ?? null,
    },
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
