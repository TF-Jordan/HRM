"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2, Plus, Star } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useCan } from "@/hooks/use-can";
import { Link, useRouter } from "@/i18n/navigation";
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

export function ReviewsQueue() {
  const t = useTranslations("reviews");
  const router = useRouter();
  const canCreate = useCan("hrm:review:create");
  const [periode, setPeriode] = React.useState<string>(defaultPeriode());
  const [filter, setFilter] = React.useState<Filter>("ALL");

  const query = useQuery({
    queryKey: ["hrm", "reviews", "list", periode],
    queryFn: () =>
      apiFetch<ReviewResponse[]>(
        `/api/hrm/reviews?periode=${encodeURIComponent(periode)}`,
      ),
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
  const visible = filter === "ALL" ? all : all.filter((r) => r.status === filter);
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
    return {
      c,
      avg: scored > 0 ? (totalScore / scored).toFixed(1) : "—",
      toClose: (c.DRAFT ?? 0) + (c.SUBMITTED ?? 0),
    };
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
            <Link href="/reviews/new">
              <Button>
                <Plus className="h-4 w-4" />
                {t("new.title")}
              </Button>
            </Link>
          ) : undefined
        }
      />

      <div className="mb-5 grid grid-cols-4 gap-4">
        <Tile label={t("kpi.cycle")} value={periode} tone="bg-orange-500" />
        <Tile label={t("kpi.toClose")} value={counts.toClose} tone="bg-warning-500" />
        <Tile label={t("kpi.averageScore")} value={counts.avg} tone="bg-success-500" />
        <Tile label={t("kpi.finalized")} value={counts.c.FINALIZED ?? 0} tone="bg-info-500" />
      </div>

      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap gap-2">
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
        <input
          type="text"
          value={periode}
          onChange={(e) => setPeriode(e.target.value)}
          className="w-32 rounded-[10px] border border-line bg-white px-3 py-1.5 text-right font-mono-tabular text-[12.5px] text-ink outline-none focus:border-orange-400"
        />
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
            <p className="text-center text-[13px] text-ink-3">{t("queue.empty")}</p>
          </CardContent>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="border-b border-line bg-bg-dim">
                <Th>{t("queue.columns.reference")}</Th>
                <Th>{t("queue.columns.employee")}</Th>
                <Th>{t("queue.columns.evaluator")}</Th>
                <Th>{t("queue.columns.cycle")}</Th>
                <Th>{t("queue.columns.score")}</Th>
                <Th>{t("queue.columns.status")}</Th>
              </tr>
            </thead>
            <tbody>
              {visible.map((r) => (
                <tr
                  key={r.id}
                  className="cursor-pointer border-b border-line-soft last:border-0 hover:bg-bg-soft"
                  onClick={() => router.push(`/reviews/${r.id}`)}
                >
                  <td className="px-5 py-3 font-mono-tabular text-[11px] text-ink-3">{shortRef(r.id)}</td>
                  <td className="px-3 py-3">
                    <div className="flex items-center gap-2.5">
                      <Avatar name={nameOf(r.employeeId)} size="sm" />
                      <span className="text-[13px] font-semibold text-ink">{nameOf(r.employeeId)}</span>
                    </div>
                  </td>
                  <td className="px-3 py-3 text-[12.5px] text-ink-2">
                    {r.evaluateurDisplayName ?? "—"}
                  </td>
                  <td className="px-3 py-3">
                    <Badge tone="orange">{r.periode}</Badge>
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
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
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
