"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type { CreateDeclarationInput, SocialDeclaration } from "@/lib/types/hrm/declaration";

export function useDeclarations() {
  return useQuery({
    queryKey: queryKeys.hrm.declarations(),
    queryFn: () => bffFetch<SocialDeclaration[]>("/api/hrm/declarations"),
  });
}

export function useDeclaration(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.declaration(id) : ["hrm", "decl", "_none"],
    queryFn: () => bffFetch<SocialDeclaration>(`/api/hrm/declarations/${id}`),
    enabled: !!id,
  });
}

export function useCreateDeclaration() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateDeclarationInput) =>
      bffFetch<SocialDeclaration>("/api/hrm/declarations", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.declarations() }),
  });
}

export function useDeclarationTransition(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (action: "generate" | "submit" | "acknowledge") =>
      bffFetch<SocialDeclaration>(`/api/hrm/declarations/${id}/${action}`, { method: "PUT" }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.declaration(id) });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.declarations() });
    },
  });
}
