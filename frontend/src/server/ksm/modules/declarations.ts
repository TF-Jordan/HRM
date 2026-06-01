import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type DeclarationType = "CNPS" | "DIPE" | "IRPP_CAC";
export type DeclarationStatus = "DRAFT" | "GENERATED" | "SUBMITTED" | "ACKNOWLEDGED";
export type DeclarationFormat = "PDF" | "CSV" | "XML";

export type SocialDeclarationResponse = {
  id: string;
  organizationId: string;
  type: DeclarationType;
  periode: string;
  format: DeclarationFormat | string;
  statut: DeclarationStatus;
  fichierId: string | null;
  generatedAt: string | null;
  submittedAt: string | null;
};

export type CreateSocialDeclarationRequest = {
  organizationId?: string;
  type: DeclarationType;
  periode: string;
  format: DeclarationFormat | string;
};

export type GenerateRequest = { fichierId: string };

/* -------- queries -------- */

export function listDeclarations(session: AppSession, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ orgId });
  return callKsm<SocialDeclarationResponse[]>(
    `/api/v1/hrm/declarations?${params}`,
    {},
    { session },
  );
}

export function getDeclaration(id: string, session: AppSession) {
  return callKsm<SocialDeclarationResponse>(
    `/api/v1/hrm/declarations/${id}`,
    {},
    { session },
  );
}

/* -------- mutations -------- */

export function createDeclaration(
  body: CreateSocialDeclarationRequest,
  session: AppSession,
) {
  const orgId = body.organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  return callKsm<SocialDeclarationResponse>(
    "/api/v1/hrm/declarations",
    { method: "POST", body: { ...body, organizationId: orgId } },
    { session },
  );
}

export function generateDeclaration(id: string, fichierId: string, session: AppSession) {
  return callKsm<SocialDeclarationResponse>(
    `/api/v1/hrm/declarations/${id}/generate`,
    { method: "PUT", body: { fichierId } },
    { session },
  );
}

export function submitDeclaration(id: string, session: AppSession) {
  return callKsm<SocialDeclarationResponse>(
    `/api/v1/hrm/declarations/${id}/submit`,
    { method: "PUT" },
    { session },
  );
}

export function acknowledgeDeclaration(id: string, session: AppSession) {
  return callKsm<SocialDeclarationResponse>(
    `/api/v1/hrm/declarations/${id}/acknowledge`,
    { method: "PUT" },
    { session },
  );
}
