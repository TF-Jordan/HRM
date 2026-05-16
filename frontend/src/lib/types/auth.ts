export type SessionUser = {
  userId: string;
  actorId: string;
  email: string;
  displayName: string;
  preferredLanguage?: string | null;
};

export type SessionContext = {
  tenantId: string;
  organizationId: string;
  agencyId?: string | null;
};

export type Session = {
  user: SessionUser;
  context: SessionContext;
  permissions: string[];
  accessToken: string;
  refreshToken?: string | null;
  expiresAt: number; // epoch ms
};

export type Workspace = {
  organizationId: string;
  organizationName?: string;
  agencyId?: string | null;
  agencyName?: string | null;
};

export type LoginChallenge =
  | { kind: "mfa"; mfaToken: string; channel: string }
  | { kind: "select-context"; contexts: AvailableContext[] }
  | { kind: "success"; session: Session };

export type AvailableContext = {
  contextId: string;
  tenantId: string;
  tenantName: string;
  organizationId: string;
  organizationName: string;
  agencyId?: string | null;
  agencyName?: string | null;
  roles: string[];
};
