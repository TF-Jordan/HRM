"use client";

import { useQuery } from "@tanstack/react-query";
import { bffFetch } from "@/lib/api-client";
import type { Agency } from "@/lib/types/hrm/agency";

export function useAgencies() {
  return useQuery({
    queryKey: ["hrm", "agencies"],
    queryFn: () => bffFetch<Agency[]>("/api/hrm/agencies"),
    staleTime: 5 * 60 * 1000,
  });
}

/** Build a Map<agencyId, name> for quick lookup. */
export function useAgencyNames(): Map<string, string> {
  const { data } = useAgencies();
  const map = new Map<string, string>();
  for (const a of data ?? []) map.set(a.id, a.name);
  return map;
}
