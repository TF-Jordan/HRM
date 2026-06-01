"use client";

import { useQuery } from "@tanstack/react-query";
import {
  AlertTriangle,
  CalendarX2,
  CheckCircle2,
  Clock,
  FileText,
  GraduationCap,
  Loader2,
  Plus,
  RefreshCcw,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { Cell, Pie, PieChart, Tooltip } from "recharts";

import { useRouter } from "@/i18n/navigation";


import { AddContractDialog } from "@/components/contracts/add-contract-dialog";
import { PageHeader } from "@/components/shell/page-header";
import { useCan } from "@/hooks/use-can";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Chip } from "@/components/ui/chip";
import { Column, DataTable } from "@/components/ui/data-table";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatMoney } from "@/lib/format";
import { cn, initials } from "@/lib/utils";
import type {
  ContractResponse,
  ContractStatusValue,
  ContractType,
} from "@/server/ksm/modules/employees";

/* ── Types ──────────────────────────────────────────────────────────── */

type ContractRow = ContractResponse & {
  employeeMatricule: string;
  employeeDisplayName: string | null;
};

type ContractsStats = {
  total: number;
  active: number;
  trialing: number;
  expiringIn90Days: number;
  cdi: number;
  cdd: number;
  stage: number;
  interim: number;
  totalEmployees: number;
};

type TrialAlert = {
  employeeId: string;
  employeeDisplayName: string | null;
  trialEnd: string;
};

type ContractsData = {
  contracts: ContractRow[];
  stats: ContractsStats;
  trialAlerts: TrialAlert[];
};

type FilterKey = "ALL" | "CDI" | "CDD" | "STAGE" | "TRIAL";

/* ── Helpers ─────────────────────────────────────────────────────────── */

function addDays(dateStr: string, days: number): Date {
  const d = new Date(dateStr);
  d.setDate(d.getDate() + days);
  return d;
}

function isInTrialPeriod(c: ContractRow): boolean {
  if (!c.periodeEssai || c.status !== "ACTIVE") return false;
  return addDays(c.dateDebut, c.periodeEssai) > new Date();
}

const TYPE_TONE: Record<ContractType, "success" | "warning" | "violet" | "gray"> = {
  CDI: "success",
  CDD: "warning",
  STAGE: "violet",
  INTERIM: "gray",
};

const STATUS_TONE: Record<ContractStatusValue, "success" | "warning" | "gray" | "danger" | "info"> = {
  ACTIVE: "success",
  TRIAL: "warning",
  EXPIRED: "gray",
  TERMINATED: "danger",
  RENEWED: "info",
};

const DONUT_COLORS: Record<string, string> = {
  CDI: "#F97316",
  CDD: "#3B82F6",
  STAGE: "#8B5CF6",
  INTERIM: "#F59E0B",
};

const AVATAR_TONES = ["orange", "blue", "green", "violet", "amber", "teal"] as const;
function avatarTone(id: string) {
  return AVATAR_TONES[id.charCodeAt(0) % AVATAR_TONES.length];
}

function pct(n: number, total: number) {
  if (total === 0) return "0%";
  return `${Math.round((n / total) * 100)}%`;
}

/* ── Local StatTile ──────────────────────────────────────────────────── */

const TILE_CLS: Record<string, string> = {
  green: "bg-success-50 text-success-600",
  amber: "bg-warning-50 text-warning-600",
  red: "bg-danger-50 text-danger-600",
  orange: "bg-orange-50 text-orange-600",
  blue: "bg-info-50 text-info-600",
  violet: "bg-violet-50 text-violet-600",
};

