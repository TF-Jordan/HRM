"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2, Sparkles } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { taskStatusTone } from "@/lib/recruitment-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { OnboardingTaskResponse } from "@/server/ksm/modules/recruitment";

type MinePayload = { employee: EmployeeResponse | null; tasks: OnboardingTaskResponse[] };

export function MyOnboarding() {
  const t = useTranslations("recruitment");
  const locale = useLocale() as "fr" | "en";
  const query = useQuery({
    queryKey: ["hrm", "onboarding-tasks", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/onboarding-tasks/mine"),
    refetchInterval: 60_000,
  });

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("mine.title") }]}
        title={t("mine.title")}
        subtitle={t("mine.subtitle")}
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !query.data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : query.data.tasks.length === 0 ? (
        <Card>
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("mine.empty")}</p>
          </CardContent>
        </Card>
      ) : (
        <div className="overflow-hidden rounded-[20px] border border-line bg-white shadow-sm-brand">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-bg-dim">
                <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("onboarding.columns.title")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("onboarding.columns.deadline")}
                </th>
                <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("onboarding.columns.status")}
                </th>
              </tr>
            </thead>
            <tbody>
              {query.data.tasks.map((task) => (
                <tr key={task.id} className="border-t border-line-soft">
                  <td className="px-5 py-3">
                    <div className="flex items-start gap-2.5">
                      <Sparkles className="mt-[2px] h-4 w-4 shrink-0 text-orange-500" />
                      <div>
                        <div className="text-[13.5px] font-semibold text-ink">{task.titre}</div>
                        {task.description && (
                          <div className="text-[11.5px] text-ink-3">{task.description}</div>
                        )}
                      </div>
                    </div>
                  </td>
                  <td className="px-3 py-3 font-mono-tabular text-[12.5px] text-ink-2">
                    {task.echeance ? formatDate(task.echeance, { locale }) : "—"}
                  </td>
                  <td className="px-5 py-3">
                    <Badge tone={taskStatusTone(task.status)}>{t(`taskStatus.${task.status}`)}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
