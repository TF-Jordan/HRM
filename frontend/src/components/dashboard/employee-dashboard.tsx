"use client";

import { useQuery } from "@tanstack/react-query";
import {
  BookOpen,
  Briefcase,
  CalendarRange,
  ChevronRight,
  Clock,
  Download,
  FileText,
  Loader2,
  TrendingUp,
  Wallet,
} from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { IconTile } from "@/components/ui/icon-tile";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { cn } from "@/lib/utils";

// ─── Types ───────────────────────────────────────────────────────────────────

type LeaveBalance = { type: string; restant: number; acquis: number; pris: number };
type RequestItem = {
  id: string;
  type: "leave" | "expense" | "mission";
  title: string;
  meta: string;
  status: string;
  step: number;
  href: string;
};
type EventItem = {
  key: string;
  date: string;
  tag: string;
  tagColor: "orange" | "amber" | "violet" | "blue" | "green" | "teal";
  title: string;
  sub: string;
  href: string;
};
type EmpDashPayload = {
  employee: { id: string; matricule: string; actorDisplayName?: string | null } | null;
  leaveBalances: LeaveBalance[];
  recentRequests: RequestItem[];
  upcomingEvents: EventItem[];
  monthlyHours: number;
  currentPeriode: string;
  payslipSeries: { periode: string; net: number }[];
  latestNet: number | null;
  annualLeaveBalance: { restant: number; acquis: number; pris: number } | null;
  activeEnrollmentCount: number;
  pendingReviewCount: number;
};

// ─── Main component ──────────────────────────────────────────────────────────

export function EmployeeDashboard() {
  const t = useTranslations("dashboard.employee");
  const { session } = useSession();

  const query = useQuery({
    queryKey: ["hrm", "dashboard", "me"],
    queryFn: () => apiFetch<EmpDashPayload>("/api/hrm/dashboard/me"),
    refetchInterval: 60_000,
  });

  if (!session) return null;
  const now = new Date();
  const hour = now.getHours();
  const firstName = session.user.firstName ?? session.user.fullName.split(" ")[0] ?? session.user.fullName;
  const greeting = hour < 12 ? t("greetingMorning", { name: firstName }) : hour < 18 ? t("greetingAfternoon", { name: firstName }) : t("greetingEvening", { name: firstName });

  const data = query.data;

  return (
    <>
      <PageHeader
        ucBadge="UC-28"
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={greeting}
        subtitle={
          data
            ? t("headline", {
                actions: (data.pendingReviewCount + data.recentRequests.filter((r) => r.step === 1).length),
              })
            : undefined
        }
        actions={
          <Link href="/leaves/new">
            <Button><CalendarRange className="h-4 w-4" />{t("quickActions.leave")}</Button>
          </Link>
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
          <KpiRow data={data} t={t} now={now} />
          <QuickActionsSection t={t} data={data} />
          <RequestsAndEvents data={data} t={t} />
          <LeaveAndPaySection data={data} t={t} />
        </div>
      )}
    </>
  );
}

// ─── KPI Row ─────────────────────────────────────────────────────────────────

function KpiRow({
  data,
  t,
  now,
}: {
  data: EmpDashPayload;
  t: ReturnType<typeof useTranslations<"dashboard.employee">>;
  now: Date;
}) {
  const annualBal = data.annualLeaveBalance;
  const monthLabel = now.toLocaleDateString("fr-FR", { month: "long" });

  const kpis = [
    {
      tone: "grad-orange",
      icon: CalendarRange,
      label: t("kpi.leaveBalance"),
      value: annualBal ? `${annualBal.restant.toFixed(1)}` : "—",
      unit: t("kpi.leaveUnit"),
      sub: annualBal ? t("kpi.leaveSub", { acquis: annualBal.acquis.toFixed(0) }) : "—",
    },
    {
      tone: "grad-dark",
      icon: Clock,
      label: t("kpi.monthHours"),
      value: `${data.monthlyHours.toFixed(0)}`,
      unit: "h",
      sub: t("kpi.monthHoursSub", { periode: monthLabel }),
    },
    {
      tone: "grad-amber",
      icon: Wallet,
      label: t("kpi.netMonth"),
      value: data.latestNet != null ? (data.latestNet / 1_000_000).toFixed(2) : "—",
      unit: data.latestNet != null ? "M XAF" : "",
      sub: t("kpi.netMonthSub"),
    },
    {
      tone: "grad-violet",
      icon: BookOpen,
      label: t("kpi.trainings"),
      value: `${data.activeEnrollmentCount}`,
      unit: t("kpi.trainingsUnit"),
      sub: t("kpi.trainingSub"),
    },
  ] as const;

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {kpis.map((k, i) => (
        <EmpKpiCard key={i} {...k} />
      ))}
    </div>
  );
}

