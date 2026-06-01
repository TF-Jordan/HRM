import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type TrainingBudgetResponse = {
  id: string;
  organizationId: string;
  agencyId: string | null;
  annee: number;
  montantAlloue: number | string;
  montantEngage: number | string;
  montantRealise: number | string;
};

export type CreateTrainingBudgetRequest = {
  organizationId?: string;
  agencyId?: string | null;
  annee: number;
  montantAlloue: number | string;
};

export function listBudgets(session: AppSession, annee: number, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId, annee: String(annee) });
  return callKsm<TrainingBudgetResponse[]>(
    `/api/v1/hrm/training-budgets?${params}`,
    {},
    { session },
  );
}

export function getBudget(id: string, session: AppSession) {
  return callKsm<TrainingBudgetResponse>(`/api/v1/hrm/training-budgets/${id}`, {}, { session });
}

export function createBudget(body: CreateTrainingBudgetRequest, session: AppSession) {
  const orgId = body.organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  return callKsm<TrainingBudgetResponse>(
    "/api/v1/hrm/training-budgets",
    { method: "POST", body: { ...body, organizationId: orgId } },
    { session },
  );
}

export function engageBudget(id: string, montant: number | string, session: AppSession) {
  return callKsm<TrainingBudgetResponse>(
    `/api/v1/hrm/training-budgets/${id}/engage`,
    { method: "PUT", body: { montant } },
    { session },
  );
}

export function realiseBudget(id: string, montant: number | string, session: AppSession) {
  return callKsm<TrainingBudgetResponse>(
    `/api/v1/hrm/training-budgets/${id}/realiser`,
    { method: "PUT", body: { montant } },
    { session },
  );
}
