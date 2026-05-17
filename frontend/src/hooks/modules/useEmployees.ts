"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type {
  Employee,
  CreateEmployeeInput,
  UpdateEmployeeInput,
  TerminateEmployeeInput,
  SuspendEmployeeInput,
} from "@/lib/types/hrm/employee";
import type { Contract, AddContractInput } from "@/lib/types/hrm/contract";
import type { Dependent, AddDependentInput } from "@/lib/types/hrm/dependent";
import type { LeaveBalance } from "@/lib/types/hrm/leave-balance";

export function useEmployees() {
  return useQuery({
    queryKey: queryKeys.hrm.employees(),
    queryFn: () => bffFetch<Employee[]>("/api/hrm/employees"),
  });
}

export function useEmployee(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId ? queryKeys.hrm.employee(employeeId) : ["hrm", "employees", "_none"],
    queryFn: () => bffFetch<Employee>(`/api/hrm/employees/${employeeId}`),
    enabled: !!employeeId,
  });
}

export function useCreateEmployee() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateEmployeeInput) =>
      bffFetch<Employee>("/api/hrm/employees", { method: "POST", body: JSON.stringify(input) }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.employees() });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.dashboard() });
    },
  });
}

export function useUpdateEmployee(employeeId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: UpdateEmployeeInput) =>
      bffFetch<Employee>(`/api/hrm/employees/${employeeId}`, {
        method: "PUT",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.employee(employeeId) });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.employees() });
    },
  });
}

export function useTerminateEmployee(employeeId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: TerminateEmployeeInput) =>
      bffFetch<Employee>(`/api/hrm/employees/${employeeId}/terminate`, {
        method: "PUT",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.employee(employeeId) });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.employees() });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.dashboard() });
    },
  });
}

export function useSuspendEmployee(employeeId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: SuspendEmployeeInput) =>
      bffFetch<Employee>(`/api/hrm/employees/${employeeId}/suspend`, {
        method: "PUT",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.employee(employeeId) });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.employees() });
    },
  });
}

export function useReactivateEmployee(employeeId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () =>
      bffFetch<Employee>(`/api/hrm/employees/${employeeId}/reactivate`, {
        method: "PUT",
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.employee(employeeId) });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.employees() });
    },
  });
}

export function useContracts(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId ? queryKeys.hrm.contracts(employeeId) : ["hrm", "contracts", "_none"],
    queryFn: () => bffFetch<Contract[]>(`/api/hrm/employees/${employeeId}/contracts`),
    enabled: !!employeeId,
  });
}

export function useAddContract(employeeId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: AddContractInput) =>
      bffFetch<Contract>(`/api/hrm/employees/${employeeId}/contracts`, {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.contracts(employeeId) });
    },
  });
}

export function useDependents(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId ? queryKeys.hrm.dependents(employeeId) : ["hrm", "dependents", "_none"],
    queryFn: () => bffFetch<Dependent[]>(`/api/hrm/employees/${employeeId}/dependents`),
    enabled: !!employeeId,
  });
}

export function useAddDependent(employeeId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: AddDependentInput) =>
      bffFetch<Dependent>(`/api/hrm/employees/${employeeId}/dependents`, {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.dependents(employeeId) });
    },
  });
}

export function useLeaveBalances(employeeId: string | undefined, annee: number) {
  return useQuery({
    queryKey: employeeId
      ? queryKeys.hrm.leaveBalances(employeeId, annee)
      : ["hrm", "leave-balances", "_none"],
    queryFn: () =>
      bffFetch<LeaveBalance[]>(
        `/api/hrm/employees/${employeeId}/leave-balances?annee=${annee}`,
      ),
    enabled: !!employeeId,
  });
}
