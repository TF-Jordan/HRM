"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type { Timesheet } from "@/lib/types/hrm/timesheet";

export function useOrgTimesheets(periode: string | null) {
  return useQuery({
    queryKey: queryKeys.hrm.orgTimesheets(periode),
    queryFn: () =>
      bffFetch<Timesheet[]>(`/api/hrm/timesheets${periode ? `?periode=${periode}` : ""}`),
  });
}

export function useValidateTimesheet() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) =>
      bffFetch<Timesheet>(`/api/hrm/timesheets/${id}/validate`, { method: "PUT" }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["hrm", "timesheets"] }),
  });
}
