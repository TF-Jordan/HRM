"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  CheckCircle2,
  ChevronRight,
  Clock,
  FileText,
  Link2,
  Loader2,
  Plus,
  Send,
  Wallet,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatNumber } from "@/lib/format";
import { expenseStatusTone } from "@/lib/expense-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  ExpenseLineResponse,
  ExpenseReportResponse,
  ExpenseReportStatus,
} from "@/server/ksm/modules/expenses";

type MinePayload = {
  employee: EmployeeResponse | null;
  reports: ExpenseReportResponse[];
  lines: ExpenseLineResponse[];
};

type Filter = "ALL" | ExpenseReportStatus;

const CATEGORY_COLOR: Record<string, string> = {
  TRANSPORT: "#3B82F6",
  REPAS: "#F97316",
  MEALS: "#F97316",
  HEBERGEMENT: "#8B5CF6",
  LODGING: "#8B5CF6",
  MATERIEL: "#16A34A",
  SUPPLIES: "#16A34A",
  AUTRES: "#94A3B8",
};

function num(v: number | string | null | undefined): number {
  if (v == null) return 0;
  const n = typeof v === "string" ? parseFloat(v) : v;
  return isNaN(n) ? 0 : n;
}

function periodeYear(periode: string): string {
  return periode?.split("-")[0] ?? "";
}

function periodeMonth(periode: string): number {
  return Number(periode?.split("-")[1] ?? 0);
}

function monthShort(monthIndex: number, locale: "fr" | "en"): string {
  const d = new Date(2000, monthIndex - 1, 1);
  return d
    .toLocaleDateString(locale === "fr" ? "fr-FR" : "en-US", { month: "short" })
    .replace(".", "");
}

