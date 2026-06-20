import "server-only";

import type { AppSession } from "@/lib/types/auth";
import { logger } from "@/server/logger";
import * as adminApi from "@/server/ksm/modules/admin";

/**
 * First-access bootstrap that turns the organisation OWNER into the HRM
 * SuperAdmin of their tenant — without any separate account.
 *
 * Context (see `PLAN-SUPERADMIN-ONBOARDING.md`): the OWNER signs up, subscribes
 * to HRM and logs into the HRM frontend with their *global* credentials. They
 * already carry `tenant:admin` (so they land in the `admin` space and may call
 * `/api/auth/register` + assign roles). Two things are still missing on a brand
 * new tenant:
 *
 *   1. The HRM role *templates* (HR_ADMIN, PAYROLL_MANAGER, EMPLOYEE, …) do not
 *      exist yet, so the SuperAdmin has nothing to assign when creating a DRH or
 *      an employee. → `POST /api/administration/roles/defaults` (idempotent).
 *      This is the critical step and needs **no re-login**: creating other users
 *      only requires `tenant:admin` + `administration:assignments:write`, never
 *      `hrm:*`.
 *
 *   2. The OWNER may lack `hrm:*` permissions for their own use of the HRM
 *      screens. → self-assign the `SUPER_ADMIN` role. This only takes effect on
 *      the **next login** (KSM resolves permissions at token issuance), so we
 *      surface `needsReconnect` rather than forcing a re-login.
 *
 * Best-effort: never throws. A failure here must not break login — the caller
 * logs the warning and proceeds.
 */

/** Role codes whose presence means the tenant is already HRM-bootstrapped. */
const HRM_PRIVILEGED_ROLE_CODES = ["SUPER_ADMIN", "HR_ADMIN", "HR_MANAGER"] as const;

/** Permission proving the caller already holds the HRM perimeter (no bootstrap needed). */
const HRM_MARKER_PERMISSION = "hrm:employee:create";

/** Tenant-admin permissions that authorise the bootstrap (any one suffices). */
const TENANT_ADMIN_PERMISSIONS = ["tenant:admin", "system:admin", "iam:admin"];

export type BootstrapResult = {
  /** True when the orchestration performed at least one write. */
  ran: boolean;
  /** Default role templates were (re)provisioned. */
  rolesProvisioned: boolean;
  /** SUPER_ADMIN was assigned to the caller during this run. */
  assignedSuperAdmin: boolean;
  /** The caller must log in again for the new SUPER_ADMIN perms to apply. */
  needsReconnect: boolean;
  /** Reason the orchestration was skipped, when `ran` is false. */
  skippedReason?: "not-admin" | "no-organization" | "already-bootstrapped" | "already-session";
  /** Non-fatal issues (e.g. a missing template) worth surfacing. */
  warnings: string[];
};

function strip(permission: string): string {
  return permission.split("#")[0] ?? permission;
}

function ownedPermissions(session: AppSession): Set<string> {
  return new Set((session.user.permissions ?? []).map(strip));
}

/** True if the session already proves a finished HRM bootstrap. */
function alreadyHasHrmAdmin(session: AppSession): boolean {
  const codes = new Set(session.user.roles ?? []);
  if (HRM_PRIVILEGED_ROLE_CODES.some((code) => codes.has(code))) return true;
  return ownedPermissions(session).has(HRM_MARKER_PERMISSION);
}

function isTenantAdmin(session: AppSession): boolean {
  const owned = ownedPermissions(session);
  return TENANT_ADMIN_PERMISSIONS.some((p) => owned.has(p));
}

/** Build the assignment scope binding for a role of the given scope type. */
function scopeBinding(
  scopeType: "SYSTEM" | "TENANT" | "ORGANIZATION" | "AGENCY",
  organizationId: string | undefined,
): { scope: string; scopeId: string | null } {
  if (scopeType === "ORGANIZATION" && organizationId) {
    return { scope: `ORGANIZATION:${organizationId}`, scopeId: organizationId };
  }
  if (scopeType === "TENANT") {
    return { scope: "TENANT", scopeId: null };
  }
  if (scopeType === "SYSTEM") {
    return { scope: "SYSTEM", scopeId: null };
  }
  // AGENCY would need an agency id we don't have here — fall back to tenant.
  return { scope: "TENANT", scopeId: null };
}

