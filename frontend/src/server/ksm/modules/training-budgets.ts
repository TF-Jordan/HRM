import "server-only";

import { callKsm } from "../client";
import type {
  CreateTrainingBudgetInput,
  MontantInput,
  TrainingBudget,
} from "@/lib/types/hrm/training";

type KsmCtx = { tenantId: string; organizationId: string; agencyId?: string | null; bearer: string };

export async function ksmListTrainingBudgets(
  annee: number,
  ctx: KsmCtx,
): Promise<TrainingBudget[]> {
  return callKsm<TrainingBudget[]>(`/api/v1/hrm/training-budgets`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: { organizationId: ctx.organizationId, annee: String(annee) },
  });
}

export async function ksmGetTrainingBudget(id: string, ctx: KsmCtx): Promise<TrainingBudget> {
  return callKsm<TrainingBudget>(`/api/v1/hrm/training-budgets/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateTrainingBudget(
  input: CreateTrainingBudgetInput,
  ctx: KsmCtx,
): Promise<TrainingBudget> {
  return callKsm<TrainingBudget>(`/api/v1/hrm/training-budgets`, {
    method: "POST",
    body: { ...input, organizationId: ctx.organizationId },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    agencyId: ctx.agencyId,
  });
}

export async function ksmEngageBudget(
  id: string,
  input: MontantInput,
  ctx: KsmCtx,
): Promise<TrainingBudget> {
  return callKsm<TrainingBudget>(`/api/v1/hrm/training-budgets/${id}/engage`, {
    method: "PUT",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmRealiseBudget(
  id: string,
  input: MontantInput,
  ctx: KsmCtx,
): Promise<TrainingBudget> {
  return callKsm<TrainingBudget>(`/api/v1/hrm/training-budgets/${id}/realiser`, {
    method: "PUT",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
