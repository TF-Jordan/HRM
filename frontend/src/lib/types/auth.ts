/** Application session stored in the httpOnly cookie. */
export type AppSession = {
  accessToken: string;
  refreshToken?: string;
  expiresAt: number; // epoch seconds
  user: SessionUser;
  workspace?: WorkspaceContext;
  forcePasswordChange?: boolean;
  csrfToken?: string;
  /**
   * Set once the HRM SuperAdmin bootstrap has run for this session (default
   * role templates provisioned + SUPER_ADMIN self-assigned to a tenant admin
   * reaching HRM for the first time). Prevents re-running the orchestration on
   * every navigation within the same session. See
   * `server/orchestration/bootstrap-hrm-superadmin.ts`.
   */
  hrmBootstrapped?: boolean;
  /**
   * True when the bootstrap just self-assigned the SUPER_ADMIN role: the current
   * token predates the assignment, so the new HRM permissions only apply after a
   * fresh login. The app shell surfaces a "please reconnect" banner until then.
   */
  hrmNeedsReconnect?: boolean;
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
