import "server-only";

import { callKsm } from "../client";
import type {
  AdminCreateUserInput,
  AdminCreateUserResponse,
  AssignRoleInput,
  CreateRoleInput,
  Role,
  UpdateRoleInput,
  UserRoleAssignment,
  UserSummary,
} from "@/lib/types/admin";

type KsmCtx = { tenantId: string; organizationId: string; agencyId?: string | null; bearer: string };

// ----- Roles -----

export async function ksmListRoles(ctx: KsmCtx): Promise<Role[]> {
  return callKsm<Role[]>(`/api/roles`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmGetRole(id: string, ctx: KsmCtx): Promise<Role> {
  return callKsm<Role>(`/api/roles/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateRole(input: CreateRoleInput, ctx: KsmCtx): Promise<Role> {
  return callKsm<Role>(`/api/roles`, {
    method: "POST",
    body: { ...input, scopeType: input.scopeType ?? "TENANT" },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmUpdateRole(id: string, input: UpdateRoleInput, ctx: KsmCtx): Promise<Role> {
  return callKsm<Role>(`/api/roles/${id}`, {
    method: "PUT",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmDeleteRole(id: string, ctx: KsmCtx): Promise<void> {
  await callKsm<void>(`/api/roles/${id}`, {
    method: "DELETE",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

// ----- Assignments -----

export async function ksmAssignRole(input: AssignRoleInput, ctx: KsmCtx): Promise<UserRoleAssignment> {
  return callKsm<UserRoleAssignment>(`/api/roles/assignments`, {
    method: "POST",
    body: { ...input, scope: input.scope ?? "TENANT" },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListAssignmentsByUser(
  userId: string,
  ctx: KsmCtx,
): Promise<UserRoleAssignment[]> {
  return callKsm<UserRoleAssignment[]>(`/api/roles/users/${userId}/assignments`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListAssignmentsByRole(
  roleId: string,
  ctx: KsmCtx,
): Promise<UserRoleAssignment[]> {
  return callKsm<UserRoleAssignment[]>(`/api/roles/${roleId}/assignments`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmRevokeAssignment(id: string, ctx: KsmCtx): Promise<void> {
  await callKsm<void>(`/api/roles/assignments/${id}`, {
    method: "DELETE",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

// ----- Users -----

export async function ksmListUsers(ctx: KsmCtx): Promise<UserSummary[]> {
  return callKsm<UserSummary[]>(`/api/users`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmAdminCreateUser(
  input: AdminCreateUserInput,
  ctx: KsmCtx,
): Promise<AdminCreateUserResponse> {
  return callKsm<AdminCreateUserResponse>(`/api/users`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
