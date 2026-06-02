"use client";

import { useQuery } from "@tanstack/react-query";
import { Briefcase, Calendar, GraduationCap, Loader2, MapPin, Plus, Users } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatNumber } from "@/lib/format";
import { gradientForId, trainingStatusTone } from "@/lib/training-status";
import { cn } from "@/lib/utils";
import type { TrainingResponse, TrainingStatus } from "@/server/ksm/modules/trainings";

type Filter = "ALL" | "PLANNED" | "IN_PROGRESS" | "COMPLETED";

const FILTERS: { key: Filter; tKey: string }[] = [
  { key: "ALL", tKey: "filters.all" },
  { key: "PLANNED", tKey: "filters.planned" },
  { key: "IN_PROGRESS", tKey: "filters.inProgress" },
  { key: "COMPLETED", tKey: "filters.completed" },
];

export function TrainingsCatalog() {
  const t = useTranslations("trainings");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const canCreate = useCan("hrm:training:create");
  const [filter, setFilter] = React.useState<Filter>("ALL");

  const query = useQuery({
    queryKey: ["hrm", "trainings", "list"],
    queryFn: () => apiFetch<TrainingResponse[]>("/api/hrm/trainings"),
    refetchInterval: 60_000,
  });

  const all = React.useMemo(() => query.data ?? [], [query.data]);
  const visible = filter === "ALL" ? all : all.filter((t) => t.status === filter);
  const counts = React.useMemo(() => {
    const c: Partial<Record<TrainingStatus, number>> = {};
    for (const r of all) c[r.status] = (c[r.status] ?? 0) + 1;
    return c;
  }, [all]);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          canCreate ? (
            <Link href="/trainings/new">
              <Button>
                <Plus className="h-4 w-4" />
                {t("new.title")}
              </Button>
            </Link>
          ) : undefined
        }
      />

      <div className="mb-5 grid grid-cols-4 gap-4">
        <Tile label={t("kpi.active")} value={(counts.PLANNED ?? 0) + (counts.IN_PROGRESS ?? 0)} tone="bg-orange-500" />
        <Tile label={t("kpi.inProgress")} value={counts.IN_PROGRESS ?? 0} tone="bg-info-500" />
        <Tile label={t("kpi.completed")} value={counts.COMPLETED ?? 0} tone="bg-success-500" />
        <Tile label={t("kpi.enrollments")} value={all.length} tone="bg-violet-500" />
      </div>

      <div className="mb-4 flex flex-wrap gap-2">
        {FILTERS.map((f) => (
          <button
            key={f.key}
            type="button"
            onClick={() => setFilter(f.key)}
            className={cn(
              "rounded-full px-3 py-1.5 text-[12px] font-semibold transition-colors",
              filter === f.key
                ? "bg-grad-orange text-white shadow-orange-brand"
                : "border border-line bg-white text-ink-2 hover:bg-bg-soft",
            )}
          >
            {t(f.tKey)}
          </button>
        ))}
      </div>

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : visible.length === 0 ? (
        <div className="rounded-[20px] border border-dashed border-line bg-white p-12 text-center text-ink-3">
          {t("catalog.empty")}
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {visible.map((tr) => (
            <button
              key={tr.id}
              type="button"
              onClick={() => router.push(`/trainings/${tr.id}`)}
              className="text-left"
            >
              <Card clickable className="h-full">
                <div
                  className="relative -mb-4 h-[88px] overflow-hidden rounded-t-[20px] px-5 py-3"
                  style={{ background: gradientForId(tr.id) }}
                >
                  <Badge className="bg-white/25 text-white backdrop-blur-sm">{t(`status.${tr.status}`)}</Badge>
                  <GraduationCap
                    className="absolute right-3 top-3 h-12 w-12 text-white/30"
                    aria-hidden="true"
                  />
                </div>
                <CardContent padding="lg">
                  <div className="font-mono-tabular text-[10px] uppercase tracking-wider text-ink-4">
                    {shortRef(tr.id)}
                  </div>
                  <div className="mt-1 text-[15px] font-bold tracking-tight text-ink">
                    {tr.intitule}
                  </div>
                  <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1 text-[11.5px] text-ink-3">
                    {tr.organisme && (
                      <span className="flex items-center gap-1">
                        <Briefcase className="h-3 w-3" />
                        {tr.organisme}
                      </span>
                    )}
                    {tr.lieu && (
                      <span className="flex items-center gap-1">
                        <MapPin className="h-3 w-3" />
                        {tr.lieu}
                      </span>
                    )}
                    {tr.nbPlaces != null && (
                      <span className="flex items-center gap-1">
                        <Users className="h-3 w-3" />
                        {tr.nbPlaces}
                      </span>
                    )}
                  </div>
                  <div className="mt-4 flex items-end justify-between border-t border-line-soft pt-3">
                    <div>
                      <div className="text-[10px] uppercase tracking-wider text-ink-4">
                        {t("card.session")}
                      </div>
                      <div className="flex items-center gap-1 text-[12px] font-semibold text-ink">
                        <Calendar className="h-3 w-3 text-orange-500" />
                        {tr.dateDebut ? formatDate(tr.dateDebut, { locale }) : "—"}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-[10px] uppercase tracking-wider text-ink-4">
                        {t("card.cost")}
                      </div>
                      <div className="font-mono-tabular text-[13px] font-bold text-ink">
                        {tr.cout != null ? `${formatNumber(Number(tr.cout), locale)} XAF` : "—"}
                      </div>
                    </div>
                  </div>
                  <div className="mt-3">
                    <Badge tone={trainingStatusTone(tr.status)}>{t(`status.${tr.status}`)}</Badge>
                  </div>
                </CardContent>
              </Card>
            </button>
          ))}
        </div>
      )}
    </>
  );
}

function Tile({ label, value, tone }: { label: string; value: React.ReactNode; tone: string }) {
  return (
    <div className="relative flex flex-col gap-1 overflow-hidden rounded-[16px] border border-line bg-white px-[18px] py-4 shadow-xs-brand">
      <div className="flex items-center justify-between">
        <span className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">{label}</span>
        <span className={cn("inline-block h-2 w-2 rounded-full", tone)} />
      </div>
      <div className="font-display font-mono-tabular text-[24px] font-extrabold tracking-tight text-ink">
        {value}
      </div>
    </div>
  );
}

function shortRef(uuid: string): string {
  return `TR-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}
