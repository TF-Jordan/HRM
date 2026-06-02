/**
 * Role registry — single source of truth for the role-partitioned frontend.
 *
 * Business rule (per tenant): exactly ONE account per privileged role, and many
 * EMPLOYEE accounts. A user therefore carries a single role, which becomes the
 * prefix of every page they can reach (e.g. `/employee/leaves`, `/manager/...`).
 *
 * KSM emits role *codes* (e.g. `HR_DIRECTOR`). The frontend maps each code to a
 * URL *slug* that names the role's dedicated route tree under `(app)`.
 *
 * This module is intentionally free of React/icons so it can be imported from
 * both server components (layout redirects) and client components (AppLink).
 */

export type RoleSlug =
  | "admin"
  | "hr-admin"
  | "drh"
  | "payroll-manager"
  | "manager"
  | "employee"
  | "recruiter"
  | "doctor"
  | "controller"
  | "accountant";

/** KSM role code → frontend URL slug. */
export const ROLE_CODE_TO_SLUG: Record<string, RoleSlug> = {
  SUPER_ADMIN: "admin",
  ORGANIZATION_ADMIN: "admin",
  HR_ADMIN: "hr-admin",
  HR_MANAGER: "hr-admin",
  HR_DIRECTOR: "drh",
  PAYROLL_MANAGER: "payroll-manager",
  MANAGER: "manager",
  EMPLOYEE: "employee",
  RECRUITER: "recruiter",
  OCCUPATIONAL_DOCTOR: "doctor",
  HR_CONTROLLER: "controller",
  ACCOUNTANT: "accountant",
};

/**
 * Roles whose dedicated `/{slug}/…` route tree has been built. Until a role is
 * listed here, AppLink/useAppRouter stay no-ops for it and its users keep the
 * legacy flat routes — so the migration can roll out one role at a time without
 * breaking the others.
 */
export const MIGRATED_ROLES: ReadonlySet<RoleSlug> = new Set<RoleSlug>([
  "employee",
  "manager",
  "hr-admin",
  "drh",
  "payroll-manager",
  "recruiter",
  "doctor",
  "controller",
]);

/** Least-privilege fallback when a session carries no recognised role. */
export const DEFAULT_ROLE_SLUG: RoleSlug = "employee";

/**
 * Resolve the active role slug from a list of KSM role codes. With one role per
 * account this is effectively `roles[0]`, but we scan defensively and fall back
 * to the least-privileged view.
 */
export function roleSlug(roles: readonly string[] | undefined | null): RoleSlug {
  for (const code of roles ?? []) {
    const slug = ROLE_CODE_TO_SLUG[code];
    if (slug) return slug;
  }
  return DEFAULT_ROLE_SLUG;
}

/** True once the role's dedicated route tree exists. */
export function isMigratedRole(slug: RoleSlug): boolean {
  return MIGRATED_ROLES.has(slug);
}

/** The landing path for a role (its dashboard inside its own namespace). */
export function roleHomePath(roles: readonly string[] | undefined | null): string {
  const slug = roleSlug(roles);
  return isMigratedRole(slug) ? `/${slug}/dashboard` : "/dashboard";
}
