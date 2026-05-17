import "server-only";

import { callKsm } from "../client";
import type { CreateDeclarationInput, SocialDeclaration } from "@/lib/types/hrm/declaration";

type KsmCtx = { tenantId: string; organizationId: string; bearer: string };

export async function ksmListDeclarations(ctx: KsmCtx): Promise<SocialDeclaration[]> {
  return callKsm<SocialDeclaration[]>(`/api/v1/hrm/declarations`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: { orgId: ctx.organizationId },
  });
}

export async function ksmGetDeclaration(id: string, ctx: KsmCtx): Promise<SocialDeclaration> {
  return callKsm<SocialDeclaration>(`/api/v1/hrm/declarations/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateDeclaration(
  input: CreateDeclarationInput,
  ctx: KsmCtx,
): Promise<SocialDeclaration> {
  return callKsm<SocialDeclaration>(`/api/v1/hrm/declarations`, {
    method: "POST",
    body: { organizationId: ctx.organizationId, ...input },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmGenerateDeclaration(
  id: string,
  fichierId: string | null,
  ctx: KsmCtx,
): Promise<SocialDeclaration> {
  return callKsm<SocialDeclaration>(`/api/v1/hrm/declarations/${id}/generate`, {
    method: "PUT",
    body: { fichierId },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmSubmitDeclaration(id: string, ctx: KsmCtx): Promise<SocialDeclaration> {
  return callKsm<SocialDeclaration>(`/api/v1/hrm/declarations/${id}/submit`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmAcknowledgeDeclaration(id: string, ctx: KsmCtx): Promise<SocialDeclaration> {
  return callKsm<SocialDeclaration>(`/api/v1/hrm/declarations/${id}/acknowledge`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