function shortRef(uuid: string): string {
  return `NF-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}

function categoryLabel(categorie: string | null, tCat: (k: string) => string): string {
  const up = (categorie ?? "AUTRES").toUpperCase();
  const known = ["TRANSPORT", "REPAS", "HEBERGEMENT", "MATERIEL", "AUTRES"];
  return known.includes(up) ? tCat(up) : (categorie ?? "—");
}

// ─── Monthly bar chart ──────────────────────────────────────────────────────

function MonthlyChart({
  reports,
  year,
  locale,
}: {
  reports: ExpenseReportResponse[];
  year: string;
  locale: "fr" | "en";
}) {
  const t = useTranslations("expenses");
  const byMonth = new Map<number, number>();
  reports
    .filter((r) => periodeYear(r.periode) === year)
    .forEach((r) => {
      const m = periodeMonth(r.periode);
      byMonth.set(m, (byMonth.get(m) ?? 0) + num(r.totalMontant));
    });
  const max = Math.max(1, ...byMonth.values());

  return (
    <Card>
      <div className="border-b border-line px-6 py-4">
        <div className="font-display text-[15px] font-bold text-ink">{t("mine.chart.title")}</div>
        <div className="text-[12px] text-ink-3">{t("mine.chart.subtitle", { year })}</div>
      </div>
      <CardContent className="p-6">
        {byMonth.size === 0 ? (
          <div className="flex h-40 items-center justify-center text-[13px] text-ink-3">
            {t("mine.chart.empty")}
          </div>
        ) : (
          <div className="flex h-44 items-end gap-2">
            {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => {
              const v = byMonth.get(m) ?? 0;
              const h = (v / max) * 100;
              return (
                <div key={m} className="flex flex-1 flex-col items-center gap-1.5">
                  <div className="flex h-full w-full items-end overflow-hidden rounded-md bg-bg-dim">
                    <div
                      className="w-full transition-all"
                      style={{ height: `${h}%`, background: v > 0 ? "var(--grad-orange, #F97316)" : "transparent" }}
                      title={v > 0 ? `${monthShort(m, locale)} · ${formatNumber(v, locale)} XAF` : undefined}
                    />
                  </div>
                  <span className={cn("text-[10px] font-medium", v > 0 ? "text-ink-3" : "text-ink-4/50")}>
                    {monthShort(m, locale)}
                  </span>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

// ─── Category donut ─────────────────────────────────────────────────────────

function CategoryDonut({
  segments,
  year,
  locale,
}: {
  segments: { label: string; raw: string; value: number }[];
  year: string;
  locale: "fr" | "en";
}) {
  const t = useTranslations("expenses");
  const total = segments.reduce((s, x) => s + x.value, 0);

  const stops = segments
    .map((s, i) => {
      const before = segments.slice(0, i).reduce((sum, x) => sum + x.value, 0);
      const from = (before / Math.max(total, 1)) * 360;
      const to = ((before + s.value) / Math.max(total, 1)) * 360;
      return `${CATEGORY_COLOR[s.raw.toUpperCase()] ?? "#94A3B8"} ${from}deg ${to}deg`;
    })
    .join(", ");

  return (
    <Card>
      <div className="border-b border-line px-6 py-4">
        <div className="font-display text-[15px] font-bold text-ink">{t("mine.distribution.title")}</div>
        <div className="text-[12px] text-ink-3">{t("mine.distribution.subtitle", { year })}</div>
      </div>
      <CardContent className="p-6">
        {total === 0 ? (
          <div className="flex h-40 items-center justify-center text-center text-[13px] text-ink-3">
            {t("mine.distribution.empty")}
          </div>
        ) : (
          <div className="flex items-center gap-5">
            <div className="relative h-28 w-28 shrink-0">
              <div className="h-full w-full rounded-full" style={{ background: `conic-gradient(${stops})` }} />
              <div className="absolute inset-[20%] flex flex-col items-center justify-center rounded-full bg-white text-center">
                <span className="tabular-nums text-[13px] font-extrabold text-ink">{formatNumber(total, locale)}</span>
                <span className="text-[9px] uppercase tracking-wide text-ink-3">XAF</span>
              </div>
            </div>
            <div className="flex-1 space-y-2">
              {segments.map((s) => (
                <div key={s.raw} className="flex items-center gap-2.5">
                  <span
                    className="h-2.5 w-2.5 shrink-0 rounded-sm"
                    style={{ background: CATEGORY_COLOR[s.raw.toUpperCase()] ?? "#94A3B8" }}
                  />
                  <span className="flex-1 text-[12.5px] text-ink-2">{s.label}</span>
                  <span className="tabular-nums text-[12.5px] font-semibold text-ink">
                    {formatNumber(s.value, locale)}
                  </span>
                  <span className="w-9 text-right text-[11px] text-ink-3">
                    {Math.round((s.value / total) * 100)}%
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────

export function MyExpenses() {
  const t = useTranslations("expenses");
  const tCat = useTranslations("expenses.category");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const queryClient = useQueryClient();
  const [filter, setFilter] = React.useState<Filter>("ALL");
  const currentYear = new Date().getFullYear().toString();
  const [activeYear, setActiveYear] = React.useState(currentYear);
  const [submittingId, setSubmittingId] = React.useState<string | null>(null);

  const query = useQuery({
    queryKey: ["hrm", "expenses", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/expenses/mine"),
    refetchInterval: 60_000,
  });

  const reports = React.useMemo(() => query.data?.reports ?? [], [query.data]);
  const lines = React.useMemo(() => query.data?.lines ?? [], [query.data]);

  const submitM = useMutation({
    mutationFn: (id: string) =>
      apiFetch<ExpenseReportResponse>(`/api/hrm/expenses/${id}/submit`, { method: "POST" }),
    onMutate: (id) => setSubmittingId(id),
    onSuccess: () => {
      toast.success(t("mine.submitSuccess"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "expenses"] });
    },
    onError: (cause) =>
      toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown")),
    onSettled: () => setSubmittingId(null),
  });

  const years = React.useMemo(
    () => [...new Set(reports.map((r) => periodeYear(r.periode)))].sort((a, b) => b.localeCompare(a)),
    [reports],
  );
  const effectiveYear = years.includes(activeYear) ? activeYear : (years[0] ?? currentYear);

  // Global status aggregates (pending refunds are not year-bound)
  const agg = React.useMemo(() => {
    const a = {
      draftCount: 0,
      submittedCount: 0,
      submittedAmount: 0,
      approvedCount: 0,
      approvedAmount: 0,
      reimbursedYearAmount: 0,
    };
    for (const r of reports) {
      const amt = num(r.totalMontant);
      if (r.status === "DRAFT") a.draftCount += 1;
      else if (r.status === "SUBMITTED") {
        a.submittedCount += 1;
        a.submittedAmount += amt;
      } else if (r.status === "APPROVED") {
        a.approvedCount += 1;
        a.approvedAmount += amt;
      } else if (r.status === "REIMBURSED" && periodeYear(r.periode) === effectiveYear) {
        a.reimbursedYearAmount += amt;
      }
    }
    return a;
  }, [reports, effectiveYear]);

  const refundPending = agg.submittedAmount + agg.approvedAmount;

  // Category breakdown for the active year (lines joined to their report's period)
  const categorySegments = React.useMemo(() => {
    const periodeByReport = new Map(reports.map((r) => [r.id, r.periode]));
    const totals = new Map<string, number>();
    for (const l of lines) {
      const periode = periodeByReport.get(l.expenseReportId);
      if (!periode || periodeYear(periode) !== effectiveYear) continue;
      const raw = (l.categorie ?? "AUTRES").toUpperCase();
      totals.set(raw, (totals.get(raw) ?? 0) + num(l.montant));
    }
    return [...totals.entries()]
      .map(([raw, value]) => ({ raw, value, label: categoryLabel(raw, tCat) }))
      .sort((a, b) => b.value - a.value);
  }, [lines, reports, effectiveYear, tCat]);

  // Per-report line counts
  const lineCountByReport = React.useMemo(() => {
    const m = new Map<string, number>();
    for (const l of lines) m.set(l.expenseReportId, (m.get(l.expenseReportId) ?? 0) + 1);
    return m;
  }, [lines]);

  // Table: scoped to selected year + status filter
  const visible = reports
    .filter((r) => periodeYear(r.periode) === effectiveYear)
    .filter((r) => (filter === "ALL" ? true : r.status === filter))
    .sort((a, b) => b.periode.localeCompare(a.periode));

  const filters: { key: Filter; label: string }[] = [
    { key: "ALL", label: t("mine.filters.all") },
    { key: "DRAFT", label: t("mine.filters.drafts") },
    { key: "SUBMITTED", label: t("mine.filters.submitted") },
    { key: "APPROVED", label: t("mine.filters.approved") },
    { key: "REIMBURSED", label: t("mine.filters.reimbursed") },
    { key: "REJECTED", label: t("mine.filters.rejected") },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("mine.title") }]}
        title={t("mine.title")}
        subtitle={t("mine.subtitle")}
        actions={
          <Link href="/expenses/new">
            <Button>
              <Plus className="h-4 w-4" />
              {t("mine.newReport")}
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
      ) : (
        <div className="space-y-5">
          {/* ── Hero: remboursement en cours ─────────────────────────── */}
          <div
            className="relative overflow-hidden rounded-[18px] p-6"
            style={{ background: "linear-gradient(135deg, #1A150E 0%, #2D2520 100%)", color: "#fff" }}
          >
            <div
              className="pointer-events-none absolute -right-24 -top-24 h-80 w-80 rounded-full"
              style={{ background: "radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)" }}
            />
            <div className="relative flex flex-wrap items-end gap-8">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 text-[11px] uppercase tracking-wider opacity-60">
                  <Wallet size={14} /> {t("mine.refundPending")}
                </div>
                <div className="mt-1.5 tabular-nums font-display text-[34px] font-extrabold" style={{ color: "#FFB066" }}>
                  {formatNumber(refundPending, locale)}{" "}
                  <span className="text-[14px] opacity-60" style={{ color: "#fff" }}>XAF</span>
                </div>
                <div className="mt-1 text-[12px] opacity-70">{t("mine.refundPendingSub")}</div>
                <div className="mt-4 flex flex-wrap gap-2">
                  <span className="flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1 text-[12px]">
                    <Clock size={12} className="text-amber-300" />
                    {t("mine.pendingValidation", { count: agg.submittedCount })}
                  </span>
                  <span className="flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1 text-[12px]">
                    <CheckCircle2 size={12} className="text-sky-300" />
                    {t("mine.toPay", { count: agg.approvedCount })}
                  </span>
                </div>
              </div>
              <div className="rounded-2xl bg-white/10 px-5 py-4">
                <div className="text-[11px] uppercase tracking-wider opacity-60">
                  {t("mine.reimbursedYear", { year: effectiveYear })}
                </div>
                <div className="mt-1 tabular-nums font-display text-[22px] font-extrabold text-white">
                  {formatNumber(agg.reimbursedYearAmount, locale)}{" "}
                  <span className="text-[12px] opacity-60">XAF</span>
                </div>
              </div>
            </div>
          </div>

          {/* ── Brouillons à soumettre ───────────────────────────────── */}
          {agg.draftCount > 0 && (
            <div className="flex items-center gap-2 rounded-[14px] border border-amber-200 bg-amber-50 px-4 py-3 text-[13px] text-amber-800">
              <FileText size={15} />
              {t("mine.draftsBanner", { count: agg.draftCount })}
              <button
                type="button"
                onClick={() => setFilter("DRAFT")}
                className="ml-auto rounded-full bg-amber-100 px-3 py-1 text-[12px] font-semibold text-amber-800 hover:bg-amber-200"
              >
                {t("mine.filters.drafts")}
              </button>
            </div>
          )}

          {/* ── Graphe mensuel + répartition catégories ──────────────── */}
          <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
            <div className="lg:col-span-2">
              <MonthlyChart reports={reports} year={effectiveYear} locale={locale} />
            </div>
            <CategoryDonut segments={categorySegments} year={effectiveYear} locale={locale} />
          </div>

          {/* ── Filtres ──────────────────────────────────────────────── */}
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-wrap gap-2">
              {filters.map((f) => (
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
                  {f.label}
                </button>
              ))}
            </div>
            {years.length > 1 && (
              <div className="flex gap-1.5">
                {years.map((y) => (
                  <button
                    key={y}
                    type="button"
                    onClick={() => setActiveYear(y)}
                    className={cn(
                      "rounded-full px-3 py-1 text-[12px] font-semibold transition-colors",
                      effectiveYear === y
                        ? "bg-orange-500 text-white"
                        : "bg-bg-dim text-ink-2 hover:bg-orange-50 hover:text-orange-700",
                    )}
                  >
                    {y}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* ── Table ────────────────────────────────────────────────── */}
          <Card>
            {visible.length === 0 ? (
              <div className="flex h-32 items-center justify-center px-6 text-center text-[13px] text-ink-3">
                {t("mine.empty")}
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
                  <thead>
                    <tr className="border-b border-line bg-bg-dim">
                      {[
                        t("mine.columns.reference"),
                        t("mine.columns.objet"),
                        t("mine.columns.period"),
                        t("mine.columns.amount"),
                        t("mine.columns.status"),
                        "",
                      ].map((h, i) => (
                        <th
                          key={i}
                          style={{
                            textAlign: i === 3 ? "right" : "left",
                            padding: "10px 16px",
                            fontSize: 11,
                            fontWeight: 600,
                            color: "var(--ink-3)",
                            textTransform: "uppercase",
                            letterSpacing: "0.05em",
                            whiteSpace: "nowrap",
                          }}
                        >
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {visible.map((r) => {
                      const count = lineCountByReport.get(r.id) ?? 0;
                      const isSubmitting = submittingId === r.id && submitM.isPending;
                      return (
                        <tr
                          key={r.id}
                          onClick={() => router.push(`/expenses/${r.id}`)}
                          className="cursor-pointer border-b border-line/60 transition-colors hover:bg-orange-50/40"
                        >
                          <td style={{ padding: "12px 16px" }}>
                            <span className="font-mono text-[11px] text-ink-3">{shortRef(r.id)}</span>
                          </td>
                          <td style={{ padding: "12px 16px" }}>
                            <div className="flex items-center gap-3">
                              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-orange-100 text-orange-600">
                                <FileText size={15} />
                              </div>
                              <div className="min-w-0">
                                <div className="truncate text-[13.5px] font-semibold text-ink">{r.motif ?? "—"}</div>
                                <div className="flex items-center gap-2 text-[11px] text-ink-3">
                                  <span>{t("mine.lineCount", { count })}</span>
                                  {r.missionOrderId && (
                                    <span className="inline-flex items-center gap-1 text-info-600">
                                      <Link2 size={11} /> {t("mine.mission")}
                                    </span>
                                  )}
                                </div>
                              </div>
                            </div>
                          </td>
                          <td style={{ padding: "12px 16px" }} className="text-ink-2">
                            {r.periode}
                          </td>
                          <td style={{ padding: "12px 16px", textAlign: "right" }} className="tabular-nums font-semibold text-ink">
                            {formatNumber(num(r.totalMontant), locale)}
                          </td>
                          <td style={{ padding: "12px 16px" }}>
                            <Badge tone={expenseStatusTone(r.status)}>{t(`status.${r.status}`)}</Badge>
                          </td>
                          <td style={{ padding: "12px 16px" }}>
                            <div className="flex items-center justify-end gap-1.5">
                              {r.status === "DRAFT" && (
                                <button
                                  type="button"
                                  disabled={isSubmitting}
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    submitM.mutate(r.id);
                                  }}
                                  className="inline-flex items-center gap-1 rounded-lg border border-orange-300 bg-white px-2.5 py-1 text-[11.5px] font-semibold text-orange-600 transition-colors hover:bg-orange-50 disabled:opacity-50"
                                >
                                  {isSubmitting ? (
                                    <Loader2 size={12} className="animate-spin" />
                                  ) : (
                                    <Send size={12} />
                                  )}
                                  {t("mine.quickSubmit")}
                                </button>
                              )}
                              <button
                                type="button"
                                aria-label={t("mine.viewDetail")}
                                className="flex h-7 w-7 items-center justify-center rounded-lg border border-line bg-white text-ink-3 transition-colors hover:border-orange-300 hover:text-orange-600"
                              >
                                <ChevronRight size={12} />
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
        </div>
      )}
    </>
  );
}
