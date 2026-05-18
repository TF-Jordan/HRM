"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type {
  CompleteEnrollmentInput,
  EnrollEmployeeInput,
  PlanTrainingInput,
  Training,
  TrainingEnrollment,
} from "@/lib/types/hrm/training";

export function useTrainings() {
  return useQuery({
    queryKey: queryKeys.hrm.trainings(),
    queryFn: () => bffFetch<Training[]>("/api/hrm/trainings"),
  });
}

export function useTraining(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.training(id) : ["hrm", "training", "_none"],
    queryFn: () => bffFetch<Training>(`/api/hrm/trainings/${id}`),
    enabled: !!id,
  });
}

export function usePlanTraining() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: PlanTrainingInput) =>
      bffFetch<Training>("/api/hrm/trainings", { method: "POST", body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.trainings() }),
  });
}

export function useTrainingTransition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, action }: { id: string; action: "start" | "complete" | "cancel" }) =>
      bffFetch<Training>(`/api/hrm/trainings/${id}/${action}`, { method: "PUT" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.trainings() }),
  });
}

export function useTrainingEnrollments(trainingId: string | undefined) {
  return useQuery({
    queryKey: trainingId
      ? queryKeys.hrm.trainingEnrollments(trainingId)
      : ["hrm", "enrollments", "_none"],
    queryFn: () =>
      bffFetch<TrainingEnrollment[]>(`/api/hrm/trainings/${trainingId}/enrollments`),
    enabled: !!trainingId,
  });
}

export function useEnrollEmployee(trainingId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: EnrollEmployeeInput) =>
      bffFetch<TrainingEnrollment>(`/api/hrm/trainings/${trainingId}/enrollments`, {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      if (trainingId) {
        qc.invalidateQueries({ queryKey: queryKeys.hrm.trainingEnrollments(trainingId) });
      }
    },
  });
}

export function useEnrollmentTransition(trainingId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      action,
      body,
    }: {
      id: string;
      action: "complete" | "cancel";
      body?: CompleteEnrollmentInput;
    }) =>
      bffFetch<TrainingEnrollment>(`/api/hrm/trainings/enrollments/${id}/${action}`, {
        method: "PUT",
        body: body ? JSON.stringify(body) : undefined,
      }),
    onSuccess: () => {
      if (trainingId) {
        qc.invalidateQueries({ queryKey: queryKeys.hrm.trainingEnrollments(trainingId) });
      }
    },
  });
}

export function useEmployeeEnrollments(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId
      ? queryKeys.hrm.employeeEnrollments(employeeId)
      : ["hrm", "enrollments", "by-employee", "_none"],
    queryFn: () =>
      bffFetch<TrainingEnrollment[]>(`/api/hrm/trainings/enrollments/employee/${employeeId}`),
    enabled: !!employeeId,
  });
}
