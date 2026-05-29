"use client";

import { useQuery } from "@tanstack/react-query";
import { GraduationCap, Loader2 } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { enrollmentStatusTone } from "@/lib/training-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { EnrollmentResponse, TrainingResponse } from "@/server/ksm/modules/trainings";

type MinePayload = {
  employee: EmployeeResponse | null;
  enrollments: EnrollmentResponse[];
  trainings: TrainingResponse[];
};

export function MyTrainings() {
  const t = useTranslations("trainings");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();

  const query = useQuery({
    queryKey: ["hrm", "trainings", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/trainings/mine"),
    refetchInterval: 60_000,
  });

  const data = query.data;
  const trainingById = React.useMemo(() => {
    const m = new Map<string, TrainingResponse>();
    for (const tr of data?.trainings ?? []) m.set(tr.id, tr);
    return m;
  }, [data?.trainings]);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("mine.title") }]}
        title={t("mine.title")}
        subtitle={t("mine.subtitle")}
        actions={
          <Link href="/trainings">
            <Button type="button" variant="secondary">
              <GraduationCap className="h-4 w-4" />
              {t("mine.browse")}
            </Button>
          </Link>
        }
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : !data?.enrollments?.length ? (
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
                  {t("mine.columns.training")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("mine.columns.dates")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("mine.columns.status")}
                </th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody>
              {data.enrollments.map((en) => {
                const tr = trainingById.get(en.trainingId);
                return (
                  <tr
                    key={en.id}
                    className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                    onClick={() => tr && router.push(`/trainings/${tr.id}`)}
                  >
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-2.5">
                        <GraduationCap className="h-4 w-4 text-orange-500" />
                        <div>
                          <div className="text-[13.5px] font-semibold text-ink">
                            {tr?.intitule ?? en.trainingId.slice(0, 8)}
                          </div>
                          {tr?.organisme && (
                            <div className="text-[11px] text-ink-3">{tr.organisme}</div>
                          )}
                        </div>
                      </div>
                    </td>
                    <td className="px-3 py-3 text-[12.5px] text-ink-2 font-mono-tabular">
                      {tr?.dateDebut ? formatDate(tr.dateDebut, { locale }) : "—"}
                      {tr?.dateFin ? ` → ${formatDate(tr.dateFin, { locale })}` : ""}
                    </td>
                    <td className="px-3 py-3">
                      <Badge tone={enrollmentStatusTone(en.status)}>{t(`enrollmentStatus.${en.status}`)}</Badge>
                    </td>
                    <td className="px-5 py-3 text-right text-[12px] font-semibold text-orange-600">
                      {tr ? "→" : ""}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
