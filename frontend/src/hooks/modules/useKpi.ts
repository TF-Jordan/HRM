"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type { CreateRhKpiSnapshotInput, RhKpiSnapshot } from "@/lib/types/hrm/kpi";

export function useKpiSnapshots() {
  return useQuery({
    queryKey: queryKeys.hrm.kpiSnapshots(),
    queryFn: () => bffFetch<RhKpiSnapshot[]>("/api/hrm/kpi"),
  });
}

export function useKpiSnapshot(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.kpiSnapshot(id) : ["hrm", "kpi", "_none"],
    queryFn: () => bffFetch<RhKpiSnapshot>(`/api/hrm/kpi/${id}`),
    enabled: !!id,
  });
}

export function useCreateKpiSnapshot() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateRhKpiSnapshotInput) =>
      bffFetch<RhKpiSnapshot>("/api/hrm/kpi", { method: "POST", body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.kpiSnapshots() }),
  });
}
