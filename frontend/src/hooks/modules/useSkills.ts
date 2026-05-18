"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type {
  CreateEmployeeSkillInput,
  CreateSkillInput,
  EmployeeSkill,
  Skill,
} from "@/lib/types/hrm/skill";

export function useSkills() {
  return useQuery({
    queryKey: queryKeys.hrm.skills(),
    queryFn: () => bffFetch<Skill[]>("/api/hrm/skills"),
  });
}

export function useSkill(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.skill(id) : ["hrm", "skill", "_none"],
    queryFn: () => bffFetch<Skill>(`/api/hrm/skills/${id}`),
    enabled: !!id,
  });
}

export function useCreateSkill() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateSkillInput) =>
      bffFetch<Skill>("/api/hrm/skills", { method: "POST", body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.hrm.skills() }),
  });
}

export function useEmployeeSkills(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId
      ? queryKeys.hrm.employeeSkills(employeeId)
      : ["hrm", "employee-skills", "_none"],
    queryFn: () => bffFetch<EmployeeSkill[]>(`/api/hrm/skills/employees/${employeeId}`),
    enabled: !!employeeId,
  });
}

export function useCreateEmployeeSkill(employeeId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateEmployeeSkillInput) =>
      bffFetch<EmployeeSkill>("/api/hrm/skills/employee-skills", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      if (employeeId) {
        qc.invalidateQueries({ queryKey: queryKeys.hrm.employeeSkills(employeeId) });
      }
    },
  });
}
