"use client";

import { useQuery } from "@tanstack/react-query";
import type { LucideIcon } from "lucide-react";
import {
  ArrowUpRight,
  Briefcase,
  CalendarRange,
  CheckCircle2,
  ChevronRight,
  ClipboardCheck,
  FileSignature,
  FileText,
  GraduationCap,
  Loader2,
  Receipt,
  Sparkles,
  Star,
  TrendingDown,
  TrendingUp,
  UserPlus,
  Users,
  Wallet,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { AppLink as Link } from "@/components/ui/app-link";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Chip } from "@/components/ui/chip";
import { IconTile } from "@/components/ui/icon-tile";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatNumber } from "@/lib/format";
import { cn } from "@/lib/utils";

// ── Types ────────────────────────────────────────────────────────────────────

type Urgency = "today" | "thisWeek" | "later";
type TodoKind = "leave" | "expense" | "review" | "contract" | "medical";
type ActivityTone = "leave" | "expense" | "training" | "review" | "recruit" | "system";

type TodoItem = {
  key: string;
  kind: TodoKind;
  employeeId: string;
  employeeName: string;
  title: string;
  description: string;
  when: string;
  urgency: Urgency;
  href: string;
};

type Upcoming = {
  key: string;
  date: string;
  kind: string;
  title: string;
  description: string;
  href: string;
  cta?: string;
};

type DashboardPayload = {
  organization: string | null;
  hero: {
    effectif: { active: number; entries: number; departs: number; delta: number };
    masseSalariale: { value: number; month: string };
    absenteism: { pct: number; deltaPt: number };
    engagement: { score: number };
  };
  evolution: {
    series: { label: string; value: number }[];
    entries: number;
    departs: number;
    turnoverPct: number;
    growthPct: number;
  };
  departments: { total: number; items: { code: string; count: number }[] };
  todo: { items: TodoItem[]; urgent: number; total: number };
  upcoming: Upcoming[];
  activity: { key: string; when: string; actor: string; verb: string; tone: ActivityTone }[];
  recruitment: { openOffers: number; activeApplications: number; hires: number; interviewing: number };
  leaves: { pending: number };
  expenses: { pending: number };
  contracts: { expiringSoon: number };
};

const DEPT_COLORS = ["#F97316", "#FB923C", "#FCD34D", "#34D399", "#60A5FA", "#A78BFA", "#F472B6", "#94A3B8"];

// ── Root ───────────────────────────────────────────────────────────────────

export function HrAdminDashboard() {
  const t = useTranslations("dashboard");
  const tCommon = useTranslations("common");
  const locale = useLocale() as "fr" | "en";
  const { session } = useSession();
  const [today] = React.useState(() => new Date());

  const query = useQuery({
    queryKey: ["hrm", "dashboard"],
    queryFn: () => apiFetch<DashboardPayload>("/api/hrm/dashboard"),
    refetchInterval: 60_000,
  });

  if (!session) return null;
  const user = session.user;
  const owned = new Set(user.permissions.map((p) => p.split("#")[0] ?? p));
  const can = (p: string) => owned.has(p);
  const data = query.data;
  const greeting = user.firstName ?? user.fullName.split(" ")[0] ?? user.fullName;
  const dateLabel = today.toLocaleDateString(locale === "fr" ? "fr-FR" : "en-US", {
    weekday: "long",
    day: "numeric",
    month: "long",
  });

  return (
    <>
      <PageHeader
        ucBadge="UC-27"
        breadcrumb={[{ label: tCommon("appName") }, { label: t("title") }]}
        title={t("greeting", { name: greeting })}
        subtitle={dateLabel.charAt(0).toUpperCase() + dateLabel.slice(1)}
        actions={
          can("hrm:employee:create") ? (
            <Link href="/employees/new">
              <Button>
                <UserPlus className="h-4 w-4" /> {t("actions.newEmployee")}
              </Button>
            </Link>
          ) : undefined
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
        <div className="flex flex-col gap-5">
          <HeroRow data={data} locale={locale} t={t} />
          <ActionCenter data={data} can={can} t={t} />
          <AnalyticsRow data={data} t={t} />
          <QueueRow data={data} t={t} />
          <BottomRow data={data} locale={locale} t={t} />
        </div>
      )}
    </>
  );
}

// ── Hero KPI cards ───────────────────────────────────────────────────────────