function EmpKpiCard({
  tone,
  icon: Icon,
  label,
  value,
  unit,
  sub,
}: {
  tone: "grad-orange" | "grad-dark" | "grad-amber" | "grad-violet";
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  value: string;
  unit: string;
  sub: string;
}) {
  const gradClass = {
    "grad-orange": "bg-grad-orange",
    "grad-dark": "bg-grad-dark",
    "grad-amber": "bg-grad-amber",
    "grad-violet": "bg-grad-violet",
  }[tone];
  return (
    <div className={cn("relative overflow-hidden rounded-[20px] p-5 text-white shadow-lg-brand", gradClass)}>
      <div
        className="pointer-events-none absolute inset-0 opacity-50"
        style={{ background: "linear-gradient(180deg,rgba(255,255,255,0.18),transparent 50%)" }}
        aria-hidden
      />
      <div className="relative">
        <span className="grid h-10 w-10 place-items-center rounded-[12px] bg-white/20 backdrop-blur">
          <Icon className="h-5 w-5" />
        </span>
        <div className="mt-5">
          <div className="text-[11.5px] font-medium uppercase tracking-[0.06em] text-white/85">{label}</div>
          <div className="mt-1 flex items-baseline gap-2">
            <span className="font-display font-mono-tabular text-[34px] font-extrabold leading-none tracking-tight">
              {value}
            </span>
            {unit && <span className="text-[12px] text-white/90">{unit}</span>}
          </div>
          <div className="mt-1.5 text-[11.5px] text-white/80">{sub}</div>
        </div>
      </div>
    </div>
  );
}

// ─── Quick Actions ────────────────────────────────────────────────────────────

function QuickActionsSection({
  t,
  data,
}: {
  t: ReturnType<typeof useTranslations<"dashboard.employee">>;
  data: EmpDashPayload;
}) {
  const annualBal = data.annualLeaveBalance;

  const actions = [
    {
      href: "/leaves/new",
      icon: CalendarRange,
      tone: "orange" as const,
      label: t("quickActions.leave"),
      sub: annualBal ? `${annualBal.restant.toFixed(1)}j disponibles` : t("quickActions.leaveSub"),
    },
    {
      href: "/expenses/new",
      icon: FileText,
      tone: "warning" as const,
      label: t("quickActions.expense"),
      sub: t("quickActions.expenseSub"),
    },
    {
      href: "/timesheets/new",
      icon: Clock,
      tone: "info" as const,
      label: t("quickActions.time"),
      sub: t("quickActions.timeSub", { periode: data.currentPeriode }),
    },
    {
      href: "/mission-orders/new",
      icon: Briefcase,
      tone: "success" as const,
      label: t("quickActions.mission"),
      sub: t("quickActions.missionSub"),
    },
    {
      href: "/trainings/mine",
      icon: BookOpen,
      tone: "violet" as const,
      label: t("quickActions.training"),
      sub: t("quickActions.trainingSub"),
    },
    {
      href: "/documents",
      icon: Download,
      tone: "gray" as const,
      label: t("quickActions.documents"),
      sub: t("quickActions.documentsSub"),
    },
  ];

  return (
    <div>
      <div className="mb-3 text-[11px] font-semibold uppercase tracking-[0.08em] text-ink-3">
        {t("quickActions.title")}
      </div>
      <div className="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6">
        {actions.map((a) => (
          <Link
            key={a.href + a.label}
            href={a.href}
            className="group flex flex-col gap-3 rounded-[14px] border border-line bg-white p-4 transition-all hover:-translate-y-px hover:border-line-strong hover:shadow-sm-brand"
          >
            <IconTile icon={a.icon} tone={a.tone} />
            <div className="flex-1">
              <div className="text-[13px] font-semibold leading-tight text-ink">{a.label}</div>
              <div className="mt-1 text-[11px] text-ink-3">{a.sub}</div>
            </div>
            <ChevronRight className="h-3.5 w-3.5 self-end text-ink-4 transition-transform group-hover:translate-x-0.5" />
          </Link>
        ))}
      </div>
    </div>
  );
}

// ─── Recent requests + Upcoming events ───────────────────────────────────────

