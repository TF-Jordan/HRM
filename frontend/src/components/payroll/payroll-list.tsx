"use client";

import { Document, Image, Page, StyleSheet, Text, View, pdf } from "@react-pdf/renderer";
import { useQuery } from "@tanstack/react-query";
import {
  ChevronRight,
  Download,
  FileSignature,
  Loader2,
  Mail,
  Plus,
  Printer,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Chip } from "@/components/ui/chip";
import { IconTile } from "@/components/ui/icon-tile";
import { useCan } from "@/hooks/use-can";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney, formatDate } from "@/lib/format";
import { toast } from "sonner";
import {
  formatPeriodFr,
  getPayrollWindowState,
  isPayrollRunTerminal,
  payrollStatusProgress,
  payrollStatusTone,
  payrollStepperState,
} from "@/lib/payroll-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  PayrollEntryResponse,
  PayrollRunResponse,
  PayrollRunStatus,
  PayslipLineResponse,
} from "@/server/ksm/modules/payroll";

export function PayrollList() {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const canCreate = useCan("hrm:payroll:run");

  const query = useQuery({
    queryKey: ["hrm", "payroll", "runs"],
    queryFn: () => apiFetch<PayrollRunResponse[]>("/api/hrm/payroll"),
    refetchInterval: 60_000,
  });

  const runs = React.useMemo(() => {
    return (query.data ?? [])
      .slice()
      .sort((a, b) => (a.periode < b.periode ? 1 : -1));
  }, [query.data]);

  const active = runs.find((r) => !isPayrollRunTerminal(r.status)) ?? runs[0] ?? null;

  // Only compute once data is loaded so an empty array doesn't look like "no run this month"
  const windowState = React.useMemo(
    () => (query.isSuccess ? getPayrollWindowState(runs) : null),
    [query.isSuccess, runs],
  );

  const years = React.useMemo(() => {
    const yMap = new Map<string, number>();
    for (const r of runs) {
      const y = r.periode.slice(0, 4);
      yMap.set(y, (yMap.get(y) ?? 0) + 1);
    }
    return [...yMap.entries()].sort((a, b) => (a[0] < b[0] ? 1 : -1));
  }, [runs]);

  const [yearFilter, setYearFilter] = React.useState<string | null>(null);
  const visibleRuns = yearFilter ? runs.filter((r) => r.periode.startsWith(yearFilter)) : runs;

  const evolution = React.useMemo(() => buildEvolution(runs), [runs]);

  return (
    <>
      <PageHeader
        ucBadge={t("uc")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <>
            {canCreate && (
              <div className="flex flex-col items-end gap-1">
                {windowState && !windowState.canRun && windowState.reason === "already_run" ? (
                  <Button disabled>
                    <Plus className="h-4 w-4" />
                    {t("actions.newRun")}
                  </Button>
                ) : (
                  <Link href="/payroll/new">
                    <Button disabled={!windowState}>
                      <Plus className="h-4 w-4" />
                      {t("actions.newRun")}
                    </Button>
                  </Link>
                )}
                {windowState && (
                  <span className="text-[11px] text-ink-3">
                    {windowState.canRun
                      ? t("cycle.openWindow", { days: windowState.daysUntilMonthEnd })
                      : windowState.reason === "already_run"
                      ? t("cycle.alreadyRun")
                      : t("cycle.tooEarlyHint", { days: windowState.daysUntilOpen })}
                  </span>
                )}
              </div>
            )}
          </>
        }
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : (
        <div className="flex flex-col gap-5">
          <Hero run={active} t={t} locale={locale} />
          <EvolutionRow runs={runs} evolution={evolution} active={active} t={t} locale={locale} />
          <Card>
            <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
              <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("table.title")}</h3>
              <div className="flex items-center gap-2">
                {years.map(([y, c], idx) => (
                  <Chip
                    key={y}
                    active={(yearFilter ?? years[0]?.[0]) === y || (yearFilter === null && idx === 0)}
                    tone={idx === 0 ? "orange" : "default"}
                    onClick={() => setYearFilter(y)}
                  >
                    {t("chart.yearChip", { year: y, count: c })}
                  </Chip>
                ))}
              </div>
            </div>
            <RunsTable runs={visibleRuns} t={t} locale={locale} />
          </Card>
          {active && <InlinePayslip runId={active.id} t={t} locale={locale} />}
        </div>
      )}
    </>
  );
}

