"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type { CreateMissionOrderInput, MissionOrder } from "@/lib/types/hrm/mission-order";

export function useEmployeeMissions(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId ? queryKeys.hrm.employeeMissions(employeeId) : ["hrm", "missions", "_none"],
    queryFn: () => bffFetch<MissionOrder[]>(`/api/hrm/mission-orders?employeeId=${employeeId}`),
    enabled: !!employeeId,
  });
}

export function useMission(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.mission(id) : ["hrm", "mission", "_none"],
    queryFn: () => bffFetch<MissionOrder>(`/api/hrm/mission-orders/${id}`),
    enabled: !!id,
  });
}

export function useCreateMission() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateMissionOrderInput) =>
      bffFetch<MissionOrder>("/api/hrm/mission-orders", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: (_d, input) =>
      qc.invalidateQueries({ queryKey: queryKeys.hrm.employeeMissions(input.employeeId) }),
  });
}

export function useMissionTransition(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (action: "approve" | "start" | "complete" | "cancel") =>
      bffFetch<MissionOrder>(`/api/hrm/mission-orders/${id}/${action}`, { method: "PUT" }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.mission(id) });
      qc.invalidateQueries({ queryKey: ["hrm", "employees"] });
    },
  });
}
