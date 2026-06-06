"use client";

import { useQuery } from "@tanstack/react-query";
import {
  ArrowRight,
  BadgeCheck,
  Banknote,
  Building2,
  CalendarClock,
  CheckCircle2,
  ClipboardList,
  Coins,
  FileText,
  Landmark,
  Loader2,
  type LucideIcon,
  PiggyBank,
  Plus,
  RefreshCw,
  Scale,
  Settings,
  TrendingDown,
  TrendingUp,
  Users,
  Wallet,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { IconTile } from "@/components/ui/icon-tile";
import { AppLink as Link } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney, formatNumber } from "@/lib/format";
import {
  formatPeriodFr,
  formatPeriodShort,
  nextPayrollAction,
  type PayrollRunAction,
  payrollStatusTone,
  payrollStepperState,
  type PayrollStepKey,
} from "@/lib/payroll-status";
import type { PayrollRunStatus } from "@/server/ksm/modules/payroll";
import { cn } from "@/lib/utils";

type DashboardData = {
  organization: string;
  currency: string;
  canRun: boolean;
  currentMonthPeriode: string;
  hasCurrentMonthRun: boolean;
  runsTotal: number;
  workforce: { active: number; paidCurrent: number };
  current: {
    id: string;
    periode: string;
    status: PayrollRunStatus | string;
    calculatedAt: string | null;
    validatedAt: string | null;
    approvedAt: string | null;
    paidAt: string | null;
    closedAt: string | null;
  } | null;
  kpis: {
    masseSalariale: number;
    netAPayer: number;
    chargesPatronales: number;
    impot: number;
    deductions: number;
    coutTotal: number;
    effectif: number;
  } | null;
  deltas: {
    gross: number | null;
    net: number | null;
    cost: number | null;
    headcount: number | null;
  };
  costStructure: {
    net: number;
    deductions: number;
    incomeTax: number;
    employerCharges: number;
  } | null;
  evolution: {
    periode: string;
    gross: number;
    net: number;
    employerCharges: number;
    cost: number;
    headcount: number;
  }[];
  pipeline: {
    draft: number;
    toValidate: number;
    toApprove: number;
    toPay: number;
    inPayment: number;
    toClose: number;
    closed: number;
  };
  garnish: { active: number; suspended: number; monthly: number; remaining: number };
  retro: { pending: number; applied: number; netDelta: number };
  settlements: { calculated: number; paid: number; toPay: number };
  config: {
    payElementsActive: number;
    payElementsTotal: number;
    taxScalesActive: number;
    lookupActive: number;
  };
  ytd: { runsCount: number; gross: number; net: number; employerCharges: number; incomeTax: number };
  recentRuns: {
    id: string;
    periode: string;
    status: PayrollRunStatus | string;
    nbEmployes: number;
    totalGross: number;
    totalNet: number;
    totalEmployerCharges: number;
    cost: number;
    date: string | null;
  }[];
};

const STEP_KEYS: PayrollStepKey[] = ["variables", "calculation", "review", "validation", "payment"];

