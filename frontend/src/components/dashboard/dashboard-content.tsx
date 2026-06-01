"use client";

import { useQuery } from "@tanstack/react-query";
import type { LucideIcon } from "lucide-react";
import {
  Briefcase,
  CalendarRange,
  Check,
  ChevronRight,
  ClipboardCheck,
  Download,
  FileSignature,
  GraduationCap,
  Loader2,
  MoreHorizontal,
  Receipt,
  ShieldCheck,
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
import { EmployeeDashboard } from "@/components/dashboard/employee-dashboard";
import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Chip } from "@/components/ui/chip";
import { IconTile } from "@/components/ui/icon-tile";
import { Link } from "@/i18n/navigation";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatNumber } from "@/lib/format";
import { cn } from "@/lib/utils";

type Urgency = "today" | "thisWeek" | "later";
type TodoKind = "leave" | "expense" | "review" | "contract" | "medical";
type ActivityTone = "leave" | "expense" | "training" | "review" | "recruit" | "system";

type DashboardPayload = {
  me: { id: string; matricule: string; actorDisplayName?: string | null } | null;
  organization: string | null;
  hero: {
    effectif: { active: number; entries: number; departs: number; delta: number };
    masseSalariale: { value: number; month: string };
    absenteism: { pct: number; deltaPt: number };
    engagement: { score: number };
  };
  evolution: { series: { label: string; value: number }[]; entries: number; departs: number; turnoverPct: number; growthPct: number };
  departments: { total: number; items: { code: string; count: number }[] };
  todo: {
    items: {
      key: string;
      kind: TodoKind;
      employeeId: string;
      employeeName: string;
      title: string;
      description: string;
      when: string;
      urgency: Urgency;
      href: string;
    }[];
    urgent: number;
    total: number;
  };
  upcoming: { key: string; date: string; kind: string; title: string; description: string; href: string; cta?: string }[];
  activity: { key: string; when: string; actor: string; verb: string; tone: ActivityTone }[];
  keyMetrics: {
    openOffers: { value: number; series: number[] };
    trainings: { value: number; series: number[] };
    reviewsClose: { value: number; series: number[] };
    bulletins: { value: number; series: number[] };
  };
};

const DEPT_COLORS = ["#F97316", "#FB923C", "#FCD34D", "#34D399", "#60A5FA", "#A78BFA", "#F472B6", "#94A3B8"];

export function DashboardContent() {
  const t = useTranslations("dashboard");
  const tCommon = useTranslations("common");
  const locale = useLocale() as "fr" | "en";
  const { session } = useSession();
  const isHrManager = useCan(["hrm:leave:approve", "hrm:employee:create"]);

  // Plain employees only see their own data
  if (session && !isHrManager) return <EmployeeDashboard />;

  const query = useQuery({
    queryKey: ["hrm", "dashboard"],
    queryFn: () => apiFetch<DashboardPayload>("/api/hrm/dashboard"),
    refetchInterval: 60_000,
  });

  if (!session) return null;
  const user = session.user;
  const data = query.data;
  const greeting = user.fullName.split(" ")[0] ?? user.fullName;

  return (
    <>
      <PageHeader
        ucBadge="UC-27"
        breadcrumb={[{ label: tCommon("appName") }, { label: t("title") }]}
        title={t("greeting", { name: greeting })}
        subtitle={
          data
            ? t("headline", { actions: data.todo.urgent, deadlines: data.upcoming.length })
            : undefined
        }
        actions={
          <>
            <Button variant="secondary"><Download className="h-4 w-4" /> {t("actions.export")}</Button>
            <Link href="/employees/new"><Button><UserPlus className="h-4 w-4" /> {t("actions.newEmployee")}</Button></Link>
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
          <HeroRow data={data} locale={locale} t={t} />
          <EvolutionRow data={data} t={t} />
          <TodoRow data={data} t={t} />
          <QuickActivityRow data={data} t={t} />
          <MetricsRow data={data} t={t} />
        </div>
      )}
    </>
  );
}

