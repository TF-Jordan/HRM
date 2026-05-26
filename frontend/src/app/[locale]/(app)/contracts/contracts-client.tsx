"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { AlertTriangle, Download, Plus } from "lucide-react";
import { useAllContracts } from "@/hooks/modules/useContracts";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { StatCard } from "@/components/ui-tokens/StatCard";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Button } from "@/components/ui/button";
import { Badge, type BadgeProps } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Link, useRouter } from "@/i18n/navigation";
import { cn } from "@/lib/utils";
import type { ContractWithEmployee, ContractType } from "@/lib/types/hrm/contract";

const TYPE_TONE: Record<ContractType, BadgeProps["tone"]> = {
  CDI: "green",
  CDD: "blue",
  STAGE: "violet",
  INTERIM: "amber",
};
const TYPE_HEX: Record<ContractType, string> = {
  CDI: "#f26b0f",
  CDD: "#3b82f6",
  STAGE: "#8b5cf6",
  INTERIM: "#f59e0b",
};

export function ContractsClient() {
  const t = useTranslations("employees.contractsPage");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const { data, isLoading } = useAllContracts();
  const employees = useEmployees();
  const [typeFilter, setTypeFilter] = React.useState<ContractType | null>(null);
  const [nowTs] = React.useState(() => Date.now());

  const contracts = React.useMemo(() => data ?? [], [data]);
  const totalEmployees = employees.data?.length ?? 0;
  const filtered = typeFilter ? contracts.filter((c) => c.type === typeFilter) : contracts;

  const stats = React.useMemo(() => {
    const active = contracts.filter(
      (c) => c.status === "ACTIVE" && !(c.dateFin && new Date(c.dateFin).getTime() < nowTs),
    ).length;
    const trial = contracts.filter((c) => {
      if (c.status !== "ACTIVE" || !c.periodeEssai) return false;
      const end = new Date(c.dateDebut);
      end.setDate(end.getDate() + c.periodeEssai);
      return end.getTime() > nowTs;
    }).length;
    const ending90 = contracts.filter((c) => {
      if (c.status !== "ACTIVE" || !c.dateFin) return false;
      const diff = (new Date(c.dateFin).getTime() - nowTs) / 86_400_000;
      return diff > 0 && diff <= 90;
    }).length;
    const byType = { CDI: 0, CDD: 0, STAGE: 0, INTERIM: 0 } as Record<ContractType, number>;
    for (const c of contracts) byType[c.type] += 1;
    const total = contracts.length;
    const cdiPct = total > 0 ? Math.round((byType.CDI / total) * 100) : 0;
    return { active, trial, ending90, byType, total, cdiPct };
  }, [contracts, nowTs]);

  const typeCounts = stats.byType;

  // Donut conic-gradient segments
  const donut = React.useMemo(() => {
    const order: ContractType[] = ["CDI", "CDD", "STAGE", "INTERIM"];
    const total = stats.total || 1;
    let acc = 0;
    const parts: string[] = [];
    for (const tp of order) {
      const start = (acc / total) * 100;
      acc += typeCounts[tp];
      const end = (acc / total) * 100;
      if (typeCounts[tp] > 0) parts.push(`${TYPE_HEX[tp]} ${start}% ${end}%`);
    }
    return parts.length ? `conic-gradient(${parts.join(", ")})` : "conic-gradient(var(--color-cream-2) 0 100%)";
  }, [typeCounts, stats.total]);

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        crumbs={[{ label: t("eyebrow") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <>
            <Button variant="secondary">
              <Download className="size-4" />
              {t("exportButton")}
            </Button>
            <Button asChild>
              <Link href="/contracts/new">
                <Plus className="size-4" />
                {t("newButton")}
              </Link>
            </Button>
          </>
        }
      />

      {/* Stats + type donut */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:col-span-2">
          {isLoading ? (
            Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-[104px] rounded-[18px]" />)
          ) : (
            <>
              <StatCard tone="green" label={t("kpi.active")} value={stats.active} footer={t("kpi.activeFooter", { total: totalEmployees })} />
              <StatCard tone="amber" label={t("kpi.trial")} value={stats.trial} footer={t("kpi.trialFooter", { count: stats.trial })} />
              <StatCard tone="red" label={t("kpi.endingSoon90")} value={stats.ending90} footer={t("kpi.endingSoon90Footer")} />
              <StatCard tone="orange" label={t("kpi.cdi")} value={typeCounts.CDI} footer={t("kpi.cdiFooter", { pct: stats.cdiPct })} />
              <StatCard tone="blue" label={t("kpi.cdd")} value={typeCounts.CDD} footer={t("kpi.cddFooter")} />
              <StatCard tone="violet" label={t("kpi.stages")} value={typeCounts.STAGE + typeCounts.INTERIM} footer={t("kpi.stagesFooter")} />
            </>
          )}
        </div>

        <Card>
          <CardHeader>
            <CardTitle>{t("typeChart")}</CardTitle>
          </CardHeader>
          <CardContent className="flex items-center gap-5">
            <div className="relative grid size-28 shrink-0 place-items-center rounded-full" style={{ background: donut }}>
              <div className="grid size-20 place-items-center rounded-full bg-white text-center">
                <div>
                  <div className="font-display text-[20px] font-extrabold leading-none text-ink tabular">{stats.total}</div>
                  <div className="text-[10px] text-ink-4">contrats</div>
                </div>
              </div>
            </div>
            <ul className="flex-1 space-y-1.5 text-[12.5px]">
              {(["CDI", "CDD", "STAGE", "INTERIM"] as const).map((tp) => (
                <li key={tp} className="flex items-center justify-between">
                  <span className="flex items-center gap-2 text-ink-2">
                    <span className="size-2.5 rounded-full" style={{ background: TYPE_HEX[tp] }} />
                    {tp}
                  </span>
                  <span className="font-semibold text-ink tabular">{typeCounts[tp]}</span>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      </div>

      {/* Trial alert */}
      {!isLoading && stats.trial > 0 && (
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-[14px] border border-status-amber-500/30 bg-status-amber-50 px-4 py-3">
          <div className="flex items-center gap-2 text-[13px] font-semibold text-status-amber-600">
            <AlertTriangle className="size-4" />
            {t("trialAlert", { count: stats.trial })}
          </div>
        </div>
      )}

      {/* Table */}
      <Card className="overflow-hidden p-0">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line-soft p-3">
          <h3 className="px-1 font-display text-[15px] font-bold text-ink">
            {t("allContracts")} ({contracts.length})
          </h3>
          <div className="flex flex-wrap gap-2">
            <Chip label={`${t("all")} (${contracts.length})`} active={typeFilter === null} onClick={() => setTypeFilter(null)} />
            {(["CDI", "CDD", "STAGE", "INTERIM"] as const).map((tp) => (
              <Chip
                key={tp}
                label={`${tp} (${typeCounts[tp]})`}
                active={typeFilter === tp}
                onClick={() => setTypeFilter(typeFilter === tp ? null : tp)}
              />
            ))}
          </div>
        </div>

        {isLoading ? (
          <div className="space-y-2 p-4">
            {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-12 w-full" />)}
          </div>
        ) : contracts.length === 0 ? (
          <div className="py-10 text-center text-sm text-ink-3">{t("table.employee")} —</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-[13px]">
              <thead>
                <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                  <th className="px-4 py-2.5 text-left font-semibold">{t("table.ref")}</th>
                  <th className="px-3 py-2.5 text-left font-semibold">{t("table.employee")}</th>
                  <th className="px-3 py-2.5 text-left font-semibold">{t("table.type")}</th>
                  <th className="px-3 py-2.5 text-left font-semibold">{t("table.dateDebut")}</th>
                  <th className="px-3 py-2.5 text-left font-semibold">{t("table.dateFin")}</th>
                  <th className="px-3 py-2.5 text-right font-semibold">{t("table.salaireBase")}</th>
                  <th className="px-3 py-2.5 text-left font-semibold">{t("table.status")}</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((c) => (
                  <ContractRow key={c.id} contract={c} fmt={fmt} nowTs={nowTs} />
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}

function Chip({ label, active, onClick }: { label: string; active: boolean; onClick: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "rounded-full px-3 py-1.5 text-[12.5px] font-semibold transition-colors",
        active
          ? "bg-grad-orange text-white shadow-brand"
          : "border border-line bg-white text-ink-2 hover:border-brand-300 hover:text-brand-700",
      )}
    >
      {label}
    </button>
  );
}

function ContractRow({
  contract: c,
  fmt,
  nowTs,
}: {
  contract: ContractWithEmployee;
  fmt: ReturnType<typeof useFormat>;
  nowTs: number;
}) {
  const router = useRouter();
  return (
    <tr
      className="cursor-pointer border-b border-line-soft/70 last:border-0 hover:bg-brand-50/40"
      onClick={() => router.push(`/contracts/${c.id}` as never)}
    >
      <td className="px-4 py-2.5 font-mono text-[11px] text-ink-4">{c.id.slice(0, 8)}</td>
      <td className="px-3 py-2.5">
        <div className="font-semibold text-ink">{c.employeeName}</div>
        <div className="text-[11px] text-ink-4">
          {c.employeeMatricule}
          {c.employeeDepartment ? ` · ${c.employeeDepartment}` : ""}
        </div>
      </td>
      <td className="px-3 py-2.5">
        <Badge tone={TYPE_TONE[c.type]}>{c.type}</Badge>
      </td>
      <td className="px-3 py-2.5 text-ink-2 tabular">{fmt.date(c.dateDebut)}</td>
      <td className="px-3 py-2.5 text-ink-2 tabular">{c.dateFin ? fmt.date(c.dateFin) : "—"}</td>
      <td className="px-3 py-2.5 text-right text-ink-2 tabular">{fmt.money(c.salaireBase)}</td>
      <td className="px-3 py-2.5">
        <ContractStatusBadge contract={c} nowTs={nowTs} />
      </td>
    </tr>
  );
}

function ContractStatusBadge({ contract: c, nowTs }: { contract: ContractWithEmployee; nowTs: number }) {
  if (c.status === "ACTIVE" && c.periodeEssai) {
    const end = new Date(c.dateDebut);
    end.setDate(end.getDate() + c.periodeEssai);
    if (end.getTime() > nowTs) return <StatusBadge kind="contract" status="TRIAL" />;
  }
  if (c.status === "ACTIVE" && c.dateFin) {
    const diff = (new Date(c.dateFin).getTime() - nowTs) / 86_400_000;
    if (diff <= 0) return <StatusBadge kind="contract" status="EXPIRED" />;
    if (diff <= 30) return <StatusBadge kind="contract" status="ENDING_SOON" />;
  }
  return <StatusBadge kind="contract" status={c.status} />;
}
