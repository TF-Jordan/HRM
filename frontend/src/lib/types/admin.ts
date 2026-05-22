export type RoleScopeType = "SYSTEM" | "TENANT" | "ORGANIZATION" | "AGENCY";

export type Role = {
  id: string;
  tenantId: string;
  code: string;
  name: string;
  scopeType: RoleScopeType;
  permissions: string[];
};

export type CreateRoleInput = {
  code: string;
  name: string;
  scopeType?: RoleScopeType;
  permissions: string[];
};

export type UpdateRoleInput = {
  name?: string;
  permissions?: string[];
};

export type UserRoleAssignment = {
  id: string;
  userId: string;
  roleId: string;
  scopeType: RoleScopeType;
  scopeId: string | null;
  scope: string;
};

export type AssignRoleInput = {
  userId: string;
  roleId: string;
  scopeType?: RoleScopeType;
  scopeId?: string | null;
  scope?: string;
};

export type UserSummary = {
  id: string;
  actorId: string;
  username: string;
  email: string;
  phoneNumber: string | null;
  status: string;
  createdAt: string;
};

export type AdminCreateUserInput = {
  actorId: string;
  username: string;
  email: string;
  phoneNumber?: string | null;
  password?: string | null;
  sendWelcomeEmail?: boolean;
};

export type AdminCreateUserResponse = {
  id: string;
  actorId: string;
  username: string;
  email: string;
  status: string;
  temporaryPassword: string;
  emailSent: boolean;
};
