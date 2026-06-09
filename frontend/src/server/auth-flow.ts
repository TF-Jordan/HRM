import "server-only";

import type {
  ContextualLoginResponse,
  DiscoveredContext,
  LoginResponse,
  UserOrganizationAccess,
} from "@/server/ksm/modules/auth";
import { getMyBusinessActor } from "@/server/ksm/modules/actors";
import { logger } from "@/server/logger";
import type { AppSession, SessionUser, WorkspaceContext } from "@/lib/types/auth";

/**
 * Decision tree shape returned to the route handlers after a discover-contexts call.
 * Drives the UX: auto-select, redirect to select-context, or fail.
 */
export type DiscoverDecision =
  | { kind: "auto"; contextId: string; organizationId?: string }
  | { kind: "needsSelection"; selectionToken: string; contexts: DiscoveredContext[] }
  | { kind: "noAccess" };

export function decideAfterDiscover(
  selectionToken: string,
  contexts: DiscoveredContext[],
): DiscoverDecision {
  if (contexts.length === 0) {
    return { kind: "noAccess" };
  }
  if (contexts.length === 1) {
    const ctx = contexts[0]!;
    if (ctx.organizations.length <= 1) {
      return {
        kind: "auto",
        contextId: ctx.contextId,
        organizationId: ctx.organizations[0]?.organizationId,
      };
    }
  }
  return { kind: "needsSelection", selectionToken, contexts };
}

export function pickPrimaryOrganization(
  organizations: UserOrganizationAccess[],
): UserOrganizationAccess | undefined {
  return organizations[0];
}

export function buildSessionFromContextual(
  contextual: ContextualLoginResponse,
): AppSession {
  return buildSession(
    contextual.session,
    contextual.selectedTenantId,
    contextual.selectedOrganizationId ?? undefined,
  );
}

export function buildSession(
  login: LoginResponse,
  tenantIdOverride?: string,
  organizationIdOverride?: string,
): AppSession {
  const tenantId = tenantIdOverride ?? login.tenantId;
  const primaryOrg =
    pickPrimaryOrganization(login.organizations ?? []) ?? null;
  const organizationId = organizationIdOverride ?? primaryOrg?.organizationId;
  const workspace: WorkspaceContext | undefined = organizationId
    ? {
        tenantId,
        organizationId,
        organizationName: primaryOrg?.organizationName,
        agencyId: primaryOrg?.agencyId,
        agencyName: primaryOrg?.agencyName,
      }
    : undefined;

  const user: SessionUser = {
    userId: login.id,
    actorId: login.actorId,
    tenantId,
    email: login.email,
    fullName: deriveFullName(login),
    permissions: Array.from(new Set(login.authorities ?? [])),
    roles: deriveRoles(primaryOrg),
  };

  const expiresAt =
    Math.floor(Date.now() / 1000) + Math.max(60, login.expiresInSeconds || 3600);

  return {
    accessToken: login.accessToken,
    expiresAt,
    user,
    workspace,
    forcePasswordChange: login.forcePasswordChange === true,
    csrfToken: cryptoRandom(),
  };
}

function deriveFullName(login: LoginResponse): string {
  if (login.username && /\s/.test(login.username)) return login.username;
  if (login.email) {
    const local = login.email.split("@")[0] ?? login.email;
    return local.split(/[._-]/).map(capitalize).join(" ");
  }
  return login.username ?? "User";
}

function deriveRoles(org?: UserOrganizationAccess | null): string[] {
  if (!org) return [];
  return org.roleCodes ?? [];
}

function capitalize(s: string): string {
  return s.length === 0 ? s : s[0]!.toUpperCase() + s.slice(1);
}

function cryptoRandom(): string {
  // Edge-runtime safe random
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return (crypto as Crypto).randomUUID();
  }
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

/**
 * Best-effort: fetch the actor's name from actor-core and populate
 * firstName / lastName / fullName on the session user. This makes
 * greetings like "Bonjour, Jordan" work with the real name instead of
 * a derivation from the email address.
 */
export async function enrichSessionWithActorName(session: AppSession): Promise<void> {
  try {
    const actor = await getMyBusinessActor(session);
    if (actor?.name) {
      const parts = actor.name.trim().split(/\s+/);
      session.user.firstName = parts[0] ?? actor.name;
      session.user.lastName = parts.slice(1).join(" ") || undefined;
      session.user.fullName = actor.name;
    }
  } catch {
    // Non-critical — keep the email-derived name.
  }
}

export function logAuthEvent(event: string, fields: Record<string, unknown>): void {
  logger.info({ event, ...fields }, `auth.${event}`);
}
