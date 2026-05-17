"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type { LeaveRequest, SubmitLeaveInput } from "@/lib/types/hrm/leave-request";

export function useEmployeeLeaves(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId
      ? queryKeys.hrm.employeeLeaves(employeeId)
      : ["hrm", "leaves", "_none"],
    queryFn: () => bffFetch<LeaveRequest[]>(`/api/hrm/employees/${employeeId}/leaves`),
    enabled: !!employeeId,
  });
}

export function useSubmitLeave(employeeId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: SubmitLeaveInput) =>
      bffFetch<LeaveRequest>("/api/hrm/leaves", { method: "POST", body: JSON.stringify(input) }),
    onSuccess: () => {
      if (employeeId) qc.invalidateQueries({ queryKey: queryKeys.hrm.employeeLeaves(employeeId) });
    },
  });
}

export function useCancelLeave(employeeId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (leaveId: string) =>
      bffFetch<LeaveRequest>(`/api/hrm/leaves/${leaveId}/cancel`, { method: "PUT" }),
    onSuccess: () => {
      if (employeeId) qc.invalidateQueries({ queryKey: queryKeys.hrm.employeeLeaves(employeeId) });
    },
  });
}

export function usePendingLeaves() {
  return useQuery({
    queryKey: queryKeys.hrm.pendingLeaves(),
    queryFn: () => bffFetch<LeaveRequest[]>("/api/hrm/leaves/pending"),
  });
}

export function useApproveLeave() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (leaveId: string) =>
      bffFetch<LeaveRequest>(`/api/hrm/leaves/${leaveId}/approve`, { method: "PUT" }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.pendingLeaves() });
      qc.invalidateQueries({ queryKey: ["hrm", "employees"] });
    },
  });
}

export function useRejectLeave() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ leaveId, commentaire }: { leaveId: string; commentaire: string }) =>
      bffFetch<LeaveRequest>(`/api/hrm/leaves/${leaveId}/reject`, {
        method: "PUT",
        body: JSON.stringify({ commentaire }),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.pendingLeaves() }),
  });
}
