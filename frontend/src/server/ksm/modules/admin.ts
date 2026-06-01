import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type AdministrationUser = {
  id: string;
  tenantId: string;
  actorId: string;
  username: string;
  email: string;
  phoneNumber?: string | null;
  status: string;
  plan: string;
  onboardingStatus: string;
  onboardingStep: number;
  emailVerified: boolean;
  phoneVerified: boolean;
  mfaEnabled: boolean;
  forcePasswordChange: boolean;
  createdAt: string;
  updatedAt: string;
};

export type AdministrationRole = {
  id: string;
  tenantId: string;
  code: string;
  name: string;
  scopeType: "SYSTEM" | "TENANT" | "ORGANIZATION" | "AGENCY";
  permissions: string[];
};

export type AdministrationRoleTemplate = {
  code: string;
  name: string;
  scopeType: string;
  permissions: string[];
  protected: boolean;
};

export type AdministrationPermission = {
  code: string;
  name: string;
  description?: string;
  module: string;
  scope: string;
  system: boolean;
  assignable: boolean;
  deprecated: boolean;
};

export type AdministrationUserRoleAssignment = {
  id: string;
  tenantId: string;
  userId: string;
  roleId: string;
  scopeType: string;
  scopeId?: string | null;
  scope: string;
};

export type AdministrationAudit = {
  id: string;
  tenantId: string;
  organizationId?: string | null;
  actorUserId?: string | null;
  action: string;
  targetType: string;
  targetId?: string | null;
  payloadSummary?: string | null;
  occurredAt: string;
};

export function listTenantUsers(session: AppSession) {
  return callKsm<AdministrationUser[]>("/api/administration/users", {}, { session });
}

export function listRoles(session: AppSession) {
  return callKsm<AdministrationRole[]>("/api/administration/roles", {}, { session });
}

export function listRoleTemplates(session: AppSession) {
  return callKsm<AdministrationRoleTemplate[]>(
    "/api/administration/role-templates",
    {},
    { session },
  );
}

export function provisionDefaultRoles(session: AppSession) {
  return callKsm<AdministrationRole[]>(
    "/api/administration/roles/defaults",
    { method: "POST", body: {} },
    { session },
  );
}

export function listUserRoles(userId: string, session: AppSession) {
  return callKsm<AdministrationUserRoleAssignment[]>(
    `/api/administration/users/${userId}/roles`,
    {},
    { session },
  );
}

export function assignRole(
  userId: string,
  body: { roleId: string; scope: string; scopeType?: string; scopeId?: string | null },
  session: AppSession,
) {
  return callKsm<AdministrationUserRoleAssignment>(
    `/api/administration/users/${userId}/roles`,
    { method: "POST", body },
    { session },
  );
}

export function revokeRole(userId: string, assignmentId: string, session: AppSession) {
  return callKsm<void>(
    `/api/administration/users/${userId}/roles/${assignmentId}`,
    { method: "DELETE" },
    { session },
  );
}

export function listPermissions(session: AppSession) {
  return callKsm<AdministrationPermission[]>(
    "/api/administration/permissions",
    {},
    { session },
  );
}

export function listAudit(session: AppSession, limit = 50) {
  return callKsm<AdministrationAudit[]>(
    `/api/administration/audit?limit=${limit}`,
    {},
    { session },
  );
}