function RequestsAndEvents({
  data,
  t,
}: {
  data: EmpDashPayload;
  t: ReturnType<typeof useTranslations<"dashboard.employee">>;
}) {
  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1.4fr_1fr]">
      {/* Recent requests */}
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <div>
            <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("requests.title")}</h3>
            <p className="mt-0.5 text-[12px] text-ink-3">{t("requests.subtitle")}</p>
          </div>
          <Link href="/leaves/my">
            <Button variant="ghost" size="sm">{t("requests.seeAll")} <ChevronRight className="h-3 w-3" /></Button>
          </Link>
        </div>
        <div className="divide-y divide-line-soft">
          {data.recentRequests.length === 0 && (
            <div className="px-6 py-10 text-center text-[13px] text-ink-3">{t("requests.empty")}</div>
          )}
          {data.recentRequests.map((r) => (
            <Link key={r.id} href={r.href} className="flex items-center gap-3 px-6 py-3.5 hover:bg-bg-soft transition-colors">
              <IconTile icon={requestIcon(r.type)} tone={requestTone(r.type)} size="sm" />
              <div className="min-w-0 flex-1">
                <div className="truncate text-[13.5px] font-semibold text-ink">{r.title}</div>
                <div className="text-[11.5px] text-ink-3">{r.meta}</div>
              </div>
              <RequestStepper step={r.step} t={t} />
            </Link>
          ))}
        </div>
      </Card>

      {/* Upcoming events */}
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("upcoming.title")}</h3>
          <CalendarRange className="h-4 w-4 text-ink-3" />
        </div>
        <div className="flex flex-col gap-1 p-2.5">
          {data.upcomingEvents.length === 0 && (
            <div className="px-6 py-10 text-center text-[13px] text-ink-3">{t("upcoming.empty")}</div>
          )}
          {data.upcomingEvents.map((ev, idx) => {
            const { day, monthShort } = splitDate(ev.date);
            const featured = idx === 0;
            return (
              <Link
                key={ev.key}
                href={ev.href}
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
                  <Badge tone={tagColorToBadgeTone(ev.tagColor)} showDot={false}>{ev.tag}</Badge>
                  <div className="mt-1.5 truncate text-[13px] font-semibold text-ink">{ev.title}</div>
                  <div className="text-[11px] text-ink-3">{ev.sub}</div>
                </div>
              </Link>
            );
          })}
        </div>
      </Card>
    </div>
  );
}

function RequestStepper({
  step,
  t,
}: {
  step: number;
  t: ReturnType<typeof useTranslations<"dashboard.employee">>;
}) {
  const steps = [t("requests.stepSubmitted"), t("requests.stepValidated"), t("requests.stepClosed")];
  return (
    <div className="flex shrink-0 items-center gap-1">
      {steps.map((label, i) => (
        <span
          key={i}
          className={cn(
            "rounded-full px-2 py-0.5 text-[10px] font-semibold",
            i + 1 <= step
              ? "bg-success-100 text-success-700"
              : i + 1 === step + 1
                ? "bg-orange-100 text-orange-700 ring-1 ring-orange-300"
                : "bg-bg-dim text-ink-4",
          )}
        >
          {label}
        </span>
      ))}
    </div>
  );
}

// ─── Leave balances + Net evolution ──────────────────────────────────────────

function LeaveAndPaySection({
  data,
  t,
}: {
  data: EmpDashPayload;
  t: ReturnType<typeof useTranslations<"dashboard.employee">>;
}) {
  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_1.5fr]">
      {/* Leave balances donut */}
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("leaveDonut.title")}</h3>
        </div>
        <div className="px-6 py-5">
          <LeaveDonut balances={data.leaveBalances} t={t} />
          <Link href="/leaves/new">
            <Button className="mt-4 w-full justify-center" size="sm">
              <CalendarRange className="h-3.5 w-3.5" />{t("leaveDonut.requestLeave")}
            </Button>
          </Link>
        </div>
      </Card>

      {/* Net evolution line chart */}
      <Card>
        <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
          <div>
            <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("netEvolution.title")}</h3>
            <p className="mt-0.5 text-[12px] text-ink-3">{t("netEvolution.subtitle")}</p>
          </div>
          {data.payslipSeries.length >= 2 && (
            <span className="inline-flex items-center gap-1 rounded-full border border-success-300 bg-success-50 px-2 py-0.5 text-[11px] font-semibold text-success-700">
              <TrendingUp className="h-3 w-3" />
              {netGrowth(data.payslipSeries)}
            </span>
          )}
        </div>
        <div className="px-6 py-5">
          {data.payslipSeries.length < 2 ? (
            <div className="flex h-[200px] items-center justify-center text-[13px] text-ink-3">
              {t("netEvolution.noData")}
            </div>
          ) : (
            <NetLineChart series={data.payslipSeries} />
          )}
          <div className="mt-4 flex flex-wrap items-center gap-4 border-t border-line-soft pt-3 text-[12px]">
            {data.latestNet != null && (
              <span>
                {t("netEvolution.currentNet")}{" "}
                <b className="font-mono-tabular">{(data.latestNet / 1_000_000).toFixed(3)} M XAF</b>
              </span>
            )}
            <Link href="/payslips" className="ml-auto">
              <Button variant="secondary" size="sm">{t("netEvolution.seePayslips")}</Button>
            </Link>
          </div>
        </div>
      </Card>
    </div>
  );
}

