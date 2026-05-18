"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type {
  CreateMedicalCertificateInput,
  CreateMedicalVisitInput,
  MedicalCertificate,
  MedicalVisit,
} from "@/lib/types/hrm/medical";

export function useEmployeeVisits(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId
      ? queryKeys.hrm.employeeVisits(employeeId)
      : ["hrm", "medical", "visits", "_none"],
    queryFn: () => bffFetch<MedicalVisit[]>(`/api/hrm/medical/employees/${employeeId}/visits`),
    enabled: !!employeeId,
  });
}

export function useCreateMedicalVisit(employeeId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateMedicalVisitInput) =>
      bffFetch<MedicalVisit>("/api/hrm/medical/visits", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      if (employeeId) {
        qc.invalidateQueries({ queryKey: queryKeys.hrm.employeeVisits(employeeId) });
      }
    },
  });
}

export function useEmployeeCertificates(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId
      ? queryKeys.hrm.employeeCertificates(employeeId)
      : ["hrm", "medical", "certificates", "_none"],
    queryFn: () =>
      bffFetch<MedicalCertificate[]>(`/api/hrm/medical/employees/${employeeId}/certificates`),
    enabled: !!employeeId,
  });
}

export function useCreateMedicalCertificate(employeeId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateMedicalCertificateInput) =>
      bffFetch<MedicalCertificate>("/api/hrm/medical/certificates", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      if (employeeId) {
        qc.invalidateQueries({ queryKey: queryKeys.hrm.employeeCertificates(employeeId) });
      }
    },
  });
}
