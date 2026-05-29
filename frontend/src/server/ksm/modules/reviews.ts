import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type ReviewStatus = "DRAFT" | "SUBMITTED" | "ACKNOWLEDGED" | "FINALIZED";

export type ReviewResponse = {
  id: string;
  organizationId: string;
  employeeId: string;
  evaluateurPartyId: string | null;
  evaluateurDisplayName: string | null;
  periode: string;
  noteGlobale: number | string | null;
  commentaires: string | null;
  planAction: string | null;
  status: ReviewStatus;
};

export type ObjectiveResponse = {
  id: string;
  reviewId: string;
  description: string;
  poids: number | string | null;
  noteAtteinte: number | string | null;
  commentaire: string | null;
};

export type CreateReviewRequest = {
  employeeId: string;
  evaluateurPartyId?: string | null;
  evaluateurDisplayName?: string | null;
  periode: string;
};

export type SubmitReviewRequest = {
  noteGlobale: number | string;
  commentaires?: string | null;
  planAction?: string | null;
};

export type AddObjectiveRequest = { description: string; poids?: number | string | null };
export type EvaluateObjectiveRequest = { noteAtteinte: number | string; commentaire?: string | null };

export function listReviewsByEmployee(employeeId: string, session: AppSession) {
  return callKsm<ReviewResponse[]>(
    `/api/v1/hrm/reviews/employee/${employeeId}`,
    {},
    { session },
  );
}

export function listReviewsByOrgAndPeriode(
  session: AppSession,
  periode: string,
  organizationId?: string,
) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId, periode });
  return callKsm<ReviewResponse[]>(`/api/v1/hrm/reviews?${params}`, {}, { session });
}

export function getReview(id: string, session: AppSession) {
  return callKsm<ReviewResponse>(`/api/v1/hrm/reviews/${id}`, {}, { session });
}

export function createReview(body: CreateReviewRequest, session: AppSession) {
  return callKsm<ReviewResponse>(
    "/api/v1/hrm/reviews",
    { method: "POST", body },
    { session },
  );
}

export function submitReview(id: string, body: SubmitReviewRequest, session: AppSession) {
  return callKsm<ReviewResponse>(
    `/api/v1/hrm/reviews/${id}/submit`,
    { method: "PUT", body },
    { session },
  );
}

export function acknowledgeReview(id: string, session: AppSession) {
  return callKsm<ReviewResponse>(
    `/api/v1/hrm/reviews/${id}/acknowledge`,
    { method: "PUT" },
    { session },
  );
}

export function finalizeReview(id: string, session: AppSession) {
  return callKsm<ReviewResponse>(
    `/api/v1/hrm/reviews/${id}/finalize`,
    { method: "PUT" },
    { session },
  );
}

export function listObjectives(id: string, session: AppSession) {
  return callKsm<ObjectiveResponse[]>(
    `/api/v1/hrm/reviews/${id}/objectives`,
    {},
    { session },
  );
}

export function addObjective(id: string, body: AddObjectiveRequest, session: AppSession) {
  return callKsm<ObjectiveResponse>(
    `/api/v1/hrm/reviews/${id}/objectives`,
    { method: "POST", body },
    { session },
  );
}

export function evaluateObjective(
  objectiveId: string,
  body: EvaluateObjectiveRequest,
  session: AppSession,
) {
  return callKsm<ObjectiveResponse>(
    `/api/v1/hrm/reviews/objectives/${objectiveId}/evaluate`,
    { method: "PUT", body },
    { session },
  );
}
