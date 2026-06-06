"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ArrowDownCircle,
  ArrowUpCircle,
  CalendarClock,
  CalendarRange,
  CheckCircle2,
  FileText,
  Loader2,
  MapPin,
  Plane,
  Receipt,
  Wallet,
  XCircle,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Textarea } from "@/components/ui/input";
import { ProgressBar } from "@/components/ui/progress-bar";
import { StatCard, StatCardGrid } from "@/components/ui/stat-card";
import { AppLink as Link } from "@/components/ui/app-link";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatMoney, formatNumber } from "@/lib/format";
import { missionStatusTone } from "@/lib/mission-status";
import type { BadgeTone } from "@/lib/mission-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { MissionOrderResponse, MissionOrderStatus } from "@/server/ksm/modules/missions";

type Reconciliation = {
  advance: number;
  approvedExpenses: number;
  pendingExpenses: number;
  reportCount: number;
  balance: number; // approvedExpenses - advance
};

type EnrichedOrder = MissionOrderResponse & { reconciliation: Reconciliation };

type MinePayload = {
  employee: EmployeeResponse | null;
  orders: EnrichedOrder[];
};

type ReconState = "NONE" | "PENDING" | "BALANCED" | "TO_REIMBURSE" | "TO_RECOVER";

function reconState(r: Reconciliation): ReconState {
  if (r.advance === 0 && r.reportCount === 0) return "NONE";
  if (r.pendingExpenses > 0 && r.approvedExpenses < r.advance) return "PENDING";
  if (r.balance > 0) return "TO_REIMBURSE";
  if (r.balance < 0) return "TO_RECOVER";
  return "BALANCED";
}

function reconTone(state: ReconState): BadgeTone {
  switch (state) {
    case "TO_REIMBURSE":
      return "success";
    case "TO_RECOVER":
      return "warning";
    case "PENDING":
      return "info";
    case "BALANCED":
      return "teal";
    default:
      return "gray";
  }
}

function daysBetween(start: string, end: string): number {
  const a = new Date(start).getTime();
  const b = new Date(end).getTime();
  if (Number.isNaN(a) || Number.isNaN(b)) return 0;
  return Math.max(1, Math.round((b - a) / 86_400_000) + 1);
}

const NEEDS_REGUL: ReconState[] = ["PENDING", "TO_REIMBURSE", "TO_RECOVER"];

