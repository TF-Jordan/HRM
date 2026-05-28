import "server-only";

import { callKsm } from "@/server/ksm/client";

/* ---------------------------- Types ---------------------------- */

export type LoginInput = { principal: string; password: string };

export type DiscoverContextsResponse = {
  selectionToken: string;
  expiresInSeconds: number;
  contexts: DiscoveredContext[];
};

export type DiscoveredContext = {
  contextId: string;
  tenantId: string;
  userId: string;
  actorId: string;
  organizations: UserOrganizationAccess[];
};

export type UserOrganizationAccess = {
  organizationId: string;
  tenantId: string;
  organizationName?: string;
  organizationCode?: string;
  agencyId?: string;
  agencyName?: string;
  roleCodes?: string[];
  authorities?: string[];
};

export type LoginResponse = {
  id: string;
  tenantId: string;
  actorId: string;
  username: string;
  email: string;
  phoneNumber?: string | null;
  authProvider: string;
  status: string;
  plan: string;
  onboardingStatus: string;
  onboardingStep: number;
  accountType?: string | null;
  businessType?: string | null;
  emailVerified: boolean;
  phoneVerified: boolean;
  mfaEnabled: boolean;
  mfaChannel?: string | null;
  forcePasswordChange: boolean;
  accessToken: string;
  sessionToken: string;
  tokenType: string;
  expiresInSeconds: number;
  authorities: string[];
  organizations: UserOrganizationAccess[];
};

export type ContextualLoginResponse = {
  selectedTenantId: string;
  selectedOrganizationId?: string | null;
  session: LoginResponse;
};

export type UserAccountResponse = LoginResponse;

export type IdentifyResponse = {
  principal: string;
  accountExists: boolean;
  recommendedAction: "SIGN_IN_PASSWORD" | "SIGN_UP";
  matchedAccountCount: number;
};

/* ---------------------------- Calls ---------------------------- */

export function identify(principal: string) {
  return callKsm<IdentifyResponse>(
    "/api/auth/identify",
    { method: "POST", body: { principal }, authenticated: false },
  );
}

export function discoverContexts(input: LoginInput) {
  return callKsm<DiscoverContextsResponse>(
    "/api/auth/discover-contexts",
    { method: "POST", body: input, authenticated: false },
  );
}

export function selectContext(input: {
  selectionToken: string;
  contextId: string;
  organizationId?: string;
}) {
  return callKsm<ContextualLoginResponse>(
    "/api/auth/select-context",
    { method: "POST", body: input, authenticated: false },
  );
}

export function logout() {
  // KSM does not currently provide a logout endpoint — sessions are stateless JWTs.
  // The BFF simply clears its own cookie. No KSM call needed.
  return Promise.resolve();
}

export function getMe(accessToken: string, tenantId: string, organizationId?: string) {
  return callKsm<UserAccountResponse>(
    "/api/users/me",
    {
      method: "GET",
      authenticated: false,
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "X-Tenant-Id": tenantId,
        ...(organizationId ? { "X-Organization-Id": organizationId } : {}),
      },
    },
  );
}

export function changePassword(
  accessToken: string,
  tenantId: string,
  input: { currentPassword: string; newPassword: string },
  organizationId?: string,
) {
  return callKsm<UserAccountResponse>(
    "/api/auth/change-password",
    {
      method: "POST",
      body: input,
      authenticated: false,
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "X-Tenant-Id": tenantId,
        ...(organizationId ? { "X-Organization-Id": organizationId } : {}),
      },
    },
  );
}
