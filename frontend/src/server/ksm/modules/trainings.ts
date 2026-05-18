import "server-only";

import { callKsm } from "../client";
import type {
  CompleteEnrollmentInput,
  EnrollEmployeeInput,
  PlanTrainingInput,
  Training,
  TrainingEnrollment,
} from "@/lib/types/hrm/training";

type KsmCtx = { tenantId: string; organizationId: string; agencyId?: string | null; bearer: string };

const Q = (orgId: string) => ({ organizationId: orgId });

export async function ksmListTrainings(ctx: KsmCtx): Promise<Training[]> {
  return callKsm<Training[]>(`/api/v1/hrm/trainings`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: Q(ctx.organizationId),
  });
}

export async function ksmGetTraining(id: string, ctx: KsmCtx): Promise<Training> {
  return callKsm<Training>(`/api/v1/hrm/trainings/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmPlanTraining(input: PlanTrainingInput, ctx: KsmCtx): Promise<Training> {
  return callKsm<Training>(`/api/v1/hrm/trainings`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    agencyId: ctx.agencyId,
  });
}

async function trainingTx(id: string, action: string, ctx: KsmCtx): Promise<Training> {
  return callKsm<Training>(`/api/v1/hrm/trainings/${id}/${action}`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
export const ksmStartTraining = (id: string, ctx: KsmCtx) => trainingTx(id, "start", ctx);
export const ksmCompleteTraining = (id: string, ctx: KsmCtx) => trainingTx(id, "complete", ctx);
export const ksmCancelTraining = (id: string, ctx: KsmCtx) => trainingTx(id, "cancel", ctx);

export async function ksmListEnrollments(trainingId: string, ctx: KsmCtx): Promise<TrainingEnrollment[]> {
  return callKsm<TrainingEnrollment[]>(`/api/v1/hrm/trainings/${trainingId}/enrollments`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListEnrollmentsByEmployee(
  employeeId: string,
  ctx: KsmCtx,
): Promise<TrainingEnrollment[]> {
  return callKsm<TrainingEnrollment[]>(`/api/v1/hrm/trainings/enrollments/employee/${employeeId}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmEnrollEmployee(
  trainingId: string,
  input: EnrollEmployeeInput,
  ctx: KsmCtx,
): Promise<TrainingEnrollment> {
  return callKsm<TrainingEnrollment>(`/api/v1/hrm/trainings/${trainingId}/enrollments`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCompleteEnrollment(
  enrollmentId: string,
  input: CompleteEnrollmentInput,
  ctx: KsmCtx,
): Promise<TrainingEnrollment> {
  return callKsm<TrainingEnrollment>(`/api/v1/hrm/trainings/enrollments/${enrollmentId}/complete`, {
    method: "PUT",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCancelEnrollment(
  enrollmentId: string,
  ctx: KsmCtx,
): Promise<TrainingEnrollment> {
  return callKsm<TrainingEnrollment>(`/api/v1/hrm/trainings/enrollments/${enrollmentId}/cancel`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
