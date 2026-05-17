"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type { LoanAdvance, RequestLoanInput } from "@/lib/types/hrm/loan-advance";

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