function Hero({
  run,
  t,
  locale,
}: {
  run: PayrollRunResponse | null;
  t: ReturnType<typeof useTranslations<"payroll">>;
  locale: "fr" | "en";
}) {
  if (!run) {
    return (
      <div
        className="relative overflow-hidden rounded-[20px] p-7 text-white shadow-lg-brand"
        style={{ background: "linear-gradient(135deg, #1A150E 0%, #2D2520 100%)" }}
      >
        <div className="text-[11px] font-semibold uppercase tracking-[0.06em] text-orange-300/80">
          {t("hero.noRun")}
        </div>
        <p className="mt-2 text-[14px] text-white/70">{t("hero.noRunSub")}</p>
      </div>
    );
  }
  const stepStates = payrollStepperState(run.status);
  const stepEntries: { key: keyof typeof stepStates; label: string }[] = [
    { key: "variables", label: t("stepper.variables") },
    { key: "calculation", label: t("stepper.calculation") },
    { key: "review", label: t("stepper.review") },
    { key: "validation", label: t("stepper.validation") },
    { key: "payment", label: t("stepper.payment") },
  ];
  const total = Number(run.totalGross ?? 0);
  const net = Number(run.totalNet ?? 0);
  const deductionsEmploye = Number(run.totalEmployeeDeductions ?? 0);

  return (
    <div
      className="relative overflow-hidden rounded-[20px] p-7 text-white shadow-lg-brand"
      style={{ background: "linear-gradient(135deg, #1A150E 0%, #2D2520 100%)" }}
    >
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -right-24 -top-24 h-[400px] w-[400px]"
        style={{
          background: "radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)",
        }}
      />
      <div className="relative flex flex-wrap items-start gap-6">
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <span className="inline-flex items-center rounded-full bg-orange-500/20 px-2.5 py-0.5 text-[11px] font-semibold text-orange-300">
              {t("status.active")}
            </span>
            <span className="font-mono-tabular text-[11px] text-white/55">
              PR-{run.periode}
            </span>
          </div>
          <div className="font-display mt-2 text-[30px] font-extrabold tracking-tight text-white">
            {t("title")} · {formatPeriodFr(run.periode)}
          </div>
          <div className="mt-1 text-[13.5px] text-white/65">
            {run.calculatedAt
              ? t("hero.calculatedOn", { date: formatDate(run.calculatedAt, { locale }) })
              : t("hero.openedOn")}
          </div>

          <div className="mt-6 flex flex-wrap items-center gap-2">
            {stepEntries.map((s, idx) => (
              <React.Fragment key={s.key}>
                <span
                  className={cn(
                    "rounded-full px-3 py-1.5 text-[11px] font-semibold tracking-wide",
                    stepStates[s.key] === "done" &&
                      "bg-success-500/20 text-success-300",
                    stepStates[s.key] === "active" &&
                      "bg-grad-orange text-white shadow-orange-brand",
                    stepStates[s.key] === "pending" &&
                      "bg-white/10 text-white/55",
                  )}
                >
                  {s.label}
                </span>
                {idx < stepEntries.length - 1 && (
                  <span className="text-white/35">→</span>
                )}
              </React.Fragment>
            ))}
          </div>
        </div>

        <div className="flex w-full flex-col gap-2 sm:w-auto sm:min-w-[210px]">
          <Link href={`/payroll/${run.id}`}>
            <Button className="w-full">{heroCtaLabel(run.status, t)}</Button>
          </Link>
          <Link href={`/payroll/${run.id}`}>
            <Button
              variant="secondary"
              className="w-full border-white/20 bg-white/10 text-white hover:bg-white/15"
            >
              <Printer className="h-3.5 w-3.5" />
              {t("actions.previewPayslip")}
            </Button>
          </Link>
        </div>
      </div>

      <div className="relative mt-7 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <HeroStat
          label={t("hero.employeesPaid")}
          value={String(run.nbEmployes)}
          sub={t("hero.employeesPaidSub", { total: run.nbEmployes })}
        />
        <HeroStat
          label={t("hero.grossMass")}
          value={formatMass(total, locale)}
          sub={t("hero.grossMassSub")}
        />
        <HeroStat
          label={t("hero.deductions")}
          value={formatMass(deductionsEmploye, locale)}
          sub={t("hero.deductionsSub")}
        />
        <HeroStat
          label={t("hero.net")}
          value={formatMass(net, locale)}
          sub={t("hero.netSub")}
        />
      </div>
    </div>
  );
}

function HeroStat({ label, value, sub }: { label: string; value: string; sub: string }) {
  return (
    <div className="rounded-[12px] border border-white/10 bg-white/[0.06] px-4 py-3.5">
      <div className="text-[11px] font-medium uppercase tracking-[0.05em] text-white/65">
        {label}
      </div>
      <div className="font-display font-mono-tabular mt-1.5 text-[24px] font-extrabold tracking-tight">
        {value}
      </div>
      <div className="mt-1 text-[11px] text-white/55">{sub}</div>
    </div>
  );
}

function EvolutionRow({
  evolution,
  active,
  t,
  locale,
}: {
  runs: PayrollRunResponse[];
  evolution: { label: string; value: number }[];
  active: PayrollRunResponse | null;
  t: ReturnType<typeof useTranslations<"payroll">>;
  locale: "fr" | "en";
}) {
  const total = active ? Number(active.totalGross ?? 0) : 0;
  const baseShare = 0.62, primesShare = 0.18, hsShare = 0.12, avantShare = 0.08;
  const slices = [
    { color: "#F97316", label: t("chart.salaireBase"), pct: baseShare, value: total * baseShare },
    { color: "#FB923C", label: t("chart.primes"), pct: primesShare, value: total * primesShare },
    { color: "#FCD34D", label: t("chart.heuresSupp"), pct: hsShare, value: total * hsShare },
    { color: "#34D399", label: t("chart.avantages"), pct: avantShare, value: total * avantShare },
  ];

  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-[2fr_1fr]">
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">
            {t("chart.evolution")}
          </h3>
        </div>
        <div className="px-6 py-5">
          {evolution.length === 0 ? (
            <div className="grid h-[220px] place-items-center text-[13px] text-ink-3">
              {t("chart.empty")}
            </div>
          ) : (
            <LineChart series={evolution} />
          )}
        </div>
      </Card>

      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">
            {t("chart.composition", {
              periode: active ? formatPeriodFr(active.periode) : "—",
            })}
          </h3>
        </div>
        <div className="flex flex-col items-center gap-4 px-5 py-5">
          <DonutCenter
            slices={slices}
            centerValue={total > 0 ? formatMass(total, locale) : "—"}
            centerSub={t("chart.grossLabel")}
          />
          <div className="w-full">
            {slices.map((s) => (
              <div key={s.label} className="flex items-center gap-2 py-1.5 text-[12.5px]">
                <span className="h-2.5 w-2.5 rounded-sm" style={{ background: s.color }} />
                <span className="flex-1 truncate text-ink-2">{s.label}</span>
                <span className="font-mono-tabular text-ink-2">
                  {total > 0 ? formatMass(s.value, locale) : "—"}
                </span>
                <span className="font-mono-tabular ml-2 w-10 text-right text-ink-3">
                  {Math.round(s.pct * 100)}%
                </span>
              </div>
            ))}
          </div>
        </div>
      </Card>
    </div>
  );
}

