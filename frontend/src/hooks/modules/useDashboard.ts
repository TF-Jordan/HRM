"use client";

import { useQuery } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";

export type HrmHeadcount = {
  total: number;
  active: number;
  onLeave: number;
  suspended: number;
  terminated: number;
};

export type HrmDashboardSummary = {
  headcount: HrmHeadcount;
};

export function useHrmDashboard() {
  return useQuery({
    queryKey: queryKeys.hrm.dashboard(),
    queryFn: () => bffFetch<HrmDashboardSummary>("/api/hrm/dashboard"),
  });
}
