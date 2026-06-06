"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  CalendarRange,
  CheckCircle2,
  Loader2,
  MapPin,
  PlaneTakeoff,
  Plus,
  Search,
  Send,
  XCircle,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { StatCard, StatCardGrid } from "@/components/ui/stat-card";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatNumber } from "@/lib/format";
import { missionStatusTone } from "@/lib/mission-status";
import { cn } from "@/lib/utils";
import type { EnrichedMissionOrderResponse } from "@/server/ksm/modules/missions";

type Filter = "ALL" | "DRAFT" | "PENDING_ACCEPTANCE" | "APPROVED" | "IN_PROGRESS" | "COMPLETED" | "DECLINED";

const FILTERS: { key: Filter; tKey: string }[] = [
  { key: "ALL", tKey: "filters.all" },
  { key: "DRAFT", tKey: "filters.draft" },
  { key: "PENDING_ACCEPTANCE", tKey: "filters.pending" },
  { key: "APPROVED", tKey: "filters.approved" },
  { key: "IN_PROGRESS", tKey: "filters.inProgress" },
  { key: "COMPLETED", tKey: "filters.completed" },
  { key: "DECLINED", tKey: "filters.declined" },
];

const norm = (s: string) => s.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();

export function MissionsQueue() {
  const t = useTranslations("missions");
  const tQ = useTranslations("missions.queue");
  const tDetail = useTranslations("missions.detail");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const queryClient = useQueryClient();
  const canCreate = useCan("hrm:mission:create");
  const canManage = useCan("hrm:mission:manage");

  const [filter, setFilter] = React.useState<Filter>("ALL");
  const [search, setSearch] = React.useState("");
  const [cancelTarget, setCancelTarget] = React.useState<EnrichedMissionOrderResponse | null>(null);
  const [actingId, setActingId] = React.useState<string | null>(null);

  const query = useQuery({
    queryKey: ["hrm", "mission-orders", "list"],
    queryFn: () => apiFetch<EnrichedMissionOrderResponse[]>("/api/hrm/mission-orders"),
    refetchInterval: 60_000,
  });

  const all = React.useMemo(() => query.data ?? [], [query.data]);

  const counts = React.useMemo(() => {
    const c: Record<string, number> = {
      ALL: all.length,
      DRAFT: 0,
      PENDING_ACCEPTANCE: 0,
      APPROVED: 0,
      IN_PROGRESS: 0,
      COMPLETED: 0,
      DECLINED: 0,
      CANCELLED: 0,
      advances: 0,
    };
    for (const o of all) {
      c[o.status] = (c[o.status] ?? 0) + 1;
      if ((o.status === "APPROVED" || o.status === "IN_PROGRESS") && o.montantAvance != null) {
        c.advances += Number(o.montantAvance);
      }
    }
    return c;
  }, [all]);

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "mission-orders"] });
  }, [queryClient]);

  const actionMutation = useMutation({
    mutationFn: ({ id, action }: { id: string; action: "issue" | "start" | "complete" | "cancel" }) =>
      apiFetch<EnrichedMissionOrderResponse>(`/api/hrm/mission-orders/${id}/${action}`, { method: "POST" }),
    onMutate: ({ id }) => setActingId(id),
    onSuccess: (_d, { action }) => {
      const key = action === "issue" ? "issueSuccess" : action === "start" ? "startSuccess" : action === "complete" ? "completeSuccess" : "cancelSuccess";
      toast.success(tDetail(key));
      invalidate();
      setCancelTarget(null);
    },
    onError: (cause) => toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown")),
    onSettled: () => setActingId(null),
  });

  const filtered = React.useMemo(() => {
    const q = norm(search.trim());
    let rows = filter === "ALL" ? all : all.filter((o) => o.status === filter);
    if (q) {
      rows = rows.filter((o) =>
        norm(`${o.employeeName ?? ""} ${o.employeeMatricule ?? ""} ${o.destination} ${o.objet}`).includes(q),
      );
    }
    const rank: Record<string, number> = {
      DRAFT: 0,
      PENDING_ACCEPTANCE: 1,
      APPROVED: 2,
      IN_PROGRESS: 3,
      DECLINED: 4,
      COMPLETED: 5,
      CANCELLED: 6,
    };
    return [...rows].sort((a, b) => {
      if (rank[a.status] !== rank[b.status]) return rank[a.status] - rank[b.status];
      return new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime();
    });
  }, [all, filter, search]);

  const employeeLabel = (o: EnrichedMissionOrderResponse) =>
    o.employeeName ?? o.employeeMatricule ?? `${o.employeeId.slice(0, 8)}…`;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: tQ("title") }]}
        title={tQ("title")}
        subtitle={t("subtitle")}
        actions={
          canCreate ? (
            <Link href="/mission-orders/new">
              <Button>
                <Plus className="h-4 w-4" />
                {t("new.title")}
              </Button>
            </Link>
          ) : undefined
        }
      />

      <StatCardGrid>
        <StatCard label={t("kpi.inProgress")} value={counts.IN_PROGRESS} sub={t("status.IN_PROGRESS")} tone="orange" />
        <StatCard label={t("kpi.pending")} value={counts.PENDING_ACCEPTANCE} sub={t("status.PENDING_ACCEPTANCE")} tone="amber" />
        <StatCard label={t("kpi.approved")} value={counts.APPROVED} sub={t("status.APPROVED")} tone="blue" />
        <StatCard
          label={t("kpi.advances")}
          value={formatNumber(counts.advances, locale)}
          sub={t("kpi.advancesSub")}
          tone="violet"
        />
      </StatCardGrid>

      {/* Toolbar */}
      <div className="mt-5 mb-4 flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-1.5">
          {FILTERS.map((f) => {
            const n = counts[f.key] ?? 0;
            const active = filter === f.key;
            return (
              <button
                key={f.key}
                type="button"
                onClick={() => setFilter(f.key)}
                className={cn(
                  "flex items-center gap-1.5 rounded-full px-3 py-1.5 text-[12px] font-semibold transition-colors",
                  active
                    ? "bg-grad-orange text-white shadow-orange-brand"
                    : "border border-line bg-white text-ink-2 hover:bg-bg-soft",
                )}
              >
                {t(f.tKey)}
                <span
                  className={cn(
                    "rounded-full px-1.5 py-0.5 text-[10px] font-bold tabular-nums",
                    active ? "bg-white/25 text-white" : "bg-bg-dim text-ink-3",
                  )}
                >
                  {n}
                </span>
              </button>
            );
          })}
        </div>
        <div className="relative">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-4" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder={tQ("searchPlaceholder")}
            className="w-64 rounded-[11px] border border-line bg-white py-[9px] pl-9 pr-3.5 text-[13px] text-ink shadow-xs-brand outline-none placeholder:text-ink-4 focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          />
        </div>
      </div>

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : filtered.length === 0 ? (
        <div className="rounded-[20px] border border-line bg-white p-12 text-center">
          <PlaneTakeoff className="mx-auto h-10 w-10 text-ink-4" />
          <p className="mt-3 text-[14px] text-ink-2">{filter === "ALL" && !search ? tQ("empty") : tQ("emptyFiltered")}</p>
        </div>
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-bg-dim">
                  <Th>{tQ("columns.reference")}</Th>
                  <Th>{tQ("columns.employee")}</Th>
                  <Th>{tQ("columns.destination")}</Th>
                  <Th>{tQ("columns.period")}</Th>
                  <Th className="text-right">{tQ("columns.allowance")}</Th>
                  <Th>{tQ("columns.status")}</Th>
                  <Th className="text-right">{tQ("columns.actions")}</Th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((o) => {
                  const busy = actingId === o.id && actionMutation.isPending;
                  const days = daysBetween(o.dateDebut, o.dateFin);
                  return (
                    <tr
                      key={o.id}
                      className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                      onClick={() => router.push(`/mission-orders/${o.id}`)}
                    >
                      <td className="px-5 py-3">
                        <span className="inline-flex items-center rounded-md bg-bg-soft px-2 py-1 font-mono-tabular text-[11px] font-semibold text-ink-2">
                          {shortRef(o.id)}
                        </span>
                      </td>
                      <td className="px-3 py-3">
                        <div className="flex items-center gap-2.5">
                          <Avatar name={employeeLabel(o)} size="sm" />
                          <div className="min-w-0">
                            <div className="truncate text-[13px] font-semibold text-ink">{employeeLabel(o)}</div>
                            <div className="truncate font-mono-tabular text-[11px] text-ink-4">
                              {o.employeeMatricule ?? o.employeeId.slice(0, 8)}
                              {o.employeeDepartment ? ` · ${o.employeeDepartment}` : ""}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="max-w-[240px] px-3 py-3">
                        <div className="flex items-start gap-2">
                          <MapPin className="mt-[2px] h-3.5 w-3.5 shrink-0 text-orange-500" />
                          <div className="min-w-0">
                            <div className="truncate text-[13px] font-semibold text-ink">{o.destination}</div>
                            <div className="truncate text-[11px] text-ink-3">{o.objet}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-3 py-3">
                        <div className="flex items-center gap-1.5 text-[12px] text-ink-2">
                          <CalendarRange className="h-3.5 w-3.5 text-ink-4" />
                          {formatDate(o.dateDebut, { locale })} → {formatDate(o.dateFin, { locale })}
                        </div>
                        <div className="mt-0.5 pl-5 font-mono-tabular text-[11px] text-ink-4">
                          {t("detail.days", { count: days })}
                        </div>
                      </td>
                      <td className="px-3 py-3 text-right">
                        {o.montantAvance != null ? (
                          <span className="font-mono-tabular text-[13px] font-bold text-ink">
                            {formatNumber(Number(o.montantAvance), locale)}
                          </span>
                        ) : (
                          <span className="text-ink-4">—</span>
                        )}
                      </td>
                      <td className="px-3 py-3">
                        <Badge tone={missionStatusTone(o.status)}>{t(`status.${o.status}`)}</Badge>
                      </td>
                      <td className="px-5 py-3">
                        <div className="flex items-center justify-end gap-1.5" onClick={(e) => e.stopPropagation()}>
                          <RowActions
                            order={o}
                            canManage={canManage}
                            busy={busy}
                            onAction={(action) => actionMutation.mutate({ id: o.id, action })}
                            onCancel={() => setCancelTarget(o)}
                            issueLabel={tQ("actions.issue")}
                            startLabel={tQ("actions.start")}
                            completeLabel={tQ("actions.complete")}
                            cancelLabel={tQ("actions.cancel")}
                            viewLabel={tQ("viewDetail")}
                          />
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      <ConfirmCancel
        target={cancelTarget}
        label={cancelTarget ? `${shortRef(cancelTarget.id)} · ${cancelTarget.destination}` : ""}
        loading={actionMutation.isPending}
        onClose={() => setCancelTarget(null)}
        onConfirm={() => cancelTarget && actionMutation.mutate({ id: cancelTarget.id, action: "cancel" })}
      />
    </>
  );
}

function RowActions({
  order,
  canManage,
  busy,
  onAction,
  onCancel,
  issueLabel,
  startLabel,
  completeLabel,
  cancelLabel,
  viewLabel,
}: {
  order: EnrichedMissionOrderResponse;
  canManage: boolean;
  busy: boolean;
  onAction: (action: "issue" | "start" | "complete") => void;
  onCancel: () => void;
  issueLabel: string;
  startLabel: string;
  completeLabel: string;
  cancelLabel: string;
  viewLabel: string;
}) {
  const spinner = <Loader2 className="h-3.5 w-3.5 animate-spin" />;
  const cancellable =
    order.status === "DRAFT" ||
    order.status === "PENDING_ACCEPTANCE" ||
    order.status === "APPROVED" ||
    order.status === "IN_PROGRESS";

  if (!canManage) {
    return <span className="text-[11.5px] text-ink-4">{viewLabel}</span>;
  }

  return (
    <>
      {order.status === "DRAFT" && (
        <Button type="button" size="sm" disabled={busy} onClick={() => onAction("issue")}>
          {busy ? spinner : <Send className="h-3.5 w-3.5" />}
          {issueLabel}
        </Button>
      )}
      {order.status === "APPROVED" && (
        <Button type="button" size="sm" disabled={busy} onClick={() => onAction("start")}>
          {busy ? spinner : <PlaneTakeoff className="h-3.5 w-3.5" />}
          {startLabel}
        </Button>
      )}
      {order.status === "IN_PROGRESS" && (
        <Button type="button" size="sm" disabled={busy} onClick={() => onAction("complete")}>
          {busy ? spinner : <CheckCircle2 className="h-3.5 w-3.5" />}
          {completeLabel}
        </Button>
      )}
      {cancellable && (
        <Button type="button" size="sm" variant="secondary" disabled={busy} onClick={onCancel}>
          <XCircle className="h-3.5 w-3.5" />
          {cancelLabel}
        </Button>
      )}
      {!cancellable && order.status !== "DRAFT" && order.status !== "APPROVED" && order.status !== "IN_PROGRESS" && (
        <span className="text-[11.5px] text-ink-4">{viewLabel}</span>
      )}
    </>
  );
}

function ConfirmCancel({
  target,
  label,
  loading,
  onClose,
  onConfirm,
}: {
  target: EnrichedMissionOrderResponse | null;
  label: string;
  loading: boolean;
  onClose: () => void;
  onConfirm: () => void;
}) {
  const tQ = useTranslations("missions.queue");
  const tCommon = useTranslations("common");
  return (
    <Dialog
      open={target !== null}
      onClose={onClose}
      title={
        <span className="flex items-center gap-2 text-danger-600">
          <XCircle className="h-5 w-5" />
          {tQ("cancelConfirm.title")}
        </span>
      }
      subtitle={label || tQ("cancelConfirm.subtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.close")}
          </Button>
          <Button type="button" variant="danger" disabled={loading} onClick={onConfirm}>
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : tQ("cancelConfirm.confirm")}
          </Button>
        </>
      }
    >
      <p className="text-[13.5px] text-ink-3">{tQ("cancelConfirm.subtitle")}</p>
    </Dialog>
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

function shortRef(uuid: string): string {
  return `MO-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}

function daysBetween(a: string, b: string): number {
  const ms = new Date(b).getTime() - new Date(a).getTime();
  return Math.max(1, Math.round(ms / 86_400_000) + 1);
}
