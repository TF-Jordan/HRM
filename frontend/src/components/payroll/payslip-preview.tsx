"use client";

import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, Download, Loader2, Mail, Printer } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { useSession } from "@/components/providers/session-provider";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney } from "@/lib/format";
import { formatPeriodFr } from "@/lib/payroll-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  PayrollEntryResponse,
  PayrollRunResponse,
  PayslipLineResponse,
} from "@/server/ksm/modules/payroll";

export function PayslipPreview({ runId, entryId }: { runId: string; entryId: string }) {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const { session } = useSession();

  const runQuery = useQuery({
    queryKey: ["hrm", "payroll", runId],
    queryFn: () => apiFetch<PayrollRunResponse>(`/api/hrm/payroll/${runId}`),
  });
  const entriesQuery = useQuery({
    queryKey: ["hrm", "payroll", runId, "entries"],
    queryFn: () => apiFetch<PayrollEntryResponse[]>(`/api/hrm/payroll/${runId}/entries`),
  });
  const linesQuery = useQuery({
    queryKey: ["hrm", "payroll", "entry", entryId, "payslip"],
    queryFn: () => apiFetch<PayslipLineResponse[]>(`/api/hrm/payroll/entries/${entryId}/payslip`),
  });

  const entry = entriesQuery.data?.find((e) => e.id === entryId) ?? null;
  const employeeId = entry?.employeeId;
  const employeeQuery = useQuery({
    queryKey: ["hrm", "employee", employeeId],
    enabled: !!employeeId,
    queryFn: () => apiFetch<EmployeeResponse>(`/api/hrm/employees/${employeeId}`),
  });

  const run = runQuery.data;
  const employee = employeeQuery.data;
  const lines = linesQuery.data ?? [];

  const earnings = lines.filter((l) => l.type === "EARNING").sort((a, b) => a.ordreAffichage - b.ordreAffichage);
  const deductions = lines.filter((l) => l.type === "DEDUCTION").sort((a, b) => a.ordreAffichage - b.ordreAffichage);

  return (
    <>
      <PageHeader
        ucBadge={t("uc")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/payroll" },
          { label: run ? formatPeriodFr(run.periode) : "…", href: `/payroll/${runId}` },
          { label: t("payslip.title") },
        ]}
        title={t("payslip.title")}
        subtitle={run ? t("payslip.subtitle", { periode: formatPeriodFr(run.periode) }) : undefined}
        actions={
          <>
            <Link href={`/payroll/${runId}`}>
              <Button variant="secondary">
                <ArrowLeft className="h-4 w-4" />
                {t("actions.back")}
              </Button>
            </Link>
            <Button variant="secondary" onClick={() => window.print()}>
              <Printer className="h-4 w-4" />
              {t("actions.printPayslip")}
            </Button>
            <Button>
              <Download className="h-4 w-4" />
              {t("actions.downloadPayslip")}
            </Button>
          </>
        }
      />

      {runQuery.isLoading || entriesQuery.isLoading || linesQuery.isLoading ? (
        <div className="grid place-items-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : !entry || !run ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {linesQuery.error instanceof BffApiError ? linesQuery.error.message : "—"}
        </div>
      ) : (
        <Card>
          <div className="p-7">
            {/* Header */}
            <div className="flex flex-wrap items-start gap-4 border-b-2 border-ink pb-4">
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2.5">
                  <span className="grid h-9 w-9 place-items-center rounded-[9px] bg-grad-orange text-[18px] font-extrabold text-white shadow-orange-brand">
                    {session?.workspace?.organizationName?.[0]?.toUpperCase() ?? "R"}
                  </span>
                  <div>
                    <div className="font-display text-[18px] font-extrabold tracking-tight text-ink">
                      {session?.workspace?.organizationName ?? "—"}
                    </div>
                    <div className="text-[11px] text-ink-3">
                      {session?.workspace?.organizationId?.slice(0, 8) ?? ""}
                    </div>
                  </div>
                </div>
              </div>
              <div className="text-right">
                <div className="font-display text-[14px] font-bold tracking-tight text-ink">
                  {t("payslip.headerTitle")}
                </div>
                <div className="font-mono-tabular text-[12px] text-ink-3">
                  {t("payslip.period")} · {formatPeriodFr(run.periode)}
                </div>
                <div className="font-mono-tabular mt-0.5 text-[11px] text-ink-4">
                  {t("payslip.ref", { ref: `PSL-${run.periode}-${employee?.matricule ?? entry.id.slice(0, 6)}` })}
                </div>
              </div>
            </div>

            {/* Employee / Payment */}
            <div className="mt-5 grid grid-cols-1 gap-3.5 md:grid-cols-2">
              <div className="rounded-[10px] bg-bg-soft p-3">
                <div className="text-[10px] font-semibold uppercase tracking-[0.06em] text-ink-3">
                  {t("payslip.employee")}
                </div>
                <div className="mt-1 text-[14px] font-semibold text-ink">
                  {employee?.actorDisplayName ?? t("payslip.noEmployee")}
                </div>
                <div className="text-[12px] text-ink-3">
                  {employee?.matricule ?? entry.employeeId.slice(0, 8)}
                  {employee?.departmentCode ? ` · ${employee.departmentCode}` : ""}
                </div>
                {employee?.numCnps && (
                  <div className="mt-1 text-[11px] text-ink-3">
                    CNPS · {employee.numCnps}
                  </div>
                )}
              </div>
              <div className="rounded-[10px] bg-bg-soft p-3">
                <div className="text-[10px] font-semibold uppercase tracking-[0.06em] text-ink-3">
                  {t("payslip.payment")}
                </div>
                <div className="mt-1 text-[14px] font-semibold text-ink">
                  {entry.paymentChannel === "BANK_TRANSFER"
                    ? t("channel.BANK_TRANSFER")
                    : entry.paymentChannel === "MTN_MOBILE_MONEY"
                    ? t("channel.MTN_MOBILE_MONEY")
                    : entry.paymentChannel === "ORANGE_MONEY"
                    ? t("channel.ORANGE_MONEY")
                    : entry.paymentChannel === "CASH"
                    ? t("channel.CASH")
                    : "—"}
                </div>
                <div className="text-[12px] text-ink-3">
                  {employee?.numMobileMoney
                    ? maskAccount(employee.numMobileMoney)
                    : employee?.compteBancaire
                    ? maskAccount(employee.compteBancaire)
                    : "—"}
                </div>
                <div className="mt-1 text-[11px] text-ink-3">{t("payslip.scheduled")}</div>
              </div>
            </div>

            {/* Lines table */}
            <table className="mt-5 w-full text-[13px]">
              <thead>
                <tr className="border-b border-line text-[10px] uppercase tracking-[0.05em] text-ink-3">
                  <th className="px-2 py-2.5 text-left">{t("payslip.tableLibelle")}</th>
                  <th className="px-2 py-2.5 text-right" style={{ width: 100 }}>{t("payslip.tableBase")}</th>
                  <th className="px-2 py-2.5 text-right" style={{ width: 80 }}>{t("payslip.tableTaux")}</th>
                  <th className="px-2 py-2.5 text-right" style={{ width: 140 }}>{t("payslip.tableGain")}</th>
                  <th className="px-2 py-2.5 text-right" style={{ width: 140 }}>{t("payslip.tableRetenue")}</th>
                </tr>
              </thead>
              <tbody className="font-mono-tabular">
                <tr className="bg-orange-50">
                  <td colSpan={5} className="px-2 py-2 text-[11.5px] font-semibold text-orange-700">
                    {t("payslip.rubricBrut")}
                  </td>
                </tr>
                {earnings.map((l) => (
                  <Line key={l.id} line={l} side="gain" locale={locale} />
                ))}
                <tr className="bg-bg-soft">
                  <td colSpan={3} className="px-2 py-2.5 text-[12.5px] font-bold text-ink">
                    {t("payslip.subtotalBrut")}
                  </td>
                  <td className="px-2 py-2.5 text-right text-[12.5px] font-bold text-ink">
                    {formatMoney(Number(entry.brut ?? 0), { locale, withCurrency: false })}
                  </td>
                  <td />
                </tr>

                <tr className="bg-orange-50">
                  <td colSpan={5} className="px-2 py-2 text-[11.5px] font-semibold text-orange-700">
                    {t("payslip.rubricRetenues")}
                  </td>
                </tr>
                {deductions.map((l) => (
                  <Line key={l.id} line={l} side="retenue" locale={locale} />
                ))}
                <tr className="bg-bg-soft">
                  <td colSpan={4} className="px-2 py-2.5 text-[12.5px] font-bold text-ink">
                    {t("payslip.subtotalRetenues")}
                  </td>
                  <td className="px-2 py-2.5 text-right text-[12.5px] font-bold text-ink">
                    {formatMoney(Number(entry.retenues ?? 0), { locale, withCurrency: false })}
                  </td>
                </tr>

                <tr className="bg-ink text-white">
                  <td className="px-3 py-3.5 text-[14px] font-bold">{t("payslip.netLabel")}</td>
                  <td colSpan={3} />
                  <td className="font-display px-3 py-3.5 text-right text-[20px] font-extrabold tracking-tight">
                    {formatMoney(Number(entry.net ?? 0), { locale, withCurrency: false })}
                  </td>
                </tr>
              </tbody>
            </table>

            <div className="mt-5 flex flex-wrap items-center gap-4 border-t border-line-soft pt-4 text-[11px] text-ink-3">
              <span>
                {t("payslip.footerCumulBrut", { year: run.periode.slice(0, 4) })}:{" "}
                <b className="font-mono-tabular text-ink-2">
                  {formatMoney(Number(entry.brut ?? 0), { locale, withCurrency: false })}
                </b>{" "}
                XAF
              </span>
              <span>
                {t("payslip.footerCumulIrpp", { year: run.periode.slice(0, 4) })}:{" "}
                <b className="font-mono-tabular text-ink-2">
                  {formatMoney(Number(entry.irpp ?? 0), { locale, withCurrency: false })}
                </b>{" "}
                XAF
              </span>
              <div className="ml-auto flex flex-wrap items-center gap-2">
                <Button variant="secondary" size="sm" onClick={() => window.print()}>
                  <Printer className="h-3.5 w-3.5" />
                  {t("actions.printPayslip")}
                </Button>
                <Button variant="secondary" size="sm">
                  <Mail className="h-3.5 w-3.5" />
                  {t("actions.emailPayslip")}
                </Button>
                <Button size="sm">
                  <Download className="h-3.5 w-3.5" />
                  {t("actions.downloadPayslip")}
                </Button>
              </div>
            </div>
          </div>
        </Card>
      )}
    </>
  );
}

function Line({
  line,
  side,
  locale,
}: {
  line: PayslipLineResponse;
  side: "gain" | "retenue";
  locale: "fr" | "en";
}) {
  return (
    <tr>
      <td className="px-2 py-2 text-ink-2">{line.libelle}</td>
      <td className="px-2 py-2 text-right text-ink-3">
        {line.base != null
          ? formatMoney(Number(line.base), { locale, withCurrency: false })
          : "—"}
      </td>
      <td className="px-2 py-2 text-right text-ink-3">
        {line.taux != null ? `${(Number(line.taux) * 100).toFixed(line.taux === 0 ? 0 : 1)}%` : "—"}
      </td>
      <td className="px-2 py-2 text-right">
        {side === "gain" ? formatMoney(Number(line.montant ?? 0), { locale, withCurrency: false }) : ""}
      </td>
      <td className="px-2 py-2 text-right">
        {side === "retenue" ? formatMoney(Number(line.montant ?? 0), { locale, withCurrency: false }) : ""}
      </td>
    </tr>
  );
}

function maskAccount(s: string): string {
  if (s.length <= 4) return s;
  return s.slice(0, 3) + " ** ** " + s.slice(-2);
}
