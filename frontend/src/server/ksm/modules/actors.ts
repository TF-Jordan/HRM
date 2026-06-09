import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type ActorResponse = {
  id: string;
  tenantId: string;
  organizationId?: string | null;
  firstName: string;
  lastName: string;
  displayName?: string | null;
  phoneNumber?: string | null;
  email?: string | null;
  gender?: string | null;
  nationality?: string | null;
  birthDate?: string | null;
  profession?: string | null;
  photoUri?: string | null;
  photoId?: string | null;
};

export type CreateActorRequest = {
  organizationId?: string;
  firstName: string;
  lastName: string;
  name?: string;
  phoneNumber?: string;
  email?: string;
  gender?: string;
  nationality?: string;
  birthDate?: string;
  profession?: string;
};

/**
 * Create a new actor (identity). The endpoint is unauthenticated upstream
 * (it accepts X-Client-Id + X-Api-Key), but the BFF always forwards the
 * caller's tenant headers so the row lands in the right tenant.
 */
export function createActor(body: CreateActorRequest, session: AppSession) {
  return callKsm<ActorResponse>(
    "/api/actors",
    { method: "POST", body },
    { session },
  );
}

export type BusinessActorResponse = {
  id: string;
  tenantId: string;
  actorId: string;
  name?: string | null;
  code?: string | null;
  isIndividual: boolean;
};

/**
 * Fetch the current user's business actor profile.
 * The `name` field is typically "firstName lastName" (built by actor-core).
 */
export function getMyBusinessActor(session: AppSession) {
  return callKsm<BusinessActorResponse>(
    "/api/actors/me",
    { method: "GET" },
    { session },
  );
}