const SKIP = (
  skippedReason: NonNullable<BootstrapResult["skippedReason"]>,
): BootstrapResult => ({
  ran: false,
  rolesProvisioned: false,
  assignedSuperAdmin: false,
  needsReconnect: false,
  skippedReason,
  warnings: [],
});

/**
 * Idempotent. Mutates `session.hrmBootstrapped` on success so the caller can
 * persist the flag and skip re-running within the same session. Safe to call on
 * every login: it short-circuits with zero network calls once the tenant is
 * bootstrapped (the caller then carries a privileged HRM role code).
 */
export async function ensureHrmSuperAdmin(session: AppSession): Promise<BootstrapResult> {
  try {
    if (session.hrmBootstrapped) return SKIP("already-session");
    if (!isTenantAdmin(session)) return SKIP("not-admin");
    if (alreadyHasHrmAdmin(session)) return SKIP("already-bootstrapped");

    const organizationId = session.workspace?.organizationId;
    if (!organizationId) return SKIP("no-organization");

    const warnings: string[] = [];

    // 1. Ensure the HRM role templates exist in the tenant.
    let roles = await adminApi.listRoles(session);
    const templatesPresent =
      roles.some((r) => r.code === "SUPER_ADMIN") && roles.some((r) => r.code === "HR_ADMIN");
    let rolesProvisioned = false;
    if (!templatesPresent) {
      await adminApi.provisionDefaultRoles(session);
      roles = await adminApi.listRoles(session);
      rolesProvisioned = true;
      logger.info({ tenantId: session.user.tenantId }, "bootstrap.default_roles_provisioned");
    }

    // 2. Self-assign SUPER_ADMIN, but only if the caller lacks the HRM perimeter.
    let assignedSuperAdmin = false;
    let needsReconnect = false;
    if (!ownedPermissions(session).has(HRM_MARKER_PERMISSION)) {
      const superAdmin = roles.find((r) => r.code === "SUPER_ADMIN");
      if (!superAdmin) {
        warnings.push("SUPER_ADMIN template missing after provisioning — cannot self-assign.");
      } else {
        const existing = await adminApi.listUserRoles(session.user.userId, session);
        const alreadyAssigned = existing.some((a) => a.roleId === superAdmin.id);
        if (!alreadyAssigned) {
          const { scope, scopeId } = scopeBinding(superAdmin.scopeType, organizationId);
          await adminApi.assignRole(
            session.user.userId,
            { roleId: superAdmin.id, scope, scopeType: superAdmin.scopeType, scopeId },
            session,
          );
          assignedSuperAdmin = true;
          logger.info(
            { userId: session.user.userId, roleId: superAdmin.id },
            "bootstrap.super_admin_self_assigned",
          );
        }
        // The current token predates the assignment — perms apply next login.
        needsReconnect = true;
      }
    }

    session.hrmBootstrapped = true;
    // Surface a reconnect banner only when the caller's token actually lacks the
    // freshly granted HRM perms (i.e. we self-assigned SUPER_ADMIN this run).
    if (needsReconnect) session.hrmNeedsReconnect = true;
    return {
      ran: rolesProvisioned || assignedSuperAdmin,
      rolesProvisioned,
      assignedSuperAdmin,
      needsReconnect,
      warnings,
    };
  } catch (cause) {
    // Never break login on a bootstrap failure.
    logger.error(
      { tenantId: session.user.tenantId, userId: session.user.userId, cause: String(cause) },
      "bootstrap.failed",
    );
    return {
      ran: false,
      rolesProvisioned: false,
      assignedSuperAdmin: false,
      needsReconnect: false,
      warnings: [`Bootstrap failed: ${(cause as Error).message}`],
    };
  }
}