export function MissionsInbox() {
  const t = useTranslations("missions");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const canExpense = useCan("hrm:expense:create");
  const [declineFor, setDeclineFor] = React.useState<MissionOrderResponse | null>(null);
  const [historyFilter, setHistoryFilter] = React.useState<MissionOrderStatus | "ALL">("ALL");
  // Captured once at mount so date math stays pure across renders.
  const [today] = React.useState(() => new Date());

  const query = useQuery({
    queryKey: ["hrm", "mission-orders", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/mission-orders/mine"),
    refetchInterval: 60_000,
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "mission-orders"] });
  }, [queryClient]);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const acceptMutation = useMutation({
    mutationFn: (id: string) =>
      apiFetch<MissionOrderResponse>(`/api/hrm/mission-orders/${id}/accept`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("detail.acceptSuccess"));
      invalidate();
    },
    onError: handleError,
  });

  const declineMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      apiFetch<MissionOrderResponse>(`/api/hrm/mission-orders/${id}/decline`, {
        method: "POST",
        body: { reason },
      }),
    onSuccess: () => {
      toast.success(t("detail.declineSuccess"));
      setDeclineFor(null);
      invalidate();
    },
    onError: handleError,
  });

  if (query.isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (query.error || !query.data) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {query.error instanceof BffApiError ? query.error.message : "—"}
      </div>
    );
  }

  const orders = query.data.orders;
  const pending = orders.filter((o) => o.status === "PENDING_ACCEPTANCE");
  const active = orders.filter(
    (o) =>
      o.status === "APPROVED" ||
      o.status === "IN_PROGRESS" ||
      (o.status === "COMPLETED" && NEEDS_REGUL.includes(reconState(o.reconciliation))),
  );
  const activeIds = new Set([...pending, ...active].map((o) => o.id));
  const history = orders.filter((o) => !activeIds.has(o.id));

  const completedCount = orders.filter((o) => o.status === "COMPLETED").length;
  const toRegularize = orders.filter((o) =>
    NEEDS_REGUL.includes(reconState(o.reconciliation)),
  ).length;
  const inProgressCount = orders.filter(
    (o) => o.status === "APPROVED" || o.status === "IN_PROGRESS",
  ).length;

  // Financial portfolio — advances vs justified, and the payroll-relevant balances. Recover/
  // reimburse are computed on COMPLETED missions to mirror what the backend actually settles.
  const completedOrders = orders.filter((o) => o.status === "COMPLETED");
  const portfolio = {
    advances: orders.reduce((s, o) => s + o.reconciliation.advance, 0),
    justified: orders.reduce((s, o) => s + o.reconciliation.approvedExpenses, 0),
    toRecover: completedOrders.reduce(
      (s, o) => s + Math.max(0, o.reconciliation.advance - o.reconciliation.approvedExpenses),
      0,
    ),
    toReimburse: completedOrders.reduce(
      (s, o) => s + Math.max(0, o.reconciliation.approvedExpenses - o.reconciliation.advance),
      0,
    ),
  };
  const hasFinancials =
    portfolio.advances > 0 || portfolio.justified > 0 || portfolio.toReimburse > 0;

  const todayMs = today.getTime();
  const heroMission =
    orders.find((o) => o.status === "IN_PROGRESS") ??
    orders
      .filter((o) => o.status === "APPROVED" && new Date(o.dateDebut).getTime() >= todayMs)
      .sort((a, b) => a.dateDebut.localeCompare(b.dateDebut))[0] ??
    [...orders]
      .filter((o) => o.status === "APPROVED" || o.status === "COMPLETED")
      .sort((a, b) => b.dateDebut.localeCompare(a.dateDebut))[0] ??
    null;
  const daysThisYear = orders
    .filter(
      (o) =>
        (o.status === "APPROVED" || o.status === "IN_PROGRESS" || o.status === "COMPLETED") &&
        new Date(o.dateDebut).getFullYear() === today.getFullYear(),
    )
    .reduce((s, o) => s + daysBetween(o.dateDebut, o.dateFin), 0);

  const filteredHistory =
    historyFilter === "ALL" ? history : history.filter((o) => o.status === historyFilter);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("inbox.title") }]}
        title={t("inbox.title")}
        subtitle={t("inbox.subtitle")}
      />

      <MissionHero
        mission={heroMission}
        daysThisYear={daysThisYear}
        todayMs={todayMs}
        locale={locale}
      />

      {hasFinancials && <FinancialPortfolio portfolio={portfolio} locale={locale} />}

      <StatCardGrid>
        <StatCard
          label={t("inbox.kpi.pending")}
          value={pending.length}
          sub={t("status.PENDING_ACCEPTANCE")}
          tone="amber"
        />
        <StatCard
          label={t("inbox.kpi.active")}
          value={inProgressCount}
          sub={t("status.IN_PROGRESS")}
          tone="orange"
        />
        <StatCard
          label={t("inbox.kpi.toRegularize")}
          value={toRegularize}
          sub={t("inbox.recon.title")}
          tone={toRegularize > 0 ? "red" : "green"}
        />
        <StatCard
          label={t("inbox.kpi.completed")}
          value={completedCount}
          sub={t("status.COMPLETED")}
          tone="green"
        />
      </StatCardGrid>

      {/* Action requise — ordres en attente de validation de l'employé */}
      <section className="mb-6">
        <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold uppercase tracking-wider text-ink-2">
          <span className="inline-block h-2 w-2 animate-pulse rounded-full bg-warning-500 shadow-[0_0_8px_rgba(245,158,11,0.6)]" />
          {t("inbox.pendingTitle")}
          {pending.length > 0 && <Badge tone="warning">{pending.length}</Badge>}
        </h2>
        {pending.length === 0 ? (
          <Card>
            <CardContent padding="md">
              <p className="text-center text-[13px] text-ink-3">{t("inbox.pendingEmpty")}</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-3 md:grid-cols-2">
            {pending.map((o) => (
              <PendingCard
                key={o.id}
                order={o}
                locale={locale}
                onAccept={() => acceptMutation.mutate(o.id)}
                onDecline={() => setDeclineFor(o)}
                accepting={acceptMutation.isPending && acceptMutation.variables === o.id}
              />
            ))}
          </div>
        )}
      </section>

      {/* Missions actives & avances à régulariser */}
      <section className="mb-6">
        <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold uppercase tracking-wider text-ink-2">
          <span className="inline-block h-2 w-2 rounded-full bg-orange-500 shadow-[0_0_8px_rgba(242,107,15,0.6)]" />
          {t("inbox.activeTitle")}
          {active.length > 0 && <Badge tone="orange">{active.length}</Badge>}
        </h2>
        {active.length === 0 ? (
          <Card>
            <CardContent padding="md">
              <p className="text-center text-[13px] text-ink-3">{t("inbox.activeEmpty")}</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-3 lg:grid-cols-2">
            {active.map((o) => (
              <ActiveMissionCard key={o.id} order={o} locale={locale} canExpense={canExpense} />
            ))}
          </div>
        )}
      </section>

      {/* Historique */}
      <section>
        <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
          <h2 className="text-[13px] font-bold uppercase tracking-wider text-ink-2">
            {t("inbox.historyTitle")}
          </h2>
          <div className="flex flex-wrap gap-1.5">
            {(["ALL", "APPROVED", "IN_PROGRESS", "COMPLETED", "DECLINED"] as const).map((f) => (
              <button
                key={f}
                type="button"
                onClick={() => setHistoryFilter(f)}
                className={
                  "rounded-full px-3 py-1 text-[12px] font-medium transition-colors " +
                  (historyFilter === f
                    ? "bg-ink text-white"
                    : "bg-bg-soft text-ink-3 hover:text-ink")
                }
              >
                {f === "ALL"
                  ? t("filters.all")
                  : f === "APPROVED"
                    ? t("filters.approved")
                    : f === "IN_PROGRESS"
                      ? t("filters.inProgress")
                      : f === "COMPLETED"
                        ? t("filters.completed")
                        : t("filters.declined")}
              </button>
            ))}
          </div>
        </div>
        {filteredHistory.length === 0 ? (
          <Card>
            <CardContent padding="md">
              <p className="text-center text-[13px] text-ink-3">{t("inbox.historyEmpty")}</p>
            </CardContent>
          </Card>
        ) : (
          <div className="overflow-hidden rounded-[20px] border border-line bg-white shadow-sm-brand">
            <table className="w-full border-collapse">
              <tbody>
                {filteredHistory.map((o) => (
                  <tr
                    key={o.id}
                    className="border-b border-line-soft last:border-b-0 hover:bg-bg-soft"
                  >
                    <td className="px-5 py-3">
                      <div className="flex items-start gap-2">
                        <MapPin className="mt-[2px] h-3.5 w-3.5 shrink-0 text-orange-500" />
                        <div>
                          <div className="text-[13.5px] font-semibold text-ink">{o.destination}</div>
                          <div className="text-[11.5px] text-ink-3">{o.objet}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-3 py-3 text-[12px] text-ink-2">
                      {formatDate(o.dateDebut, { locale })} → {formatDate(o.dateFin, { locale })}
                    </td>
                    <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] font-bold text-ink">
                      {o.montantAvance != null ? formatNumber(Number(o.montantAvance), locale) : "—"}
                    </td>
                    <td className="px-3 py-3">
                      <Badge tone={missionStatusTone(o.status)}>{t(`status.${o.status}`)}</Badge>
                    </td>
                    <td className="px-5 py-3 text-right">
                      <Link
                        href={`/mission-orders/${o.id}`}
                        className="text-[12px] font-semibold text-orange-600 hover:text-orange-700"
                      >
                        {tCommon("actions.view")} →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <DeclineDialog
        order={declineFor}
        onClose={() => setDeclineFor(null)}
        onConfirm={(reason) => declineFor && declineMutation.mutate({ id: declineFor.id, reason })}
        loading={declineMutation.isPending}
      />
    </>
  );
}

function MissionHero({
  mission,
  daysThisYear,
  todayMs,
  locale,
}: {
  mission: EnrichedOrder | null;
  daysThisYear: number;
  todayMs: number;
  locale: "fr" | "en";
}) {
  const t = useTranslations("missions");
  const startMs = mission ? new Date(mission.dateDebut).getTime() : 0;
  const startsInDays = mission ? Math.ceil((startMs - todayMs) / 86_400_000) : 0;
  const isUpcoming = mission?.status === "APPROVED" && startsInDays > 0;

  return (
    <div className="mb-6 overflow-hidden rounded-[22px] border border-line bg-gradient-to-br from-ink to-[#23314d] text-white shadow-sm">
      <div className="grid gap-6 p-6 md:grid-cols-[1.4fr_1fr] md:p-7">
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-[12px] font-medium uppercase tracking-wide text-white/60">
            <Plane className="h-4 w-4" />
            {mission == null
              ? t("inbox.hero.none")
              : mission.status === "IN_PROGRESS"
                ? t("inbox.hero.ongoing")
                : isUpcoming
                  ? t("inbox.hero.upcoming")
                  : t("inbox.hero.latest")}
          </div>

          {mission == null ? (
            <p className="text-[14px] text-white/70">{t("inbox.hero.noneHint")}</p>
          ) : (
            <>
              <div className="flex items-start gap-2.5">
                <MapPin className="mt-1 h-5 w-5 shrink-0 text-orange-400" />
                <div>
                  <p className="text-[22px] font-bold leading-tight">{mission.destination}</p>
                  <p className="text-[13px] text-white/70">{mission.objet}</p>
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-x-5 gap-y-2 pt-1 text-[13px] text-white/80">
                <span className="inline-flex items-center gap-1.5">
                  <CalendarRange className="h-4 w-4 text-white/50" />
                  <span className="font-mono-tabular">
                    {formatDate(mission.dateDebut, { locale })} →{" "}
                    {formatDate(mission.dateFin, { locale })}
                  </span>
                </span>
                <span className="font-mono-tabular text-white/60">
                  {t("detail.days", { count: daysBetween(mission.dateDebut, mission.dateFin) })}
                </span>
                <Badge tone={missionStatusTone(mission.status)}>
                  {t(`status.${mission.status}`)}
                </Badge>
              </div>
              {isUpcoming && (
                <div className="inline-flex items-center gap-1.5 rounded-full bg-orange-500/20 px-3 py-1 text-[12.5px] font-medium text-orange-200">
                  <CalendarClock className="h-3.5 w-3.5" />
                  {t("inbox.hero.startsIn", { count: startsInDays })}
                </div>
              )}
            </>
          )}
        </div>

        <div className="flex items-center justify-center rounded-[16px] bg-white/5 p-5">
          <div className="text-center">
            <p className="font-mono-tabular text-[40px] font-bold leading-none">{daysThisYear}</p>
            <p className="mt-1.5 text-[12.5px] text-white/60">{t("inbox.hero.daysThisYear")}</p>
          </div>
        </div>
      </div>
    </div>
  );
}

function FinancialPortfolio({
  portfolio,
  locale,
}: {
  portfolio: { advances: number; justified: number; toRecover: number; toReimburse: number };
  locale: "fr" | "en";
}) {
  const t = useTranslations("missions");
  const cards = [
    {
      icon: Wallet,
      tone: "text-ink",
      label: t("inbox.portfolio.advances"),
      value: portfolio.advances,
    },
    {
      icon: CheckCircle2,
      tone: "text-success-600",
      label: t("inbox.portfolio.justified"),
      value: portfolio.justified,
    },
    {
      icon: ArrowDownCircle,
      tone: portfolio.toRecover > 0 ? "text-warning-600" : "text-ink-3",
      label: t("inbox.portfolio.toRecover"),
      value: portfolio.toRecover,
    },
    {
      icon: ArrowUpCircle,
      tone: portfolio.toReimburse > 0 ? "text-orange-600" : "text-ink-3",
      label: t("inbox.portfolio.toReimburse"),
      value: portfolio.toReimburse,
    },
  ];
  return (
    <div className="mb-6">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {cards.map((c) => (
          <Card key={c.label}>
            <CardContent padding="md">
              <div className="flex items-center justify-between">
                <span className="text-[12.5px] font-medium text-ink-3">{c.label}</span>
                <c.icon className={"h-4 w-4 " + c.tone} />
              </div>
              <p className={"mt-2 font-mono-tabular text-[20px] font-bold " + c.tone}>
                {formatMoney(c.value, { locale })}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>
      <p className="mt-2.5 flex items-start gap-1.5 text-[12px] text-ink-3">
        <Wallet className="mt-0.5 h-3.5 w-3.5 shrink-0 text-orange-500" />
        {t("inbox.portfolio.payrollNote")}
      </p>
    </div>
  );
}

function PendingCard({
  order,
  locale,
  onAccept,
  onDecline,
  accepting,
}: {
  order: EnrichedOrder;
  locale: "fr" | "en";
  onAccept: () => void;
  onDecline: () => void;
  accepting: boolean;
}) {
  const t = useTranslations("missions");
  return (
    <Card>
      <CardContent padding="md">
        <div className="mb-2 flex items-center justify-between">
          <Badge tone="warning">{t("inbox.myAction")}</Badge>
          <Link
            href={`/mission-orders/${order.id}`}
            className="text-[10.5px] font-semibold uppercase tracking-wider text-ink-4 hover:text-orange-600"
          >
            {t("inbox.viewDetail")} →
          </Link>
        </div>
        <div className="mb-3 flex items-start gap-2">
          <MapPin className="mt-1 h-4 w-4 shrink-0 text-orange-500" />
          <div>
            <div className="text-[15px] font-bold text-ink">{order.destination}</div>
            <div className="text-[12px] text-ink-3">{order.objet}</div>
          </div>
        </div>
        <dl className="mb-4 grid grid-cols-3 gap-x-4 gap-y-2 text-[12px]">
          <div>
            <dt className="text-[10.5px] uppercase tracking-wider text-ink-4">
              {t("inbox.periodLabel")}
            </dt>
            <dd className="font-mono-tabular text-ink-2">
              {formatDate(order.dateDebut, { locale })} → {formatDate(order.dateFin, { locale })}
            </dd>
          </div>
          <div>
            <dt className="text-[10.5px] uppercase tracking-wider text-ink-4">
              {t("inbox.daysLabel")}
            </dt>
            <dd className="font-mono-tabular text-ink-2">
              {t("detail.days", { count: daysBetween(order.dateDebut, order.dateFin) })}
            </dd>
          </div>
          <div>
            <dt className="text-[10.5px] uppercase tracking-wider text-ink-4">
              {t("inbox.allowanceLabel")}
            </dt>
            <dd className="font-mono-tabular font-bold text-ink">
              {order.montantAvance != null ? formatNumber(Number(order.montantAvance), locale) : "—"}
            </dd>
          </div>
        </dl>
        <div className="flex gap-2">
          <Button type="button" variant="secondary" onClick={onDecline} className="flex-1">
            <XCircle className="h-4 w-4" />
            {t("inbox.decline")}
          </Button>
          <Button type="button" onClick={onAccept} disabled={accepting} className="flex-1">
            {accepting ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <CheckCircle2 className="h-4 w-4" />
            )}
            {t("inbox.accept")}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

function ActiveMissionCard({
  order,
  locale,
  canExpense,
}: {
  order: EnrichedOrder;
  locale: "fr" | "en";
  canExpense: boolean;
}) {
  const t = useTranslations("missions");
  const r = order.reconciliation;
  const state = reconState(r);
  const hasAdvance = r.advance > 0;
  const covered = hasAdvance ? Math.min(r.approvedExpenses, r.advance) : r.approvedExpenses;

  return (
    <Card>
      <CardContent padding="md">
        <div className="mb-3 flex items-start justify-between gap-3">
          <div className="flex items-start gap-2">
            <MapPin className="mt-1 h-4 w-4 shrink-0 text-orange-500" />
            <div>
              <div className="text-[15px] font-bold text-ink">{order.destination}</div>
              <div className="text-[12px] text-ink-3">{order.objet}</div>
            </div>
          </div>
          <Badge tone={missionStatusTone(order.status)}>{t(`status.${order.status}`)}</Badge>
        </div>

        <div className="mb-4 flex items-center gap-4 text-[12px] text-ink-2">
          <span className="inline-flex items-center gap-1.5">
            <CalendarRange className="h-3.5 w-3.5 text-ink-4" />
            <span className="font-mono-tabular">
              {formatDate(order.dateDebut, { locale })} → {formatDate(order.dateFin, { locale })}
            </span>
          </span>
          <span className="font-mono-tabular text-ink-3">
            {t("detail.days", { count: daysBetween(order.dateDebut, order.dateFin) })}
          </span>
        </div>

        {/* Bloc régularisation de l'avance par notes de frais */}
        <div className="rounded-[14px] border border-line-soft bg-bg-soft/60 p-3.5">
          <div className="mb-2.5 flex items-center justify-between">
            <span className="flex items-center gap-1.5 text-[11px] font-bold uppercase tracking-wider text-ink-3">
              <Wallet className="h-3.5 w-3.5 text-orange-500" />
              {t("inbox.recon.title")}
            </span>
            <Badge tone={reconTone(state)}>
              {state === "NONE"
                ? t("inbox.recon.none")
                : state === "PENDING"
                  ? t("inbox.recon.pendingState")
                  : state === "BALANCED"
                    ? t("inbox.recon.balanced")
                    : state === "TO_REIMBURSE"
                      ? `${t("inbox.recon.toReimburse")} · ${formatMoney(Math.abs(r.balance), { locale })}`
                      : `${t("inbox.recon.toRecover")} · ${formatMoney(Math.abs(r.balance), { locale })}`}
            </Badge>
          </div>

          {state !== "NONE" && (
            <>
              {hasAdvance && (
                <ProgressBar value={covered} max={r.advance} size="sm" className="mb-2.5" />
              )}
              <dl className="grid grid-cols-3 gap-2 text-[11.5px]">
                <ReconCell label={t("inbox.recon.advance")} value={formatMoney(r.advance, { locale })} />
                <ReconCell
                  label={t("inbox.recon.approved")}
                  value={formatMoney(r.approvedExpenses, { locale })}
                  tone="success"
                />
                <ReconCell
                  label={t("inbox.recon.pending")}
                  value={formatMoney(r.pendingExpenses, { locale })}
                  tone="muted"
                />
              </dl>
            </>
          )}

          <div className="mt-3 flex items-center justify-between gap-2">
            <span className="inline-flex items-center gap-1.5 text-[11.5px] text-ink-3">
              <Receipt className="h-3.5 w-3.5 text-ink-4" />
              {t("inbox.recon.reports", { count: r.reportCount })}
            </span>
            <div className="flex items-center gap-2">
              <Link
                href={`/mission-orders/${order.id}`}
                className="inline-flex items-center gap-1 text-[11.5px] font-semibold text-ink-3 hover:text-orange-600"
              >
                <FileText className="h-3.5 w-3.5" />
                {t("inbox.viewDetail")}
              </Link>
              {canExpense && hasAdvance && (
                <Link href={`/expenses/new?missionOrderId=${order.id}`}>
                  <Button type="button" variant="secondary" className="!h-8 !px-3 !text-[12px]">
                    <Receipt className="h-3.5 w-3.5" />
                    {t("inbox.recon.justify")}
                  </Button>
                </Link>
              )}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function ReconCell({
  label,
  value,
  tone,
}: {
  label: string;
  value: string;
  tone?: "success" | "muted";
}) {
  return (
    <div>
      <dt className="text-[10px] uppercase tracking-wider text-ink-4">{label}</dt>
      <dd
        className={
          "font-mono-tabular font-bold " +
          (tone === "success" ? "text-success-600" : tone === "muted" ? "text-ink-3" : "text-ink")
        }
      >
        {value}
      </dd>
    </div>
  );
}

function DeclineDialog({
  order,
  onClose,
  onConfirm,
  loading,
}: {
  order: MissionOrderResponse | null;
  onClose: () => void;
  onConfirm: (reason: string) => void;
  loading: boolean;
}) {
  const t = useTranslations("missions.detail.decline");
  const tCommon = useTranslations("common");
  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{ reason: string }>({ mode: "onChange" });

  React.useEffect(() => {
    if (!order) reset({ reason: "" });
  }, [order, reset]);

  return (
    <Dialog
      open={!!order}
      onClose={onClose}
      title={
        <span className="flex items-center gap-2 text-danger-600">
          <XCircle className="h-5 w-5" />
          {t("title")}
        </span>
      }
      subtitle={t("subtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            variant="danger"
            disabled={!isValid || loading}
            onClick={handleSubmit((v) => onConfirm(v.reason))}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : t("confirm")}
          </Button>
        </>
      }
    >
      <Field label={t("reasonLabel")}>
        <Textarea
          rows={4}
          placeholder={t("reasonPlaceholder")}
          {...register("reason", { required: true, minLength: 5 })}
        />
      </Field>
    </Dialog>
  );
}
