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
  "admin",
]);

/** Least-privilege fallback when a session carries no recognised role. */
export const DEFAULT_ROLE_SLUG: RoleSlug = "employee";

/**
 * Resolve the active role slug from a list of KSM role codes. With one role per
 * account this is effectively `roles[0]`, but we scan defensively.
 *
 * If `roles` is empty (KSM's `discover-contexts` currently fails to populate
 * `UserOrganizationAccess.roleCodes` for users whose only org membership comes
 * from a `roles_core.user_role_assignment` — i.e. scoped-only users without a
 * business_actor_profile in the org), we infer the slug from the permissions
 * granted to the session. Each role template carries a permission set with
 * distinctive markers, listed below in *decreasing privilege* order so a user
 * with two overlapping role patterns is mapped to the more privileged one.
 *
 * Falls back to the least-privileged view (`employee`) when nothing matches.
 */
export function roleSlug(
  roles: readonly string[] | undefined | null,
  permissions?: readonly string[] | undefined | null,
): RoleSlug {
  for (const code of roles ?? []) {
    const slug = ROLE_CODE_TO_SLUG[code];
    if (slug) return slug;
  }
  return inferRoleSlugFromPermissions(permissions);
}

/**
 * Heuristic role inference based on the permission set. Each `match` is an
 * AND-list — every permission must be present (after stripping the scope
 * suffix) for the slug to apply. Ordered most-privileged → least-privileged.
 *
 * Exported for tests; production code goes through `roleSlug()`.
 */
export function inferRoleSlugFromPermissions(
  permissions: readonly string[] | undefined | null,
): RoleSlug {
  if (!permissions || permissions.length === 0) return DEFAULT_ROLE_SLUG;
  const owned = new Set(permissions.map((p) => p.split("#")[0] ?? p));
  const has = (...perms: string[]): boolean => perms.every((p) => owned.has(p));

  // SUPER_ADMIN / ORGANIZATION_ADMIN — only roles holding `tenant:admin`.
  if (has("tenant:admin")) return "admin";
  // HR_ADMIN / HR_MANAGER — full HRM perimeter incl. payroll run + employee create.
  if (has("hrm:payroll:run", "hrm:employee:create", "hrm:declaration:create")) return "hr-admin";
  // PAYROLL_MANAGER — payroll run/validate without employee/declaration create.
  if (has("hrm:payroll:run", "hrm:payroll:validate")) return "payroll-manager";
  // HR_DIRECTOR (DRH) — training+budget governance without payroll:run.
  if (has("hrm:training:manage", "hrm:budget:manage", "hrm:review:manage")) return "drh";
  // RECRUITER — recruitment + onboarding manage + can create employees.
  if (has("hrm:recruitment:manage", "hrm:onboarding:manage", "hrm:employee:create")) return "recruiter";
  // OCCUPATIONAL_DOCTOR — medical-only.
  if (has("hrm:medical:create", "hrm:medical:read") && !has("hrm:leave:read")) return "doctor";
  // ACCOUNTANT — accounting permissions (no HRM management).
  if (has("accounting:read", "accounting:write")) return "accountant";
  // HR_CONTROLLER — broad HRM reads + KPI create, no manage.
  if (has("hrm:kpi:create", "hrm:declaration:read", "hrm:payroll:read") &&
      !has("hrm:payroll:run") && !has("hrm:employee:create")) return "controller";
  // MANAGER — leave approval + timesheet validation + mission management.
  if (has("hrm:leave:approve", "hrm:timesheet:validate", "hrm:mission:manage")) return "manager";
  // Plain EMPLOYEE.
  return "employee";
}

/** True once the role's dedicated route tree exists. */
export function isMigratedRole(slug: RoleSlug): boolean {
  return MIGRATED_ROLES.has(slug);
}

/**
 * Privilege ordering (most → least). Drives the default landing workspace and
 * the order workspaces are listed in the switcher.
 */
const SLUG_PRIORITY: readonly RoleSlug[] = [
  "admin",
  "hr-admin",
  "drh",
  "payroll-manager",
  "controller",
  "recruiter",
  "doctor",
  "manager",
  "accountant",
  "employee",
];

/**
 * All workspaces a user may enter. A single account can hold several roles —
 * a company employee may be assigned a management function (Payroll Manager,
 * Recruiter, …) and then switches between their Employee self-service space and
 * that function's space.
 *
 * The Employee space is offered ONLY when the account is actually an employee
 * (it carries the EMPLOYEE role). Dedicated, non-employee accounts (e.g. the HR
 * administrator) therefore get just their own space and no switcher.
 *
 * Returned most-privileged first; plain employees get a single-entry list.
 */
export function entitledSlugs(
  roles: readonly string[] | undefined | null,
  permissions?: readonly string[] | undefined | null,
): RoleSlug[] {
  const fromRoles = new Set<RoleSlug>();
  for (const code of roles ?? []) {
    const slug = ROLE_CODE_TO_SLUG[code];
    if (slug) fromRoles.add(slug);
  }
  if (fromRoles.size === 0) fromRoles.add(inferRoleSlugFromPermissions(permissions));

  // Migrated, switchable privileged spaces the user holds (employee excluded).
  const privileged = SLUG_PRIORITY.filter(
    (s) => s !== "employee" && fromRoles.has(s) && isMigratedRole(s),
  );
  // The employee self-service space only when the account is an employee.
  const result = [...privileged];
  if (fromRoles.has("employee")) result.push("employee");
  if (result.length > 0) return result;

  // Non-migrated single role (e.g. accountant) keeps its legacy flat experience.
  return SLUG_PRIORITY.filter((s) => fromRoles.has(s));
}

/** The default (highest-privilege) workspace slug for a user. */
export function defaultSlug(
  roles: readonly string[] | undefined | null,
  permissions?: readonly string[] | undefined | null,
): RoleSlug {
  return entitledSlugs(roles, permissions)[0] ?? DEFAULT_ROLE_SLUG;
}

/**
 * The active workspace, derived from the current (locale-stripped) pathname's
 * first segment, bounded to the user's entitlements. Falls back to the default
 * workspace when the path isn't inside a known, entitled namespace.
 */
export function activeSlugFromPath(
  pathname: string,
  entitled: readonly RoleSlug[],
): RoleSlug {
  const seg = pathname.split("/").filter(Boolean)[0] as RoleSlug | undefined;
  if (seg && entitled.includes(seg)) return seg;
  return entitled[0] ?? DEFAULT_ROLE_SLUG;
}

/** The landing path for a user (their highest-privilege dashboard namespace). */
export function roleHomePath(
  roles: readonly string[] | undefined | null,
  permissions?: readonly string[] | undefined | null,
): string {
  const slug = defaultSlug(roles, permissions);
  return isMigratedRole(slug) ? `/${slug}/dashboard` : "/dashboard";
}
