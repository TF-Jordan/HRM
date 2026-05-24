"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { FileText, Clock, AlertTriangle, RefreshCw } from "lucide-react";
import { useAllContracts } from "@/hooks/modules/useContracts";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { KpiCard } from "@/components/ui-tokens/KpiCard";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Link } from "@/i18n/navigation";
import type { ContractWithEmployee, ContractType } from "@/lib/types/hrm/contract";

const TYPE_COLORS: Record<ContractType, string> = {
  CDI: "bg-brand-500 text-white",
  CDD: "bg-status-blue-500 text-white",
  STAGE: "bg-status-violet-500 text-white",
  INTERIM: "bg-status-teal-500 text-white",
};

export function ContractsClient() {
  const t = useTranslations("employees.contractsPage");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const { data, isLoading } = useAllContracts();
  const [typeFilter, setTypeFilter] = React.useState<ContractType | null>(null);

  const contracts = data ?? [];
  const filtered = typeFilter ? contracts.filter((c) => c.type === typeFilter) : contracts;

  const now = new Date();
  const activeCount = contracts.filter((c) => {
    if (c.status !== "ACTIVE") return false;
    if (c.dateFin && new Date(c.dateFin).getTime() < now.getTime()) return false;
    return true;
  }).length;
  const trialCount = contracts.filter((c) => {
    if (c.status !== "ACTIVE" || !c.periodeEssai) return false;
    const start = new Date(c.dateDebut);
    const trialEnd = new Date(start);
    trialEnd.setDate(trialEnd.getDate() + c.periodeEssai);
    return trialEnd > now;
  }).length;
  const endingSoonCount = contracts.filter((c) => {
    if (c.status !== "ACTIVE" || !c.dateFin) return false;
    const end = new Date(c.dateFin);
    const diff = (end.getTime() - now.getTime()) / 86_400_000;
    return diff > 0 && diff <= 30;
  }).length;
  const renewedCount = contracts.filter((c) => c.status === "RENEWED").length;
  const expiredActiveCount = contracts.filter((c) => {
    if (c.status !== "ACTIVE" || !c.dateFin) return false;
    return new Date(c.dateFin).getTime() < now.getTime();
  }).length;
  const invalidPeriodCount = contracts.filter(
    (c) => c.dateFin && c.dateDebut && c.dateFin < c.dateDebut,
  ).length;

  const typeCounts = contracts.reduce<Record<ContractType, number>>(
    (acc, c) => {
      acc[c.type] = (acc[c.type] || 0) + 1;
      return acc;
    },
    { CDI: 0, CDD: 0, STAGE: 0, INTERIM: 0 },
  );

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-04"
        crumbs={[{ label: tNav("items.contracts") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button asChild>
            <Link href="/contracts/new">{t("newButton")}</Link>
          </Button>
        }
      />

      {/* Compliance alert banner */}
      {!isLoading && (expiredActiveCount > 0 || invalidPeriodCount > 0) && (
        <div className="rounded-lg border border-status-red-200 bg-status-red-50 p-4 text-status-red-700">
          <div className="flex items-center gap-2 font-semibold text-sm">
            <AlertTriangle className="size-4" />
            Alertes de conformité
          </div>
          <ul className="mt-1.5 space-y-0.5 text-[13px]">
            {expiredActiveCount > 0 && (
              <li>• {expiredActiveCount} contrat(s) expiré(s) encore au statut ACTIF — action requise</li>
            )}
            {invalidPeriodCount > 0 && (
              <li>• {invalidPeriodCount} contrat(s) avec des périodes incohérentes (date de début {">"} date de fin)</li>
            )}
          </ul>
        </div>
      )}

      {isLoading ? (
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="h-28" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
          <KpiCard icon={FileText} label={t("kpi.active")} value={activeCount} tone="orange" />
          <KpiCard icon={Clock} label={t("kpi.trial")} value={trialCount} />
          <KpiCard icon={AlertTriangle} label={t("kpi.endingSoon")} value={endingSoonCount} tone="amber" />
          <KpiCard icon={RefreshCw} label={t("kpi.renewed")} value={renewedCount} tone="green" />
        </div>
      )}

      {isLoading ? (
        <Skeleton className="h-64 w-full" />
      ) : contracts.length === 0 ? (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">
            Aucun contrat
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardContent className="p-0">
            <div className="flex items-center justify-between border-b border-line px-4 py-3">
              <h3 className="font-display text-base font-bold text-ink">
                {typeFilter
                  ? `${typeFilter} (${filtered.length})`
                  : `Tous les contrats (${contracts.length})`}
              </h3>
              <div className="flex gap-2">
                {(["CDI", "CDD", "STAGE", "INTERIM"] as const).map((tp) => (
                  <button
                    key={tp}
                    onClick={() => setTypeFilter(typeFilter === tp ? null : tp)}
                    className="focus:outline-none"
                  >
                    <Badge
                      className={`cursor-pointer px-3 py-1 text-xs font-semibold ${
                        typeFilter === tp
                          ? TYPE_COLORS[tp]
                          : "bg-cream-soft text-ink-2 hover:bg-cream-dim"
                      }`}
                    >
                      {tp} {typeCounts[tp]}
                    </Badge>
                  </button>
                ))}
              </div>
            </div>

            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("table.employee"),
                    t("table.type"),
                    t("table.dateDebut"),
                    t("table.dateFin"),
                    t("table.salaireBase"),
                    t("table.status"),
                  ].map((h) => (
                    <th
                      key={h}
                      className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.map((c) => (
                  <ContractRow key={c.id} contract={c} fmt={fmt} />
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

function ContractRow({
  contract: c,
  fmt,
}: {
  contract: ContractWithEmployee;
  fmt: ReturnType<typeof useFormat>;
}) {
  return (
    <tr className="cursor-pointer hover:bg-brand-50/40">
      <td className="border-b border-line-soft px-4 py-3">
        <Link href={`/contracts/${c.id}` as never} className="hover:text-brand-700">
          <div className="text-[13.5px] font-semibold text-ink">{c.employeeName}</div>
          <div className="text-[12px] text-ink-3">
            {c.employeeMatricule}
            {c.employeeDepartment ? ` · ${c.employeeDepartment}` : ""}
          </div>
        </Link>
      </td>
      <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
        <Badge className={`${TYPE_COLORS[c.type]} text-[11px]`}>
          {c.type}
        </Badge>
      </td>
      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
        {fmt.date(c.dateDebut)}
      </td>
      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
        {c.dateFin ? fmt.date(c.dateFin) : "—"}
      </td>
      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">
        {fmt.money(c.salaireBase)}
      </td>
      <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
        <ContractStatusBadge status={c.status} dateFin={c.dateFin} periodeEssai={c.periodeEssai} dateDebut={c.dateDebut} />
      </td>
    </tr>
  );
}

function ContractStatusBadge({
  status,
  dateFin,
  periodeEssai,
  dateDebut,
}: {
  status: string;
  dateFin: string | null;
  periodeEssai: number | null;
  dateDebut: string;
}) {
  const now = new Date();

  if (status === "ACTIVE" && periodeEssai) {
    const start = new Date(dateDebut);
    const trialEnd = new Date(start);
    trialEnd.setDate(trialEnd.getDate() + periodeEssai);
    if (trialEnd > now) {
      return <StatusBadge kind="contract" status="TRIAL" />;
    }
  }

  if (status === "ACTIVE" && dateFin) {
    const end = new Date(dateFin);
    const diff = (end.getTime() - now.getTime()) / 86_400_000;
    if (diff <= 0) {
      return <StatusBadge kind="contract" status="EXPIRED" />;
    }
    if (diff <= 30) {
      return <StatusBadge kind="contract" status="ENDING_SOON" />;
    }
  }

  if (status === "ACTIVE" && dateFin && dateDebut && dateFin < dateDebut) {
    return <StatusBadge kind="contract" status="EXPIRED" />;
  }

  return <StatusBadge kind="contract" status={status} />;
}
