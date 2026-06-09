"use client";

import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, Download, Loader2, Mail } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { useSession } from "@/components/providers/session-provider";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney } from "@/lib/format";
import { formatPeriodFr } from "@/lib/payroll-status";
import type { OrganizationResponse } from "@/server/ksm/modules/organization";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  PayrollEntryResponse,
  PayrollRunResponse,
  PayslipLineResponse,
} from "@/server/ksm/modules/payroll";

type PayrollDocumentResponse = {
  id: string;
  fileId: string;
  fileName: string;
  type: string;
  verificationCode: string;
};

/** Download a backend-generated signed PDF for a payroll entry. */
async function downloadBackendPayslip(entryId: string): Promise<void> {
  const doc = await apiFetch<PayrollDocumentResponse>(
    `/api/hrm/payroll/documents/payslip?entryId=${entryId}`,
    { method: "POST" },
  );
  const res = await fetch(`/api/files/${doc.fileId}`);
  if (!res.ok) throw new Error(`File download failed: ${res.status}`);
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = doc.fileName || `bulletin-${entryId}.pdf`;
  link.click();
  URL.revokeObjectURL(url);
}

export function PayslipPreview({ runId, entryId }: { runId: string; entryId: string }) {
  const t = useTranslations("payroll");
  const { session } = useSession();
  const [isDownloading, setIsDownloading] = React.useState(false);
  const locale = "fr" as const;

  const orgQuery = useQuery({
    queryKey: ["admin", "organization"],
    queryFn: () => apiFetch<OrganizationResponse>("/api/admin/organization"),
    staleTime: 5 * 60_000,
  });

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
  const org = orgQuery.data;

  // Split lines by type (same order as PDF: EARNING + DEDUCTION together, then EMPLOYER_INFO)
  const earningAndDeductionLines = lines
    .filter((l) => l.type === "EARNING" || l.type === "DEDUCTION")
    .sort((a, b) => a.ordreAffichage - b.ordreAffichage);
  const employerLines = lines
    .filter((l) => l.type === "EMPLOYER_INFO")
    .sort((a, b) => a.ordreAffichage - b.ordreAffichage);

  const totalEmployerCharges = employerLines.reduce((s, l) => s + Number(l.montant ?? 0), 0);

  async function handleDownload() {
    if (!entry || !run || isDownloading) return;
    setIsDownloading(true);
    try {
      await downloadBackendPayslip(entryId);
    } finally {
      setIsDownloading(false);
    }
  }

  const fmt = (v: number | string | null | undefined) =>
    formatMoney(Number(v ?? 0), { locale, withCurrency: false });

  const fmtRate = (taux: number | string | null | undefined) => {
    if (taux == null) return "—";
    const n = Number(taux);
    return `${(n * 100).toFixed(n === 0 ? 0 : 1)}%`;
  };

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
            <Button onClick={handleDownload} disabled={isDownloading || !entry || !run}>
              {isDownloading ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Download className="h-4 w-4" />
              )}
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
            {/* ══════════════════ TITLE ══════════════════ */}
            <div className="border-b-2 border-ink pb-4 text-center">
              <div className="font-display text-[18px] font-extrabold tracking-tight text-ink">
                {t("payslip.headerTitle")}
              </div>
              <div className="mt-1 text-[12px] text-ink-3">
                {t("payslip.period")} : {formatPeriodFr(run.periode)}
              </div>
            </div>

            {/* ══════════════════ EMPLOYEUR ══════════════════ */}
            <div className="mt-5">
              <div className="text-[11px] font-bold uppercase tracking-[0.06em] text-ink-3">
                {t("payslip.employer")}
              </div>
              <div className="mt-2 grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-[12px]">
                <span className="text-ink-3">{t("payslip.raisonSociale")}</span>
                <span className="font-semibold text-ink">
                  {org?.legalName ?? org?.longName ?? org?.shortName ?? session?.workspace?.organizationName ?? "—"}
                </span>
                {org?.legalForm && (
                  <>
                    <span className="text-ink-3">{t("payslip.formeJuridique")}</span>
                    <span className="text-ink">
                      {org.legalForm}
                      {org.capitalShare ? ` au capital de ${Number(org.capitalShare).toLocaleString("fr-FR")} FCFA` : ""}
                    </span>
                  </>
                )}
                {org?.email && (
                  <>
                    <span className="text-ink-3">Email</span>
                    <span className="text-ink">{org.email}</span>
                  </>
                )}
                {org?.businessRegistrationNumber && (
                  <>
                    <span className="text-ink-3">{t("payslip.rccm")}</span>
                    <span className="text-ink">{org.businessRegistrationNumber}</span>
                  </>
                )}
                {org?.taxNumber && (
                  <>
                    <span className="text-ink-3">{t("payslip.niu")}</span>
                    <span className="text-ink">{org.taxNumber}</span>
                  </>
                )}
              </div>
            </div>

            {/* ══════════════════ SALARIÉ ══════════════════ */}
            <div className="mt-5">
              <div className="text-[11px] font-bold uppercase tracking-[0.06em] text-ink-3">
                {t("payslip.employee")}
              </div>
              <div className="mt-2 grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-[12px]">
                <span className="text-ink-3">{t("payslip.name")}</span>
                <span className="font-semibold text-ink">
                  {employee?.actorDisplayName ?? t("payslip.noEmployee")}
                </span>
                <span className="text-ink-3">{t("payslip.matricule")}</span>
                <span className="text-ink">{employee?.matricule ?? entry.employeeId.slice(0, 8)}</span>
                {employee?.numCnps && (
                  <>
                    <span className="text-ink-3">{t("payslip.cnps")}</span>
                    <span className="text-ink">{employee.numCnps}</span>
                  </>
                )}
                <span className="text-ink-3">{t("payslip.categorieEchelon")}</span>
                <span className="text-ink">
                  {employee?.categorie ?? "—"} / {employee?.echelon ?? "—"}
                </span>
                {employee?.dateEmbauche && (
                  <>
                    <span className="text-ink-3">{t("payslip.hireDate")}</span>
                    <span className="text-ink">
                      {new Date(employee.dateEmbauche).toLocaleDateString("fr-FR")}
                    </span>
                  </>
                )}
              </div>
            </div>

            {/* ══════════════════ DÉTAIL DES ÉLÉMENTS DE PAIE ══════════════════ */}
            <div className="mt-5">
              <div className="mb-2 text-[11px] font-bold uppercase tracking-[0.06em] text-ink-3">
                {t("payslip.payDetail")}
              </div>
              <table className="w-full text-[12px]">
                <thead>
                  <tr className="border-b-2 border-ink text-[10px] uppercase tracking-[0.05em] text-ink-3">
                    <th className="px-2 py-2 text-left">{t("payslip.tableLibelle")}</th>
                    <th className="px-2 py-2 text-right" style={{ width: 100 }}>{t("payslip.tableBase")}</th>
                    <th className="px-2 py-2 text-right" style={{ width: 80 }}>{t("payslip.tableTaux")}</th>
                    <th className="px-2 py-2 text-right" style={{ width: 120 }}>{t("payslip.tableMontant")}</th>
                  </tr>
                </thead>
                <tbody className="font-mono-tabular">
                  {earningAndDeductionLines.map((l) => (
                    <tr key={l.id} className="border-b border-line/40">
                      <td className="px-2 py-1.5 text-ink-2">{l.libelle}</td>
                      <td className="px-2 py-1.5 text-right text-ink-3">
                        {l.base != null ? fmt(l.base) : "—"}
                      </td>
                      <td className="px-2 py-1.5 text-right text-ink-3">
                        {fmtRate(l.taux)}
                      </td>
                      <td className="px-2 py-1.5 text-right text-ink">
                        {fmt(l.montant)}
                      </td>
                    </tr>
                  ))}

                  {/* SALAIRE BRUT */}
                  <tr className="border-t-2 border-ink bg-bg-soft">
                    <td colSpan={3} className="px-2 py-2 text-[12px] font-bold text-ink">
                      {t("payslip.subtotalGross")}
                    </td>
                    <td className="px-2 py-2 text-right text-[12px] font-bold text-ink">
                      {fmt(entry.brut)}
                    </td>
                  </tr>

                  {/* Total retenues salariales */}
                  <tr>
                    <td colSpan={3} className="px-2 py-1.5 text-ink-2">
                      {t("payslip.subtotalRetenues")}
                    </td>
                    <td className="px-2 py-1.5 text-right text-ink">
                      {fmt(entry.totalDeductions)}
                    </td>
                  </tr>

                  {/* dont IRPP */}
                  {Number(entry.incomeTax ?? 0) > 0 && (
                    <tr>
                      <td colSpan={3} className="px-2 py-1.5 pl-6 text-ink-3">
                        {t("payslip.dontIrpp")}
                      </td>
                      <td className="px-2 py-1.5 text-right text-ink-3">
                        {fmt(entry.incomeTax)}
                      </td>
                    </tr>
                  )}

                  {/* Separator */}
                  <tr>
                    <td colSpan={4} className="py-0">
                      <div className="border-t-2 border-ink" />
                    </td>
                  </tr>

                  {/* NET À PAYER */}
                  <tr className="bg-ink text-white">
                    <td colSpan={3} className="px-3 py-3 text-[14px] font-bold">
                      {t("payslip.netLabel")}
                    </td>
                    <td className="font-display px-3 py-3 text-right text-[20px] font-extrabold tracking-tight">
                      {fmt(entry.net)}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            {/* ══════════════════ CHARGES PATRONALES ══════════════════ */}
            {(employerLines.length > 0 || totalEmployerCharges > 0) && (
              <div className="mt-5">
                <div className="mb-2 text-[11px] font-bold uppercase tracking-[0.06em] text-ink-3">
                  {t("payslip.rubricEmployer")}
                </div>
                <table className="w-full text-[12px]">
                  <tbody className="font-mono-tabular">
                    {employerLines.map((l) => (
                      <tr key={l.id} className="border-b border-line/40">
                        <td className="px-2 py-1.5 text-ink-2">{l.libelle}</td>
                        <td className="px-2 py-1.5 text-right text-ink-3" style={{ width: 100 }}>
                          {l.base != null ? fmt(l.base) : "—"}
                        </td>
                        <td className="px-2 py-1.5 text-right text-ink-3" style={{ width: 80 }}>
                          {fmtRate(l.taux)}
                        </td>
                        <td className="px-2 py-1.5 text-right text-ink" style={{ width: 120 }}>
                          {fmt(l.montant)}
                        </td>
                      </tr>
                    ))}
                    <tr className="border-t-2 border-ink bg-bg-soft">
                      <td colSpan={3} className="px-2 py-2 text-[12px] font-bold text-ink">
                        {t("payslip.totalEmployerCharges")}
                      </td>
                      <td className="px-2 py-2 text-right text-[12px] font-bold text-ink" style={{ width: 120 }}>
                        {fmt(entry.employerCharges)}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            )}

            {/* ══════════════════ CUMULS ANNUELS ══════════════════ */}
            <div className="mt-5">
              <div className="mb-2 text-[11px] font-bold uppercase tracking-[0.06em] text-ink-3">
                {t("payslip.cumulAnnuels")}
              </div>
              <div className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-[12px]">
                <span className="text-ink-3">{t("payslip.cumulBrut")}</span>
                <span className="font-mono-tabular text-ink">{fmt(entry.brut)} XAF</span>
                <span className="text-ink-3">{t("payslip.cumulNet")}</span>
                <span className="font-mono-tabular text-ink">{fmt(entry.net)} XAF</span>
              </div>
            </div>

            {/* ══════════════════ MODE DE PAIEMENT ══════════════════ */}
            {entry.paymentChannel && (
              <div className="mt-5">
                <div className="mb-2 text-[11px] font-bold uppercase tracking-[0.06em] text-ink-3">
                  {t("payslip.payment")}
                </div>
                <div className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-[12px]">
                  <span className="text-ink-3">{t("payslip.channel")}</span>
                  <span className="text-ink">
                    {entry.paymentChannel === "BANK_TRANSFER"
                      ? t("channel.BANK_TRANSFER")
                      : entry.paymentChannel === "MTN_MOBILE_MONEY"
                      ? t("channel.MTN_MOBILE_MONEY")
                      : entry.paymentChannel === "ORANGE_MONEY"
                      ? t("channel.ORANGE_MONEY")
                      : entry.paymentChannel === "CASH"
                      ? t("channel.CASH")
                      : entry.paymentChannel}
                  </span>
                  {entry.accountRef && (
                    <>
                      <span className="text-ink-3">{t("payslip.accountRef")}</span>
                      <span className="text-ink">{entry.accountRef}</span>
                    </>
                  )}
                </div>
              </div>
            )}

            {/* ══════════════════ CERTIFICATION ══════════════════ */}
            <div className="mt-6 border-t border-line-soft pt-4 text-[11px] text-ink-2 italic">
              {t("payslip.certification", {
                amount: `${fmt(entry.net)} francs CFA`,
              })}
            </div>

            {/* ══════════════════ FOOTER ACTIONS ══════════════════ */}
            <div className="mt-4 flex flex-wrap items-center gap-2 border-t border-line-soft pt-4">
              <div className="ml-auto flex flex-wrap items-center gap-2">
                <Button variant="secondary" size="sm">
                  <Mail className="h-3.5 w-3.5" />
                  {t("actions.emailPayslip")}
                </Button>
                <Button
                  size="sm"
                  onClick={handleDownload}
                  disabled={isDownloading}
                >
                  {isDownloading ? (
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                  ) : (
                    <Download className="h-3.5 w-3.5" />
                  )}
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