export function PayrollDashboard() {
  const t = useTranslations("payroll");
  const tc = useTranslations("common");
  const locale = useLocale() as "fr" | "en";

  const query = useQuery({
    queryKey: ["hrm", "payroll", "dashboard"],
    queryFn: () => apiFetch<DashboardData>("/api/hrm/payroll/dashboard"),
    refetchInterval: 60_000,
  });
  const data = query.data;

  return (
    <>
      <PageHeader
        ucBadge={t("dash.uc")}
        breadcrumb={[{ label: tc("appName") }, { label: t("title") }, { label: t("dash.title") }]}
        title={t("dash.title")}
        subtitle={t("dash.subtitle")}
        actions={
          <>
            <Link href="/payroll">
              <Button variant="secondary">
                <Wallet className="h-4 w-4" /> {t("dash.viewRuns")}
              </Button>
            </Link>
            {data?.canRun && (
              <Link href="/payroll/new">
                <Button disabled={data.hasCurrentMonthRun}>
                  <Plus className="h-4 w-4" /> {t("dash.newRun")}
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
      ) : !data ? null : (
        <div className="flex flex-col gap-4">
          <CurrentCycleCard data={data} locale={locale} t={t} />
          {data.kpis && <HeroRow data={data} locale={locale} t={t} />}
          {data.kpis && <SecondaryRow data={data} locale={locale} t={t} />}
          <ChartsRow data={data} locale={locale} t={t} />
          <ActionsRow data={data} locale={locale} t={t} />
          <BottomRow data={data} locale={locale} t={t} />
        </div>
      )}
    </>
  );
}

type T = ReturnType<typeof useTranslations<"payroll">>;

// ────────────────────────────────────────────────────────────────────────
// Current cycle — status, stepper, next action
// ────────────────────────────────────────────────────────────────────────

const ACTION_LABEL: Record<PayrollRunAction, string> = {
  validate: "dash.action.validate",
  approve: "dash.action.approve",
  "initiate-payment": "dash.action.pay",
  close: "dash.action.close",
};

function CurrentCycleCard({ data, locale, t }: { data: DashboardData; locale: "fr" | "en"; t: T }) {
  if (!data.current || !data.kpis) {
    return (
      <Card className="flex flex-col items-center gap-3 px-6 py-12 text-center">
        <IconTile icon={Wallet} tone="orange" size="lg" />
        <div className="text-[15px] font-bold text-ink">{t("dash.noRun")}</div>
        <p className="max-w-md text-[13px] text-ink-3">{t("dash.noRunSub")}</p>
        {data.canRun && (
          <Link href="/payroll/new">
            <Button>
              <Plus className="h-4 w-4" /> {t("dash.newRun")}
            </Button>
          </Link>
        )}
      </Card>
    );
  }

  const run = data.current;
  const stepper = payrollStepperState(run.status);
  const action = nextPayrollAction(run.status);
  const tone = payrollStatusTone(run.status);

  return (
    <Card className="overflow-hidden">
      <div className="flex flex-col gap-4 border-b border-line-soft px-6 py-5 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex items-center gap-4">
          <span className="grid h-12 w-12 place-items-center rounded-[14px] bg-grad-orange text-white shadow-md-brand">
            <CalendarClock className="h-6 w-6" />
          </span>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">
                {t("dash.currentCycle")}
              </span>
              <Badge tone={tone} showDot={false}>
                {t(`status.${run.status as PayrollRunStatus}`)}
              </Badge>
            </div>
            <div className="font-display text-[22px] font-extrabold capitalize leading-tight tracking-tight text-ink">
              {formatPeriodFr(run.periode)}
            </div>
            <div className="font-mono-tabular text-[11.5px] text-ink-4">PR-{run.periode}</div>
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-6">
          <InlineStat
            label={t("dash.kpi.headcount")}
            value={`${formatNumber(data.kpis.effectif, locale)} / ${formatNumber(data.workforce.active, locale)}`}
          />
          <InlineStat
            label={t("dash.kpi.net")}
            value={formatMoney(data.kpis.netAPayer, { locale, withCurrency: false })}
            suffix={data.currency}
          />
          <Link href={`/payroll/${run.id}`}>
            <Button variant={action ? "primary" : "secondary"}>
              {action ? t(ACTION_LABEL[action]) : t("dash.action.open")}
              <ArrowRight className="h-4 w-4" />
            </Button>
          </Link>
        </div>
      </div>

      <div className="px-6 py-5">
        <Stepper stepper={stepper} t={t} />
      </div>
    </Card>
  );
}

function InlineStat({ label, value, suffix }: { label: string; value: string; suffix?: string }) {
  return (
    <div>
      <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">{label}</div>
      <div className="font-mono-tabular mt-0.5 text-[17px] font-bold tracking-tight text-ink">
        {value}
        {suffix && <span className="ml-1 text-[11px] font-medium text-ink-3">{suffix}</span>}
      </div>
    </div>
  );
}

function Stepper({
  stepper,
  t,
}: {
  stepper: Record<PayrollStepKey, "done" | "active" | "pending">;
  t: T;
}) {
  return (
    <div className="flex items-center">
      {STEP_KEYS.map((key, idx) => {
        const state = stepper[key];
        const isLast = idx === STEP_KEYS.length - 1;
        return (
          <React.Fragment key={key}>
            <div className="flex min-w-0 flex-col items-center gap-1.5">
              <span
                className={cn(
                  "grid h-8 w-8 place-items-center rounded-full text-[12px] font-bold transition-colors",
                  state === "done" && "bg-success-500 text-white",
                  state === "active" && "bg-grad-orange text-white ring-4 ring-orange-500/15",
                  state === "pending" && "border border-line bg-white text-ink-4",
                )}
              >
                {state === "done" ? <CheckCircle2 className="h-4 w-4" /> : idx + 1}
              </span>
              <span
                className={cn(
                  "text-center text-[11px] font-medium",
                  state === "pending" ? "text-ink-4" : "text-ink-2",
                )}
              >
                {t(`dash.step.${key}`)}
              </span>
            </div>
            {!isLast && (
              <div
                className={cn(
                  "mx-1 mb-5 h-0.5 flex-1 rounded-full",
                  state === "done" ? "bg-success-500" : "bg-line",
                )}
              />
            )}
          </React.Fragment>
        );
      })}
    </div>
  );
}

// ────────────────────────────────────────────────────────────────────────
// Hero KPI gradient cards
// ────────────────────────────────────────────────────────────────────────

function HeroRow({ data, locale, t }: { data: DashboardData; locale: "fr" | "en"; t: T }) {
  const k = data.kpis!;
  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      <HeroKpi
        tone="grad-dark"
        icon={Building2}
        label={t("dash.kpi.employerCost")}
        value={compact(k.coutTotal, locale)}
        unit={data.currency}
        delta={data.deltas.cost}
        sub={t("dash.kpi.employerCostSub")}
      />
      <HeroKpi
        tone="grad-orange"
        icon={Wallet}
        label={t("dash.kpi.grossMass")}
        value={compact(k.masseSalariale, locale)}
        unit={data.currency}
        delta={data.deltas.gross}
        sub={t("dash.kpi.grossMassSub")}
      />
      <HeroKpi
        tone="grad-amber"
        icon={Banknote}
        label={t("dash.kpi.net")}
        value={compact(k.netAPayer, locale)}
        unit={data.currency}
        delta={data.deltas.net}
        sub={t("dash.kpi.netSub")}
      />
      <HeroKpi
        tone="grad-violet"
        icon={Landmark}
        label={t("dash.kpi.employerCharges")}
        value={compact(k.chargesPatronales, locale)}
        unit={data.currency}
        sub={t("dash.kpi.employerChargesSub")}
      />
    </div>
  );
}

