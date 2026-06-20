import "server-only";

import type { AppSession } from "@/lib/types/auth";
import * as adminApi from "@/server/ksm/modules/admin";
import type { AdministrationUser } from "@/server/ksm/modules/admin";

/**
 * Management functions that may be held by exactly ONE account at a time
 * (business rule: a tenant has a single HR admin, payroll manager, recruiter, …).
 * Plain employees, team managers and the SUPER_ADMIN account itself are excluded
 * — they are not exclusive, tenant-wide functions.
 *
 * Single source of truth, shared by:
 *   - the role-assignments management screen (`/api/admin/role-assignments`)
 *   - the user-creation flow (`/api/admin/users`), which must refuse to assign a
 *     function role that is already held.
 */
export const EXCLUSIVE_FUNCTION_CODES: readonly string[] = [
  "HR_ADMIN",
  "HR_DIRECTOR",
  "PAYROLL_MANAGER",
  "RECRUITER",
  "HR_CONTROLLER",
  "OCCUPATIONAL_DOCTOR",
  "ACCOUNTANT",
];

export function isExclusiveFunctionCode(code: string): boolean {
  return EXCLUSIVE_FUNCTION_CODES.includes(code);
}

export type RoleHolder = {
  userId: string;
  username: string;
  email: string;
  assignmentId: string;
};

/**
 * Scan every tenant account for assignments of the given role ids. There is no
 * KSM endpoint to list holders by role, so we fan out over the user list. The
 * function set is small and held by few people, so the result map stays tiny.
 */
export async function holdersByRole(
  users: AdministrationUser[],
  roleIds: Set<string>,
  session: AppSession,
): Promise<Map<string, RoleHolder[]>> {
  const byRole = new Map<string, RoleHolder[]>();
  if (roleIds.size === 0) return byRole;
  const perUser = await Promise.all(
    users.map(async (u) => {
      try {
        const assignments = await adminApi.listUserRoles(u.id, session);
        return { user: u, assignments };
      } catch {
        return { user: u, assignments: [] };
      }
    }),
  );
  for (const { user, assignments } of perUser) {
    for (const a of assignments) {
      if (!roleIds.has(a.roleId)) continue;
      const list = byRole.get(a.roleId) ?? [];
      list.push({
        userId: user.id,
        username: user.username,
        email: user.email,
        assignmentId: a.id,
      });
      byRole.set(a.roleId, list);
    }
  }
  return byRole;
}
