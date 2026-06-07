"use client";

import { useQuery } from "@tanstack/react-query";
import { GraduationCap, Loader2, Plus, Search } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { TrainingPreviewCard } from "@/components/trainings/training-preview-card";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { Button } from "@/components/ui/button";
import { StatCard, StatCardGrid } from "@/components/ui/stat-card";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatNumber } from "@/lib/format";
import { cn } from "@/lib/utils";
import type { TrainingResponse, TrainingStatus } from "@/server/ksm/modules/trainings";

type Filter = "ALL" | "PLANNED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";

const FILTERS: { key: Filter; tKey: string }[] = [
  { key: "ALL", tKey: "filters.all" },
  { key: "PLANNED", tKey: "filters.planned" },
  { key: "IN_PROGRESS", tKey: "filters.inProgress" },
  { key: "COMPLETED", tKey: "filters.completed" },
  { key: "CANCELLED", tKey: "filters.cancelled" },
];

function normalize(s: string): string {
  return s
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();
}

export function TrainingsCatalog() {
  const t = useTranslations("trainings");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const canCreate = useCan("hrm:training:create");
  const [filter, setFilter] = React.useState<Filter>("ALL");
  const [search, setSearch] = React.useState("");

  const query = useQuery({
    queryKey: ["hrm", "trainings", "list"],
    queryFn: () => apiFetch<TrainingResponse[]>("/api/hrm/trainings"),
    refetchInterval: 60_000,
  });

  const all = React.useMemo(() => query.data ?? [], [query.data]);

  const stats = React.useMemo(() => {
    const c: Partial<Record<TrainingStatus, number>> = {};
    let budget = 0;
    for (const r of all) {
      c[r.status] = (c[r.status] ?? 0) + 1;
      if (r.status !== "CANCELLED" && r.cout != null) budget += Number(r.cout);
    }
    return { c, budget };
  }, [all]);

  const visible = React.useMemo(() => {
    const q = normalize(search.trim());
    return all
      .filter((r) => filter === "ALL" || r.status === filter)
      .filter(
        (r) =>
          !q ||
          normalize(`${r.intitule} ${r.organisme ?? ""} ${r.lieu ?? ""}`).includes(q),
      );
  }, [all, filter, search]);

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

      <StatCardGrid>
        <StatCard
          label={t("kpi.active")}
          value={(stats.c.PLANNED ?? 0) + (stats.c.IN_PROGRESS ?? 0)}
          tone="orange"
          sub={t("kpi.activeSub")}
        />
        <StatCard label={t("kpi.inProgress")} value={stats.c.IN_PROGRESS ?? 0} tone="blue" />
        <StatCard label={t("kpi.completed")} value={stats.c.COMPLETED ?? 0} tone="green" />
        <StatCard
          label={t("kpi.budget")}
          value={`${formatNumber(stats.budget, locale)}`}
          tone="violet"
          sub={t("kpi.budgetSub")}
        />
      </StatCardGrid>

      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap gap-2">
          {FILTERS.map((f) => {
            const n = f.key === "ALL" ? all.length : stats.c[f.key as TrainingStatus] ?? 0;
            return (
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
                <span className={cn("ml-1.5", filter === f.key ? "text-white/80" : "text-ink-4")}>
                  {n}
                </span>
              </button>
            );
          })}
        </div>
        <div className="relative">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-ink-4" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder={t("catalog.searchPlaceholder")}
            className="h-9 w-64 rounded-[10px] border border-line bg-white pl-8 pr-3 text-[12.5px] text-ink outline-none placeholder:text-ink-4 focus:border-orange-400"
          />
        </div>
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
        <div className="grid place-items-center rounded-[20px] border border-dashed border-line bg-white p-12 text-center">
          <GraduationCap className="mb-2 h-8 w-8 text-ink-4" />
          <p className="text-[13px] text-ink-3">
            {all.length === 0 ? t("catalog.empty") : t("catalog.emptyFiltered")}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {visible.map((tr) => (
            <TrainingPreviewCard
              key={tr.id}
              headerLabel={t(`status.${tr.status}`)}
              title={tr.intitule}
              organisme={tr.organisme}
              lieu={tr.lieu}
              dateDebut={tr.dateDebut}
              dateFin={tr.dateFin}
              cout={tr.cout != null ? Number(tr.cout) : null}
              nbPlaces={tr.nbPlaces}
              locale={locale}
              onClick={() => router.push(`/trainings/${tr.id}`)}
            />
          ))}
        </div>
      )}
    </>
  );
}