function HeroKpi({
  tone,
  icon: Icon,
  label,
  value,
  unit,
  delta,
  sub,
}: {
  tone: "grad-orange" | "grad-dark" | "grad-amber" | "grad-violet";
  icon: LucideIcon;
  label: string;
  value: string;
  unit?: string;
  delta?: number | null;
  sub: string;
}) {
  const gradClass = {
    "grad-orange": "bg-grad-orange",
    "grad-dark": "bg-grad-dark",
    "grad-amber": "bg-grad-amber",
    "grad-violet": "bg-grad-violet",
  }[tone];
  const hasDelta = typeof delta === "number" && delta !== 0;
  const positive = (delta ?? 0) >= 0;
  return (
    <div className={cn("relative overflow-hidden rounded-[20px] p-5 text-white shadow-lg-brand", gradClass)}>
      <div
        className="pointer-events-none absolute inset-0 opacity-50"
        style={{ background: "linear-gradient(180deg,rgba(255,255,255,0.18),transparent 50%)" }}
        aria-hidden="true"
      />
      <div className="relative flex items-start justify-between">
        <span className="grid h-10 w-10 place-items-center rounded-[12px] bg-white/20 backdrop-blur">
          <Icon className="h-5 w-5" />
        </span>
        {hasDelta && (
          <span className="inline-flex items-center gap-1 rounded-full bg-white/20 px-2 py-0.5 text-[11px] font-semibold backdrop-blur">
            {positive ? <TrendingUp className="h-3 w-3" /> : <TrendingDown className="h-3 w-3" />}
            {positive ? "+" : ""}
            {delta}%
          </span>
        )}
      </div>
      <div className="relative mt-6">
        <div className="text-[11.5px] font-medium uppercase tracking-[0.06em] text-white/85">{label}</div>
        <div className="font-display font-mono-tabular mt-1 flex items-baseline gap-1.5 text-[30px] font-extrabold leading-none tracking-tight">
          {value}
          {unit && <span className="text-[12px] font-semibold text-white/75">{unit}</span>}
        </div>
        <div className="mt-1.5 text-[11.5px] text-white/80">{sub}</div>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────────────────
// Secondary stat cards
// ────────────────────────────────────────────────────────────────────────

function SecondaryRow({ data, locale, t }: { data: DashboardData; locale: "fr" | "en"; t: T }) {
  const k = data.kpis!;
  return (
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
      <StatCard
        icon={Users}
        tone="info"
        label={t("dash.kpi.headcount")}
        value={`${formatNumber(k.effectif, locale)} / ${formatNumber(data.workforce.active, locale)}`}
        sub={t("dash.kpi.headcountSub")}
      />
      <StatCard
        icon={PiggyBank}
        tone="warning"
        label={t("dash.kpi.deductions")}
        value={formatMoney(k.deductions, { locale, withCurrency: false })}
        sub={t("dash.kpi.deductionsSub")}
      />
      <StatCard
        icon={Landmark}
        tone="violet"
        label={t("dash.kpi.incomeTax")}
        value={formatMoney(k.impot, { locale, withCurrency: false })}
        sub={t("dash.kpi.incomeTaxSub")}
      />
      <StatCard
        icon={CalendarClock}
        tone="success"
        label={t("dash.kpi.ytd")}
        value={compact(data.ytd.gross, locale)}
        sub={t("dash.kpi.ytdSub", { count: data.ytd.runsCount })}
      />
    </div>
  );
}

// ────────────────────────────────────────────────────────────────────────
// Evolution chart + cost structure donut
// ────────────────────────────────────────────────────────────────────────

function ChartsRow({ data, locale, t }: { data: DashboardData; locale: "fr" | "en"; t: T }) {
  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-[2fr_1fr]">
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <div>
            <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("dash.evolution.title")}</h3>
            <p className="text-[12px] text-ink-3">{t("dash.evolution.subtitle")}</p>
          </div>
        </div>
        <div className="px-6 py-5">
          {data.evolution.length === 0 ? (
            <EmptyChart label={t("dash.evolution.empty")} />
          ) : (
            <>
              <StackedBars data={data.evolution} locale={locale} />
              <div className="mt-4 flex flex-wrap items-center gap-5 border-t border-line-soft pt-3">
                <Legend dot="bg-orange-500" label={t("dash.evolution.net")} />
                <Legend dot="bg-amber-400" label={t("dash.evolution.withheld")} />
                <Legend dot="bg-ink" label={t("dash.evolution.employer")} />
              </div>
            </>
          )}
        </div>
      </Card>

      <Card>
        <div className="border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("dash.structure.title")}</h3>
          <p className="text-[12px] text-ink-3">
            {data.current ? formatPeriodFr(data.current.periode) : t("dash.structure.subtitle")}
          </p>
        </div>
        <div className="px-5 py-5">
          {data.costStructure ? (
            <CostDonut structure={data.costStructure} total={data.kpis?.coutTotal ?? 0} locale={locale} t={t} />
          ) : (
            <EmptyChart label={t("dash.structure.empty")} />
          )}
        </div>
      </Card>
    </div>
  );
}

