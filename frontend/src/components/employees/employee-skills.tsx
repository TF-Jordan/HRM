"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";

import { BffApiError, apiFetch } from "@/lib/api-client";
import { cn } from "@/lib/utils";
import type { EmployeeSkillView } from "@/server/ksm/modules/skills";

const MAX_LEVEL = 5;

export function EmployeeSkills({ employeeId }: { employeeId: string }) {
  const tOv = useTranslations("employees.detail.overview");

  const query = useQuery({
    queryKey: ["hrm", "employee", "skills", employeeId],
    queryFn: () => apiFetch<EmployeeSkillView[]>(`/api/hrm/employees/${employeeId}/skills`),
  });

  return (
    <>
      <div className="mb-3 text-[14px] font-bold tracking-tight text-ink">{tOv("skills")}</div>
      {query.isLoading ? (
        <Loader2 className="h-5 w-5 animate-spin text-orange-500" />
      ) : query.error ? (
        <div className="rounded-[10px] border border-dashed border-line bg-bg-soft px-3 py-3 text-[12px] text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : !query.data || query.data.length === 0 ? (
        <div className="rounded-[10px] border border-dashed border-line bg-bg-soft px-3 py-3 text-[12px] text-ink-3">
          {tOv("skillsEmpty")}
        </div>
      ) : (
        <div className="flex flex-col gap-2.5">
          {query.data.map((s) => (
            <div key={s.id}>
              <div className="mb-1 flex items-center justify-between">
                <span className="text-[13px] font-medium text-ink">{s.skillName}</span>
                <span className="flex items-center gap-1" aria-label={`${s.niveauActuel}/${MAX_LEVEL}`}>
                  {Array.from({ length: MAX_LEVEL }).map((_, j) => (
                    <span
                      key={j}
                      className={cn(
                        "h-3 w-1.5 rounded-[2px]",
                        j < s.niveauActuel ? "bg-orange-500" : "bg-bg-soft",
                      )}
                    />
                  ))}
                </span>
              </div>
              {s.niveauAttendu > s.niveauActuel && (
                <div className="font-mono-tabular text-[10.5px] text-ink-4">
                  {s.niveauActuel}/{MAX_LEVEL} · {tOv("skillTarget")} {s.niveauAttendu}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </>
  );
}