function HeroRow({
  data,
  locale,
  t,
}: {
  data: DashboardPayload;
  locale: "fr" | "en";
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  const masseM = data.hero.masseSalariale.value / 1_000_000;
  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
      <HeroKpi
        tone="grad-orange"
        icon={Users}
        label={t("hero.effectif")}
        value={formatNumber(data.hero.effectif.active, locale)}
        delta={data.hero.effectif.delta}
        sub={t("hero.effectifSub", {
          entries: data.hero.effectif.entries,
          departs: data.hero.effectif.departs,
        })}
      />
      <HeroKpi
        tone="grad-dark"
        icon={Wallet}
        label={t("hero.masseSalariale")}
        value={`${masseM.toLocaleString(locale === "fr" ? "fr-FR" : "en-US", { maximumFractionDigits: 1 })}M`}
        sub={`XAF · ${data.hero.masseSalariale.month}`}
      />
      <HeroKpi
        tone="grad-amber"
        icon={CalendarRange}
        label={t("hero.absenteism")}
        value={`${data.hero.absenteism.pct.toLocaleString(locale === "fr" ? "fr-FR" : "en-US", { maximumFractionDigits: 1 })}%`}
        delta={data.hero.absenteism.deltaPt}
        deltaUnit="pt"
        sub={t("hero.absenteismSub")}
      />
      <HeroKpi
        tone="grad-violet"
        icon={Star}
        label={t("hero.engagement")}
        value={`${data.hero.engagement.score}`}
        sub={t("hero.engagementSub")}
      />
    </div>
  );
}

function HeroKpi({
  tone,
  icon: Icon,
  label,
  value,
  delta,
  deltaUnit,
  sub,
}: {
  tone: "grad-orange" | "grad-dark" | "grad-amber" | "grad-violet";
  icon: LucideIcon;
  label: string;
  value: string;
  delta?: number;
  deltaUnit?: string;
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
            {delta}
            {deltaUnit ?? ""}
          </span>
        )}
      </div>
      <div className="relative mt-6">
        <div className="text-[11.5px] font-medium uppercase tracking-[0.06em] text-white/85">{label}</div>
        <div className="font-display font-mono-tabular mt-1 text-[34px] font-extrabold leading-none tracking-tight">
          {value}
        </div>
        <div className="mt-1.5 text-[11.5px] text-white/80">{sub}</div>
      </div>
    </div>
  );
}

// ── Action center (operational priority tiles) ───────────────────────────────

function ActionCenter({
  data,
  can,
  t,
}: {
  data: DashboardPayload;
  can: (p: string) => boolean;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  const reviewsCount = data.todo.items.filter((x) => x.kind === "review").length;
  const tiles: {
    key: string;
    icon: LucideIcon;
    tone: "orange" | "success" | "violet" | "info" | "warning";
    count: number;
    href: string;
    perm: string;
  }[] = [
    { key: "leaves", icon: CalendarRange, tone: "orange", count: data.leaves.pending, href: "/leaves", perm: "hrm:leave:approve" },
    { key: "expenses", icon: Receipt, tone: "success", count: data.expenses.pending, href: "/expenses", perm: "hrm:expense:read" },
    { key: "reviews", icon: ClipboardCheck, tone: "violet", count: reviewsCount, href: "/reviews", perm: "hrm:review:read" },
    { key: "contracts", icon: FileText, tone: "info", count: data.contracts.expiringSoon, href: "/contracts", perm: "hrm:contract:read" },
    { key: "recruitment", icon: Briefcase, tone: "warning", count: data.recruitment.activeApplications, href: "/recruitment", perm: "hrm:recruitment:read" },
  ];

  const visible = tiles.filter((x) => can(x.perm));
  const totalPending = visible.reduce((a, x) => a + x.count, 0);

  return (
    <Card>
      <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
        <div className="flex items-center gap-2.5">
          <span className="grid h-8 w-8 place-items-center rounded-[10px] bg-grad-orange text-white shadow-sm-brand">
            <Sparkles className="h-4 w-4" />
          </span>
          <div>
            <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("hradmin.actionCenter")}</h3>
            <p className="text-[12px] text-ink-3">{t("hradmin.actionCenterSub")}</p>
          </div>
        </div>
        {totalPending === 0 ? (
          <Badge tone="success" showDot={false}>
            <CheckCircle2 className="h-3.5 w-3.5" /> {t("hradmin.allCaughtUp")}
          </Badge>
        ) : (
          <Badge tone="orange" showDot={false}>
            {t("hradmin.pendingTotal", { count: totalPending })}
          </Badge>
        )}
      </div>
      <div className="grid grid-cols-2 gap-3 p-5 md:grid-cols-3 xl:grid-cols-5">
        {visible.map((x) => (
          <Link
            key={x.key}
            href={x.href}
            className="group relative flex flex-col gap-3 overflow-hidden rounded-[16px] border border-line bg-white p-4 transition-all hover:-translate-y-0.5 hover:border-line-strong hover:shadow-md-brand"
          >
            <div className="flex items-center justify-between">
              <IconTile icon={x.icon} tone={x.tone} />
              <ArrowUpRight className="h-4 w-4 text-ink-4 transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
            </div>
            <div>
              <div className="font-display font-mono-tabular text-[28px] font-extrabold leading-none tracking-tight text-ink">
                {x.count}
              </div>
              <div className="mt-1 text-[12.5px] font-semibold text-ink-2">{t(`hradmin.actions.${x.key}`)}</div>
              <div className="text-[11px] text-ink-4">{x.count > 0 ? t("hradmin.toProcess") : t("hradmin.nothing")}</div>
            </div>
          </Link>
        ))}
      </div>
    </Card>
  );
}

