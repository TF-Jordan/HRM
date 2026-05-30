"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Check, Coins, Loader2, Plus, X } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useSearchParams } from "next/navigation";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { useCan } from "@/hooks/use-can";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney, formatNumber } from "@/lib/format";
import {
  loanKindOf,
  loanKindTone,
  loanProgressPct,
  loanStatusTone,
  shortLoanRef,
  type LoanKind,
} from "@/lib/loan-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { LoanAdvanceResponse } from "@/server/ksm/modules/loans";

type Filter = "ALL" | "ADVANCES" | "LOANS" | "ACTIVE" | "PENDING";

const FILTERS: { key: Filter; tKey: string }[] = [
  { key: "ALL", tKey: "filters.all" },
  { key: "ADVANCES", tKey: "filters.advances" },
  { key: "LOANS", tKey: "filters.loans" },
  { key: "ACTIVE", tKey: "filters.active" },
  { key: "PENDING", tKey: "filters.pending" },
];

const KIND_COLORS: Record<LoanKind, string> = {
  HOUSING: "#34D399",
  VEHICLE: "#60A5FA",
  PERSONAL: "#A78BFA",
  ADVANCE: "#FCD34D",
};

export function LoansOverview() {
  const t = useTranslations("loans");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const queryClient = useQueryClient();
  const canApprove = useCan("hrm:loan:approve");
  const canCreate = useCan("hrm:loan:create");
  const searchParams = useSearchParams();
  const initialFilter = (searchParams.get("filter") as Filter | null) ?? "ALL";
  const [filter, setFilter] = React.useState<Filter>(
    FILTERS.some((f) => f.key === initialFilter) ? initialFilter : "ALL",
  );

  const loansQuery = useQuery({
    queryKey: ["hrm", "loans", "list"],
    queryFn: () => apiFetch<LoanAdvanceResponse[]>(`/api/hrm/loans`),
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

  const all = React.useMemo(() => loansQuery.data ?? [], [loansQuery.data]);

  const visible = React.useMemo(() => {
    return all.filter((l) => {
      const kind = loanKindOf(l.nbEcheances);
      switch (filter) {
        case "ALL":
          return true;
        case "ADVANCES":
          return kind === "ADVANCE";
        case "LOANS":
          return kind !== "ADVANCE";
        case "ACTIVE":
          return l.status === "IN_REPAYMENT";
        case "PENDING":
          return l.status === "PENDING";
      }
    });
  }, [all, filter]);

  const stats = React.useMemo(() => {
    let pendingCount = 0;
    let pendingAmount = 0;
    let activeCount = 0;
    let activeAmount = 0;
    let monthlyDue = 0;
    const byKind = new Map<LoanKind, number>();
    for (const l of all) {
      const montant = Number(l.montant ?? 0);
      const solde = Number(l.soldeRestant ?? 0);
      const monthly = Number(l.mensualite ?? 0);
      if (l.status === "PENDING") {
        pendingCount += 1;
        pendingAmount += montant;
      }
      if (l.status === "IN_REPAYMENT") {
        activeCount += 1;
        activeAmount += solde;
        monthlyDue += monthly;
        const k = loanKindOf(l.nbEcheances);
        byKind.set(k, (byKind.get(k) ?? 0) + solde);
      }
    }
    return { pendingCount, pendingAmount, activeCount, activeAmount, monthlyDue, byKind };
  }, [all]);

  const evolution = React.useMemo(() => buildEvolution(all, locale), [all, locale]);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["hrm", "loans"] });

  const approveM = useMutation({
    mutationFn: (id: string) => apiFetch(`/api/hrm/loans/${id}/approve`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("detail.approveSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const rejectM = useMutation({
    mutationFn: (id: string) =>
      apiFetch(`/api/hrm/loans/${id}/reject`, { method: "POST", body: { motif: "—" } }),
    onSuccess: () => {
      toast.success(t("detail.rejectSuccess"));
      invalidate();
    },
    onError: handleError,
  });

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          canCreate ? (
            <Link href="/loans/new">
              <Button>
                <Plus className="h-4 w-4" />
                {t("mine.newCta")}
              </Button>
            </Link>
          ) : undefined
        }
      />

      <div className="mb-5 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiTile
          label={t("kpi.pending")}
          value={formatNumber(stats.pendingCount, locale)}
          sub={t("kpi.pendingSub", {
            count: stats.pendingCount,
            amount: formatMoney(stats.pendingAmount, { locale, withCurrency: false }),
          })}
          tone="amber"
        />
        <KpiTile
          label={t("kpi.active")}
          value={formatNumber(stats.activeCount, locale)}
          sub={t("kpi.activeSub", {
            amount: formatMoney(stats.activeAmount, { locale, withCurrency: false }),
          })}
          tone="orange"
        />
        <KpiTile
          label={t("kpi.monthly")}
          value={compactCurrency(stats.monthlyDue)}
          sub={t("kpi.monthlySub")}
          tone="green"
        />
        <KpiTile label={t("kpi.incidents")} value="0%" sub={t("kpi.incidentsSub")} tone="blue" />
      </div>

      <div className="mb-5 grid grid-cols-1 gap-4 lg:grid-cols-[2fr_1fr]">
        <Card>
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line px-5 py-4">
            <span className="text-[14px] font-bold tracking-tight text-ink">
              {t("charts.evolution")}
            </span>
          </div>
          <div className="px-5 py-4">
            {evolution.length === 0 ? (
              <div className="py-8 text-center text-[12px] text-ink-3">
                {t("charts.evolutionEmpty")}
              </div>
            ) : (
              <LineChart series={evolution} />
            )}
          </div>
        </Card>

        <Card>
          <div className="border-b border-line px-5 py-4 text-[14px] font-bold tracking-tight text-ink">
            {t("charts.breakdown")}
          </div>
          <div className="flex flex-col items-center gap-4 px-5 py-5">
            <DonutBreakdown
              slices={kindSlices(stats.byKind)}
              centerValue={compactCurrency(stats.activeAmount)}
              centerSub={t("charts.breakdownCenterSub")}
            />
            <div className="w-full">
              {KIND_ORDER.map((kind) => {
                const value = stats.byKind.get(kind) ?? 0;
                return (
                  <div key={kind} className="flex items-center gap-2 py-1 text-[12px]">
                    <span
                      className="inline-block h-2.5 w-2.5 rounded-[3px]"
                      style={{ background: KIND_COLORS[kind] }}
                    />
                    <span className="flex-1 text-ink-2">{t(`kind.${kind}`)}</span>
                    <span className="font-mono-tabular font-semibold text-ink">
                      {compactCurrency(value)}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        </Card>
      </div>

      <Card>
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line px-5 py-4">
          <span className="text-[14px] font-bold tracking-tight text-ink">{t("table.title")}</span>
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
        </div>

        {loansQuery.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
          </div>
        ) : loansQuery.error ? (
          <div className="px-5 py-10 text-center text-ink-3">
            {loansQuery.error instanceof BffApiError ? loansQuery.error.message : "—"}
          </div>
        ) : visible.length === 0 ? (
          <div className="px-5 py-12 text-center text-[13px] text-ink-3">
            <Coins className="mx-auto mb-2 h-7 w-7 text-ink-4" />
            {t("table.empty")}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="border-b border-line bg-[linear-gradient(180deg,var(--color-bg-dim)_0%,var(--color-bg-soft)_100%)]">
                  <Th>{t("table.columns.ref")}</Th>
                  <Th>{t("table.columns.employee")}</Th>
                  <Th>{t("table.columns.type")}</Th>
                  <Th className="text-right">{t("table.columns.amount")}</Th>
                  <Th>{t("table.columns.term")}</Th>
                  <Th className="text-right">{t("table.columns.monthly")}</Th>
                  <Th className="text-right">{t("table.columns.remaining")}</Th>
                  <Th>{t("table.columns.progress")}</Th>
                  <Th>{t("table.columns.status")}</Th>
                  <Th className="text-right" />
                </tr>
              </thead>
              <tbody>
                {visible.map((l) => {
                  const kind = loanKindOf(l.nbEcheances);
                  const montant = Number(l.montant ?? 0);
                  const remaining = Number(l.soldeRestant ?? 0);
                  const monthly = Number(l.mensualite ?? 0);
                  const pct = loanProgressPct(montant, remaining);
                  return (
                    <tr
                      key={l.id}
                      className="cursor-pointer border-b border-line-soft last:border-0 hover:bg-bg-soft"
                      onClick={() => router.push(`/loans/${l.id}`)}
                    >
                      <td className="px-5 py-3 font-mono-tabular text-[11px] text-ink-3">
                        {shortLoanRef(l.id, kind)}
                      </td>
                      <td className="px-3 py-3">
                        <div className="flex items-center gap-2.5">
                          <Avatar name={nameOf(l.employeeId)} size="sm" />
                          <span className="text-[13px] font-semibold text-ink">
                            {nameOf(l.employeeId)}
                          </span>
                        </div>
                      </td>
                      <td className="px-3 py-3">
                        <Badge tone={loanKindTone(kind)}>{t(`kind.${kind}`)}</Badge>
                      </td>
                      <td className="px-3 py-3 text-right font-mono-tabular text-[13px] font-bold text-ink">
                        {formatMoney(montant, { locale, withCurrency: false })}
                      </td>
                      <td className="px-3 py-3 font-mono-tabular text-[12px] text-ink-3">
                        {t("table.term", { months: l.nbEcheances })}
                      </td>
                      <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] text-ink-2">
                        {formatMoney(monthly, { locale, withCurrency: false })}
                      </td>
                      <td
                        className={cn(
                          "px-3 py-3 text-right font-mono-tabular text-[12.5px]",
                          remaining > 0 ? "text-ink-2" : "text-ink-4",
                        )}
                      >
                        {formatMoney(remaining, { locale, withCurrency: false })}
                      </td>
                      <td className="px-3 py-3" style={{ minWidth: 140 }}>
                        <div className="flex items-center gap-2">
                          <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-bg-soft">
                            <div
                              className="h-full rounded-full bg-grad-orange"
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                          <span className="min-w-[32px] font-mono-tabular text-[11px] text-ink-3">
                            {pct}%
                          </span>
                        </div>
                      </td>
                      <td className="px-3 py-3">
                        <Badge tone={loanStatusTone(l.status)}>{t(`status.${l.status}`)}</Badge>
                      </td>
                      <td
                        className="px-5 py-3 text-right"
                        onClick={(e) => e.stopPropagation()}
                      >
                        {canApprove && l.status === "PENDING" ? (
                          <div className="flex justify-end gap-2">
                            <button
                              type="button"
                              aria-label={t("detail.reject")}
                              onClick={() => rejectM.mutate(l.id)}
                              className="grid h-7 w-7 place-items-center rounded-md border border-line bg-white text-ink-3 hover:border-danger-300 hover:text-danger-600"
                            >
                              <X className="h-3.5 w-3.5" />
                            </button>
                            <button
                              type="button"
                              aria-label={t("detail.approve")}
                              onClick={() => approveM.mutate(l.id)}
                              className="grid h-7 w-7 place-items-center rounded-md bg-grad-orange text-white shadow-orange-brand"
                            >
                              <Check className="h-3.5 w-3.5" />
                            </button>
                          </div>
                        ) : null}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </>
  );
}

// ────────────────────────────────────────────────────────────────────────
// Sub-components
// ────────────────────────────────────────────────────────────────────────

function KpiTile({
  label,
  value,
  sub,
  tone,
}: {
  label: string;
  value: React.ReactNode;
  sub: React.ReactNode;
  tone: "amber" | "orange" | "green" | "blue";
}) {
  const dot = {
    amber: "bg-warning-500",
    orange: "bg-orange-500",
    green: "bg-success-500",
    blue: "bg-info-500",
  }[tone];
  return (
    <div className="relative flex flex-col gap-1 overflow-hidden rounded-[16px] border border-line bg-white px-[18px] py-4 shadow-xs-brand">
      <div className="flex items-center justify-between">
        <span className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">
          {label}
        </span>
        <span className={cn("inline-block h-2 w-2 rounded-full", dot)} />
      </div>
      <div className="font-display font-mono-tabular text-[24px] font-extrabold tracking-tight text-ink">
        {value}
      </div>
      <div className="text-[11px] text-ink-3">{sub}</div>
    </div>
  );
}

function Th({ children, className }: { children?: React.ReactNode; className?: string }) {
  return (
    <th
      className={cn(
        "px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3 first:pl-5 last:pr-5",
        className,
      )}
    >
      {children}
    </th>
  );
}

const KIND_ORDER: LoanKind[] = ["HOUSING", "VEHICLE", "PERSONAL", "ADVANCE"];

function kindSlices(byKind: Map<LoanKind, number>): { color: string; pct: number }[] {
  const total = [...byKind.values()].reduce((a, b) => a + b, 0);
  if (total === 0) return KIND_ORDER.map((k) => ({ color: KIND_COLORS[k], pct: 25 }));
  return KIND_ORDER.map((k) => ({
    color: KIND_COLORS[k],
    pct: ((byKind.get(k) ?? 0) / total) * 100,
  }));
}

function compactCurrency(n: number): string {
  if (n >= 1_000_000_000) return `${(n / 1_000_000_000).toFixed(1)}G`;
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1).replace(/\.0$/, "")}M`;
  if (n >= 1_000) return `${(n / 1_000).toFixed(0)}k`;
  return String(Math.round(n));
}

// Build 12-month outstanding-balance evolution from the loans collection.
// For each month we sum the principal still due, approximated by linear
// amortisation between dateDebut and (dateDebut + nbEcheances months).
function buildEvolution(
  loans: LoanAdvanceResponse[],
  locale: "fr" | "en",
): { label: string; value: number }[] {
  const now = new Date();
  const months: Date[] = [];
  for (let i = 11; i >= 0; i--) {
    months.push(new Date(now.getFullYear(), now.getMonth() - i, 1));
  }
  const labelsFr = ["Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"];
  const labelsEn = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  const labels = locale === "fr" ? labelsFr : labelsEn;

  return months.map((m) => {
    let total = 0;
    for (const l of loans) {
      if (l.status === "REJECTED" || l.status === "PENDING") continue;
      const debut = new Date(l.dateDebut);
      if (m < new Date(debut.getFullYear(), debut.getMonth(), 1)) continue;
      const monthsSince =
        (m.getFullYear() - debut.getFullYear()) * 12 + (m.getMonth() - debut.getMonth());
      const montant = Number(l.montant ?? 0);
      const monthly = Number(l.mensualite ?? 0);
      const remaining = Math.max(0, montant - monthly * monthsSince);
      total += remaining;
    }
    return { label: labels[m.getMonth()] ?? "", value: total };
  });
}

function LineChart({ series }: { series: { label: string; value: number }[] }) {
  const W = 720;
  const H = 220;
  const padL = 44,
    padR = 14,
    padT = 14,
    padB = 30;
  const innerW = W - padL - padR;
  const innerH = H - padT - padB;
  const values = series.map((p) => p.value);
  const max = Math.max(1, ...values);
  const min = Math.min(0, ...values);
  const range = Math.max(1, max - min);
  const step = series.length > 1 ? innerW / (series.length - 1) : innerW;

  const pts = series.map((p, i) => ({
    x: padL + step * i,
    y: padT + (1 - (p.value - min) / range) * innerH,
  }));
  const path = pts.map((p, i) => `${i === 0 ? "M" : "L"}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(" ");
  const last = pts[pts.length - 1] ?? { x: padL, y: padT + innerH };
  const areaPath = `${path} L${last.x.toFixed(1)},${padT + innerH} L${pts[0]!.x.toFixed(1)},${padT + innerH} Z`;

  const gridLines = 4;
  const gridYs = Array.from({ length: gridLines + 1 }, (_, i) => padT + (innerH / gridLines) * i);
  const gridVs = Array.from({ length: gridLines + 1 }, (_, i) =>
    Math.round(max - (range / gridLines) * i),
  );

  return (
    <svg
      viewBox={`0 0 ${W} ${H}`}
      className="h-[220px] w-full"
      role="img"
      aria-label="Outstanding balance evolution"
    >
      <defs>
        <linearGradient id="loanEvoArea" x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stopColor="#F97316" stopOpacity="0.32" />
          <stop offset="100%" stopColor="#F97316" stopOpacity="0" />
        </linearGradient>
      </defs>
      {gridYs.map((y, i) => (
        <g key={i}>
          <line x1={padL} x2={W - padR} y1={y} y2={y} stroke="#E5E7EB" strokeDasharray="3 4" />
          <text x={padL - 8} y={y + 4} textAnchor="end" fontSize="10" fill="#9CA3AF">
            {compactCurrency(gridVs[i] ?? 0)}
          </text>
        </g>
      ))}
      <path d={areaPath} fill="url(#loanEvoArea)" />
      <path
        d={path}
        fill="none"
        stroke="#F97316"
        strokeWidth="2.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      {pts.map((p, i) => (
        <circle key={i} cx={p.x} cy={p.y} r="3" fill="#fff" stroke="#F97316" strokeWidth="2" />
      ))}
      {series.map((p, i) => (
        <text
          key={i}
          x={padL + step * i}
          y={H - 8}
          textAnchor="middle"
          fontSize="10"
          fill="#9CA3AF"
        >
          {p.label}
        </text>
      ))}
    </svg>
  );
}

function DonutBreakdown({
  slices,
  centerValue,
  centerSub,
}: {
  slices: { color: string; pct: number }[];
  centerValue: string;
  centerSub: string;
}) {
  const size = 160;
  const thick = 22;
  const r = (size - thick) / 2;
  const cx = size / 2,
    cy = size / 2;
  let offset = 0;
  const sum = slices.reduce((a, b) => a + b.pct, 0) || 1;
  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg viewBox={`0 0 ${size} ${size}`} className="h-full w-full -rotate-90">
        <circle cx={cx} cy={cy} r={r} fill="none" stroke="#F3F4F6" strokeWidth={thick} />
        {slices.map((s, idx) => {
          const frac = s.pct / sum;
          const len = 2 * Math.PI * r;
          const dash = `${frac * len} ${len - frac * len}`;
          const dashOffset = -offset;
          offset += frac * len;
          return (
            <circle
              key={idx}
              cx={cx}
              cy={cy}
              r={r}
              fill="none"
              stroke={s.color}
              strokeWidth={thick}
              strokeDasharray={dash}
              strokeDashoffset={dashOffset}
              strokeLinecap="butt"
            />
          );
        })}
      </svg>
      <div className="absolute inset-0 grid place-items-center text-center">
        <div>
          <div className="font-display font-mono-tabular text-[22px] font-extrabold tracking-tight text-ink">
            {centerValue}
          </div>
          <div className="text-[11px] text-ink-3">{centerSub}</div>
        </div>
      </div>
    </div>
  );
}