function StackedBars({
  data,
  locale,
}: {
  data: DashboardData["evolution"];
  locale: "fr" | "en";
}) {
  const max = Math.max(1, ...data.map((d) => d.cost));
  return (
    <div className="flex h-[220px] items-end gap-2">
      {data.map((d) => {
        const withheld = Math.max(0, d.gross - d.net);
        const segments = [
          { v: d.net, cls: "bg-orange-500" },
          { v: withheld, cls: "bg-amber-400" },
          { v: d.employerCharges, cls: "bg-ink" },
        ];
        return (
          <div key={d.periode} className="group flex h-full flex-1 flex-col items-center justify-end gap-1.5">
            <div className="font-mono-tabular text-[10px] font-semibold text-ink-3 opacity-0 transition-opacity group-hover:opacity-100">
              {compact(d.cost, locale)}
            </div>
            <div
              className="flex w-full max-w-[42px] flex-col-reverse overflow-hidden rounded-[6px]"
              style={{ height: `${(d.cost / max) * 100}%` }}
              title={`${d.periode} · ${compact(d.cost, locale)}`}
            >
              {segments.map((s, i) => (
                <div key={i} className={s.cls} style={{ height: `${(s.v / Math.max(1, d.cost)) * 100}%` }} />
              ))}
            </div>
            <div className="text-[10px] text-ink-4">{formatPeriodShort(d.periode, locale).replace(".", "")}</div>
          </div>
        );
      })}
    </div>
  );
}

