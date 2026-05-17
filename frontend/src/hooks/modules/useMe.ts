"use client";

import { useQuery } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type { Employee } from "@/lib/types/hrm/employee";

export function useMyEmployee() {
  return useQuery({
    queryKey: queryKeys.hrm.meEmployee(),
    queryFn: () => bffFetch<Employee>("/api/hrm/me/employee"),
    retry: false,
  });
}
