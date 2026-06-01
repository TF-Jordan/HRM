"use client";

import { Document, Image, Page, StyleSheet, Text, View, pdf } from "@react-pdf/renderer";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, Download, Loader2, Mail } from "lucide-react";
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

type OrgLegalData = {
  shortName?: string | null;
  longName?: string | null;
  businessRegistrationNumber?: string | null;
  taxNumber?: string | null;
  capitalShare?: number | string | null;
  ceoName?: string | null;
  email?: string | null;
  websiteUrl?: string | null;
  legalForm?: string | null;
  logoUri?: string | null;
  logoId?: string | null;
};

/** Fetch any URL (relative BFF route) and return a base64 data URL for react-pdf. */
export async function toDataUrl(url: string): Promise<string> {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`logo fetch ${res.status}`);
  const blob = await res.blob();
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });
}

export function PayslipPreview({ runId, entryId }: { runId: string; entryId: string }) {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const { session } = useSession();
  const [isDownloading, setIsDownloading] = React.useState(false);

  const orgQuery = useQuery({
    queryKey: ["admin", "organization"],
    queryFn: () => apiFetch<OrgLegalData>("/api/admin/organization"),
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

  // Legal header lines — shown in both screen preview and PDF
  const orgLegalLine = [
    org?.businessRegistrationNumber ? `RCCM ${org.businessRegistrationNumber}` : null,
    org?.taxNumber ? `NIU ${org.taxNumber}` : null,
    org?.capitalShare ? `Capital ${Number(org.capitalShare).toLocaleString("fr-FR")} XAF` : null,
    org?.ceoName ? `DG ${org.ceoName}` : null,
  ].filter(Boolean).join(" · ") || (session?.workspace?.organizationId?.slice(0, 8) ?? "");

  const orgContactLine = [
    org?.email,
    org?.websiteUrl?.replace(/^https?:\/\//, ""),
  ].filter(Boolean).join(" · ");

  const earnings = lines
    .filter((l) => l.type === "EARNING")
    .sort((a, b) => a.ordreAffichage - b.ordreAffichage);
  const deductions = lines
    .filter((l) => l.type === "DEDUCTION")
    .sort((a, b) => a.ordreAffichage - b.ordreAffichage);

  async function handleDownload() {
    if (!entry || !run || isDownloading) return;
    setIsDownloading(true);
    try {
      // Always fetch fresh org data at download time — never rely on React Query cache alone
      let orgData: OrgLegalData | undefined;
      try {
        orgData = await apiFetch<OrgLegalData>("/api/admin/organization");
      } catch { /* continue with session fallback */ }

      const legalLine = [
        orgData?.businessRegistrationNumber ? `RCCM ${orgData.businessRegistrationNumber}` : null,
        orgData?.taxNumber ? `NIU ${orgData.taxNumber}` : null,
        orgData?.capitalShare ? `Capital ${Number(orgData.capitalShare).toLocaleString("fr-FR")} XAF` : null,
        orgData?.ceoName ? `DG ${orgData.ceoName}` : null,
      ].filter(Boolean).join(" · ") || (session?.workspace?.organizationId?.slice(0, 8) ?? "");

      const contactLine = [
        orgData?.email,
        orgData?.websiteUrl?.replace(/^https?:\/\//, ""),
      ].filter(Boolean).join(" · ");

      // Use logoId to build the BFF proxy URL (/api/files/{id}) — logoUri from KSM
      // is the KSM direct URL which is not accessible from the browser without headers.
      let logoDataUrl: string | undefined;
      if (orgData?.logoId) {
        try { logoDataUrl = await toDataUrl(`/api/files/${orgData.logoId}`); } catch { /* fallback to initial */ }
      }

      const blob = await pdf(
        <PayslipPdfDocument
          organizationName={orgData?.longName || orgData?.shortName || session?.workspace?.organizationName || "—"}
          organizationRef={legalLine}
          organizationContact={contactLine}
          logoDataUrl={logoDataUrl}
          run={run}
          entry={entry}
          employee={employee ?? null}
          earnings={earnings}
          deductions={deductions}
          locale={locale}
          headerTitle={t("payslip.headerTitle")}
          periodLabel={t("payslip.period")}
          refPrefix="PSL"
          employeeLabel={t("payslip.employee")}
          noEmployeeLabel={t("payslip.noEmployee")}
          rubricBrutLabel={t("payslip.rubricBrut")}
          rubricRetenuesLabel={t("payslip.rubricRetenues")}
          subtotalBrutLabel={t("payslip.subtotalBrut")}
          subtotalRetenuesLabel={t("payslip.subtotalRetenues")}
          netLabel={t("payslip.netLabel")}
          colLibelle={t("payslip.tableLibelle")}
          colBase={t("payslip.tableBase")}
          colTaux={t("payslip.tableTaux")}
          colGain={t("payslip.tableGain")}
          colRetenue={t("payslip.tableRetenue")}
        />,
      ).toBlob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `bulletin-${run.periode}-${employee?.matricule ?? entry.id.slice(0, 6)}.pdf`;
      link.click();
      URL.revokeObjectURL(url);
    } finally {
      setIsDownloading(false);
    }
  }

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
            {/* Header */}
            <div className="flex flex-wrap items-start gap-4 border-b-2 border-ink pb-4">
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2.5">
                  {org?.logoUri ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={org.logoUri} alt="" className="h-9 w-9 rounded-[9px] object-contain shadow-sm" />
                  ) : (
                    <span className="grid h-9 w-9 place-items-center rounded-[9px] bg-grad-orange text-[18px] font-extrabold text-white shadow-orange-brand">
                      {(org?.shortName || session?.workspace?.organizationName || "R")[0]?.toUpperCase()}
                    </span>
                  )}
                  <div>
                    <div className="font-display text-[18px] font-extrabold tracking-tight text-ink">
                      {org?.longName || org?.shortName || session?.workspace?.organizationName || "—"}
                    </div>
                    <div className="text-[11px] text-ink-3">{orgLegalLine}</div>
                    {orgContactLine && (
                      <div className="text-[10px] text-ink-4">{orgContactLine}</div>
                    )}
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
                  <div className="mt-1 text-[11px] text-ink-3">CNPS · {employee.numCnps}</div>
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
                  <th className="px-2 py-2.5 text-right" style={{ width: 100 }}>
                    {t("payslip.tableBase")}
                  </th>
                  <th className="px-2 py-2.5 text-right" style={{ width: 80 }}>
                    {t("payslip.tableTaux")}
                  </th>
                  <th className="px-2 py-2.5 text-right" style={{ width: 140 }}>
                    {t("payslip.tableGain")}
                  </th>
                  <th className="px-2 py-2.5 text-right" style={{ width: 140 }}>
                    {t("payslip.tableRetenue")}
                  </th>
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

// ────────────────────────────────────────────────────────────────────────
// DOM helpers
// ────────────────────────────────────────────────────────────────────────

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
        {line.taux != null ? `${(Number(line.taux) * 100).toFixed(Number(line.taux) === 0 ? 0 : 1)}%` : "—"}
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

// ────────────────────────────────────────────────────────────────────────
// PDF document — mirrors the on-screen payslip from the org header to NET À PAYER
// ────────────────────────────────────────────────────────────────────────

const S = StyleSheet.create({
  page: {
    paddingHorizontal: 40,
    paddingVertical: 36,
    fontSize: 9,
    fontFamily: "Helvetica",
    color: "#111827",
    backgroundColor: "#ffffff",
  },
  // ── Header ──
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-start",
    paddingBottom: 10,
    borderBottom: "2pt solid #111827",
    marginBottom: 14,
  },
  orgRow: { flexDirection: "row", alignItems: "center" },
  orgInitial: {
    width: 30,
    height: 30,
    backgroundColor: "#F97316",
    borderRadius: 6,
    justifyContent: "center",
    alignItems: "center",
    marginRight: 8,
  },
  orgInitialText: { fontFamily: "Helvetica-Bold", fontSize: 15, color: "#ffffff" },
  orgLogo: { width: 30, height: 30, borderRadius: 6, marginRight: 8, objectFit: "contain" },
  orgName: { fontFamily: "Helvetica-Bold", fontSize: 13, color: "#111827" },
  orgRef: { fontSize: 8, color: "#6B7280", marginTop: 2 },
  headerRight: { alignItems: "flex-end" },
  headerTitle: { fontFamily: "Helvetica-Bold", fontSize: 11, color: "#111827" },
  headerPeriod: { fontSize: 8, color: "#6B7280", marginTop: 2 },
  headerRef: { fontSize: 7, color: "#9CA3AF", marginTop: 1 },
  // ── Info boxes ──
  infoGrid: { flexDirection: "row", gap: 10, marginBottom: 14 },
  infoBox: { flex: 1, backgroundColor: "#F9FAFB", borderRadius: 6, padding: 8 },
  infoLabel: {
    fontSize: 7,
    color: "#6B7280",
    textTransform: "uppercase",
    fontFamily: "Helvetica-Bold",
    marginBottom: 4,
  },
  infoValue: { fontFamily: "Helvetica-Bold", fontSize: 10, color: "#111827" },
  infoSub: { fontSize: 8, color: "#6B7280", marginTop: 2 },
  // ── Table ──
  tableHeaderRow: {
    flexDirection: "row",
    borderBottom: "0.75pt solid #D1D5DB",
    paddingBottom: 4,
    marginBottom: 2,
  },
  thLibelle: { flex: 1, fontSize: 7, color: "#9CA3AF", fontFamily: "Helvetica-Bold", textTransform: "uppercase" },
  thNum: { width: 72, fontSize: 7, color: "#9CA3AF", fontFamily: "Helvetica-Bold", textTransform: "uppercase", textAlign: "right" },
  thTaux: { width: 44, fontSize: 7, color: "#9CA3AF", fontFamily: "Helvetica-Bold", textTransform: "uppercase", textAlign: "right" },
  thAmount: { width: 82, fontSize: 7, color: "#9CA3AF", fontFamily: "Helvetica-Bold", textTransform: "uppercase", textAlign: "right" },
  // Section header
  sectionRow: { backgroundColor: "#FFF7ED", paddingVertical: 4, paddingHorizontal: 4, marginTop: 4 },
  sectionText: { fontSize: 8, color: "#C2410C", fontFamily: "Helvetica-Bold" },
  // Data row
  dataRow: {
    flexDirection: "row",
    paddingVertical: 3,
    paddingHorizontal: 4,
    borderBottom: "0.5pt solid #F3F4F6",
  },
  cellLibelle: { flex: 1, fontSize: 8.5, color: "#374151" },
  cellBase: { width: 72, fontSize: 8.5, color: "#6B7280", textAlign: "right", fontFamily: "Courier" },
  cellTaux: { width: 44, fontSize: 8.5, color: "#6B7280", textAlign: "right", fontFamily: "Courier" },
  cellGain: { width: 82, fontSize: 8.5, color: "#111827", textAlign: "right", fontFamily: "Courier" },
  cellRetenue: { width: 82, fontSize: 8.5, color: "#111827", textAlign: "right", fontFamily: "Courier" },
  // Subtotal row
  subtotalRow: { flexDirection: "row", backgroundColor: "#F9FAFB", paddingVertical: 5, paddingHorizontal: 4 },
  subtotalLabel: { flex: 1, fontSize: 9, color: "#111827", fontFamily: "Helvetica-Bold" },
  subtotalValue: { width: 82, fontSize: 9, color: "#111827", textAlign: "right", fontFamily: "Helvetica-Bold" },
  subtotalEmpty: { width: 82 },
  // NET À PAYER
  netRow: { flexDirection: "row", backgroundColor: "#111827", paddingVertical: 10, paddingHorizontal: 6, marginTop: 4 },
  netLabel: { flex: 1, fontSize: 12, color: "#ffffff", fontFamily: "Helvetica-Bold" },
  netValue: { width: 82, fontSize: 16, color: "#ffffff", textAlign: "right", fontFamily: "Helvetica-Bold" },
});

export type PdfProps = {
  organizationName: string;
  organizationRef: string;
  organizationContact?: string;
  logoDataUrl?: string;
  run: PayrollRunResponse;
  entry: PayrollEntryResponse;
  employee: EmployeeResponse | null;
  earnings: PayslipLineResponse[];
  deductions: PayslipLineResponse[];
  locale: "fr" | "en";
  headerTitle: string;
  periodLabel: string;
  refPrefix: string;
  employeeLabel: string;
  noEmployeeLabel: string;
  rubricBrutLabel: string;
  rubricRetenuesLabel: string;
  subtotalBrutLabel: string;
  subtotalRetenuesLabel: string;
  netLabel: string;
  colLibelle: string;
  colBase: string;
  colTaux: string;
  colGain: string;
  colRetenue: string;
};

export function PayslipPdfDocument(props: PdfProps) {
  const {
    organizationName, organizationRef, organizationContact, logoDataUrl,
    run, entry, employee, earnings, deductions, locale,
    headerTitle, periodLabel, refPrefix, employeeLabel, noEmployeeLabel,
    rubricBrutLabel, rubricRetenuesLabel, subtotalBrutLabel, subtotalRetenuesLabel, netLabel,
    colLibelle, colBase, colTaux, colGain, colRetenue,
  } = props;

  const fmt = (n: number | string) =>
    formatMoney(Number(n ?? 0), { locale, withCurrency: false });

  const fmtTaux = (taux: number | string | null) => {
    if (taux == null) return "—";
    const n = Number(taux);
    return `${(n * 100).toFixed(n === 0 ? 0 : 1)}%`;
  };

  const matricule = employee?.matricule ?? entry.employeeId.slice(0, 8);
  const deptCode = employee?.departmentCode ? ` · ${employee.departmentCode}` : "";
  const ref = `${refPrefix}-${run.periode}-${employee?.matricule ?? entry.id.slice(0, 6)}`;
  const initial = organizationName?.[0]?.toUpperCase() ?? "R";

  return (
    <Document>
      <Page size="A4" style={S.page}>
        {/* ── Header ── */}
        <View style={S.header}>
          <View style={S.orgRow}>
            {logoDataUrl ? (
              <Image src={logoDataUrl} style={S.orgLogo} />
            ) : (
              <View style={S.orgInitial}>
                <Text style={S.orgInitialText}>{initial}</Text>
              </View>
            )}
            <View>
              <Text style={S.orgName}>{organizationName}</Text>
              {organizationRef ? <Text style={S.orgRef}>{organizationRef}</Text> : null}
              {organizationContact ? <Text style={[S.orgRef, { marginTop: 1 }]}>{organizationContact}</Text> : null}
            </View>
          </View>
          <View style={S.headerRight}>
            <Text style={S.headerTitle}>{headerTitle}</Text>
            <Text style={S.headerPeriod}>
              {periodLabel} · {formatPeriodFr(run.periode)}
            </Text>
            <Text style={S.headerRef}>{`N° ${ref}`}</Text>
          </View>
        </View>

        {/* ── Employee / Payment info ── */}
        <View style={S.infoGrid}>
          <View style={S.infoBox}>
            <Text style={S.infoLabel}>{employeeLabel}</Text>
            <Text style={S.infoValue}>
              {employee?.actorDisplayName ?? noEmployeeLabel}
            </Text>
            <Text style={S.infoSub}>
              {matricule}{deptCode}
            </Text>
            {employee?.numCnps ? (
              <Text style={S.infoSub}>{"CNPS · " + employee.numCnps}</Text>
            ) : null}
          </View>
          <View style={S.infoBox}>
            <Text style={S.infoLabel}>{"Matricule · Poste"}</Text>
            <Text style={S.infoValue}>{matricule}</Text>
            <Text style={S.infoSub}>{employee?.departmentCode ?? "—"}</Text>
          </View>
        </View>

        {/* ── Table header ── */}
        <View style={S.tableHeaderRow}>
          <Text style={S.thLibelle}>{colLibelle}</Text>
          <Text style={S.thNum}>{colBase}</Text>
          <Text style={S.thTaux}>{colTaux}</Text>
          <Text style={S.thAmount}>{colGain}</Text>
          <Text style={S.thAmount}>{colRetenue}</Text>
        </View>

        {/* ── RÉMUNÉRATION BRUTE ── */}
        <View style={S.sectionRow}>
          <Text style={S.sectionText}>{rubricBrutLabel}</Text>
        </View>
        {earnings.map((l) => (
          <View key={l.id} style={S.dataRow}>
            <Text style={S.cellLibelle}>{l.libelle}</Text>
            <Text style={S.cellBase}>
              {l.base != null ? fmt(l.base) : "—"}
            </Text>
            <Text style={S.cellTaux}>{fmtTaux(l.taux)}</Text>
            <Text style={S.cellGain}>{fmt(l.montant)}</Text>
            <Text style={S.cellRetenue}>{""}</Text>
          </View>
        ))}
        <View style={S.subtotalRow}>
          <Text style={S.subtotalLabel}>{subtotalBrutLabel}</Text>
          <Text style={[S.subtotalValue, { width: 72 + 44 }]}>{fmt(entry.brut)}</Text>
          <Text style={S.subtotalEmpty}>{""}</Text>
        </View>

        {/* ── RETENUES SALARIALES ── */}
        <View style={[S.sectionRow, { marginTop: 8 }]}>
          <Text style={S.sectionText}>{rubricRetenuesLabel}</Text>
        </View>
        {deductions.map((l) => (
          <View key={l.id} style={S.dataRow}>
            <Text style={S.cellLibelle}>{l.libelle}</Text>
            <Text style={S.cellBase}>
              {l.base != null ? fmt(l.base) : "—"}
            </Text>
            <Text style={S.cellTaux}>{fmtTaux(l.taux)}</Text>
            <Text style={S.cellGain}>{""}</Text>
            <Text style={S.cellRetenue}>{fmt(l.montant)}</Text>
          </View>
        ))}
        <View style={S.subtotalRow}>
          <Text style={S.subtotalLabel}>{subtotalRetenuesLabel}</Text>
          <Text style={[S.subtotalValue, { width: 72 + 44 + 82 }]}>{fmt(entry.retenues)}</Text>
        </View>

        {/* ── NET À PAYER ── */}
        <View style={S.netRow}>
          <Text style={S.netLabel}>{netLabel}</Text>
          <Text style={S.netValue}>{fmt(entry.net)}</Text>
        </View>
      </Page>
    </Document>
  );
}
