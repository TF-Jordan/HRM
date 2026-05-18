"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type {
  Application,
  CreateApplicationInput,
  CreateJobOfferInput,
  JobOffer,
} from "@/lib/types/hrm/recruitment";

export function useJobOffers() {
  return useQuery({
    queryKey: queryKeys.hrm.jobOffers(),
    queryFn: () => bffFetch<JobOffer[]>("/api/hrm/job-offers"),
  });
}

export function useJobOffer(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.jobOffer(id) : ["hrm", "job-offer", "_none"],
    queryFn: () => bffFetch<JobOffer>(`/api/hrm/job-offers/${id}`),
    enabled: !!id,
  });
}

export function useCreateJobOffer() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateJobOfferInput) =>
      bffFetch<JobOffer>("/api/hrm/job-offers", { method: "POST", body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.jobOffers() }),
  });
}

export function useJobOfferTransition() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, action }: { id: string; action: "publish" | "close" }) =>
      bffFetch<JobOffer>(`/api/hrm/job-offers/${id}/${action}`, { method: "PUT" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.jobOffers() }),
  });
}

export function useApplications(jobOfferId: string | undefined) {
  return useQuery({
    queryKey: jobOfferId
      ? queryKeys.hrm.applications(jobOfferId)
      : ["hrm", "applications", "_none"],
    queryFn: () => bffFetch<Application[]>(`/api/hrm/job-offers/${jobOfferId}/applications`),
    enabled: !!jobOfferId,
  });
}

export function useCreateApplication(jobOfferId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateApplicationInput) =>
      bffFetch<Application>("/api/hrm/applications", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      if (jobOfferId) qc.invalidateQueries({ queryKey: queryKeys.hrm.applications(jobOfferId) });
    },
  });
}

export function useApplicationTransition(jobOfferId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      action,
    }: {
      id: string;
      action: "shortlist" | "interview" | "offer" | "reject" | "hire";
    }) => bffFetch<Application>(`/api/hrm/applications/${id}/${action}`, { method: "PUT" }),
    onSuccess: () => {
      if (jobOfferId) qc.invalidateQueries({ queryKey: queryKeys.hrm.applications(jobOfferId) });
    },
  });
}
