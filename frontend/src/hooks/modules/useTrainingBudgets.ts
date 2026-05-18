"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type {
  CreateTrainingBudgetInput,
  MontantInput,
  TrainingBudget,
} from "@/lib/types/hrm/training";

export function useTrainingBudgets(annee: number) {
  return useQuery({
    queryKey: queryKeys.hrm.trainingBudgets(annee),
    queryFn: () => bffFetch<TrainingBudget[]>(`/api/hrm/training-budgets?annee=${annee}`),
  });
}

export function useTrainingBudget(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.trainingBudget(id) : ["hrm", "training-budget", "_none"],
    queryFn: () => bffFetch<TrainingBudget>(`/api/hrm/training-budgets/${id}`),
    enabled: !!id,
  });
}

export function useCreateTrainingBudget(annee: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateTrainingBudgetInput) =>
      bffFetch<TrainingBudget>("/api/hrm/training-budgets", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.trainingBudgets(annee) }),
  });
}

export function useBudgetTransition(annee: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      action,
      body,
    }: {
      id: string;
      action: "engage" | "realiser";
      body: MontantInput;
    }) =>
      bffFetch<TrainingBudget>(`/api/hrm/training-budgets/${id}/${action}`, {
        method: "PUT",
        body: JSON.stringify(body),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.trainingBudgets(annee) }),
  });
}
