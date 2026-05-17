"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type {
  PayrollEntry,
  PayrollRun,
  PayslipLine,
  RunPayrollInput,
} from "@/lib/types/hrm/payroll";

export function usePayrollRuns() {
  return useQuery({
    queryKey: queryKeys.hrm.payrollRuns(),
    queryFn: () => bffFetch<PayrollRun[]>("/api/hrm/payroll/runs"),
  });
}

export function usePayrollRun(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.payrollRun(id) : ["hrm", "payroll-run", "_none"],
    queryFn: () => bffFetch<PayrollRun>(`/api/hrm/payroll/runs/${id}`),
    enabled: !!id,
  });
}

export function usePayrollEntries(runId: string | undefined) {
  return useQuery({
    queryKey: runId ? queryKeys.hrm.payrollEntries(runId) : ["hrm", "payroll-entries", "_none"],
    queryFn: () => bffFetch<PayrollEntry[]>(`/api/hrm/payroll/runs/${runId}/entries`),
    enabled: !!runId,
  });
}

export function usePayslipLines(entryId: string | undefined) {
  return useQuery({
    queryKey: entryId ? queryKeys.hrm.payslipLines(entryId) : ["hrm", "payslip", "_none"],
    queryFn: () => bffFetch<PayslipLine[]>(`/api/hrm/payroll/entries/${entryId}/payslip`),
    enabled: !!entryId,
  });
}

export function useRunPayroll() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: RunPayrollInput) =>
      bffFetch<PayrollRun>("/api/hrm/payroll/run", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.payrollRuns() }),
  });
}

export function useValidatePayrollRun() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) =>
      bffFetch<PayrollRun>(`/api/hrm/payroll/runs/${id}/validate`, { method: "PUT" }),
    onSuccess: (_d, id) => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.payrollRuns() });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.payrollRun(id) });
    },
  });
}
