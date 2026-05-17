"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type {
  AddObjectiveInput,
  CreateReviewInput,
  Review,
  ReviewObjective,
  SubmitReviewInput,
} from "@/lib/types/hrm/review";

export function useReviews(periode: string | null) {
  return useQuery({
    queryKey: queryKeys.hrm.reviews(periode),
    queryFn: () =>
      bffFetch<Review[]>(`/api/hrm/reviews${periode ? `?periode=${periode}` : ""}`),
  });
}

export function useReview(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.review(id) : ["hrm", "review", "_none"],
    queryFn: () => bffFetch<Review>(`/api/hrm/reviews/${id}`),
    enabled: !!id,
  });
}

export function useReviewObjectives(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.reviewObjectives(id) : ["hrm", "review-objs", "_none"],
    queryFn: () => bffFetch<ReviewObjective[]>(`/api/hrm/reviews/${id}/objectives`),
    enabled: !!id,
  });
}

export function useCreateReview() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateReviewInput) =>
      bffFetch<Review>("/api/hrm/reviews", { method: "POST", body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["hrm", "reviews"] }),
  });
}

export function useAddObjective(reviewId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: AddObjectiveInput) =>
      bffFetch<ReviewObjective>(`/api/hrm/reviews/${reviewId}/objectives`, {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () =>
      qc.invalidateQueries({ queryKey: queryKeys.hrm.reviewObjectives(reviewId) }),
  });
}

export function useSubmitReview(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: SubmitReviewInput) =>
      bffFetch<Review>(`/api/hrm/reviews/${id}/submit`, {
        method: "PUT",
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.review(id) }),
  });
}

export function useReviewTransition(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (action: "acknowledge" | "finalize") =>
      bffFetch<Review>(`/api/hrm/reviews/${id}/${action}`, { method: "PUT" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.review(id) }),
  });
}
