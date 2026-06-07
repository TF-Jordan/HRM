"use client";

import { useQuery } from "@tanstack/react-query";
import { ChevronRight, Loader2, Plus, Search, Sparkles, Star } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { ProgressBar } from "@/components/ui/progress-bar";
import { StatCard, StatCardGrid } from "@/components/ui/stat-card";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { reviewStatusTone } from "@/lib/training-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { ReviewResponse, ReviewStatus } from "@/server/ksm/modules/reviews";

type Filter = "ALL" | "DRAFT" | "SUBMITTED" | "ACKNOWLEDGED" | "FINALIZED";

const FILTERS: { key: Filter; tKey: string }[] = [
  { key: "ALL", tKey: "filters.all" },
  { key: "DRAFT", tKey: "filters.draft" },
  { key: "SUBMITTED", tKey: "filters.submitted" },
  { key: "ACKNOWLEDGED", tKey: "filters.acknowledged" },
  { key: "FINALIZED", tKey: "filters.finalized" },
];

function defaultPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-Q${Math.floor(now.getMonth() / 3) + 1}`;
}

/** Recent quarters + annual cycles, newest first, for the period selector. */
function periodOptions(): { value: string; label: string }[] {
  const year = new Date().getFullYear();
  const opts: { value: string; label: string }[] = [];
  for (const y of [year, year - 1]) {
    for (let q = 4; q >= 1; q--) opts.push({ value: `${y}-Q${q}`, label: `${y} · T${q}` });
    opts.push({ value: `${y}`, label: `${y} · Annuel` });
  }
  return opts;
}

function normalize(s: string): string {
  return s
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();
}

export function ReviewsQueue() {
  const t = useTranslations("reviews");
  const router = useRouter();
  const canCreate = useCan("hrm:review:create");
  const canManage = useCan("hrm:review:manage");
  const [periode, setPeriode] = React.useState<string>(defaultPeriode());
  const [filter, setFilter] = React.useState<Filter>("ALL");
  const [search, setSearch] = React.useState("");

  const options = React.useMemo(() => {
    const base = periodOptions();
    if (!base.some((o) => o.value === periode)) {
      base.unshift({ value: periode, label: periode });
    }
    return base;
  }, [periode]);

  const query = useQuery({
    queryKey: ["hrm", "reviews", "list", periode],
    queryFn: () =>
      apiFetch<ReviewResponse[]>(`/api/hrm/reviews?periode=${encodeURIComponent(periode)}`),
    refetchInterval: 60_000,
  });

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });
  const nameOf = React.useCallback(
    (employeeId: string) => {
      const e = employeesQuery.data?.find((x) => x.id === employeeId);
      return e?.actorDisplayName ?? e?.matricule ?? `${employeeId.slice(0, 8)}…`;
    },
    [employeesQuery.data],
  );

  const all = React.useMemo(() => query.data ?? [], [query.data]);

  const counts = React.useMemo(() => {
    const c: Partial<Record<ReviewStatus, number>> = {};
    let totalScore = 0;
    let scored = 0;
    for (const r of all) {
      c[r.status] = (c[r.status] ?? 0) + 1;
      if (r.noteGlobale != null) {
        totalScore += Number(r.noteGlobale);
        scored++;
      }
    }
    const total = all.length;
    const finalized = c.FINALIZED ?? 0;
    return {
      c,
      total,
      finalized,
      avg: scored > 0 ? (totalScore / scored).toFixed(1) : "—",
      toComplete: (c.DRAFT ?? 0) + (c.SUBMITTED ?? 0),
      progress: total > 0 ? Math.round((finalized / total) * 100) : 0,
    };
  }, [all]);

  const visible = React.useMemo(() => {
    const q = normalize(search.trim());
    return all
      .filter((r) => filter === "ALL" || r.status === filter)
      .filter((r) => !q || normalize(nameOf(r.employeeId)).includes(q))
      .sort((a, b) => nameOf(a.employeeId).localeCompare(nameOf(b.employeeId)));
  }, [all, filter, search, nameOf]);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          canCreate ? (
            <Link href="/reviews/new">
              <Button>
                <Plus className="h-4 w-4" />
                {t("new.title")}
              </Button>
            </Link>
          ) : undefined
        }
      />

      <StatCardGrid>
        <StatCard label={t("kpi.toComplete")} value={counts.toComplete} tone="amber" sub={t("kpi.toCompleteSub")} />
        <StatCard label={t("kpi.averageScore")} value={counts.avg} tone="green" sub={t("kpi.outOf5")} />
        <StatCard
          label={t("kpi.progress")}
          value={`${counts.progress}%`}
          tone="blue"
          sub={
            <span className="mt-1 block">
              <ProgressBar value={counts.progress} size="sm" />
            </span>
          }
        />
        <StatCard label={t("kpi.finalized")} value={counts.finalized} tone="violet" sub={`${counts.total} ${t("kpi.totalSuffix")}`} />
      </StatCardGrid>

      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap gap-2">
          {FILTERS.map((f) => {
            const n = f.key === "ALL" ? counts.total : counts.c[f.key as ReviewStatus] ?? 0;
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
        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-ink-4" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder={t("queue.searchPlaceholder")}
              className="h-9 w-56 rounded-[10px] border border-line bg-white pl-8 pr-3 text-[12.5px] text-ink outline-none placeholder:text-ink-4 focus:border-orange-400"
            />
          </div>
          <select
            value={periode}
            onChange={(e) => setPeriode(e.target.value)}
            className="h-9 rounded-[10px] border border-line bg-white px-3 text-[12.5px] font-semibold text-ink outline-none focus:border-orange-400"
            aria-label={t("queue.periodLabel")}
          >
            {options.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      <Card>
        {query.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
          </div>
        ) : query.error ? (
          <div className="px-5 py-10 text-center text-ink-3">
            {query.error instanceof BffApiError ? query.error.message : "—"}
          </div>
        ) : visible.length === 0 ? (
          <CardContent padding="lg">
            <div className="grid place-items-center py-10 text-center">
              <Sparkles className="mb-2 h-7 w-7 text-ink-4" />
              <p className="text-[13px] text-ink-3">
                {all.length === 0 ? t("queue.empty") : t("queue.emptyFiltered")}
              </p>
            </div>
          </CardContent>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="border-b border-line bg-bg-dim">
                <Th>{t("queue.columns.employee")}</Th>
                <Th>{t("queue.columns.evaluator")}</Th>
                <Th>{t("queue.columns.score")}</Th>
                <Th>{t("queue.columns.status")}</Th>
                <Th> </Th>
              </tr>
            </thead>
            <tbody>
              {visible.map((r) => (
                <tr
                  key={r.id}
                  className="group cursor-pointer border-b border-line-soft last:border-0 hover:bg-bg-soft"
                  onClick={() => router.push(`/reviews/${r.id}`)}
                >
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-2.5">
                      <Avatar name={nameOf(r.employeeId)} size="sm" />
                      <div>
                        <div className="text-[13px] font-semibold text-ink">
                          {nameOf(r.employeeId)}
                        </div>
                        <div className="font-mono-tabular text-[11px] text-ink-4">{shortRef(r.id)}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-3 py-3 text-[12.5px] text-ink-2">
                    {r.evaluateurDisplayName ?? "—"}
                  </td>
                  <td className="px-3 py-3">
                    {r.noteGlobale != null ? (
                      <div className="flex items-center gap-2">
                        <span className="font-display font-mono-tabular text-[16px] font-bold text-ink">
                          {Number(r.noteGlobale).toFixed(1)}
                        </span>
                        <div className="flex items-center gap-0.5">
                          {[1, 2, 3, 4, 5].map((i) => (
                            <Star
                              key={i}
                              className={cn(
                                "h-2.5 w-2.5",
                                i <= Math.round(Number(r.noteGlobale))
                                  ? "fill-orange-500 text-orange-500"
                                  : "fill-bg-soft text-bg-soft",
                              )}
                            />
                          ))}
                        </div>
                      </div>
                    ) : (
                      <span className="text-ink-4">—</span>
                    )}
                  </td>
                  <td className="px-3 py-3">
                    <Badge tone={reviewStatusTone(r.status)}>{t(`status.${r.status}`)}</Badge>
                  </td>
                  <td className="px-5 py-3 text-right">
                    <span className="inline-flex items-center gap-1 text-[12px] font-semibold text-orange-600 opacity-0 transition-opacity group-hover:opacity-100">
                      {canManage && r.status === "DRAFT" ? t("studio.launch") : t("queue.viewDetail")}
                      <ChevronRight className="h-3.5 w-3.5" />
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </>
  );
}

function Th({ children }: { children: React.ReactNode }) {
  return (
    <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3 first:pl-5 last:pr-5">
      {children}
    </th>
  );
}

function shortRef(uuid: string): string {
  return `EV-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}
