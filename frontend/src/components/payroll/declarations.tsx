"use client";

import { useQuery } from "@tanstack/react-query";
import {
  Building2,
  Coins,
  Download,
  Landmark,
  Loader2,
  type LucideIcon,
  ShieldCheck,
  Users,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Chip } from "@/components/ui/chip";
import { IconTile } from "@/components/ui/icon-tile";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney } from "@/lib/format";
import { formatPeriodShort } from "@/lib/payroll-status";
import type {
  DeclarationResponse,
  DeclarationType,
  PayrollRunResponse,
  PayrollRunStatus,
} from "@/server/ksm/modules/payroll";

const TYPES: DeclarationType[] = ["CNPS", "DIPE", "IRPP_CAC"];

function typeTone(type: DeclarationType): "info" | "warning" | "violet" {
  switch (type) {
    case "CNPS":
      return "info";
    case "DIPE":
      return "violet";
    default:
      return "warning";
  }
}

export function Declarations() {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const canRead = useCan("hrm:payroll:read");

  const [type, setType] = React.useState<DeclarationType>("CNPS");
  const [runId, setRunId] = React.useState("");

  const runsQuery = useQuery({
    queryKey: ["hrm", "payroll", "runs"],
    queryFn: () => apiFetch<PayrollRunResponse[]>("/api/hrm/payroll"),
  });

  const runs = React.useMemo(
    () =>
      (runsQuery.data ?? [])
        .filter((r) => r.status !== "DRAFT" && r.status !== "VARIABLES_LOCKED")
        .sort((a, b) => b.periode.localeCompare(a.periode)),
    [runsQuery.data],
  );

  // Default to the most recent calculated run once loaded.
  React.useEffect(() => {
    if (!runId && runs.length > 0) setRunId(runs[0].id);
  }, [runs, runId]);

  const declarationQuery = useQuery({
    queryKey: ["hrm", "payroll", "declaration", type, runId],
    queryFn: () =>
      apiFetch<DeclarationResponse>(
        `/api/hrm/payroll/declarations?type=${type}&runId=${runId}`,
      ),
    enabled: Boolean(runId),
  });

  const doc = declarationQuery.data;
  const selectedRun = runs.find((r) => r.id === runId) ?? null;

  return (
    <>
      <PageHeader
        ucBadge={t("declarations.uc")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }, { label: t("declarations.title") }]}
        title={t("declarations.title")}
        subtitle={t("declarations.subtitle")}
        actions={
          <div className="flex items-center gap-2 rounded-[11px] border border-line bg-white px-3 py-1.5 shadow-xs-brand">
            <Building2 className="h-4 w-4 text-ink-3" />
            <select
              value={runId}
              onChange={(e) => setRunId(e.target.value)}
              className="bg-transparent text-[13px] font-semibold text-ink outline-none"
              aria-label={t("declarations.period")}
            >
              {runs.length === 0 && <option value="">{t("declarations.noRuns")}</option>}
              {runs.map((r) => (
                <option key={r.id} value={r.id}>
                  {formatPeriodShort(r.periode, locale)} · {t(`status.${r.status as PayrollRunStatus}`)}
                </option>
              ))}
            </select>
          </div>
        }
      />

      <div className="flex flex-col gap-5">
        <div className="flex flex-wrap items-center gap-2">
          {TYPES.map((tp) => (
            <Chip key={tp} active={type === tp} tone="orange" onClick={() => setType(tp)}>
              {t(`declarations.types.${tp}.short`)}
            </Chip>
          ))}
        </div>

        <Card className="flex items-start gap-3 p-5">
          <IconTile icon={ShieldCheck} tone={typeTone(type)} size="md" />
          <div className="min-w-0">
            <h3 className="text-[15px] font-bold tracking-tight text-ink">
              {t(`declarations.types.${type}.name`)}
            </h3>
            <p className="text-[12.5px] text-ink-3">{t(`declarations.types.${type}.desc`)}</p>
          </div>
        </Card>

        {runsQuery.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
          </div>
        ) : runs.length === 0 ? (
          <Card className="px-6 py-12 text-center text-[13px] text-ink-3">
            {t("declarations.noRunsHint")}
          </Card>
        ) : declarationQuery.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
          </div>
        ) : declarationQuery.error ? (
          <Card className="px-6 py-10 text-center text-[13px] text-ink-3">
            {declarationQuery.error instanceof BffApiError ? declarationQuery.error.message : "—"}
          </Card>
        ) : doc ? (
          <>
            <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
              <StatCard icon={Users} tone="orange" label={t("declarations.stats.employees")} value={String(doc.employeeCount)} />
              <StatCard icon={Landmark} tone="info" label={t("declarations.stats.base")} value={formatMoney(Number(doc.totalGrossBase), { locale, withCurrency: false })} />
              <StatCard icon={Coins} tone="warning" label={t("declarations.stats.employee")} value={formatMoney(Number(doc.totalEmployee), { locale, withCurrency: false })} />
              <StatCard icon={Building2} tone="violet" label={t("declarations.stats.employer")} value={formatMoney(Number(doc.totalEmployer), { locale, withCurrency: false })} />
            </div>

            <Card>
              <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line-soft px-6 py-4">
                <div>
                  <h3 className="text-[15px] font-bold tracking-tight text-ink">
                    {t("declarations.table.title")}
                    {selectedRun ? ` · ${formatPeriodShort(selectedRun.periode, locale)}` : ""}
                  </h3>
                  <p className="text-[12px] text-ink-3">
                    {t("declarations.table.grandTotal")} :{" "}
                    <span className="font-mono-tabular font-semibold text-ink">
                      {formatMoney(Number(doc.grandTotal), { locale, withCurrency: false })}
                    </span>
                  </p>
                </div>
                {canRead && (
                  <a
                    href={`/api/hrm/payroll/declarations/csv?type=${type}&runId=${runId}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-1.5 rounded-[11px] bg-orange-500 px-3.5 py-2 text-[13px] font-semibold text-white shadow-xs-brand hover:bg-orange-500/90"
                  >
                    <Download className="h-4 w-4" />
                    {t("declarations.downloadCsv")}
                  </a>
                )}
              </div>

              {doc.items.length === 0 ? (
                <div className="px-6 py-12 text-center text-[13px] text-ink-3">{t("declarations.table.empty")}</div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-[13px]">
                    <thead>
                      <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
                        <th className="px-6 py-3">{t("declarations.cols.employee")}</th>
                        <th className="px-3 py-3">{t("declarations.cols.ssn")}</th>
                        <th className="px-3 py-3 text-right">{t("declarations.cols.base")}</th>
                        <th className="px-3 py-3 text-right">{t("declarations.cols.employeeShare")}</th>
                        <th className="px-6 py-3 text-right">{t("declarations.cols.employer")}</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-line-soft">
                      {doc.items.map((it) => (
                        <tr key={it.employeeId} className="hover:bg-bg-soft">
                          <td className="px-6 py-3">
                            <div className="text-[13px] font-semibold text-ink">{it.employeeName}</div>
                            <div className="font-mono-tabular text-[11px] text-ink-3">{it.matricule}</div>
                          </td>
                          <td className="font-mono-tabular px-3 py-3 text-[12px] text-ink-3">
                            {it.socialSecurityNo ?? "—"}
                          </td>
                          <td className="font-mono-tabular px-3 py-3 text-right text-ink-2">
                            {formatMoney(Number(it.grossBase), { locale, withCurrency: false })}
                          </td>
                          <td className="font-mono-tabular px-3 py-3 text-right text-ink-2">
                            {formatMoney(Number(it.employeeContribution), { locale, withCurrency: false })}
                          </td>
                          <td className="font-mono-tabular px-6 py-3 text-right text-ink-2">
                            {formatMoney(Number(it.employerContribution), { locale, withCurrency: false })}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr className="border-t border-line bg-bg-soft/50 text-[12.5px] font-semibold">
                        <td className="px-6 py-3 text-ink">{t("declarations.table.totals")}</td>
                        <td className="px-3 py-3" />
                        <td className="font-mono-tabular px-3 py-3 text-right text-ink">
                          {formatMoney(Number(doc.totalGrossBase), { locale, withCurrency: false })}
                        </td>
                        <td className="font-mono-tabular px-3 py-3 text-right text-ink">
                          {formatMoney(Number(doc.totalEmployee), { locale, withCurrency: false })}
                        </td>
                        <td className="font-mono-tabular px-6 py-3 text-right text-ink">
                          {formatMoney(Number(doc.totalEmployer), { locale, withCurrency: false })}
                        </td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              )}
            </Card>
          </>
        ) : null}
      </div>
    </>
  );
}

function StatCard({
  icon: Icon,
  tone,
  label,
  value,
}: {
  icon: LucideIcon;
  tone: "orange" | "info" | "warning" | "violet";
  label: string;
  value: string;
}) {
  return (
    <Card className="p-5">
      <div className="flex items-start justify-between">
        <div className="min-w-0">
          <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">{label}</div>
          <div className="font-display font-mono-tabular mt-1.5 text-[22px] font-extrabold tracking-tight text-ink">
            {value}
          </div>
        </div>
        <IconTile icon={Icon} tone={tone} size="sm" />
      </div>
    </Card>
  );
}
