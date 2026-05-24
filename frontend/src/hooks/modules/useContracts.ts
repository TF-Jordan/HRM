"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type { Contract, ContractWithEmployee, AddContractInput } from "@/lib/types/hrm/contract";
import type { StoredFile } from "@/lib/types/hrm/file";

export function useAllContracts() {
  return useQuery({
    queryKey: queryKeys.hrm.allContracts(),
    queryFn: () => bffFetch<ContractWithEmployee[]>("/api/hrm/contracts"),
  });
}

export function useRenewContract(employeeId: string, contractId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (newDateFin: string) =>
      bffFetch<Contract>(
        `/api/hrm/employees/${employeeId}/contracts/${contractId}/renew`,
        { method: "PUT", body: JSON.stringify({ newDateFin }) },
      ),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.allContracts() });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.contracts(employeeId) });
    },
  });
}

export function useTerminateContract(employeeId: string, contractId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (motif: string) =>
      bffFetch<Contract>(
        `/api/hrm/employees/${employeeId}/contracts/${contractId}/terminate`,
        { method: "PUT", body: JSON.stringify({ motif }) },
      ),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.allContracts() });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.contracts(employeeId) });
    },
  });
}

export function useAttachContractDocument(employeeId: string, contractId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (documentFileId: string) =>
      bffFetch<Contract>(
        `/api/hrm/employees/${employeeId}/contracts/${contractId}/document`,
        { method: "PUT", body: JSON.stringify({ documentFileId }) },
      ),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.allContracts() });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.contracts(employeeId) });
    },
  });
}

export async function uploadFile(file: File): Promise<StoredFile> {
  const formData = new FormData();
  formData.append("file", file);
  return bffFetch<StoredFile>("/api/hrm/files", {
    method: "POST",
    body: formData,
  });
}

export function useCreateContractWithDocument(employeeId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (params: { contract: AddContractInput; file?: File }) => {
      const created = await bffFetch<Contract>(
        `/api/hrm/employees/${employeeId}/contracts`,
        { method: "POST", body: JSON.stringify(params.contract) },
      );
      if (params.file) {
        const stored = await uploadFile(params.file);
        return bffFetch<Contract>(
          `/api/hrm/employees/${employeeId}/contracts/${created.id}/document`,
          { method: "PUT", body: JSON.stringify({ documentFileId: stored.id }) },
        );
      }
      return created;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.allContracts() });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.contracts(employeeId) });
    },
  });
}
