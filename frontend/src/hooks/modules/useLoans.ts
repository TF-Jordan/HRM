"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type {
  LoanAdvance,
  LoanAdvanceWithEmployee,
  RequestLoanInput,
} from "@/lib/types/hrm/loan-advance";

export function useAllLoans() {
  return useQuery({
    queryKey: ["hrm", "loans", "all"],
    queryFn: () => bffFetch<LoanAdvanceWithEmployee[]>("/api/hrm/loans"),
  });
}

export function useEmployeeLoans(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId
      ? queryKeys.hrm.employeeLoans(employeeId)
      : ["hrm", "loans", "_none"],
    queryFn: () => bffFetch<LoanAdvance[]>(`/api/hrm/employees/${employeeId}/loans`),
    enabled: !!employeeId,
  });
}

export function useRequestLoan(employeeId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: RequestLoanInput) =>
      bffFetch<LoanAdvance>("/api/hrm/loans", { method: "POST", body: JSON.stringify(input) }),
    onSuccess: () => {
      if (employeeId) qc.invalidateQueries({ queryKey: queryKeys.hrm.employeeLoans(employeeId) });
    },
  });
}

export function usePendingLoans() {
  return useQuery({
    queryKey: queryKeys.hrm.pendingLoans(),
    queryFn: () => bffFetch<LoanAdvance[]>("/api/hrm/loans/pending"),
  });
}

export function useApproveLoan() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (loanId: string) =>
      bffFetch<LoanAdvance>(`/api/hrm/loans/${loanId}/approve`, { method: "PUT" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["hrm", "loans"] }),
  });
}

export function useRejectLoan() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ loanId, motif }: { loanId: string; motif: string }) =>
      bffFetch<LoanAdvance>(`/api/hrm/loans/${loanId}/reject`, {
        method: "PUT",
        body: JSON.stringify({ motif }),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["hrm", "loans"] }),
  });
}