// ────────────────────────────────────────────────────────────────────────
// Hero gradient KPI cards
// ────────────────────────────────────────────────────────────────────────

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
  icon: React.ComponentType<{ className?: string }>;
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
    <div
      className={cn(
        "relative overflow-hidden rounded-[20px] p-5 text-white shadow-lg-brand",
        gradClass,
      )}
    >
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
            {positive ? "+" : ""}{delta}{deltaUnit ?? ""}
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

// ────────────────────────────────────────────────────────────────────────
// Evolution chart + Departments donut
// ────────────────────────────────────────────────────────────────────────

function EvolutionRow({
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
            <Chip active tone="orange">12M</Chip>
            <Chip disabled>YTD</Chip>
            <Chip disabled>All</Chip>
          </div>
        </div>
        <div className="px-6 py-5">
          <LineChart series={data.evolution.series} />
          <div className="mt-4 flex flex-wrap items-center gap-5 border-t border-line-soft pt-3">
            <Legend dot="bg-success-500" label={<><b>+{data.evolution.entries}</b> {t("evolution.entries")}</>} />
            <Legend dot="bg-danger-500" label={<><b>-{data.evolution.departs}</b> {t("evolution.departs")}</>} />
            <Legend
              dot="bg-orange-500"
              label={<>{t("evolution.turnover")} <b>{data.evolution.turnoverPct.toLocaleString("fr-FR", { maximumFractionDigits: 1 })}%</b></>}
            />
            <div className="ml-auto text-[12px] text-ink-3">
              {t("evolution.netGrowth")}{" "}
              <b className={data.evolution.growthPct >= 0 ? "text-success-600" : "text-danger-600"}>
                {data.evolution.growthPct >= 0 ? "+" : ""}{data.evolution.growthPct.toLocaleString("fr-FR", { maximumFractionDigits: 1 })}%
              </b>
            </div>
          </div>
        </div>
      </Card>

      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("departments.title")}</h3>
          <button className="grid h-7 w-7 place-items-center rounded-[9px] border border-line text-ink-3 hover:border-line-strong">
            <MoreHorizontal className="h-3.5 w-3.5" />
          </button>
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
  const padL = 36, padR = 12, padT = 14, padB = 30;
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
        <linearGradient id="effArea" x1="0" x2="0" y1="0" y2="1">
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
      <path d={areaPath} fill="url(#effArea)" />
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
  const cx = size / 2, cy = size / 2;
  const sum = Math.max(1, items.reduce((a, b) => a + b.count, 0));
  let offset = 0;
  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg viewBox={`0 0 ${size} ${size}`} className="h-full w-full -rotate-90">
        <circle cx={cx} cy={cy} r={r} fill="none" stroke="#F3F4F6" strokeWidth={thick} />
        {items.map((it, idx) => {
          const frac = it.count / sum;
          const len = 2 * Math.PI * r;
          const dash = `${frac * len} ${len - frac * len}`;
          const dashOffset = -offset;
          offset += frac * len;
          return (
            <circle
              key={it.code}
              cx={cx}
              cy={cy}
              r={r}
              fill="none"
              stroke={DEPT_COLORS[idx % DEPT_COLORS.length]}
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

// ────────────────────────────────────────────────────────────────────────
// À traiter + Prochaines échéances
// ────────────────────────────────────────────────────────────────────────

const TODO_META: Record<TodoKind, { label: string; tone: "orange" | "success" | "violet" | "info" | "danger" | "warning" }> = {
  leave: { label: "Congé", tone: "warning" },
  expense: { label: "Note de frais", tone: "success" },
  review: { label: "Évaluation", tone: "violet" },
  contract: { label: "Contrat", tone: "info" },
  medical: { label: "Médical", tone: "danger" },
};

function TodoRow({
  data,
  t,
}: {
  data: DashboardPayload;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  const [filter, setFilter] = React.useState<"all" | "urgent">("all");
  const items = filter === "urgent" ? data.todo.items.filter((x) => x.urgency === "today") : data.todo.items;

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
              {t("todo.all", { count: data.todo.total })}
            </Chip>
            <Chip active={filter === "urgent"} tone="orange" onClick={() => setFilter("urgent")}>
              {t("todo.urgent", { count: data.todo.urgent })}
            </Chip>
          </div>
        </div>
        <div className="divide-y divide-line-soft">
          {items.length === 0 && (
            <div className="px-6 py-10 text-center text-[13px] text-ink-3">{t("todo.empty")}</div>
          )}
          {items.map((it) => {
            const meta = TODO_META[it.kind];
            return (
              <div key={it.key} className="flex items-center gap-3 px-6 py-3.5">
                <Avatar size="md" tone={pickAvatarTone(it.employeeId)} name={it.employeeName} />
                <div className="min-w-0 flex-1">
                  <div className="text-[13.5px] font-semibold text-ink">
                    {it.employeeName}
                    <span className="ml-1.5 font-normal text-ink-3">· {meta.label}</span>
                  </div>
                  <div className="truncate text-[12px] text-ink-3">{it.description}</div>
                </div>
                <UrgencyBadge urgency={it.urgency} t={t} />
                <div className="ml-2 flex items-center gap-1.5">
                  <Link href={it.href}>
                    <Button variant="secondary" size="sm">{t("todo.details")}</Button>
                  </Link>
                  <Link href={it.href}>
                    <Button size="sm"><Check className="h-3.5 w-3.5" /></Button>
                  </Link>
                </div>
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
          {data.upcoming.length === 0 && (
            <div className="px-6 py-10 text-center text-[13px] text-ink-3">{t("upcoming.empty")}</div>
          )}
          {data.upcoming.map((u, idx) => {
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
                  <Badge tone={kindTone(u.kind)} showDot={false}>{u.kind}</Badge>
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
  if (urgency === "today") return <Badge tone="danger" showDot={false}>{t("urgency.today")}</Badge>;
  if (urgency === "thisWeek") return <Badge tone="warning" showDot={false}>{t("urgency.thisWeek")}</Badge>;
  return <Badge tone="gray" showDot={false}>{t("urgency.later")}</Badge>;
}

function kindTone(kind: string): "orange" | "info" | "danger" | "violet" | "warning" | "success" | "gray" {
  const k = kind.toLowerCase();
  if (k.includes("paie")) return "orange";
  if (k.includes("recrut")) return "info";
  if (k.includes("conform")) return "danger";
  if (k.includes("personnel")) return "violet";
  return "gray";
}

function pickAvatarTone(seed: string): "orange" | "blue" | "green" | "violet" | "amber" | "teal" {
  const tones: ("orange" | "blue" | "green" | "violet" | "amber" | "teal")[] = ["orange", "blue", "green", "violet", "amber", "teal"];
  let h = 0;
  for (let i = 0; i < seed.length; i++) h = (h * 31 + seed.charCodeAt(i)) >>> 0;
  return tones[h % tones.length];
}

function splitDate(s: string): { day: string; monthShort: string } {
  if (!s) return { day: "—", monthShort: "" };
  const d = new Date(s);
  if (isNaN(d.getTime())) {
    // Period YYYY-MM
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

// ────────────────────────────────────────────────────────────────────────
// Quick actions + Activity feed
// ────────────────────────────────────────────────────────────────────────

const QUICK_ACTIONS = [
  { key: "employee", icon: UserPlus, tone: "orange" as const, href: "/employees/new" },
  { key: "payroll", icon: Wallet, tone: "warning" as const, href: "/payroll" },
  { key: "leaveCal", icon: CalendarRange, tone: "violet" as const, href: "/leaves" },
  { key: "offer", icon: Briefcase, tone: "info" as const, href: "/recruitment/offers/new" },
  { key: "training", icon: GraduationCap, tone: "success" as const, href: "/trainings/new" },
  { key: "declaration", icon: ShieldCheck, tone: "danger" as const, href: "/declarations/new" },
];

const ACTIVITY_META: Record<ActivityTone, { icon: LucideIcon; tone: "orange" | "success" | "warning" | "violet" | "info" | "gray" }> = {
  leave: { icon: CalendarRange, tone: "orange" },
  expense: { icon: Receipt, tone: "success" },
  training: { icon: GraduationCap, tone: "violet" },
  review: { icon: ClipboardCheck, tone: "warning" },
  recruit: { icon: Briefcase, tone: "info" },
  system: { icon: FileSignature, tone: "gray" },
};

function QuickActivityRow({
  data,
  t,
}: {
  data: DashboardPayload;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_1.4fr]">
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("quick.title")}</h3>
        </div>
        <div className="grid grid-cols-2 gap-3 p-5">
          {QUICK_ACTIONS.map((a) => (
            <Link
              key={a.key}
              href={a.href}
              className="group flex flex-col items-start gap-2.5 rounded-[12px] border border-line bg-white p-3.5 transition-all hover:-translate-y-px hover:border-line-strong hover:shadow-sm-brand"
            >
              <IconTile icon={a.icon} tone={a.tone} />
              <div className="text-[13px] font-semibold text-ink">{t(`quick.${a.key}`)}</div>
              <ChevronRight className="ml-auto h-3.5 w-3.5 text-ink-4 transition-transform group-hover:translate-x-0.5" />
            </Link>
          ))}
        </div>
      </Card>

      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("activity.title")}</h3>
          <Button variant="ghost" size="sm">{t("activity.seeAll")}</Button>
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
                <div className="text-[11px] text-ink-4">{shortRelative(a.when)}</div>
              </div>
            );
          })}
        </div>
      </Card>
    </div>
  );
}

function shortRelative(s: string): string {
  if (!s) return "—";
  const d = new Date(s);
  if (isNaN(d.getTime())) return s;
  const diff = Date.now() - d.getTime();
  const day = 86_400_000;
  if (diff < day) return "aujourd'hui";
  if (diff < 2 * day) return "hier";
  const days = Math.floor(diff / day);
  if (days < 7) return `il y a ${days}j`;
  if (days < 30) return `il y a ${Math.floor(days / 7)}sem`;
  return d.toLocaleDateString("fr-FR", { day: "2-digit", month: "short" });
}

// ────────────────────────────────────────────────────────────────────────
// Bottom sparkline metrics
// ────────────────────────────────────────────────────────────────────────

function MetricsRow({
  data,
  t,
}: {
  data: DashboardPayload;
  t: ReturnType<typeof useTranslations<"dashboard">>;
}) {
  const cards = [
    { key: "openOffers", value: data.keyMetrics.openOffers.value, series: data.keyMetrics.openOffers.series, color: "#3B82F6" },
    { key: "trainings", value: data.keyMetrics.trainings.value, series: data.keyMetrics.trainings.series, color: "#10B981" },
    { key: "reviewsClose", value: data.keyMetrics.reviewsClose.value, series: data.keyMetrics.reviewsClose.series, color: "#8B5CF6" },
    { key: "bulletins", value: data.keyMetrics.bulletins.value, series: data.keyMetrics.bulletins.series, color: "#F59E0B" },
  ];
  return (
    <div>
      <div className="mb-3 mt-2 text-[11px] font-semibold uppercase tracking-[0.08em] text-ink-3">
        {t("metrics.title")}
      </div>
      <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
        {cards.map((c) => (
          <div key={c.key} className="flex flex-col gap-2 rounded-[20px] border border-line bg-white p-4 shadow-sm-brand">
            <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
              {t(`metrics.${c.key}`)}
            </div>
            <div className="flex items-end justify-between">
              <div className="font-display font-mono-tabular text-[26px] font-extrabold leading-none tracking-tight text-ink">
                {c.value}
              </div>
              <Sparkline data={c.series} color={c.color} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function Sparkline({ data, color }: { data: number[]; color: string }) {
  const W = 90, H = 36;
  const id = React.useId();
  if (!data || data.length < 2) return <div style={{ width: W, height: H }} />;
  const max = Math.max(...data);
  const min = Math.min(...data);
  const range = Math.max(1, max - min);
  const step = W / (data.length - 1);
  const pts = data.map((v, i) => ({
    x: i * step,
    y: H - ((v - min) / range) * (H - 4) - 2,
  }));
  const path = pts.map((p, i) => `${i === 0 ? "M" : "L"}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(" ");
  const area = `${path} L${W},${H} L0,${H} Z`;
  return (
    <svg viewBox={`0 0 ${W} ${H}`} width={W} height={H}>
      <defs>
        <linearGradient id={id} x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stopColor={color} stopOpacity="0.28" />
          <stop offset="100%" stopColor={color} stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={area} fill={`url(#${id})`} />
      <path d={path} fill="none" stroke={color} strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}
