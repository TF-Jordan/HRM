import "server-only";

import { callKsm } from "../client";

export type KsmCreateActorRequest = {
  organizationId?: string | null;
  firstName: string;
  lastName: string;
  name?: string | null;
  email?: string | null;
  phoneNumber?: string | null;
  description?: string | null;
  type?: string | null;
  gender?: string | null;
  photoUri?: string | null;
  photoId?: string | null;
  nationality?: string | null;
  birthDate?: string | null;
  profession?: string | null;
  biography?: string | null;
  addresses?: unknown[] | null;
  contacts?: unknown[] | null;
};

export type KsmActorResponse = {
  id: string;
  tenantId: string;
  firstName: string;
  lastName: string;
  email: string | null;
  phoneNumber: string | null;
};

export async function ksmCreateActor(
  input: KsmCreateActorRequest,
  ctx: { tenantId: string; bearer: string },
): Promise<KsmActorResponse> {
  return callKsm<KsmActorResponse>("/api/actors", {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
  });
}

export async function ksmUpdateMyPhoto(
  photoId: string | null,
  ctx: { tenantId: string; organizationId: string; bearer: string },
): Promise<KsmActorResponse> {
  return callKsm<KsmActorResponse>("/api/actors/me/photo", {
    method: "PUT",
    body: { photoId },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmChangeMyPassword(
  oldPassword: string,
  newPassword: string,
  ctx: { tenantId: string; organizationId: string; bearer: string },
): Promise<unknown> {
  return callKsm<unknown>("/api/users/me/change-password", {
    method: "POST",
    body: { oldPassword, newPassword },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