// ── Analytics: evolution + departments ───────────────────────────────────────

function AnalyticsRow({
  data,
  t,
}: {
  data: DashboardPayload;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-[2fr_1fr]">
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <div>
            <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("evolution.title")}</h3>
            <p className="text-[12px] text-ink-3">{t("evolution.subtitle")}</p>
          </div>
          <div className="flex items-center gap-2">
            <Chip active tone="orange">
              12M
            </Chip>
          </div>
        </div>
        <div className="px-6 py-5">
          <LineChart series={data.evolution.series} />
          <div className="mt-4 flex flex-wrap items-center gap-5 border-t border-line-soft pt-3">
            <Legend
              dot="bg-success-500"
              label={
                <>
                  <b>+{data.evolution.entries}</b> {t("evolution.entries")}
                </>
              }
            />
            <Legend
              dot="bg-danger-500"
              label={
                <>
                  <b>-{data.evolution.departs}</b> {t("evolution.departs")}
                </>
              }
            />
            <Legend
              dot="bg-orange-500"
              label={
                <>
                  {t("evolution.turnover")}{" "}
                  <b>{data.evolution.turnoverPct.toLocaleString("fr-FR", { maximumFractionDigits: 1 })}%</b>
                </>
              }
            />
            <div className="ml-auto text-[12px] text-ink-3">
              {t("evolution.netGrowth")}{" "}
              <b className={data.evolution.growthPct >= 0 ? "text-success-600" : "text-danger-600"}>
                {data.evolution.growthPct >= 0 ? "+" : ""}
                {data.evolution.growthPct.toLocaleString("fr-FR", { maximumFractionDigits: 1 })}%
              </b>
            </div>
          </div>
        </div>
      </Card>

      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("departments.title")}</h3>
        </div>
        <div className="flex flex-col items-center gap-4 px-5 py-5">
          <DonutChart items={data.departments.items} total={data.departments.total} />
          <div className="flex w-full flex-col gap-2">
            {data.departments.items.slice(0, 6).map((d, idx) => (
              <div key={d.code} className="flex items-center gap-2 text-[12.5px]">
                <span
                  className="h-2 w-2 shrink-0 rounded-sm"
                  style={{ background: DEPT_COLORS[idx % DEPT_COLORS.length] }}
                />
                <span className="truncate text-ink-2">{d.code}</span>
                <span className="font-mono-tabular ml-auto text-ink-3">{d.count}</span>
              </div>
            ))}
            {data.departments.items.length === 0 && (
              <div className="py-2 text-center text-[12px] text-ink-3">{t("departments.empty")}</div>
            )}
          </div>
        </div>
      </Card>
    </div>
  );
}