// ─── Leave Donut ─────────────────────────────────────────────────────────────

const BALANCE_META: Record<string, { label: string; color: string; dot: string }> = {
  ANNUAL: { label: "Congé annuel", color: "#F97316", dot: "bg-orange-500" },
  SICK: { label: "Maladie", color: "#EF4444", dot: "bg-red-500" },
  MATERNITY: { label: "Maternité", color: "#EC4899", dot: "bg-pink-500" },
  PATERNITY: { label: "Paternité", color: "#3B82F6", dot: "bg-blue-500" },
  UNPAID: { label: "Sans solde", color: "#94A3B8", dot: "bg-slate-400" },
  SPECIAL: { label: "Congé spécial", color: "#F59E0B", dot: "bg-amber-500" },
};

function LeaveDonut({
  balances,
  t,
}: {
  balances: LeaveBalance[];
  t: ReturnType<typeof useTranslations<"dashboard.employee">>;
}) {
  const annualBal = balances.find((b) => b.type === "ANNUAL");
  const totalRestant = balances.reduce((a, b) => a + Math.max(0, b.restant), 0);
  const donutData = balances
    .filter((b) => b.acquis > 0)
    .map((b) => ({ value: Math.max(0, b.restant), color: BALANCE_META[b.type]?.color ?? "#94A3B8" }));
  const totalAcquis = balances.reduce((a, b) => a + b.acquis, 0);
  if (totalAcquis === 0) donutData.push({ value: 1, color: "#F3F4F6" });

  const size = 180, thick = 26;
  const r = (size - thick) / 2;
  const cx = size / 2, cy = size / 2;
  const sum = Math.max(1, donutData.reduce((a, b) => a + b.value, 0));
  let offset = 0;
  const len = 2 * Math.PI * r;

  return (
    <div className="flex flex-col gap-4">
      <div className="relative mx-auto" style={{ width: size, height: size }}>
        <svg viewBox={`0 0 ${size} ${size}`} className="h-full w-full -rotate-90">
          <circle cx={cx} cy={cy} r={r} fill="none" stroke="#F3F4F6" strokeWidth={thick} />
          {donutData.map((seg, i) => {
            const frac = seg.value / sum;
            const dash = `${frac * len} ${len - frac * len}`;
            const dashOffset = -offset;
            offset += frac * len;
            return (
              <circle
                key={i}
                cx={cx} cy={cy} r={r}
                fill="none"
                stroke={seg.color}
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
            <div className="font-display font-mono-tabular text-[32px] font-extrabold leading-none tracking-tight text-ink">
              {totalRestant.toFixed(1)}
            </div>
            <div className="text-[11px] text-ink-3">{t("leaveDonut.remaining")}</div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-2">
        {balances.filter((b) => b.acquis > 0).map((b) => {
          const meta = BALANCE_META[b.type] ?? { label: b.type, dot: "bg-slate-400" };
          return (
            <div key={b.type} className="rounded-[10px] bg-bg-dim p-2.5">
              <div className="flex items-center gap-1.5">
                <span className={cn("h-1.5 w-1.5 rounded-sm", meta.dot)} />
                <span className="text-[10.5px] font-semibold text-ink-3">{meta.label}</span>
              </div>
              <div className="mt-1 flex items-baseline gap-1.5">
                <span className="font-mono-tabular text-[17px] font-bold text-ink">{b.restant.toFixed(1)}</span>
                <span className="text-[10px] text-ink-3">/ {b.acquis.toFixed(0)}j</span>
              </div>
            </div>
          );
        })}
        {balances.filter((b) => b.acquis > 0).length === 0 && (
          <div className="col-span-2 text-center text-[12px] text-ink-3">{t("leaveDonut.noBalances")}</div>
        )}
      </div>
    </div>
  );
}

// ─── Net line chart ───────────────────────────────────────────────────────────

function NetLineChart({ series }: { series: { periode: string; net: number }[] }) {
  const W = 600, H = 200;
  const padL = 44, padR = 12, padT = 12, padB = 28;
  const innerW = W - padL - padR;
  const innerH = H - padT - padB;
  const values = series.map((p) => p.net);
  const maxV = Math.max(1, ...values);
  const minV = Math.min(0, ...values);
  const range = Math.max(1, maxV - minV);
  const step = series.length > 1 ? innerW / (series.length - 1) : innerW;

  const pts = series.map((p, i) => ({
    x: padL + step * i,
    y: padT + (1 - (p.net - minV) / range) * innerH,
  }));
  const path = pts.map((p, i) => `${i === 0 ? "M" : "L"}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(" ");
  const areaPath = `${path} L${(pts.at(-1)?.x ?? 0).toFixed(1)},${padT + innerH} L${pts[0]?.x.toFixed(1)},${padT + innerH} Z`;

  const gridLines = 4;
  const gridYs = Array.from({ length: gridLines + 1 }, (_, i) => padT + (innerH / gridLines) * i);
  const gridVs = Array.from({ length: gridLines + 1 }, (_, i) =>
    (maxV - (range / gridLines) * i) / 1_000_000,
  );

  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="h-[200px] w-full" role="img">
      <defs>
        <linearGradient id="netArea" x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stopColor="#F97316" stopOpacity="0.30" />
          <stop offset="100%" stopColor="#F97316" stopOpacity="0" />
        </linearGradient>
      </defs>
      {gridYs.map((y, i) => (
        <g key={i}>
          <line x1={padL} x2={W - padR} y1={y} y2={y} stroke="#E5E7EB" strokeDasharray="3 4" />
          <text x={padL - 6} y={y + 4} textAnchor="end" fontSize="10" fill="#9CA3AF">
            {gridVs[i]?.toFixed(2)}M
          </text>
        </g>
      ))}
      <path d={areaPath} fill="url(#netArea)" />
      <path d={path} fill="none" stroke="#F97316" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
      {pts.map((p, i) => (
        <circle key={i} cx={p.x} cy={p.y} r="3" fill="#fff" stroke="#F97316" strokeWidth="2" />
      ))}
      {series.map((p, i) => (
        <text key={i} x={padL + step * i} y={H - 6} textAnchor="middle" fontSize="9.5" fill="#9CA3AF">
          {p.periode.slice(5)}
        </text>
      ))}
    </svg>
  );
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function requestIcon(type: RequestItem["type"]) {
  if (type === "leave") return CalendarRange;
  if (type === "expense") return FileText;
  return Briefcase;
}

function requestTone(type: RequestItem["type"]): "orange" | "warning" | "info" {
  if (type === "leave") return "orange";
  if (type === "expense") return "warning";
  return "info";
}

function tagColorToBadgeTone(
  c: EventItem["tagColor"],
): "orange" | "warning" | "violet" | "info" | "success" | "gray" {
  const map: Record<string, "orange" | "warning" | "violet" | "info" | "success" | "gray"> = {
    orange: "orange",
    amber: "warning",
    violet: "violet",
    blue: "info",
    green: "success",
    teal: "success",
  };
  return map[c] ?? "gray";
}

function splitDate(s: string): { day: string; monthShort: string } {
  if (!s) return { day: "—", monthShort: "" };
  const d = new Date(s);
  if (isNaN(d.getTime())) {
    const parts = s.split("-");
    if (parts.length >= 2) {
      const mIdx = Number(parts[1]) - 1;
      return { day: "01", monthShort: MONTHS[mIdx] ?? "" };
    }
    return { day: s.slice(-2), monthShort: "" };
  }
  return {
    day: String(d.getDate()).padStart(2, "0"),
    monthShort: MONTHS[d.getMonth()] ?? "",
  };
}

const MONTHS = ["jan", "fév", "mar", "avr", "mai", "juin", "juil", "août", "sep", "oct", "nov", "déc"];

function netGrowth(series: { net: number }[]): string {
  const first = series[0]?.net ?? 0;
  const last = series.at(-1)?.net ?? 0;
  if (first === 0) return "+0%";
  const pct = ((last - first) / first) * 100;
  return `${pct >= 0 ? "+" : ""}${pct.toFixed(0)}%`;
}

