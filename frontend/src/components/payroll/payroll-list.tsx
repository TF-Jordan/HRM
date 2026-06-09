"use client";

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
              <Link href="/payroll/new">
                <Button>
                  <Plus className="h-4 w-4" />
                  {t("actions.newRun")}
                </Button>
              </Link>
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
// Run download button — triggers backend PDF generation for each entry
// Disabled until the cycle is VALIDATED or PAID
// ────────────────────────────────────────────────────────────────────────────

type PayrollDocumentResponse = {
  id: string;
  fileId: string;
  fileName: string;
  type: string;
  verificationCode: string;
};

function RunDownloadButton({
  run,
  t,
}: {
  run: PayrollRunResponse;
  t: ReturnType<typeof useTranslations<"payroll">>;
  locale: "fr" | "en";
}) {
  const [isDownloading, setIsDownloading] = React.useState(false);
  const isValidated = run.status === "VALIDATED" || run.status === "PAID";

  async function handleDownload() {
    if (!isValidated || isDownloading) return;
    setIsDownloading(true);
    try {
      // Generate backend PDFs for all entries in the run, then download each
      const entries = await apiFetch<PayrollEntryResponse[]>(
        `/api/hrm/payroll/${run.id}/entries`,
      );
      for (const entry of entries) {
        const doc = await apiFetch<PayrollDocumentResponse>(
          `/api/hrm/payroll/documents/payslip?entryId=${entry.id}`,
          { method: "POST" },
        );
        const res = await fetch(`/api/files/${doc.fileId}`);
        if (!res.ok) continue;
        const blob = await res.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = doc.fileName || `bulletin-${run.periode}-${entry.id.slice(0, 6)}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      }
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