function LineChart({ series }: { series: { label: string; value: number }[] }) {
  const W = 720;
  const H = 220;
  const padL = 36,
    padR = 12,
    padT = 14,
    padB = 30;
  const innerW = W - padL - padR;
  const innerH = H - padT - padB;
  const values = series.map((p) => p.value);
  const max = Math.max(1, ...values);
  const min = Math.min(0, ...values);
  const range = Math.max(1, max - min);
  const step = series.length > 1 ? innerW / (series.length - 1) : innerW;

  const pts = series.map((p, i) => {
    const x = padL + step * i;
    const y = padT + (1 - (p.value - min) / range) * innerH;
    return { x, y };
  });
  const path = pts.map((p, i) => `${i === 0 ? "M" : "L"}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(" ");
  const areaPath = `${path} L${(pts.at(-1)?.x ?? 0).toFixed(1)},${padT + innerH} L${pts[0]?.x.toFixed(1)},${padT + innerH} Z`;

  const gridLines = 4;
  const gridYs = Array.from({ length: gridLines + 1 }, (_, i) => padT + (innerH / gridLines) * i);
  const gridVs = Array.from({ length: gridLines + 1 }, (_, i) => Math.round(max - (range / gridLines) * i));

  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="h-[220px] w-full" role="img" aria-label="Évolution effectif">
      <defs>
        <linearGradient id="effAreaAdmin" x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stopColor="#F97316" stopOpacity="0.32" />
          <stop offset="100%" stopColor="#F97316" stopOpacity="0" />
        </linearGradient>
      </defs>
      {gridYs.map((y, i) => (
        <g key={i}>
          <line x1={padL} x2={W - padR} y1={y} y2={y} stroke="#E5E7EB" strokeDasharray="3 4" />
          <text x={padL - 6} y={y + 4} textAnchor="end" fontSize="10" fill="#9CA3AF">
            {gridVs[i]}
          </text>
        </g>
      ))}
      <path d={areaPath} fill="url(#effAreaAdmin)" />
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

function DonutChart({ items, total }: { items: { code: string; count: number }[]; total: number }) {
  const size = 180;
  const thick = 26;
  const r = (size - thick) / 2;
  const cx = size / 2,
    cy = size / 2;
  const sum = Math.max(1, items.reduce((a, b) => a + b.count, 0));
  const len = 2 * Math.PI * r;
  // Prefix sums precomputed so the render map stays free of mutation.
  const segments = items.map((it, idx) => {
    const consumed = (it.count / sum) * len;
    const before = items.slice(0, idx).reduce((acc, x) => acc + (x.count / sum) * len, 0);
    return {
      code: it.code,
      dash: `${consumed} ${len - consumed}`,
      dashOffset: -before,
      color: DEPT_COLORS[idx % DEPT_COLORS.length],
    };
  });

  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg viewBox={`0 0 ${size} ${size}`} className="h-full w-full -rotate-90">
        <circle cx={cx} cy={cy} r={r} fill="none" stroke="#F3F4F6" strokeWidth={thick} />
        {segments.map((s) => (
          <circle
            key={s.code}
            cx={cx}
            cy={cy}
            r={r}
            fill="none"
            stroke={s.color}
            strokeWidth={thick}
            strokeDasharray={s.dash}
            strokeDashoffset={s.dashOffset}
            strokeLinecap="butt"
          />
        ))}
      </svg>
      <div className="absolute inset-0 grid place-items-center text-center">
        <div>
          <div className="font-display font-mono-tabular text-[26px] font-extrabold leading-none tracking-tight text-ink">
            {total}
          </div>
          <div className="text-[11px] text-ink-3">Total</div>
        </div>
      </div>
    </div>
  );
}

function Legend({ dot, label }: { dot: string; label: React.ReactNode }) {
  return (
    <div className="flex items-center gap-2 text-[12px] text-ink-2">
      <span className={cn("h-2 w-2 rounded-full", dot)} />
      <span>{label}</span>
    </div>
  );
}

// ── Queue: à traiter + prochaines échéances ──────────────────────────────────

function QueueRow({
  data,
  t,
}: {
  data: DashboardPayload;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  const [filter, setFilter] = React.useState<"all" | "urgent">("all");
  // Medical is out of HR-Admin scope (page removed) — never surface it here.
  const base = data.todo.items.filter((x) => x.kind !== "medical");
  const items = filter === "urgent" ? base.filter((x) => x.urgency === "today") : base;
  // Declarations are out of HR-Admin scope — drop them from upcoming deadlines.
  const upcoming = data.upcoming.filter((u) => !u.href.startsWith("/declarations"));

  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1.2fr_1fr]">
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <div>
            <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("todo.title")}</h3>
            <p className="text-[12px] text-ink-3">{t("todo.subtitle")}</p>
          </div>
          <div className="flex items-center gap-2">
            <Chip active={filter === "all"} onClick={() => setFilter("all")}>
              {t("todo.all", { count: base.length })}
            </Chip>
            <Chip
              active={filter === "urgent"}
              tone="orange"
              onClick={() => setFilter("urgent")}
            >
              {t("todo.urgent", { count: base.filter((x) => x.urgency === "today").length })}
            </Chip>
          </div>
        </div>
        <div className="divide-y divide-line-soft">
          {items.length === 0 && (
            <div className="flex flex-col items-center gap-2 px-6 py-12 text-center">
              <span className="grid h-12 w-12 place-items-center rounded-full bg-success-50 text-success-500">
                <CheckCircle2 className="h-6 w-6" />
              </span>
              <div className="text-[13px] text-ink-3">{t("todo.empty")}</div>
            </div>
          )}
          {items.map((it) => {
            return (
              <div key={it.key} className="flex items-center gap-3 px-6 py-3.5">
                <Avatar size="md" tone={pickAvatarTone(it.employeeId)} name={it.employeeName} />
                <div className="min-w-0 flex-1">
                  <div className="text-[13.5px] font-semibold text-ink">
                    {it.employeeName}
                    <span className="ml-1.5 font-normal text-ink-3">· {t(`hradmin.kind.${it.kind}`)}</span>
                  </div>
                  <div className="truncate text-[12px] text-ink-3">{it.description}</div>
                </div>
                <UrgencyBadge urgency={it.urgency} t={t} />
                <Link href={it.href} className="ml-2">
                  <Button variant="secondary" size="sm">
                    {t("todo.details")}
                    <ChevronRight className="h-3.5 w-3.5" />
                  </Button>
                </Link>
              </div>
            );
          })}
        </div>
      </Card>

      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("upcoming.title")}</h3>
        </div>
        <div className="flex flex-col gap-1 p-2.5">
          {upcoming.length === 0 && (
            <div className="px-6 py-10 text-center text-[13px] text-ink-3">{t("upcoming.empty")}</div>
          )}
          {upcoming.map((u, idx) => {
            const featured = idx === 0;
            const { day, monthShort } = splitDate(u.date);
            return (
              <Link
                key={u.key}
                href={u.href}
                className={cn(
                  "flex items-start gap-3 rounded-[12px] p-3 transition-colors",
                  featured
                    ? "border border-orange-200 bg-[linear-gradient(135deg,#FFF4EB,#FFF8F2)]"
                    : "hover:bg-bg-soft",
                )}
              >
                <div
                  className={cn(
                    "w-12 shrink-0 rounded-[10px] py-1.5 text-center",
                    featured ? "bg-grad-orange text-white" : "border border-line bg-white text-ink",
                  )}
                >
                  <div className="font-display text-[18px] font-extrabold leading-none">{day}</div>
                  <div className="text-[9px] uppercase tracking-[0.06em] opacity-80">{monthShort}</div>
                </div>
                <div className="min-w-0 flex-1">
                  <Badge tone={kindTone(u.kind)} showDot={false}>
                    {u.kind}
                  </Badge>
                  <div className="mt-1.5 text-[13.5px] font-semibold text-ink">{u.title}</div>
                  <div className="text-[12px] text-ink-3">{u.description}</div>
                </div>
                {u.cta && (
                  <Button variant={featured ? "dark" : "secondary"} size="sm">
                    {u.cta}
                  </Button>
                )}
              </Link>
            );
          })}
        </div>
      </Card>
    </div>
  );
}

function UrgencyBadge({
  urgency,
  t,
}: {
  urgency: Urgency;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  if (urgency === "today")
    return (
      <Badge tone="danger" showDot={false}>
        {t("urgency.today")}
      </Badge>
    );
  if (urgency === "thisWeek")
    return (
      <Badge tone="warning" showDot={false}>
        {t("urgency.thisWeek")}
      </Badge>
    );
  return (
    <Badge tone="gray" showDot={false}>
      {t("urgency.later")}
    </Badge>
  );
}

// ── Bottom: recruitment pipeline + activity feed ─────────────────────────────

const ACTIVITY_META: Record<ActivityTone, { icon: LucideIcon; tone: "orange" | "success" | "warning" | "violet" | "info" | "gray" }> = {
  leave: { icon: CalendarRange, tone: "orange" },
  expense: { icon: Receipt, tone: "success" },
  training: { icon: GraduationCap, tone: "violet" },
  review: { icon: ClipboardCheck, tone: "warning" },
  recruit: { icon: Briefcase, tone: "info" },
  system: { icon: FileSignature, tone: "gray" },
};

function BottomRow({
  data,
  locale,
  t,
}: {
  data: DashboardPayload;
  locale: "fr" | "en";
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  const r = data.recruitment;
  const funnel = [
    { key: "openOffers", value: r.openOffers, tone: "info" as const, icon: Briefcase },
    { key: "applications", value: r.activeApplications, tone: "orange" as const, icon: Users },
    { key: "interviewing", value: r.interviewing, tone: "violet" as const, icon: ClipboardCheck },
    { key: "hires", value: r.hires, tone: "success" as const, icon: CheckCircle2 },
  ];
  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_1.4fr]">
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("hradmin.recruitment.title")}</h3>
          <Link href="/recruitment">
            <Button variant="ghost" size="sm">
              {t("activity.seeAll")}
            </Button>
          </Link>
        </div>
        <div className="grid grid-cols-2 gap-3 p-5">
          {funnel.map((f) => (
            <div
              key={f.key}
              className="flex items-center gap-3 rounded-[14px] border border-line bg-white p-3.5 shadow-xs-brand"
            >
              <IconTile icon={f.icon} tone={f.tone} />
              <div className="min-w-0">
                <div className="font-display font-mono-tabular text-[22px] font-extrabold leading-none tracking-tight text-ink">
                  {formatNumber(f.value, locale)}
                </div>
                <div className="mt-0.5 truncate text-[11.5px] text-ink-3">{t(`hradmin.recruitment.${f.key}`)}</div>
              </div>
            </div>
          ))}
        </div>
      </Card>

      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("activity.title")}</h3>
        </div>
        <div className="divide-y divide-line-soft">
          {data.activity.length === 0 && (
            <div className="px-6 py-10 text-center text-[13px] text-ink-3">{t("activity.empty")}</div>
          )}
          {data.activity.map((a) => {
            const meta = ACTIVITY_META[a.tone];
            return (
              <div key={a.key} className="flex items-center gap-3 px-6 py-3">
                <IconTile icon={meta.icon} tone={meta.tone} size="sm" />
                <div className="min-w-0 flex-1 text-[13px] text-ink-2">
                  <b className="text-ink">{a.actor}</b> <span className="text-ink-3">{a.verb}</span>
                </div>
                <div className="text-[11px] text-ink-4">{shortRelative(a.when, locale)}</div>
              </div>
            );
          })}
        </div>
      </Card>
    </div>
  );
}

// ── Helpers ──────────────────────────────────────────────────────────────────

function kindTone(kind: string): "orange" | "info" | "danger" | "violet" | "warning" | "success" | "gray" {
  const k = kind.toLowerCase();
  if (k.includes("paie")) return "orange";
  if (k.includes("recrut")) return "info";
  if (k.includes("conform")) return "danger";
  if (k.includes("personnel")) return "violet";
  return "gray";
}

function pickAvatarTone(seed: string): "orange" | "blue" | "green" | "violet" | "amber" | "teal" {
  const tones: ("orange" | "blue" | "green" | "violet" | "amber" | "teal")[] = [
    "orange",
    "blue",
    "green",
    "violet",
    "amber",
    "teal",
  ];
  let h = 0;
  for (let i = 0; i < seed.length; i++) h = (h * 31 + seed.charCodeAt(i)) >>> 0;
  return tones[h % tones.length];
}

function splitDate(s: string): { day: string; monthShort: string } {
  if (!s) return { day: "—", monthShort: "" };
  const d = new Date(s);
  if (isNaN(d.getTime())) {
    const parts = s.split("-");
    if (parts.length >= 2) {
      const mIdx = Number(parts[1]) - 1;
      return { day: "01", monthShort: monthShort(mIdx) };
    }
    return { day: s.slice(-2), monthShort: "" };
  }
  return { day: String(d.getDate()).padStart(2, "0"), monthShort: monthShort(d.getMonth()) };
}

function monthShort(i: number) {
  return ["jan", "fév", "mar", "avr", "mai", "juin", "juil", "août", "sep", "oct", "nov", "déc"][i] ?? "";
}

function shortRelative(s: string, locale: "fr" | "en"): string {
  if (!s) return "—";
  const d = new Date(s);
  if (isNaN(d.getTime())) return s;
  const diff = Date.now() - d.getTime();
  const day = 86_400_000;
  if (diff < day) return locale === "fr" ? "aujourd'hui" : "today";
  if (diff < 2 * day) return locale === "fr" ? "hier" : "yesterday";
  const days = Math.floor(diff / day);
  if (days < 7) return locale === "fr" ? `il y a ${days}j` : `${days}d ago`;
  if (days < 30) return locale === "fr" ? `il y a ${Math.floor(days / 7)}sem` : `${Math.floor(days / 7)}w ago`;
  return d.toLocaleDateString(locale === "fr" ? "fr-FR" : "en-US", { day: "2-digit", month: "short" });
}
