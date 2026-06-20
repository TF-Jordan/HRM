import "server-only";

import type { NextRequest } from "next/server";

import { ROLE_CODE_TO_SLUG } from "@/lib/roles";
import { fail, ok } from "@/server/api-response";
import { EXCLUSIVE_FUNCTION_CODES, holdersByRole } from "@/server/admin/exclusive-roles";
import { requirePermissionRoute } from "@/server/handlers";
import * as adminApi from "@/server/ksm/modules/admin";
import type { AdministrationRole } from "@/server/ksm/modules/admin";

export async function GET() {
  return requirePermissionRoute(
    ["administration:roles:read", "administration:roles:write"],
    async (session) => {
      const [roles, users] = await Promise.all([
        adminApi.listRoles(session),
        adminApi.listTenantUsers(session),
      ]);

      const functionRoles = roles.filter((r) => EXCLUSIVE_FUNCTION_CODES.includes(r.code));
      const roleIds = new Set(functionRoles.map((r) => r.id));
      const holders = await holdersByRole(users, roleIds, session);

      const functions = functionRoles
        .map((r) => ({
          code: r.code,
          roleId: r.id,
          roleName: r.name,
          scopeType: r.scopeType,
          slug: ROLE_CODE_TO_SLUG[r.code] ?? null,
          permissionCount: r.permissions.length,
          holders: holders.get(r.id) ?? [],
        }))
        .sort(
          (a, b) =>
            EXCLUSIVE_FUNCTION_CODES.indexOf(a.code) - EXCLUSIVE_FUNCTION_CODES.indexOf(b.code),
        );

      const accounts = users
        .map((u) => ({
          id: u.id,
          username: u.username,
          email: u.email,
          status: u.status,
        }))
        .sort((a, b) => a.username.localeCompare(b.username));

      return ok({ functions, accounts });
    },
  );
}

/**
 * Assign (or vacate) a management function. Enforces the single-holder rule:
 * the role is first revoked from every current holder, then granted to the
 * target account. Pass `userId: null` to leave the function unassigned.
 */
export async function POST(request: NextRequest) {
  return requirePermissionRoute("administration:roles:write", async (session) => {
    const body = (await request.json()) as {
      roleId?: string;
      userId?: string | null;
    };
    const roleId = body.roleId;
    const userId = body.userId ?? null;

    if (!roleId) {
      return fail(400, "BAD_REQUEST", "roleId is required");
    }

    const [roles, users] = await Promise.all([
      adminApi.listRoles(session),
      adminApi.listTenantUsers(session),
    ]);
    const role: AdministrationRole | undefined = roles.find((r) => r.id === roleId);
    if (!role) {
      return fail(404, "NOT_FOUND", "Unknown role");
    }

    const holders = (await holdersByRole(users, new Set([roleId]), session)).get(roleId) ?? [];

    // Already assigned to the requested account → nothing to do.
    const already = userId ? holders.find((h) => h.userId === userId) : undefined;

    // Revoke from every current holder except the target (single-holder rule).
    await Promise.all(
      holders
        .filter((h) => h.userId !== userId)
        .map((h) => adminApi.revokeRole(h.userId, h.assignmentId, session)),
    );

    if (userId && !already) {
      await adminApi.assignRole(
        userId,
        { roleId, scope: role.scopeType, scopeType: role.scopeType },
        session,
      );
    }

    return ok({ ok: true });
  });
}
