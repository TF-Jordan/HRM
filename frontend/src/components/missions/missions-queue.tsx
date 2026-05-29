"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2, MapPin, Plus } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Column, DataTable } from "@/components/ui/data-table";
import { StatCard, StatCardGrid } from "@/components/ui/stat-card";
import { useCan } from "@/hooks/use-can";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatNumber } from "@/lib/format";
import { missionStatusTone } from "@/lib/mission-status";
import { cn } from "@/lib/utils";
import type { MissionOrderResponse, MissionOrderStatus } from "@/server/ksm/modules/missions";

type Filter = "ALL" | "PENDING_ACCEPTANCE" | "APPROVED" | "IN_PROGRESS" | "DECLINED" | "COMPLETED";

const FILTERS: { key: Filter; tKey: string }[] = [
  { key: "ALL", tKey: "filters.all" },
  { key: "PENDING_ACCEPTANCE", tKey: "filters.pending" },
  { key: "APPROVED", tKey: "filters.approved" },
  { key: "IN_PROGRESS", tKey: "filters.inProgress" },
  { key: "DECLINED", tKey: "filters.declined" },
  { key: "COMPLETED", tKey: "filters.completed" },
];

export function MissionsQueue() {
  const t = useTranslations("missions");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const canCreate = useCan("hrm:mission:create");
  const [filter, setFilter] = React.useState<Filter>("ALL");

  const query = useQuery({
    queryKey: ["hrm", "mission-orders", "list", filter],
    queryFn: () => {
      const qs = filter === "ALL" ? "" : `?status=${filter}`;
      return apiFetch<MissionOrderResponse[]>(`/api/hrm/mission-orders${qs}`);
    },
    refetchInterval: 60_000,
  });

  const all = query.data ?? [];
  const counts = React.useMemo(() => {
    const c: Partial<Record<MissionOrderStatus, number>> = {};
    for (const o of all) c[o.status] = (c[o.status] ?? 0) + 1;
    return c;
  }, [all]);

  const columns: Column<MissionOrderResponse>[] = [
    {
      key: "ref",
      header: t("queue.columns.reference"),
      cell: (o) => (
        <span className="font-mono-tabular text-[11.5px] text-ink-3">{shortRef(o.id)}</span>
      ),
    },
    {
      key: "employee",
      header: t("queue.columns.employee"),
      cell: (o) => (
        <span className="font-mono-tabular text-[12.5px] text-ink-2">
          {o.employeeId.slice(0, 8)}…
        </span>
      ),
    },
    {
      key: "destination",
      header: t("queue.columns.destination"),
      cell: (o) => (
        <div className="flex items-start gap-2">
          <MapPin className="mt-[2px] h-3.5 w-3.5 shrink-0 text-orange-500" />
          <div className="min-w-0">
            <div className="text-[13px] font-semibold text-ink">{o.destination}</div>
            <div className="truncate text-[11px] text-ink-3">{o.objet}</div>
          </div>
        </div>
      ),
    },
    {
      key: "period",
      header: t("queue.columns.period"),
      cell: (o) => (
        <div>
          <div className="text-[12.5px] text-ink-2">
            {formatDate(o.dateDebut, { locale })} → {formatDate(o.dateFin, { locale })}
          </div>
          <div className="font-mono-tabular text-[11px] text-ink-3">
            {t("detail.days", { count: daysBetween(o.dateDebut, o.dateFin) })}
          </div>
        </div>
      ),
    },
    {
      key: "allowance",
      header: t("queue.columns.allowance"),
      cell: (o) => (
        <span className="font-mono-tabular font-bold text-ink">
          {o.montantAvance != null ? formatNumber(Number(o.montantAvance), locale) : "—"}
        </span>
      ),
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "status",
      header: t("queue.columns.status"),
      cell: (o) => <Badge tone={missionStatusTone(o.status)}>{t(`status.${o.status}`)}</Badge>,
    },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("queue.title") }]}
        title={t("queue.title")}
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
        <StatCard
          label={t("kpi.inProgress")}
          value={counts.IN_PROGRESS ?? 0}
          sub={t("status.IN_PROGRESS")}
          tone="orange"
        />
        <StatCard
          label={t("kpi.pending")}
          value={counts.PENDING_ACCEPTANCE ?? 0}
          sub={t("status.PENDING_ACCEPTANCE")}
          tone="amber"
        />
        <StatCard
          label={t("kpi.approved")}
          value={counts.APPROVED ?? 0}
          sub={t("status.APPROVED")}
          tone="blue"
        />
        <StatCard
          label={t("kpi.declined")}
          value={counts.DECLINED ?? 0}
          sub={t("status.DECLINED")}
          tone="red"
        />
      </StatCardGrid>

      <div className="mb-4 flex flex-wrap items-center gap-2">
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

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={all}
          rowKey={(o) => o.id}
          onRowClick={(o) => router.push(`/mission-orders/${o.id}`)}
          empty={t("queue.empty")}
        />
      )}
    </>
  );
}

function shortRef(uuid: string): string {
  return `MO-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}

function daysBetween(a: string, b: string): number {
  const ms = new Date(b).getTime() - new Date(a).getTime();
  return Math.max(1, Math.round(ms / 86_400_000) + 1);
}