function RunsTable({
  runs,
  t,
  locale,
}: {
  runs: PayrollRunResponse[];
  t: ReturnType<typeof useTranslations<"payroll">>;
  locale: "fr" | "en";
}) {
  if (runs.length === 0) {
    return <div className="px-6 py-10 text-center text-[13px] text-ink-3">{t("table.empty")}</div>;
  }
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-[13px]">
        <thead>
          <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
            <th className="px-6 py-3">{t("table.period")}</th>
            <th className="px-4 py-3">{t("table.status")}</th>
            <th className="px-4 py-3 text-right">{t("table.employees")}</th>
            <th className="px-4 py-3 text-right">{t("table.gross")}</th>
            <th className="px-4 py-3 text-right">{t("table.net")}</th>
            <th className="px-4 py-3">{t("table.date")}</th>
            <th className="px-6 py-3" />
          </tr>
        </thead>
        <tbody className="divide-y divide-line-soft">
          {runs.map((r) => {
            const tone = payrollStatusTone(r.status);
            const progress = payrollStatusProgress(r.status);
            return (
              <tr key={r.id} className="hover:bg-bg-soft">
                <td className="px-6 py-3.5">
                  <div className="flex items-center gap-2.5">
                    <IconTile
                      icon={FileSignature}
                      tone={r.status === "PAID" ? "success" : "orange"}
                      size="sm"
                    />
                    <div>
                      <div className="text-[13.5px] font-semibold text-ink">
                        {formatPeriodFr(r.periode)}
                      </div>
                      <div className="font-mono-tabular text-[11px] text-ink-3">
                        PR-{r.periode}
                      </div>
                    </div>
                  </div>
                </td>
                <td className="px-4 py-3.5">
                  <Badge tone={tone} showDot={false}>
                    {t(`status.${r.status as PayrollRunStatus}`)}
                  </Badge>
                  <div className="mt-1.5 h-1 w-32 overflow-hidden rounded-full bg-bg-soft">
                    <div
                      className="h-full bg-grad-orange"
                      style={{ width: `${Math.round(progress * 100)}%` }}
                    />
                  </div>
                </td>
                <td className="font-mono-tabular px-4 py-3.5 text-right">{r.nbEmployes}</td>
                <td className="font-mono-tabular px-4 py-3.5 text-right font-semibold text-ink">
                  {formatMoneyXAF(Number(r.totalGross ?? 0), locale)}
                </td>
                <td className="font-mono-tabular px-4 py-3.5 text-right text-ink-2">
                  {formatMoneyXAF(Number(r.totalNet ?? 0), locale)}
                </td>
                <td className="px-4 py-3.5 text-ink-3">
                  {r.validatedAt ? formatDate(r.validatedAt, { locale }) : r.calculatedAt ? formatDate(r.calculatedAt, { locale }) : "—"}
                </td>
                <td className="px-6 py-3.5 text-right">
                  <div className="flex items-center justify-end gap-1.5">
                    <RunDownloadButton run={r} t={t} locale={locale} />
                    <Link href={`/payroll/${r.id}`}>
                      <Button variant="secondary" size="sm">
                        {t("actions.details")} <ChevronRight className="h-3 w-3" />
                      </Button>
                    </Link>
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

// ────────────────────────────────────────────────────────────────────────
// Charts inline SVG
// ────────────────────────────────────────────────────────────────────────

function LineChart({ series }: { series: { label: string; value: number }[] }) {
  const W = 720;
  const H = 220;
  const padL = 44, padR = 14, padT = 14, padB = 30;
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
  const gridVs = Array.from({ length: gridLines + 1 }, (_, i) => Math.round(max - (range / gridLines) * i));

  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="h-[220px] w-full" role="img" aria-label="Salary mass evolution">
      <defs>
        <linearGradient id="payrollEvoArea" x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stopColor="#F97316" stopOpacity="0.32" />
          <stop offset="100%" stopColor="#F97316" stopOpacity="0" />
        </linearGradient>
      </defs>
      {gridYs.map((y, i) => (
        <g key={i}>
          <line x1={padL} x2={W - padR} y1={y} y2={y} stroke="#E5E7EB" strokeDasharray="3 4" />
          <text x={padL - 8} y={y + 4} textAnchor="end" fontSize="10" fill="#9CA3AF">
            {compactNumber(gridVs[i] ?? 0)}
          </text>
        </g>
      ))}
      <path d={areaPath} fill="url(#payrollEvoArea)" />
      <path d={path} fill="none" stroke="#F97316" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
      {pts.map((p, i) => (
        <circle key={i} cx={p.x} cy={p.y} r="3" fill="#fff" stroke="#F97316" strokeWidth="2" />
      ))}
      {series.map((p, i) => (
        <text key={i} x={padL + step * i} y={H - 8} textAnchor="middle" fontSize="10" fill="#9CA3AF">
          {p.label}
        </text>
      ))}
    </svg>
  );
}

function DonutCenter({
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
  const cx = size / 2, cy = size / 2;
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

// ────────────────────────────────────────────────────────────────────────
// Helpers
// ────────────────────────────────────────────────────────────────────────

function heroCtaLabel(
  status: PayrollRunStatus | string,
  t: ReturnType<typeof useTranslations<"payroll">>,
): string {
  switch (status) {
    case "CALCULATED":
    case "REVIEW":
      return t("actions.continueReview");
    case "VALIDATED":
      return t("actions.approve");
    case "APPROVED":
      return t("actions.startPayment");
    case "PAYMENT_INITIATED":
      return t("actions.trackPayment");
    case "PAID":
      return t("actions.close");
    default:
      return t("actions.details");
  }
}

function buildEvolution(runs: PayrollRunResponse[]): { label: string; value: number }[] {
  if (runs.length === 0) return [];
  const sorted = runs.slice().sort((a, b) => (a.periode < b.periode ? -1 : 1));
  const last12 = sorted.slice(-12);
  return last12.map((r) => {
    const [_, m] = r.periode.split("-").map(Number);
    const monthIdx = (m ?? 1) - 1;
    const labels = ["Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"];
    return { label: labels[monthIdx] ?? r.periode.slice(5), value: Number(r.totalGross ?? 0) };
  });
}

function compactNumber(n: number): string {
  if (n >= 1_000_000_000) return `${(n / 1_000_000_000).toFixed(1)}G`;
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(0)}M`;
  if (n >= 1_000) return `${(n / 1_000).toFixed(0)}k`;
  return String(n);
}

function formatMass(n: number, locale: "fr" | "en"): string {
  if (n >= 1_000_000) {
    return `${(n / 1_000_000).toLocaleString(locale === "fr" ? "fr-FR" : "en-US", { maximumFractionDigits: 2 })}M`;
  }
  return formatMoneyXAF(n, locale);
}

function formatMoneyXAF(n: number, locale: "fr" | "en"): string {
  return formatMoney(n, { locale, withCurrency: false });
}

// ────────────────────────────────────────────────────────────────────────
// Inline payslip preview (Écart 5)
// ────────────────────────────────────────────────────────────────────────

function InlinePayslip({
  runId,
  t,
  locale,
}: {
  runId: string;
  t: ReturnType<typeof useTranslations<"payroll">>;
  locale: "fr" | "en";
}) {
  const { session } = useSession();

  const entriesQuery = useQuery({
    queryKey: ["hrm", "payroll", runId, "entries"],
    queryFn: () => apiFetch<PayrollEntryResponse[]>(`/api/hrm/payroll/${runId}/entries`),
  });

  const firstEntry = entriesQuery.data?.[0] ?? null;

  const linesQuery = useQuery({
    queryKey: ["hrm", "payroll", "entry", firstEntry?.id, "payslip"],
    enabled: !!firstEntry,
    queryFn: () =>
      apiFetch<PayslipLineResponse[]>(`/api/hrm/payroll/entries/${firstEntry!.id}/payslip`),
  });

  const employeeQuery = useQuery({
    queryKey: ["hrm", "employee", firstEntry?.employeeId],
    enabled: !!firstEntry?.employeeId,
    queryFn: () =>
      apiFetch<EmployeeResponse>(`/api/hrm/employees/${firstEntry!.employeeId}`),
  });

  const entry = firstEntry;
  const lines = linesQuery.data ?? [];
  const employee = employeeQuery.data ?? null;

  const earnings = lines
    .filter((l) => l.type === "EARNING")
    .sort((a, b) => a.ordreAffichage - b.ordreAffichage);
  const deductions = lines
    .filter((l) => l.type === "DEDUCTION")
    .sort((a, b) => a.ordreAffichage - b.ordreAffichage);

  if (entriesQuery.isLoading) {
    return (
      <Card>
        <div className="grid place-items-center py-10">
          <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
        </div>
      </Card>
    );
  }

  if (!entry) return null;

  return (
    <Card>
      <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
        <div>
          <h3 className="text-[15px] font-bold tracking-tight text-ink">
            {t("payslip.inlineTitle")}
          </h3>
          <p className="text-[12px] text-ink-3">
            {employee?.actorDisplayName ?? entry.employeeId.slice(0, 8)}
            {" · "}
            {t("payslip.firstEntry")}
          </p>
        </div>
        <Link href={`/payroll/${runId}/entries/${entry.id}`}>
          <Button variant="secondary" size="sm">
            {t("actions.viewPayslip")} <ChevronRight className="h-3 w-3" />
          </Button>
        </Link>
      </div>

      {linesQuery.isLoading ? (
        <div className="grid place-items-center py-8">
          <Loader2 className="h-5 w-5 animate-spin text-orange-500" />
        </div>
      ) : (
        <div className="p-6">
          {/* Header */}
          <div className="flex flex-wrap items-start gap-4 border-b-2 border-ink pb-4">
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-2.5">
                <span className="grid h-9 w-9 place-items-center rounded-[9px] bg-grad-orange text-[18px] font-extrabold text-white shadow-orange-brand">
                  {session?.workspace?.organizationName?.[0]?.toUpperCase() ?? "R"}
                </span>
                <div>
                  <div className="font-display text-[18px] font-extrabold tracking-tight text-ink">
                    {session?.workspace?.organizationName ?? "—"}
                  </div>
                  <div className="text-[11px] text-ink-3">
                    {session?.workspace?.organizationId?.slice(0, 8) ?? ""}
                  </div>
                </div>
              </div>
            </div>
            <div className="text-right">
              <div className="font-display text-[14px] font-bold tracking-tight text-ink">
                {t("payslip.headerTitle")}
              </div>
              <div className="font-mono-tabular text-[12px] text-ink-3">
                {t("payslip.employee")} · {employee?.actorDisplayName ?? t("payslip.noEmployee")}
              </div>
              <div className="font-mono-tabular mt-0.5 text-[11px] text-ink-4">
                {employee?.matricule ?? entry.employeeId.slice(0, 8)}
                {employee?.departmentCode ? ` · ${employee.departmentCode}` : ""}
              </div>
            </div>
          </div>

          {/* Lines table */}
          <table className="mt-5 w-full text-[13px]">
            <thead>
              <tr className="border-b border-line text-[10px] uppercase tracking-[0.05em] text-ink-3">
                <th className="px-2 py-2.5 text-left">{t("payslip.tableLibelle")}</th>
                <th className="px-2 py-2.5 text-right" style={{ width: 100 }}>
                  {t("payslip.tableBase")}
                </th>
                <th className="px-2 py-2.5 text-right" style={{ width: 80 }}>
                  {t("payslip.tableTaux")}
                </th>
                <th className="px-2 py-2.5 text-right" style={{ width: 140 }}>
                  {t("payslip.tableGain")}
                </th>
                <th className="px-2 py-2.5 text-right" style={{ width: 140 }}>
                  {t("payslip.tableRetenue")}
                </th>
              </tr>
            </thead>
            <tbody className="font-mono-tabular">
              <tr className="bg-orange-50">
                <td colSpan={5} className="px-2 py-2 text-[11.5px] font-semibold text-orange-700">
                  {t("payslip.rubricBrut")}
                </td>
              </tr>
              {earnings.map((l) => (
                <PayslipLine key={l.id} line={l} side="gain" locale={locale} />
              ))}
              <tr className="bg-bg-soft">
                <td colSpan={3} className="px-2 py-2.5 text-[12.5px] font-bold text-ink">
                  {t("payslip.subtotalGross")}
                </td>
                <td className="px-2 py-2.5 text-right text-[12.5px] font-bold text-ink">
                  {formatMoney(Number(entry.brut ?? 0), { locale, withCurrency: false })}
                </td>
                <td />
              </tr>
              <tr className="bg-orange-50">
                <td colSpan={5} className="px-2 py-2 text-[11.5px] font-semibold text-orange-700">
                  {t("payslip.rubricRetenues")}
                </td>
              </tr>
              {deductions.map((l) => (
                <PayslipLine key={l.id} line={l} side="retenue" locale={locale} />
              ))}
              <tr className="bg-bg-soft">
                <td colSpan={4} className="px-2 py-2.5 text-[12.5px] font-bold text-ink">
                  {t("payslip.subtotalRetenues")}
                </td>
                <td className="px-2 py-2.5 text-right text-[12.5px] font-bold text-ink">
                  {formatMoney(Number(entry.totalDeductions ?? 0), { locale, withCurrency: false })}
                </td>
              </tr>
              <tr className="bg-ink text-white">
                <td className="px-3 py-3.5 text-[14px] font-bold">{t("payslip.netLabel")}</td>
                <td colSpan={3} />
                <td className="font-display px-3 py-3.5 text-right text-[20px] font-extrabold tracking-tight">
                  {formatMoney(Number(entry.net ?? 0), { locale, withCurrency: false })}
                </td>
              </tr>
            </tbody>
          </table>

          <div className="mt-5 flex flex-wrap items-center gap-4 border-t border-line-soft pt-4 text-[11px] text-ink-3">
            <span>
              {t("payslip.footerCumulBrut", { year: new Date().getFullYear().toString() })}:{" "}
              <b className="font-mono-tabular text-ink-2">
                {formatMoney(Number(entry.brut ?? 0), { locale, withCurrency: false })}
              </b>{" "}
              XAF
            </span>
            <span>
              {t("payslip.footerCumulIrpp", { year: new Date().getFullYear().toString() })}:{" "}
              <b className="font-mono-tabular text-ink-2">
                {formatMoney(Number(entry.incomeTax ?? 0), { locale, withCurrency: false })}
              </b>{" "}
              XAF
            </span>
            <div className="ml-auto flex flex-wrap items-center gap-2">
              <Button variant="secondary" size="sm">
                <Mail className="h-3.5 w-3.5" />
                {t("actions.emailPayslip")}
              </Button>
              <Link href={`/payroll/${runId}/entries/${entry.id}`}>
                <Button size="sm">
                  <Download className="h-3.5 w-3.5" />
                  {t("actions.downloadPayslip")}
                </Button>
              </Link>
            </div>
          </div>
        </div>
      )}
    </Card>
  );
}

function PayslipLine({
  line,
  side,
  locale,
}: {
  line: PayslipLineResponse;
  side: "gain" | "retenue";
  locale: "fr" | "en";
}) {
  return (
    <tr>
      <td className="px-2 py-2 text-ink-2">{line.libelle}</td>
      <td className="px-2 py-2 text-right text-ink-3">
        {line.base != null
          ? formatMoney(Number(line.base), { locale, withCurrency: false })
          : "—"}
      </td>
      <td className="px-2 py-2 text-right text-ink-3">
        {line.taux != null
          ? `${(Number(line.taux) * 100).toFixed(line.taux === 0 ? 0 : 1)}%`
          : "—"}
      </td>
      <td className="px-2 py-2 text-right">
        {side === "gain"
          ? formatMoney(Number(line.montant ?? 0), { locale, withCurrency: false })
          : ""}
      </td>
      <td className="px-2 py-2 text-right">
        {side === "retenue"
          ? formatMoney(Number(line.montant ?? 0), { locale, withCurrency: false })
          : ""}
      </td>
    </tr>
  );
}

// ────────────────────────────────────────────────────────────────────────────
// Run download button — generates full cycle PDF (summary + all payslips)
// Disabled until the cycle is VALIDATED or PAID
// ────────────────────────────────────────────────────────────────────────────

type OrgLegalData = {
  shortName?: string | null;
  longName?: string | null;
  businessRegistrationNumber?: string | null;
  taxNumber?: string | null;
  capitalShare?: number | string | null;
  ceoName?: string | null;
  email?: string | null;
  websiteUrl?: string | null;
  logoUri?: string | null;
  logoId?: string | null;
};

async function toDataUrl(url: string): Promise<string> {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`logo fetch ${res.status}`);
  const blob = await res.blob();
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });
}

function RunDownloadButton({
  run,
  t,
  locale,
}: {
  run: PayrollRunResponse;
  t: ReturnType<typeof useTranslations<"payroll">>;
  locale: "fr" | "en";
}) {
  const { session } = useSession();
  const [isDownloading, setIsDownloading] = React.useState(false);
  const isValidated = run.status === "VALIDATED" || run.status === "PAID";


  async function handleDownload() {
    if (!isValidated || isDownloading) return;
    setIsDownloading(true);
    try {
      // Always fetch fresh org data at download time
      let orgData: OrgLegalData | undefined;
      try {
        orgData = await apiFetch<OrgLegalData>("/api/admin/organization");
      } catch { /* session fallback */ }

      const legalLine = [
        orgData?.businessRegistrationNumber ? `RCCM ${orgData.businessRegistrationNumber}` : null,
        orgData?.taxNumber ? `NIU ${orgData.taxNumber}` : null,
        orgData?.capitalShare ? `Capital ${Number(orgData.capitalShare).toLocaleString("fr-FR")} XAF` : null,
        orgData?.ceoName ? `DG ${orgData.ceoName}` : null,
      ].filter(Boolean).join(" · ") || (session?.workspace?.organizationId?.slice(0, 8) ?? "");

      const contactLine = [
        orgData?.email,
        orgData?.websiteUrl?.replace(/^https?:\/\//, ""),
      ].filter(Boolean).join(" · ");

      let logoDataUrl: string | undefined;
      if (orgData?.logoId) {
        try { logoDataUrl = await toDataUrl(`/api/files/${orgData.logoId}`); } catch { /* fallback */ }
      }

      const entries = await apiFetch<PayrollEntryResponse[]>(
        `/api/hrm/payroll/${run.id}/entries`,
      );
      const entryData = await Promise.all(
        entries.map(async (entry) => {
          const [lines, employee] = await Promise.all([
            apiFetch<PayslipLineResponse[]>(
              `/api/hrm/payroll/entries/${entry.id}/payslip`,
            ),
            apiFetch<EmployeeResponse>(`/api/hrm/employees/${entry.employeeId}`).catch(
              () => null,
            ),
          ]);
          return { entry, employee: employee as EmployeeResponse | null, lines };
        }),
      );

      const blob = await pdf(
        <PayrollCyclePdfDocument
          organizationName={orgData?.longName || orgData?.shortName || session?.workspace?.organizationName || "—"}
          organizationRef={legalLine}
          organizationContact={contactLine}
          logoDataUrl={logoDataUrl}
          run={run}
          entries={entryData}
          locale={locale}
          reportTitle={t("pdf.reportTitle")}
          periodLabel={t("payslip.period")}
          nbEmployeesLabel={t("hero.employeesPaid")}
          grossMassLabel={t("hero.grossMass")}
          netPayableLabel={t("payslip.netLabel")}
          cnpsEmployeurLabel={t("detail.cnpsEmployeur")}
          irppLabel={t("detail.irpp")}
          totalRetenuesLabel={t("pdf.totalRetenues")}
          validatedByLabel={t("pdf.validatedBy")}
          validatedAtLabel={t("pdf.validatedAt")}
          calculatedAtLabel={t("pdf.calculatedAt")}
          statusLabel={t("pdf.statusLabel")}
          payslipTitle={t("payslip.headerTitle")}
          employeeLabel={t("payslip.employee")}
          noEmployeeLabel={t("payslip.noEmployee")}
          rubricBrutLabel={t("payslip.rubricBrut")}
          rubricRetenuesLabel={t("payslip.rubricRetenues")}
          subtotalGrossLabel={t("payslip.subtotalGross")}
          subtotalRetenuesLabel={t("payslip.subtotalRetenues")}
          netLabel={t("payslip.netLabel")}
          colLibelle={t("payslip.tableLibelle")}
          colBase={t("payslip.tableBase")}
          colTaux={t("payslip.tableTaux")}
          colGain={t("payslip.tableGain")}
          colRetenue={t("payslip.tableRetenue")}
        />,
      ).toBlob();

      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `cycle-paie-${run.periode}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      toast.error(t("form.errorTitle"));
    } finally {
      setIsDownloading(false);
    }
  }

  return (
    <Button
      variant="ghost"
      size="sm"
      className="h-8 w-8 p-0"
      onClick={handleDownload}
      disabled={!isValidated || isDownloading}
      title={
        !isValidated
          ? t("cycle.downloadRequiresValidation")
          : t("actions.downloadRun")
      }
    >
      {isDownloading ? (
        <Loader2 className="h-3.5 w-3.5 animate-spin" />
      ) : (
        <Download className="h-3.5 w-3.5" />
      )}
    </Button>
  );
}

// ────────────────────────────────────────────────────────────────────────────
// Payroll cycle PDF document
// Page 1: run summary  |  Pages 2+: one payslip per employee
// ────────────────────────────────────────────────────────────────────────────

const SC = StyleSheet.create({
  page: {
    paddingHorizontal: 36,
    paddingVertical: 32,
    fontSize: 9,
    fontFamily: "Helvetica",
    color: "#111827",
    backgroundColor: "#ffffff",
  },
  // ── Shared header ──
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-start",
    paddingBottom: 10,
    borderBottom: "2pt solid #111827",
    marginBottom: 14,
  },
  orgRow: { flexDirection: "row", alignItems: "center" },
  orgInitial: {
    width: 28,
    height: 28,
    backgroundColor: "#F97316",
    borderRadius: 6,
    justifyContent: "center",
    alignItems: "center",
    marginRight: 7,
  },
  orgInitialText: { fontFamily: "Helvetica-Bold", fontSize: 14, color: "#ffffff" },
  orgLogo: { width: 28, height: 28, borderRadius: 6, marginRight: 7, objectFit: "contain" },
  orgName: { fontFamily: "Helvetica-Bold", fontSize: 12, color: "#111827" },
  orgRef: { fontSize: 7.5, color: "#6B7280", marginTop: 2 },
  headerRight: { alignItems: "flex-end" },
  headerTitle: { fontFamily: "Helvetica-Bold", fontSize: 11, color: "#111827" },
  headerSub: { fontSize: 8, color: "#6B7280", marginTop: 2 },
  // ── Summary page: stats ──
  statsRow: { flexDirection: "row", gap: 8, marginBottom: 14 },
  statBox: { flex: 1, backgroundColor: "#F9FAFB", borderRadius: 6, padding: 8 },
  statLabel: {
    fontSize: 7,
    color: "#6B7280",
    fontFamily: "Helvetica-Bold",
    textTransform: "uppercase",
    marginBottom: 3,
  },
  statValue: { fontFamily: "Helvetica-Bold", fontSize: 13, color: "#111827" },
  statSub: { fontSize: 7, color: "#9CA3AF", marginTop: 2 },
  // ── Summary page: totals table ──
  tableSection: { marginBottom: 16 },
  tableSectionTitle: {
    fontFamily: "Helvetica-Bold",
    fontSize: 8,
    color: "#C2410C",
    backgroundColor: "#FFF7ED",
    padding: 5,
  },
  tableRow: {
    flexDirection: "row",
    borderBottom: "0.5pt solid #F3F4F6",
    paddingVertical: 5,
    paddingHorizontal: 5,
  },
  tableRowBg: { backgroundColor: "#F9FAFB" },
  tableRowDark: { backgroundColor: "#111827" },
  tableLabel: { flex: 1, fontSize: 8.5, color: "#374151" },
  tableLabelBold: { flex: 1, fontSize: 8.5, color: "#111827", fontFamily: "Helvetica-Bold" },
  tableLabelWhite: { flex: 1, fontSize: 10, color: "#ffffff", fontFamily: "Helvetica-Bold" },
  tableValue: {
    width: 100,
    fontSize: 8.5,
    color: "#374151",
    textAlign: "right",
    fontFamily: "Courier",
  },
  tableValueBold: {
    width: 100,
    fontSize: 8.5,
    color: "#111827",
    textAlign: "right",
    fontFamily: "Helvetica-Bold",
  },
  tableValueWhite: {
    width: 100,
    fontSize: 12,
    color: "#ffffff",
    textAlign: "right",
    fontFamily: "Helvetica-Bold",
  },
  // ── Summary page: validation info ──
  validationBox: {
    marginTop: 16,
    backgroundColor: "#F9FAFB",
    borderRadius: 6,
    padding: 10,
    flexDirection: "row",
    gap: 24,
  },
  validationLabel: {
    fontSize: 7,
    color: "#6B7280",
    fontFamily: "Helvetica-Bold",
    textTransform: "uppercase",
    marginBottom: 3,
  },
  validationValue: { fontSize: 8.5, color: "#111827", fontFamily: "Helvetica-Bold" },
  // ── Payslip page: info boxes ──
  infoGrid: { flexDirection: "row", gap: 10, marginBottom: 14 },
  infoBox: { flex: 1, backgroundColor: "#F9FAFB", borderRadius: 6, padding: 8 },
  infoLabel: {
    fontSize: 7,
    color: "#6B7280",
    textTransform: "uppercase",
    fontFamily: "Helvetica-Bold",
    marginBottom: 4,
  },
  infoValue: { fontFamily: "Helvetica-Bold", fontSize: 10, color: "#111827" },
  infoSub: { fontSize: 8, color: "#6B7280", marginTop: 2 },
  // ── Payslip page: lines table ──
  tableHeaderRow: {
    flexDirection: "row",
    borderBottom: "0.75pt solid #D1D5DB",
    paddingBottom: 4,
    marginBottom: 2,
  },
  thLibelle: {
    flex: 1,
    fontSize: 7,
    color: "#9CA3AF",
    fontFamily: "Helvetica-Bold",
    textTransform: "uppercase",
  },
  thNum: {
    width: 72,
    fontSize: 7,
    color: "#9CA3AF",
    fontFamily: "Helvetica-Bold",
    textTransform: "uppercase",
    textAlign: "right",
  },
  thTaux: {
    width: 44,
    fontSize: 7,
    color: "#9CA3AF",
    fontFamily: "Helvetica-Bold",
    textTransform: "uppercase",
    textAlign: "right",
  },
  thAmount: {
    width: 82,
    fontSize: 7,
    color: "#9CA3AF",
    fontFamily: "Helvetica-Bold",
    textTransform: "uppercase",
    textAlign: "right",
  },
  sectionRow: { backgroundColor: "#FFF7ED", paddingVertical: 4, paddingHorizontal: 4, marginTop: 4 },
  sectionText: { fontSize: 8, color: "#C2410C", fontFamily: "Helvetica-Bold" },
  dataRow: {
    flexDirection: "row",
    paddingVertical: 3,
    paddingHorizontal: 4,
    borderBottom: "0.5pt solid #F3F4F6",
  },
  cellLibelle: { flex: 1, fontSize: 8.5, color: "#374151" },
  cellBase: { width: 72, fontSize: 8.5, color: "#6B7280", textAlign: "right", fontFamily: "Courier" },
  cellTaux: { width: 44, fontSize: 8.5, color: "#6B7280", textAlign: "right", fontFamily: "Courier" },
  cellGain: { width: 82, fontSize: 8.5, color: "#111827", textAlign: "right", fontFamily: "Courier" },
  cellRetenue: { width: 82, fontSize: 8.5, color: "#111827", textAlign: "right", fontFamily: "Courier" },
  subtotalRow: {
    flexDirection: "row",
    backgroundColor: "#F9FAFB",
    paddingVertical: 5,
    paddingHorizontal: 4,
  },
  subtotalLabel: { flex: 1, fontSize: 9, color: "#111827", fontFamily: "Helvetica-Bold" },
  subtotalValue: { width: 82, fontSize: 9, color: "#111827", textAlign: "right", fontFamily: "Helvetica-Bold" },
  subtotalEmpty: { width: 82 },
  netRow: {
    flexDirection: "row",
    backgroundColor: "#111827",
    paddingVertical: 10,
    paddingHorizontal: 6,
    marginTop: 4,
  },
  netLabel: { flex: 1, fontSize: 12, color: "#ffffff", fontFamily: "Helvetica-Bold" },
  netValue: { width: 82, fontSize: 16, color: "#ffffff", textAlign: "right", fontFamily: "Helvetica-Bold" },
});

type CyclePdfEntry = {
  entry: PayrollEntryResponse;
  employee: EmployeeResponse | null;
  lines: PayslipLineResponse[];
};

type CyclePdfProps = {
  organizationName: string;
  organizationRef: string;
  organizationContact?: string;
  logoDataUrl?: string;
  run: PayrollRunResponse;
  entries: CyclePdfEntry[];
  locale: "fr" | "en";
  reportTitle: string;
  periodLabel: string;
  nbEmployeesLabel: string;
  grossMassLabel: string;
  netPayableLabel: string;
  cnpsEmployeurLabel: string;
  irppLabel: string;
  totalRetenuesLabel: string;
  validatedByLabel: string;
  validatedAtLabel: string;
  calculatedAtLabel: string;
  statusLabel: string;
  payslipTitle: string;
  employeeLabel: string;
  noEmployeeLabel: string;
  rubricBrutLabel: string;
  rubricRetenuesLabel: string;
  subtotalGrossLabel: string;
  subtotalRetenuesLabel: string;
  netLabel: string;
  colLibelle: string;
  colBase: string;
  colTaux: string;
  colGain: string;
  colRetenue: string;
};

function PayrollCyclePdfDocument(props: CyclePdfProps) {
  const {
    organizationName, organizationRef, organizationContact, logoDataUrl,
    run, entries, locale,
    reportTitle, periodLabel, nbEmployeesLabel, grossMassLabel, netPayableLabel,
    cnpsEmployeurLabel, irppLabel, totalRetenuesLabel,
    validatedByLabel: _vbl, validatedAtLabel, calculatedAtLabel, statusLabel,
    payslipTitle, employeeLabel, noEmployeeLabel,
    rubricBrutLabel, rubricRetenuesLabel, subtotalGrossLabel, subtotalRetenuesLabel,
    netLabel, colLibelle, colBase, colTaux, colGain, colRetenue,
  } = props;

  const fmt = (n: number | string) =>
    formatMoney(Number(n ?? 0), { locale, withCurrency: false });

  const fmtTaux = (taux: number | string | null) => {
    if (taux == null) return "—";
    const n = Number(taux);
    return `${(n * 100).toFixed(n === 0 ? 0 : 1)}%`;
  };

  const fmtDate = (iso: string | null) => {
    if (!iso) return "—";
    return new Date(iso).toLocaleDateString(locale === "fr" ? "fr-FR" : "en-US", {
      day: "2-digit",
      month: "long",
      year: "numeric",
    });
  };

  const initial = organizationName?.[0]?.toUpperCase() ?? "R";
  const totalRetenues = Number(run.totalEmployeeDeductions ?? 0);

  return (
    <Document>
      {/* ── Page 1: Cycle summary ── */}
      <Page size="A4" style={SC.page}>
        <View style={SC.header}>
          <View style={SC.orgRow}>
            {logoDataUrl ? (
              <Image src={logoDataUrl} style={SC.orgLogo} />
            ) : (
              <View style={SC.orgInitial}>
                <Text style={SC.orgInitialText}>{initial}</Text>
              </View>
            )}
            <View>
              <Text style={SC.orgName}>{organizationName}</Text>
              {organizationRef ? <Text style={SC.orgRef}>{organizationRef}</Text> : null}
              {organizationContact ? <Text style={[SC.orgRef, { marginTop: 1 }]}>{organizationContact}</Text> : null}
            </View>
          </View>
          <View style={SC.headerRight}>
            <Text style={SC.headerTitle}>{reportTitle}</Text>
            <Text style={SC.headerSub}>
              {periodLabel} · {formatPeriodFr(run.periode)}
            </Text>
            <Text style={[SC.headerSub, { color: "#F97316", marginTop: 3 }]}>
              {run.nbEmployes} {nbEmployeesLabel.toLowerCase()}
            </Text>
          </View>
        </View>

        {/* Stats */}
        <View style={SC.statsRow}>
          <View style={SC.statBox}>
            <Text style={SC.statLabel}>{nbEmployeesLabel}</Text>
            <Text style={SC.statValue}>{run.nbEmployes}</Text>
          </View>
          <View style={SC.statBox}>
            <Text style={SC.statLabel}>{grossMassLabel}</Text>
            <Text style={SC.statValue}>{fmt(run.totalGross)}</Text>
            <Text style={SC.statSub}>XAF brut</Text>
          </View>
          <View style={SC.statBox}>
            <Text style={SC.statLabel}>{netPayableLabel}</Text>
            <Text style={[SC.statValue, { color: "#F97316" }]}>{fmt(run.totalNet)}</Text>
            <Text style={SC.statSub}>XAF net</Text>
          </View>
        </View>

        {/* Totals */}
        <View style={SC.tableSection}>
          <Text style={SC.tableSectionTitle}>RÉCAPITULATIF DES CHARGES</Text>
          <View style={SC.tableRow}>
            <Text style={SC.tableLabel}>Masse salariale brute</Text>
            <Text style={SC.tableValue}>{fmt(run.totalGross)}</Text>
          </View>
          <View style={SC.tableRow}>
            <Text style={SC.tableLabel}>{irppLabel}</Text>
            <Text style={SC.tableValue}>{fmt(run.totalIncomeTax)}</Text>
          </View>
          <View style={SC.tableRow}>
            <Text style={SC.tableLabel}>{cnpsEmployeurLabel}</Text>
            <Text style={SC.tableValue}>{fmt(run.totalEmployerCharges)}</Text>
          </View>
          <View style={[SC.tableRow, SC.tableRowBg]}>
            <Text style={SC.tableLabelBold}>{totalRetenuesLabel}</Text>
            <Text style={SC.tableValueBold}>{fmt(totalRetenues)}</Text>
          </View>
          <View style={[SC.tableRow, SC.tableRowDark]}>
            <Text style={SC.tableLabelWhite}>{netPayableLabel}</Text>
            <Text style={SC.tableValueWhite}>{fmt(run.totalNet)}</Text>
          </View>
        </View>

        {/* Validation info */}
        <View style={SC.validationBox}>
          {run.calculatedAt && (
            <View>
              <Text style={SC.validationLabel}>{calculatedAtLabel}</Text>
              <Text style={SC.validationValue}>{fmtDate(run.calculatedAt)}</Text>
            </View>
          )}
          {run.validatedAt && (
            <View>
              <Text style={SC.validationLabel}>{validatedAtLabel}</Text>
              <Text style={SC.validationValue}>{fmtDate(run.validatedAt)}</Text>
            </View>
          )}
          <View>
            <Text style={SC.validationLabel}>{statusLabel}</Text>
            <Text style={[SC.validationValue, { color: "#22C55E" }]}>{run.status}</Text>
          </View>
        </View>
      </Page>

      {/* ── Pages 2+N: Individual payslips ── */}
      {entries.map(({ entry, employee, lines }) => {
        const earnings = lines
          .filter((l) => l.type === "EARNING")
          .sort((a, b) => a.ordreAffichage - b.ordreAffichage);
        const deductions = lines
          .filter((l) => l.type === "DEDUCTION")
          .sort((a, b) => a.ordreAffichage - b.ordreAffichage);
        const matricule = employee?.matricule ?? entry.employeeId.slice(0, 8);
        const deptCode = employee?.departmentCode ? ` · ${employee.departmentCode}` : "";
        const ref = `PSL-${run.periode}-${employee?.matricule ?? entry.id.slice(0, 6)}`;

        return (
          <Page key={entry.id} size="A4" style={SC.page}>
            <View style={SC.header}>
              <View style={SC.orgRow}>
                <View style={SC.orgInitial}>
                  <Text style={SC.orgInitialText}>{initial}</Text>
                </View>
                <View>
                  <Text style={SC.orgName}>{organizationName}</Text>
                  <Text style={SC.orgRef}>{organizationRef}</Text>
                </View>
              </View>
              <View style={SC.headerRight}>
                <Text style={SC.headerTitle}>{payslipTitle}</Text>
                <Text style={SC.headerSub}>
                  {periodLabel} · {formatPeriodFr(run.periode)}
                </Text>
                <Text style={[SC.headerSub, { fontSize: 7 }]}>{`N° ${ref}`}</Text>
              </View>
            </View>

            <View style={SC.infoGrid}>
              <View style={SC.infoBox}>
                <Text style={SC.infoLabel}>{employeeLabel}</Text>
                <Text style={SC.infoValue}>
                  {employee?.actorDisplayName ?? noEmployeeLabel}
                </Text>
                <Text style={SC.infoSub}>{matricule}{deptCode}</Text>
                {employee?.numCnps ? (
                  <Text style={SC.infoSub}>{"CNPS · " + employee.numCnps}</Text>
                ) : null}
              </View>
              <View style={SC.infoBox}>
                <Text style={SC.infoLabel}>{"Matricule · Poste"}</Text>
                <Text style={SC.infoValue}>{matricule}</Text>
                <Text style={SC.infoSub}>{employee?.departmentCode ?? "—"}</Text>
              </View>
            </View>

            <View style={SC.tableHeaderRow}>
              <Text style={SC.thLibelle}>{colLibelle}</Text>
              <Text style={SC.thNum}>{colBase}</Text>
              <Text style={SC.thTaux}>{colTaux}</Text>
              <Text style={SC.thAmount}>{colGain}</Text>
              <Text style={SC.thAmount}>{colRetenue}</Text>
            </View>

            <View style={SC.sectionRow}>
              <Text style={SC.sectionText}>{rubricBrutLabel}</Text>
            </View>
            {earnings.map((l) => (
              <View key={l.id} style={SC.dataRow}>
                <Text style={SC.cellLibelle}>{l.libelle}</Text>
                <Text style={SC.cellBase}>{l.base != null ? fmt(l.base) : "—"}</Text>
                <Text style={SC.cellTaux}>{fmtTaux(l.taux)}</Text>
                <Text style={SC.cellGain}>{fmt(l.montant)}</Text>
                <Text style={SC.cellRetenue}>{""}</Text>
              </View>
            ))}
            <View style={SC.subtotalRow}>
              <Text style={SC.subtotalLabel}>{subtotalGrossLabel}</Text>
              <Text style={[SC.subtotalValue, { width: 72 + 44 }]}>{fmt(entry.brut)}</Text>
              <Text style={SC.subtotalEmpty}>{""}</Text>
            </View>

            <View style={[SC.sectionRow, { marginTop: 8 }]}>
              <Text style={SC.sectionText}>{rubricRetenuesLabel}</Text>
            </View>
            {deductions.map((l) => (
              <View key={l.id} style={SC.dataRow}>
                <Text style={SC.cellLibelle}>{l.libelle}</Text>
                <Text style={SC.cellBase}>{l.base != null ? fmt(l.base) : "—"}</Text>
                <Text style={SC.cellTaux}>{fmtTaux(l.taux)}</Text>
                <Text style={SC.cellGain}>{""}</Text>
                <Text style={SC.cellRetenue}>{fmt(l.montant)}</Text>
              </View>
            ))}
            <View style={SC.subtotalRow}>
              <Text style={SC.subtotalLabel}>{subtotalRetenuesLabel}</Text>
              <Text style={[SC.subtotalValue, { width: 72 + 44 + 82 }]}>
                {fmt(entry.totalDeductions)}
              </Text>
            </View>

            <View style={SC.netRow}>
              <Text style={SC.netLabel}>{netLabel}</Text>
              <Text style={SC.netValue}>{fmt(entry.net)}</Text>
            </View>
          </Page>
        );
      })}
    </Document>
  );
}
