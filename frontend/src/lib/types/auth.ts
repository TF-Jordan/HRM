/** Application session stored in the httpOnly cookie. */
export type AppSession = {
  accessToken: string;
  refreshToken?: string;
  expiresAt: number; // epoch seconds
  user: SessionUser;
  workspace?: WorkspaceContext;
  forcePasswordChange?: boolean;
  csrfToken?: string;
};

export type SessionUser = {
  userId: string;
  actorId: string;
  tenantId: string;
  email: string;
  firstName?: string;
  lastName?: string;
  fullName: string;
  permissions: string[];
  roles: string[];
};

export type WorkspaceContext = {
  tenantId: string;
  tenantName?: string;
  organizationId: string;
  organizationName?: string;
  agencyId?: string;
  agencyName?: string;
};