function CostDonut({
  structure,
  total,
  locale,
  t,
}: {
  structure: NonNullable<DashboardData["costStructure"]>;
  total: number;
  locale: "fr" | "en";
  t: T;
}) {
  const slices = [
    { key: "net", value: structure.net, color: "#F97316" },
    { key: "deductions", value: structure.deductions, color: "#FBBF24" },
    { key: "incomeTax", value: structure.incomeTax, color: "#A78BFA" },
    { key: "employerCharges", value: structure.employerCharges, color: "#1F2937" },
  ];
  const size = 168;
  const thick = 24;
  const r = (size - thick) / 2;
  const cx = size / 2;
  const cy = size / 2;
  const sum = Math.max(1, slices.reduce((a, s) => a + s.value, 0));
  let offset = 0;
  const len = 2 * Math.PI * r;
  return (
    <div className="flex flex-col items-center gap-4">
      <div className="relative" style={{ width: size, height: size }}>
        <svg viewBox={`0 0 ${size} ${size}`} className="h-full w-full -rotate-90">
          <circle cx={cx} cy={cy} r={r} fill="none" stroke="#F3F4F6" strokeWidth={thick} />
          {slices.map((s) => {
            const frac = s.value / sum;
            const dash = `${frac * len} ${len - frac * len}`;
            const dashOffset = -offset;
            offset += frac * len;
            return (
              <circle
                key={s.key}
                cx={cx}
                cy={cy}
                r={r}
                fill="none"
                stroke={s.color}
                strokeWidth={thick}
                strokeDasharray={dash}
                strokeDashoffset={dashOffset}
              />
            );
          })}
        </svg>
        <div className="absolute inset-0 grid place-items-center text-center">
          <div>
            <div className="font-display font-mono-tabular text-[20px] font-extrabold leading-none tracking-tight text-ink">
              {compact(total, locale)}
            </div>
            <div className="text-[10.5px] text-ink-3">{t("dash.structure.total")}</div>
          </div>
        </div>
      </div>
      <div className="flex w-full flex-col gap-2">
        {slices.map((s) => (
          <div key={s.key} className="flex items-center gap-2 text-[12.5px]">
            <span className="h-2.5 w-2.5 shrink-0 rounded-sm" style={{ background: s.color }} />
            <span className="text-ink-2">{t(`dash.structure.${s.key}`)}</span>
            <span className="font-mono-tabular ml-auto text-ink-3">
              {formatMoney(s.value, { locale, withCurrency: false })}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function EmptyChart({ label }: { label: string }) {
  return <div className="grid h-[200px] place-items-center text-[13px] text-ink-3">{label}</div>;
}

function Legend({ dot, label }: { dot: string; label: string }) {
  return (
    <div className="flex items-center gap-2 text-[12px] text-ink-2">
      <span className={cn("h-2 w-2 rounded-full", dot)} />
      <span>{label}</span>
    </div>
  );
}

// ────────────────────────────────────────────────────────────────────────
// Actions to handle
// ────────────────────────────────────────────────────────────────────────

function ActionsRow({ data, locale, t }: { data: DashboardData; locale: "fr" | "en"; t: T }) {
  const tiles: {
    key: string;
    icon: LucideIcon;
    tone: "warning" | "info" | "orange" | "violet" | "danger" | "success";
    count: number;
    href: string;
    amount?: number;
  }[] = [
    { key: "toValidate", icon: ClipboardList, tone: "warning", count: data.pipeline.toValidate, href: "/payroll" },
    { key: "toApprove", icon: BadgeCheck, tone: "info", count: data.pipeline.toApprove, href: "/payroll" },
    { key: "toPay", icon: Banknote, tone: "orange", count: data.pipeline.toPay, href: "/payroll" },
    { key: "retro", icon: RefreshCw, tone: "violet", count: data.retro.pending, href: "/retroactive", amount: data.retro.netDelta },
    { key: "settlements", icon: FileText, tone: "danger", count: data.settlements.calculated, href: "/final-settlements", amount: data.settlements.toPay },
    { key: "garnish", icon: Scale, tone: "success", count: data.garnish.active, href: "/garnishments", amount: data.garnish.monthly },
  ];
  const totalPending = tiles.reduce((a, x) => a + x.count, 0);

  return (
    <Card>
      <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
        <div>
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("dash.actions.title")}</h3>
          <p className="text-[12px] text-ink-3">{t("dash.actions.subtitle", { count: totalPending })}</p>
        </div>
      </div>
      <div className="grid grid-cols-1 gap-3 p-5 sm:grid-cols-2 xl:grid-cols-3">
        {tiles.map((tile) => (
          <Link
            key={tile.key}
            href={tile.href}
            className={cn(
              "group flex items-center gap-3.5 rounded-[14px] border border-line bg-white p-4 transition-all hover:-translate-y-px hover:border-line-strong hover:shadow-sm-brand",
              tile.count === 0 && "opacity-65",
            )}
          >
            <IconTile icon={tile.icon} tone={tile.tone} />
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-2">
                <span className="font-display font-mono-tabular text-[20px] font-extrabold leading-none text-ink">
                  {tile.count}
                </span>
                <span className="text-[13px] font-semibold text-ink-2">{t(`dash.actions.${tile.key}`)}</span>
              </div>
              <div className="mt-0.5 truncate text-[11.5px] text-ink-3">
                {tile.amount != null && tile.amount > 0
                  ? `${formatMoney(tile.amount, { locale, withCurrency: false })} ${data.currency}`
                  : t(`dash.actions.${tile.key}Hint`)}
              </div>
            </div>
            <ArrowRight className="h-4 w-4 text-ink-4 transition-transform group-hover:translate-x-0.5" />
          </Link>
        ))}
      </div>
    </Card>
  );
}

// ────────────────────────────────────────────────────────────────────────
// Recent runs + configuration health
// ────────────────────────────────────────────────────────────────────────

function BottomRow({ data, locale, t }: { data: DashboardData; locale: "fr" | "en"; t: T }) {
  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-[2fr_1fr]">
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("dash.recent.title")}</h3>
          <Link href="/payroll">
            <Button variant="ghost" size="sm">
              {t("dash.recent.all")}
            </Button>
          </Link>
        </div>
        {data.recentRuns.length === 0 ? (
          <div className="px-6 py-12 text-center text-[13px] text-ink-3">{t("dash.recent.empty")}</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-[13px]">
              <thead>
                <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
                  <th className="px-6 py-3">{t("dash.recent.period")}</th>
                  <th className="px-3 py-3">{t("dash.recent.status")}</th>
                  <th className="px-3 py-3 text-right">{t("dash.recent.headcount")}</th>
                  <th className="px-3 py-3 text-right">{t("dash.recent.gross")}</th>
                  <th className="px-3 py-3 text-right">{t("dash.recent.net")}</th>
                  <th className="px-6 py-3 text-right">{t("dash.recent.cost")}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line-soft">
                {data.recentRuns.map((r) => (
                  <tr key={r.id} className="hover:bg-bg-soft">
                    <td className="px-6 py-3">
                      <Link href={`/payroll/${r.id}`} className="block">
                        <div className="text-[13px] font-semibold capitalize text-ink hover:text-orange-600">
                          {formatPeriodFr(r.periode)}
                        </div>
                        <div className="font-mono-tabular text-[11px] text-ink-4">PR-{r.periode}</div>
                      </Link>
                    </td>
                    <td className="px-3 py-3">
                      <Badge tone={payrollStatusTone(r.status)} showDot={false}>
                        {t(`status.${r.status as PayrollRunStatus}`)}
                      </Badge>
                    </td>
                    <td className="font-mono-tabular px-3 py-3 text-right text-ink-2">
                      {formatNumber(r.nbEmployes, locale)}
                    </td>
                    <td className="font-mono-tabular px-3 py-3 text-right text-ink-2">
                      {formatMoney(r.totalGross, { locale, withCurrency: false })}
                    </td>
                    <td className="font-mono-tabular px-3 py-3 text-right text-ink-2">
                      {formatMoney(r.totalNet, { locale, withCurrency: false })}
                    </td>
                    <td className="font-mono-tabular px-6 py-3 text-right font-semibold text-ink">
                      {formatMoney(r.cost, { locale, withCurrency: false })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <Card>
        <div className="border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("dash.config.title")}</h3>
          <p className="text-[12px] text-ink-3">{t("dash.config.subtitle")}</p>
        </div>
        <div className="flex flex-col divide-y divide-line-soft">
          <ConfigRow
            icon={Settings}
            tone="orange"
            label={t("dash.config.payElements")}
            value={`${data.config.payElementsActive} / ${data.config.payElementsTotal}`}
            href="/pay-elements"
          />
          <ConfigRow
            icon={Scale}
            tone="violet"
            label={t("dash.config.taxScales")}
            value={String(data.config.taxScalesActive)}
            href="/tax-brackets"
          />
          <ConfigRow
            icon={Coins}
            tone="info"
            label={t("dash.config.lookup")}
            value={String(data.config.lookupActive)}
            href="/tax-brackets"
          />
          <div className="px-6 py-4">
            <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
              {t("dash.config.ytdTitle")}
            </div>
            <div className="mt-2 grid grid-cols-2 gap-3">
              <YtdStat label={t("dash.config.ytdGross")} value={compact(data.ytd.gross, locale)} unit={data.currency} />
              <YtdStat label={t("dash.config.ytdEmployer")} value={compact(data.ytd.employerCharges, locale)} unit={data.currency} />
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
}

function ConfigRow({
  icon,
  tone,
  label,
  value,
  href,
}: {
  icon: LucideIcon;
  tone: "orange" | "violet" | "info";
  label: string;
  value: string;
  href: string;
}) {
  return (
    <Link href={href} className="flex items-center gap-3 px-6 py-3.5 transition-colors hover:bg-bg-soft">
      <IconTile icon={icon} tone={tone} size="sm" />
      <span className="flex-1 text-[13px] font-medium text-ink-2">{label}</span>
      <span className="font-mono-tabular text-[14px] font-bold text-ink">{value}</span>
      <ArrowRight className="h-3.5 w-3.5 text-ink-4" />
    </Link>
  );
}

function YtdStat({ label, value, unit }: { label: string; value: string; unit: string }) {
  return (
    <div className="rounded-[12px] border border-line bg-bg-soft/40 p-3">
      <div className="text-[10.5px] font-medium uppercase tracking-[0.04em] text-ink-3">{label}</div>
      <div className="font-mono-tabular mt-1 text-[16px] font-bold tracking-tight text-ink">
        {value}
        <span className="ml-1 text-[10px] font-medium text-ink-3">{unit}</span>
      </div>
    </div>
  );
}

function StatCard({
  icon: Icon,
  tone,
  label,
  value,
  sub,
}: {
  icon: LucideIcon;
  tone: "info" | "warning" | "violet" | "success";
  label: string;
  value: string;
  sub: string;
}) {
  return (
    <Card className="p-5">
      <div className="flex items-start justify-between">
        <div className="min-w-0">
          <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">{label}</div>
          <div className="font-display font-mono-tabular mt-1.5 text-[22px] font-extrabold tracking-tight text-ink">
            {value}
          </div>
          <div className="mt-1 text-[11.5px] text-ink-3">{sub}</div>
        </div>
        <IconTile icon={Icon} tone={tone} size="sm" />
      </div>
    </Card>
  );
}

// ────────────────────────────────────────────────────────────────────────

function compact(n: number, locale: "fr" | "en"): string {
  const loc = locale === "fr" ? "fr-FR" : "en-US";
  if (Math.abs(n) >= 1_000_000) {
    return `${(n / 1_000_000).toLocaleString(loc, { maximumFractionDigits: 1 })}M`;
  }
  if (Math.abs(n) >= 1_000) {
    return `${(n / 1_000).toLocaleString(loc, { maximumFractionDigits: 0 })}k`;
  }
  return n.toLocaleString(loc, { maximumFractionDigits: 0 });
}
