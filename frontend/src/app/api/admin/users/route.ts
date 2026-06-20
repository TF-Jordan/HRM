import "server-only";

import type { NextRequest } from "next/server";

import { EXCLUSIVE_FUNCTION_CODES, holdersByRole } from "@/server/admin/exclusive-roles";
import { requirePermissionRoute } from "@/server/handlers";
import * as adminApi from "@/server/ksm/modules/admin";
import { createUserOrchestrated, type CreateUserInput } from "@/server/orchestration/create-user";

export async function GET() {
  return requirePermissionRoute(
    ["administration:roles:read", "administration:roles:write"],
    async (session) => {
      const data = await adminApi.listTenantUsers(session);
      return Response.json({ ok: true, data });
    },
  );
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("tenant:admin", async (session) => {
    const body = (await request.json()) as CreateUserInput;
    if (!body.firstName || !body.lastName || !body.email) {
      return Response.json(
        { ok: false, status: 400, errorCode: "VALIDATION_ERROR", message: "firstName, lastName and email are required" },
        { status: 400 },
      );
    }

    // Single-holder rule: a function role (DRH, payroll manager, …) may be held
    // by exactly one account per tenant. Reject before creating anything if any
    // requested role is an exclusive function already held by someone — the same
    // rule the role-assignments screen enforces. Plain EMPLOYEE is unbounded.
    const requestedRoleIds = new Set((body.assignments ?? []).map((a) => a.roleId));
    if (requestedRoleIds.size > 0) {
      const [roles, users] = await Promise.all([
        adminApi.listRoles(session),
        adminApi.listTenantUsers(session),
      ]);
      const exclusiveRequested = roles.filter(
        (r) => requestedRoleIds.has(r.id) && EXCLUSIVE_FUNCTION_CODES.includes(r.code),
      );
      if (exclusiveRequested.length > 0) {
        const holders = await holdersByRole(
          users,
          new Set(exclusiveRequested.map((r) => r.id)),
          session,
        );
        const conflict = exclusiveRequested.find((r) => (holders.get(r.id)?.length ?? 0) > 0);
        if (conflict) {
          const holder = holders.get(conflict.id)![0]!;
          return Response.json(
            {
              ok: false,
              status: 409,
              errorCode: "ROLE_ALREADY_ASSIGNED",
              message: `The "${conflict.name}" function is already held by ${holder.username}. Vacate it first, or assign a different role.`,
              details: { roleId: conflict.id, roleCode: conflict.code, holder },
            },
            { status: 409 },
          );
        }
      }
    }

    const result = await createUserOrchestrated(body, session);
    return Response.json({ ok: true, data: result }, { status: 201 });
  });
}