function StatTile({
  label,
  value,
  sub,
  icon: Icon,
  tone,
}: {
  label: string;
  value: React.ReactNode;
  sub?: string;
  icon: LucideIcon;
  tone: keyof typeof TILE_CLS;
}) {
  return (
    <div className="relative flex flex-col gap-1 overflow-hidden rounded-[16px] border border-line bg-white px-[18px] py-4 shadow-xs-brand before:pointer-events-none before:absolute before:inset-0 before:bg-[linear-gradient(180deg,rgba(255,255,255,0.5),transparent_40%)]">
      <span
        className={cn(
          "relative mb-1.5 grid h-8 w-8 shrink-0 place-items-center rounded-[9px]",
          TILE_CLS[tone],
        )}
      >
        <Icon className="h-4 w-4" />
      </span>
      <p className="relative font-display text-[26px] font-extrabold leading-none tracking-tight text-ink tabular-nums">
        {value}
      </p>
      <p className="relative text-[12.5px] font-semibold text-ink-2">{label}</p>
      {sub && <p className="relative text-[11px] text-ink-3">{sub}</p>}
    </div>
  );
}

/* ── Main Component ──────────────────────────────────────────────────── */

export function ContractsList() {
  const t = useTranslations("contracts");
  const router = useRouter();
  const [filter, setFilter] = React.useState<FilterKey>("ALL");
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const canCreate = useCan("hrm:contract:create");

  const query = useQuery({
    queryKey: ["hrm", "contracts"],
    queryFn: () => apiFetch<ContractsData>("/api/hrm/contracts"),
    staleTime: 60_000,
  });

  /* ── Loading ─────────────────────────────────────────────────────── */
  if (query.isLoading) {
    return (
      <div className="grid place-items-center py-24">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }

  /* ── Error ───────────────────────────────────────────────────────── */
  if (query.error || !query.data) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {query.error instanceof BffApiError
          ? query.error.message
          : "Erreur de chargement des contrats."}
      </div>
    );
  }

  const { contracts, stats, trialAlerts } = query.data;

  /* ── Filter ──────────────────────────────────────────────────────── */
  const filtered = contracts.filter((c) => {
    switch (filter) {
      case "CDI":   return c.type === "CDI";
      case "CDD":   return c.type === "CDD";
      case "STAGE": return c.type === "STAGE";
      case "TRIAL": return isInTrialPeriod(c);
      default:      return true;
    }
  });

  /* ── Donut data ──────────────────────────────────────────────────── */
  const donutData = [
    { name: t("contractType.CDI"),    value: stats.cdi,    color: DONUT_COLORS.CDI },
    { name: t("contractType.CDD"),    value: stats.cdd,    color: DONUT_COLORS.CDD },
    { name: t("contractType.STAGE"),  value: stats.stage,  color: DONUT_COLORS.STAGE },
    { name: t("contractType.INTERIM"),value: stats.interim, color: DONUT_COLORS.INTERIM },
  ].filter((d) => d.value > 0);

  /* ── Table columns ───────────────────────────────────────────────── */
  const columns: Column<ContractRow>[] = [
    {
      key: "ref",
      header: t("table.columns.ref"),
      headClassName: "w-28",
      cell: (c) => (
        <span className="font-mono-tabular text-[11px] font-medium text-orange-600">
          CT-{c.id.slice(0, 8).toUpperCase()}
        </span>
      ),
    },
    {
      key: "employee",
      header: t("table.columns.employee"),
      cell: (c) => (
        <div className="flex items-center gap-2.5">
          <Avatar
            name={c.employeeDisplayName ?? c.employeeMatricule}
            initials={initials(c.employeeDisplayName ?? c.employeeMatricule, 2)}
            tone={avatarTone(c.employeeId)}
            size="sm"
          />
          <span className="text-[13px] font-semibold text-ink">
            {c.employeeDisplayName ?? c.employeeMatricule}
          </span>
        </div>
      ),
    },
    {
      key: "type",
      header: t("table.columns.type"),
      headClassName: "w-28",
      cell: (c) => (
        <Badge tone={TYPE_TONE[c.type]} showDot={false}>
          {t(`contractType.${c.type}`)}
        </Badge>
      ),
    },
    {
      key: "dateDebut",
      header: t("table.columns.dateDebut"),
      headClassName: "w-32",
      cell: (c) => (
        <span className="text-[13px] text-ink-2">{formatDate(c.dateDebut)}</span>
      ),
    },
    {
      key: "dateFin",
      header: t("table.columns.dateFin"),
      headClassName: "w-32",
      cell: (c) =>
        c.dateFin ? (
          <span className="text-[13px] text-ink-2">{formatDate(c.dateFin)}</span>
        ) : (
          <span className="text-ink-4">{t("table.noEnd")}</span>
        ),
    },
    {
      key: "salary",
      header: t("table.columns.salary"),
      headClassName: "w-40",
      cell: (c) => (
        <span className="font-mono-tabular text-[13px] font-semibold text-ink">
          {formatMoney(Number(c.salaireBase), { withCurrency: false })}
        </span>
      ),
    },
    {
      key: "status",
      header: t("table.columns.status"),
      headClassName: "w-36",
      cell: (c) => {
        const inTrial = isInTrialPeriod(c);
        if (c.status === "ACTIVE" && inTrial) {
          return (
            <Badge tone="warning">{t("contractStatus.TRIAL")}</Badge>
          );
        }
        return (
          <Badge tone={STATUS_TONE[c.status]}>
            {t(`contractStatus.${c.status}`)}
          </Badge>
        );
      },
    },
  ];

  /* ── Render ──────────────────────────────────────────────────────── */
  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          canCreate ? (
            <Button type="button" onClick={() => setDialogOpen(true)}>
              <Plus className="h-4 w-4" />
              {t("form.title")}
            </Button>
          ) : undefined
        }
      />

      {/* ── KPI tiles + Donut ────────────────────────────────────────── */}
      <div className="mb-5 grid gap-4 lg:grid-cols-[1fr_284px]">
        {/* 6 stat tiles */}
        <div className="grid grid-cols-2 gap-3.5 sm:grid-cols-3">
          <StatTile
            label={t("kpi.active")}
            value={stats.active}
            sub={`sur ${stats.totalEmployees} employés`}
            icon={CheckCircle2}
            tone="green"
          />
          <StatTile
            label={t("kpi.trialing")}
            value={stats.trialing}
            sub={`${trialAlerts.length} fin(s) ce mois`}
            icon={Clock}
            tone="amber"
          />
          <StatTile
            label={t("kpi.expiring")}
            value={stats.expiringIn90Days}
            sub={t("kpi.expiringSub")}
            icon={CalendarX2}
            tone="red"
          />
          <StatTile
            label={t("kpi.cdi")}
            value={stats.cdi}
            sub={`${pct(stats.cdi, stats.total)} ${t("kpi.ofEffective")}`}
            icon={FileText}
            tone="orange"
          />
          <StatTile
            label={t("kpi.cdd")}
            value={stats.cdd}
            sub={`${pct(stats.cdd, stats.total)} ${t("kpi.ofEffective")}`}
            icon={FileText}
            tone="blue"
          />
          <StatTile
            label={t("kpi.stage")}
            value={stats.stage}
            sub={`${pct(stats.stage, stats.total)} ${t("kpi.ofEffective")}`}
            icon={GraduationCap}
            tone="violet"
          />
        </div>

        {/* Donut chart card */}
        <Card>
          <CardContent padding="lg">
            <p className="mb-4 text-[13px] font-bold text-ink">{t("chart.title")}</p>
            <div className="flex flex-col items-center gap-4">
              {/* Chart */}
              <div className="relative">
                <PieChart width={160} height={160}>
                  <Pie
                    data={donutData.length > 0 ? donutData : [{ name: "—", value: 1, color: "#E5E7EB" }]}
                    cx={75}
                    cy={75}
                    innerRadius={52}
                    outerRadius={75}
                    dataKey="value"
                    strokeWidth={2}
                    stroke="#fff"
                  >
                    {(donutData.length > 0 ? donutData : [{ color: "#E5E7EB" }]).map((d, i) => (
                      <Cell key={i} fill={d.color} />
                    ))}
                  </Pie>
                  {donutData.length > 0 && (
                    <Tooltip
                      formatter={(v: number) => [v, ""]}
                      contentStyle={{ fontSize: 12 }}
                    />
                  )}
                </PieChart>
                {/* Center label */}
                <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
                  <span className="font-display text-[22px] font-extrabold tabular-nums text-ink">
                    {stats.total}
                  </span>
                  <span className="text-[11px] text-ink-3">{t("chart.total")}</span>
                </div>
              </div>

              {/* Legend */}
              <div className="w-full space-y-2">
                {donutData.map((d) => (
                  <div key={d.name} className="flex items-center gap-2 text-[12.5px]">
                    <span
                      className="h-2.5 w-2.5 shrink-0 rounded-[3px]"
                      style={{ background: d.color }}
                    />
                    <span className="flex-1 text-ink-2">{d.name}</span>
                    <span className="font-mono-tabular font-semibold text-ink">{d.value}</span>
                  </div>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* ── Trial period alert ────────────────────────────────────────── */}
      {trialAlerts.length > 0 && (
        <div
          className="mb-4 flex items-start gap-3 rounded-[16px] border border-warning-300 px-5 py-4"
          style={{ background: "linear-gradient(90deg,#FFF6E0 0%,#FFFAEA 100%)" }}
        >
          <span className="grid h-11 w-11 shrink-0 place-items-center rounded-[11px] bg-warning-100 text-warning-600">
            <AlertTriangle className="h-5 w-5" />
          </span>
          <div className="min-w-0 flex-1">
            <p className="text-[14px] font-bold text-ink">
              {trialAlerts.length}{" "}
              {trialAlerts.length === 1
                ? t("alert.title", { count: trialAlerts.length })
                : t("alert.title", { count: trialAlerts.length })}
            </p>
            <p className="mt-0.5 line-clamp-2 text-[12.5px] text-ink-2">
              {trialAlerts.map((a) => a.employeeDisplayName ?? "—").join(", ")}
              {" — "}
              {t("alert.body")}
            </p>
          </div>
          <Button
            type="button"
            variant="secondary"
            size="sm"
            onClick={() => setFilter("TRIAL")}
            className="shrink-0"
          >
            {t("alert.cta")}
          </Button>
        </div>
      )}

      {/* ── Contracts table ───────────────────────────────────────────── */}
      <Card>
        {/* Table header with filter chips */}
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line px-6 py-4">
          <p className="text-[15px] font-bold text-ink">{t("table.title")}</p>
          <div className="flex flex-wrap items-center gap-1.5">
            {(
              [
                { key: "ALL",   labelKey: "table.filters.all"   },
                { key: "CDI",   labelKey: "table.filters.cdi"   },
                { key: "CDD",   labelKey: "table.filters.cdd"   },
                { key: "STAGE", labelKey: "table.filters.stage" },
                { key: "TRIAL", labelKey: "table.filters.trial" },
              ] as const
            ).map(({ key, labelKey }) => (
              <Chip
                key={key}
                active={filter === key}
                tone="orange"
                onClick={() => setFilter(key)}
              >
                {t(labelKey)}
              </Chip>
            ))}
          </div>
        </div>

        <DataTable
          columns={columns}
          data={filtered}
          rowKey={(c) => c.id}
          onRowClick={(c) => router.push(`/contracts/${c.id}?employeeId=${c.employeeId}`)}
          empty={
            <span className="text-ink-3">{t("table.empty")}</span>
          }
        />
      </Card>

      {/* Refresh hint */}
      {query.isStale && (
        <div className="mt-3 flex justify-end">
          <button
            type="button"
            onClick={() => query.refetch()}
            className="flex items-center gap-1.5 text-[12px] text-ink-3 hover:text-orange-600"
          >
            <RefreshCcw className="h-3 w-3" />
            Actualiser
          </button>
        </div>
      )}

      {/* New contract dialog (with employee selection step) */}
      <AddContractDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        onSuccess={() => {
          setDialogOpen(false);
          query.refetch();
        }}
      />
    </>
  );
}
