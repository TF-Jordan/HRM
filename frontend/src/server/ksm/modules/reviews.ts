import "server-only";

import { callKsm } from "../client";
import type {
  AddObjectiveInput,
  CreateReviewInput,
  EvaluateObjectiveInput,
  Review,
  ReviewObjective,
  SubmitReviewInput,
} from "@/lib/types/hrm/review";

type KsmCtx = { tenantId: string; organizationId: string; bearer: string };

export async function ksmListReviews(periode: string | null, ctx: KsmCtx): Promise<Review[]> {
  return callKsm<Review[]>(`/api/v1/hrm/reviews`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: { organizationId: ctx.organizationId, periode: periode ?? undefined },
  });
}

export async function ksmGetReview(id: string, ctx: KsmCtx): Promise<Review> {
  return callKsm<Review>(`/api/v1/hrm/reviews/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListEmployeeReviews(employeeId: string, ctx: KsmCtx): Promise<Review[]> {
  return callKsm<Review[]>(`/api/v1/hrm/reviews/employee/${employeeId}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateReview(input: CreateReviewInput, ctx: KsmCtx): Promise<Review> {
  return callKsm<Review>(`/api/v1/hrm/reviews`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmSubmitReview(id: string, input: SubmitReviewInput, ctx: KsmCtx): Promise<Review> {
  return callKsm<Review>(`/api/v1/hrm/reviews/${id}/submit`, {
    method: "PUT",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmAcknowledgeReview(id: string, ctx: KsmCtx): Promise<Review> {
  return callKsm<Review>(`/api/v1/hrm/reviews/${id}/acknowledge`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmFinalizeReview(id: string, ctx: KsmCtx): Promise<Review> {
  return callKsm<Review>(`/api/v1/hrm/reviews/${id}/finalize`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListObjectives(reviewId: string, ctx: KsmCtx): Promise<ReviewObjective[]> {
  return callKsm<ReviewObjective[]>(`/api/v1/hrm/reviews/${reviewId}/objectives`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmAddObjective(
  reviewId: string,
  input: AddObjectiveInput,
  ctx: KsmCtx,
): Promise<ReviewObjective> {
  return callKsm<ReviewObjective>(`/api/v1/hrm/reviews/${reviewId}/objectives`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmEvaluateObjective(
  objectiveId: string,
  input: EvaluateObjectiveInput,
  ctx: KsmCtx,
): Promise<ReviewObjective> {
  return callKsm<ReviewObjective>(`/api/v1/hrm/reviews/objectives/${objectiveId}/evaluate`, {
    method: "PUT",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
