"use client";

import { useQuery } from "@tanstack/react-query";
import { pdf } from "@react-pdf/renderer";
import {
  ArrowLeft,
  ChevronRight,
  Download,
  FileText,
  Loader2,
  Printer,
  Send,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { apiFetch } from "@/lib/api-client";
import { formatDate, formatMoney } from "@/lib/format";
import { formatPeriodFr } from "@/lib/payroll-status";
import { cn } from "@/lib/utils";
import type { MyPayslipSummaryResponse, PayslipLineResponse } from "@/server/ksm/modules/payroll";
import type { OrganizationResponse } from "@/server/ksm/modules/organization";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import { PayslipPdfDocument, toDataUrl } from "./payslip-preview";

type OrgLegalData = {
  shortName?: string | null;
  longName?: string | null;
  businessRegistrationNumber?: string | null;
  taxNumber?: string | null;
  capitalShare?: number | string | null;
  ceoName?: string | null;
  email?: string | null;
  websiteUrl?: string | null;
  logoId?: string | null;
};

/** Génère et déclenche le téléchargement d'un bulletin PDF en réutilisant PayslipPdfDocument. */
async function downloadPayslipPdf(
  summary: MyPayslipSummaryResponse,
  lines: PayslipLineResponse[],
  employee: EmployeeResponse | null | undefined,
  org: OrgLegalData | null | undefined,
  sessionOrgName: string | undefined,
  locale: "fr" | "en",
  tPayroll: ReturnType<typeof useTranslations<"payroll">>,
) {
  const orgData = org ?? (await apiFetch<OrgLegalData>("/api/admin/organization").catch(() => null));

  const legalLine = [
    orgData?.businessRegistrationNumber ? `RCCM ${orgData.businessRegistrationNumber}` : null,
    orgData?.taxNumber ? `NIU ${orgData.taxNumber}` : null,
    orgData?.capitalShare ? `Capital ${Number(orgData.capitalShare).toLocaleString("fr-FR")} XAF` : null,
    orgData?.ceoName ? `DG ${orgData.ceoName}` : null,
  ].filter(Boolean).join(" · ");

  let logoDataUrl: string | undefined;
  if (orgData?.logoId) {
    try { logoDataUrl = await toDataUrl(`/api/files/${orgData.logoId}`); } catch { /* fallback */ }
  }

  const earnings = lines.filter((l) => l.type === "EARNING").sort((a, b) => a.ordreAffichage - b.ordreAffichage);
  const deductions = lines.filter((l) => l.type === "DEDUCTION").sort((a, b) => a.ordreAffichage - b.ordreAffichage);
  const totalRetenues = deductions.reduce((s, l) => s + Number(l.montant ?? 0), 0);

  // Adapter MyPayslipSummaryResponse → shapes attendues par PayslipPdfDocument
  const runForPdf = { id: summary.runId, periode: summary.periode, status: summary.runStatus } as Parameters<typeof PayslipPdfDocument>[0]["run"];
  const entryForPdf = {
    id: summary.entryId,
    employeeId: employee?.id ?? "",
    salaireBase: 0,
    brut: summary.brut,
    net: summary.net,
    cnpsEmploye: summary.cnpsEmploye,
    cnpsEmployeur: 0,
    irpp: summary.irpp,
    cac: summary.cac,
    cfc: summary.cfc,
    primes: 0,
    retenues: totalRetenues,
    avancesDeduites: 0,
    paymentStatus: summary.paymentStatus,
    paymentChannel: summary.paymentChannel ?? "—",
  } as Parameters<typeof PayslipPdfDocument>[0]["entry"];

  const blob = await pdf(
    <PayslipPdfDocument
      organizationName={orgData?.longName || orgData?.shortName || sessionOrgName || "—"}
      organizationRef={legalLine}
      logoDataUrl={logoDataUrl}
      run={runForPdf}
      entry={entryForPdf}
      employee={employee ?? null}
      earnings={earnings}
      deductions={deductions}
      locale={locale}
      headerTitle={tPayroll("payslip.headerTitle")}
      periodLabel={tPayroll("payslip.period")}
      refPrefix="PSL"
      employeeLabel={tPayroll("payslip.employee")}
      noEmployeeLabel={tPayroll("payslip.noEmployee")}
      rubricBrutLabel={tPayroll("payslip.rubricBrut")}
      rubricRetenuesLabel={tPayroll("payslip.rubricRetenues")}
      subtotalBrutLabel={tPayroll("payslip.subtotalBrut")}
      subtotalRetenuesLabel={tPayroll("payslip.subtotalRetenues")}
      netLabel={tPayroll("payslip.netLabel")}
      colLibelle={tPayroll("payslip.tableLibelle")}
      colBase={tPayroll("payslip.tableBase")}
      colTaux={tPayroll("payslip.tableTaux")}
      colGain={tPayroll("payslip.tableGain")}
      colRetenue={tPayroll("payslip.tableRetenue")}
    />,
  ).toBlob();

  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `bulletin-${summary.periode}-${employee?.matricule ?? summary.entryId.slice(0, 6)}.pdf`;
  link.click();
  URL.revokeObjectURL(url);
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

function num(v: number | string | null | undefined): number {
  if (v == null) return 0;
  const n = typeof v === "string" ? parseFloat(v) : v;
  return isNaN(n) ? 0 : n;
}

function fmtMoney(v: number | string | null | undefined) {
  return formatMoney(num(v), { currency: "XAF", locale: "fr" });
}

function periodeYear(periode: string): string {
  return periode?.split("-")[0] ?? "";
}

function availableYears(items: MyPayslipSummaryResponse[]): string[] {
  const years = [...new Set(items.map((p) => periodeYear(p.periode)))].sort((a, b) => b.localeCompare(a));
  return years;
}

// ─── YTD stats ────────────────────────────────────────────────────────────────

function computeYtd(items: MyPayslipSummaryResponse[], year: string) {
  const filtered = items.filter((p) => periodeYear(p.periode) === year);
  return {
    gross: filtered.reduce((s, p) => s + num(p.brut), 0),
    net: filtered.reduce((s, p) => s + num(p.net), 0),
    irpp: filtered.reduce((s, p) => s + num(p.irpp) + num(p.cac), 0),
    cnps: filtered.reduce((s, p) => s + num(p.cnpsEmploye), 0),
  };
}

// ─── Payslip detail view ──────────────────────────────────────────────────────

function PayslipDetail({
  summary,
  orgData,
  onBack,
}: {
  summary: MyPayslipSummaryResponse;
  orgData: OrganizationResponse | null | undefined;
  onBack: () => void;
}) {
  const t = useTranslations("payslips");
  const tPayroll = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const { session } = useSession();
  const [isDownloading, setIsDownloading] = React.useState(false);

  const employeeQuery = useQuery({
    queryKey: ["hrm", "profile", "me"],
    queryFn: () => apiFetch<EmployeeResponse>("/api/hrm/profile/me"),
    staleTime: 5 * 60_000,
  });

  const linesQuery = useQuery({
    queryKey: ["hrm", "payroll", "mine", summary.entryId, "payslip"],
    queryFn: () => apiFetch<PayslipLineResponse[]>(`/api/hrm/payroll/mine/${summary.entryId}/payslip`),
  });

  const lines = linesQuery.data ?? [];
  const earnings = lines.filter((l) => l.type === "EARNING").sort((a, b) => a.ordreAffichage - b.ordreAffichage);
  const deductions = lines.filter((l) => l.type === "DEDUCTION").sort((a, b) => a.ordreAffichage - b.ordreAffichage);
  const totalEarnings = earnings.reduce((s, l) => s + num(l.montant), 0);
  const totalDeductions = deductions.reduce((s, l) => s + num(l.montant), 0);

  const period = formatPeriodFr(summary.periode);
  const isPaid = summary.runStatus === "VALIDATED" || summary.runStatus === "PAID";
  const paymentDate = summary.paymentDate ? formatDate(summary.paymentDate) : null;
  const orgName = orgData?.longName ?? orgData?.shortName ?? session?.workspace?.organizationName ?? "—";
  const orgInitial = orgName[0]?.toUpperCase() ?? "R";
  const orgRef = [
    orgData?.businessRegistrationNumber ? `RCCM ${orgData.businessRegistrationNumber}` : null,
    orgData?.taxNumber ? `NIU ${orgData.taxNumber}` : null,
  ].filter(Boolean).join(" · ");

  const employeeName = session?.user.fullName ?? "—";
  const channel = summary.paymentChannel ?? "—";

  return (
    <div>
      {/* Breadcrumb nav */}
      <div className="mb-4 flex items-center gap-2">
        <Button variant="ghost" size="sm" onClick={onBack} className="gap-1.5 text-ink-3">
          <ArrowLeft size={14} /> {t("detail.back")}
        </Button>
        <span className="text-[12px] text-ink-4">
          {t("detail.breadcrumb", { period })}
        </span>
      </div>

      {/* Header */}
      <div className="mb-5 flex flex-wrap items-start gap-4">
        <div>
          <h1 className="font-display text-[26px] font-extrabold leading-tight text-ink">
            {t("detail.title", { period })}
          </h1>
          <div className="mt-1.5 flex items-center gap-2">
            <span className="font-mono text-[11px] text-ink-4">{summary.entryId}</span>
            <Badge tone={isPaid ? "success" : "warning"}>
              {isPaid && paymentDate ? t("detail.statusPaid", { date: paymentDate }) : t("detail.statusPending")}
            </Badge>
          </div>
        </div>
        <div className="ml-auto flex shrink-0 gap-2">
          <Button variant="secondary" size="sm">
            <Printer size={13} /> {t("detail.print")}
          </Button>
          <Button variant="secondary" size="sm">
            <Send size={13} /> {t("detail.sendEmail")}
          </Button>
          <Button
            size="sm"
            disabled={isDownloading || linesQuery.isLoading}
            onClick={async () => {
              if (!linesQuery.data) return;
              setIsDownloading(true);
              try {
                await downloadPayslipPdf(
                  summary,
                  linesQuery.data,
                  employeeQuery.data,
                  orgData,
                  session?.workspace?.organizationName,
                  locale,
                  tPayroll,
                );
              } catch {
                // silent — PDF errors are non-critical
              } finally {
                setIsDownloading(false);
              }
            }}
          >
            {isDownloading ? <Loader2 size={13} className="animate-spin" /> : <Download size={13} />}
            {t("detail.downloadPdf")}
          </Button>
        </div>
      </div>

      {/* Payslip document card */}
      <Card>
        <CardContent className="p-8">
          {/* Header: org + title */}
          <div className="mb-5 flex items-start justify-between border-b-2 border-ink pb-4">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-orange-500 font-display text-[18px] font-extrabold text-white">
                {orgInitial}
              </div>
              <div>
                <div className="font-display text-[17px] font-extrabold text-ink">{orgName}</div>
                {orgRef && <div className="text-[11px] text-ink-3">{orgRef}</div>}
              </div>
            </div>
            <div className="text-right">
              <div className="font-display text-[14px] font-bold uppercase tracking-wide text-ink">Bulletin de paie</div>
              <div className="font-mono text-[12px] text-ink-3">Période · {period}</div>
              <div className="mt-0.5 font-mono text-[11px] text-ink-4">N° {summary.entryId.split("-")[0]}</div>
            </div>
          </div>

          {/* Employee + payment info blocks */}
          <div className="mb-5 grid grid-cols-2 gap-3">
            <div className="rounded-xl bg-bg-dim p-4">
              <div className="mb-1.5 text-[10px] font-semibold uppercase tracking-wider text-ink-3">{t("detail.employer")}</div>
              <div className="text-[14px] font-semibold text-ink">{employeeName}</div>
              <div className="mt-0.5 text-[11px] text-ink-3">
                {session?.user.roles?.[0] ?? ""}{" · CDI"}
              </div>
            </div>
            <div className="rounded-xl bg-bg-dim p-4">
              <div className="mb-1.5 text-[10px] font-semibold uppercase tracking-wider text-ink-3">{t("detail.payment")}</div>
              <div className="text-[14px] font-semibold text-ink">{channel}</div>
              {paymentDate && (
                <div className="mt-0.5 text-[11px] text-ink-3">{t("detail.paidOn", { date: paymentDate })}</div>
              )}
            </div>
          </div>

          {/* Lines table */}
          {linesQuery.isLoading ? (
            <div className="flex h-32 items-center justify-center">
              <Loader2 className="animate-spin text-ink-3" size={22} />
            </div>
          ) : (
            <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
              <thead>
                <tr className="border-b border-line">
                  {[t("detail.labelCol"), t("detail.baseCol"), t("detail.rateCol"), t("detail.earnCol"), t("detail.deductCol")].map((h, i) => (
                    <th
                      key={i}
                      style={{
                        textAlign: i === 0 ? "left" : "right",
                        padding: "10px 8px",
                        fontSize: 11,
                        color: "var(--ink-3)",
                        textTransform: "uppercase",
                        letterSpacing: "0.05em",
                        width: i === 0 ? undefined : i >= 3 ? 130 : 90,
                      }}
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="tabular-nums">
                {/* Earnings section */}
                <tr className="bg-orange-50">
                  <td style={{ padding: "8px", fontWeight: 600 }} colSpan={5}>
                    {t("detail.sectionGross")}
                  </td>
                </tr>
                {earnings.map((l) => (
                  <tr key={l.id} className="border-b border-line/40">
                    <td style={{ padding: "8px" }}>{l.libelle}</td>
                    <td style={{ padding: "8px", textAlign: "right", color: "var(--ink-3)" }}>
                      {l.base != null ? fmtMoney(l.base) : "—"}
                    </td>
                    <td style={{ padding: "8px", textAlign: "right", color: "var(--ink-3)" }}>
                      {l.taux != null ? `${(num(l.taux) * 100).toFixed(1)}%` : "—"}
                    </td>
                    <td style={{ padding: "8px", textAlign: "right" }}>{fmtMoney(l.montant)}</td>
                    <td />
                  </tr>
                ))}
                <tr className="bg-bg-dim">
                  <td style={{ padding: "10px 8px", fontWeight: 700 }} colSpan={3}>{t("detail.grossLine")}</td>
                  <td style={{ padding: "10px 8px", textAlign: "right", fontWeight: 700 }}>{fmtMoney(summary.brut)}</td>
                  <td />
                </tr>

                {/* Deductions section */}
                <tr className="bg-orange-50">
                  <td style={{ padding: "8px", fontWeight: 600 }} colSpan={5}>
                    {t("detail.sectionDeductions")}
                  </td>
                </tr>
                {deductions.map((l) => (
                  <tr key={l.id} className="border-b border-line/40">
                    <td style={{ padding: "8px" }}>{l.libelle}</td>
                    <td style={{ padding: "8px", textAlign: "right", color: "var(--ink-3)" }}>
                      {l.base != null ? fmtMoney(l.base) : "—"}
                    </td>
                    <td style={{ padding: "8px", textAlign: "right", color: "var(--ink-3)" }}>
                      {l.taux != null ? `${(num(l.taux) * 100).toFixed(1)}%` : "—"}
                    </td>
                    <td />
                    <td style={{ padding: "8px", textAlign: "right" }}>{fmtMoney(l.montant)}</td>
                  </tr>
                ))}
                <tr className="bg-bg-dim">
                  <td style={{ padding: "10px 8px", fontWeight: 700 }} colSpan={4}>{t("detail.totalDeductions")}</td>
                  <td style={{ padding: "10px 8px", textAlign: "right", fontWeight: 700 }}>{fmtMoney(totalDeductions)}</td>
                </tr>

                {/* Net à payer */}
                <tr style={{ background: "var(--ink)", color: "#fff" }}>
                  <td style={{ padding: "14px 10px", fontWeight: 700, fontSize: 14 }} colSpan={4}>
                    {t("detail.netToPay")}
                  </td>
                  <td
                    style={{
                      padding: "14px 10px",
                      textAlign: "right",
                      fontFamily: "Inter Tight, sans-serif",
                      fontSize: 22,
                      fontWeight: 800,
                    }}
                  >
                    {fmtMoney(summary.net)}
                  </td>
                </tr>
              </tbody>
            </table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────

export function MyPayslips() {
  const t = useTranslations("payslips");
  const tPayroll = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const { session } = useSession();
  const [selectedEntry, setSelectedEntry] = React.useState<MyPayslipSummaryResponse | null>(null);
  const [isDownloading, setIsDownloading] = React.useState(false);
  const currentYear = new Date().getFullYear().toString();
  const [activeYear, setActiveYear] = React.useState(currentYear);

  const payslipsQuery = useQuery({
    queryKey: ["hrm", "payroll", "mine"],
    queryFn: () => apiFetch<MyPayslipSummaryResponse[]>("/api/hrm/payroll/mine"),
  });

  const orgQuery = useQuery({
    queryKey: ["admin", "organization"],
    queryFn: () => apiFetch<OrganizationResponse>("/api/admin/organization"),
    staleTime: 5 * 60_000,
  });

  const employeeQuery = useQuery({
    queryKey: ["hrm", "profile", "me"],
    queryFn: () => apiFetch<EmployeeResponse>("/api/hrm/profile/me"),
    staleTime: 5 * 60_000,
  });

  async function handleDownloadEntry(summary: MyPayslipSummaryResponse) {
    setIsDownloading(true);
    try {
      const lines = await apiFetch<PayslipLineResponse[]>(
        `/api/hrm/payroll/mine/${summary.entryId}/payslip`,
      );
      await downloadPayslipPdf(
        summary,
        lines,
        employeeQuery.data,
        orgQuery.data,
        session?.workspace?.organizationName,
        locale,
        tPayroll,
      );
    } catch {
      // silent
    } finally {
      setIsDownloading(false);
    }
  }

  const allPayslips = payslipsQuery.data ?? [];
  const years = availableYears(allPayslips);
  if (years.length > 0 && !years.includes(activeYear) && activeYear === currentYear) {
    // default to most recent available year
  }
  const filteredPayslips = allPayslips
    .filter((p) => periodeYear(p.periode) === activeYear)
    .sort((a, b) => b.periode.localeCompare(a.periode));

  const ytd = computeYtd(allPayslips, activeYear);

  // Latest payslip for hero (most recent VALIDATED or PAID)
  const latestPaid = [...allPayslips]
    .sort((a, b) => b.periode.localeCompare(a.periode))
    .find((p) => p.runStatus === "VALIDATED" || p.runStatus === "PAID");

  if (payslipsQuery.isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="animate-spin text-ink-3" size={28} />
      </div>
    );
  }

  if (selectedEntry) {
    return (
      <PayslipDetail
        summary={selectedEntry}
        orgData={orgQuery.data}
        onBack={() => setSelectedEntry(null)}
      />
    );
  }

  return (
    <div className="space-y-5">
      <PageHeader
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <div className="flex gap-2">
            <Button variant="secondary" size="sm">
              <Download size={13} /> {t("actions.downloadAll")}
            </Button>
            <Button
              size="sm"
              disabled={isDownloading || !latestPaid}
              onClick={() => latestPaid && handleDownloadEntry(latestPaid)}
            >
              {isDownloading ? <Loader2 size={13} className="animate-spin" /> : <Download size={13} />}
              {t("actions.downloadMyPayslip")}
            </Button>
          </div>
        }
      />

      {/* ── Hero: dernier bulletin reçu ───────────────────────────────── */}
      <div
        className="relative overflow-hidden rounded-[18px] p-6"
        style={{ background: "linear-gradient(135deg, #1A150E 0%, #2D2520 100%)", color: "#fff" }}
      >
        <div
          className="pointer-events-none absolute -right-24 -top-24 h-80 w-80 rounded-full"
          style={{ background: "radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)" }}
        />
        <div className="relative flex flex-wrap items-start gap-6">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
              <span
                className="rounded-full px-2.5 py-0.5 text-[11px] font-semibold"
                style={{ background: "rgba(249,115,22,0.2)", color: "#FFB066" }}
              >
                {t("hero.badge")}
              </span>
              {latestPaid && (
                <span className="font-mono text-[11px] opacity-60">{latestPaid.entryId.split("-")[0]}</span>
              )}
            </div>
            <div className="mt-2 font-display text-[28px] font-extrabold text-white">
              {latestPaid
                ? t("hero.title", { period: formatPeriodFr(latestPaid.periode) })
                : t("hero.noPending")}
            </div>
            {latestPaid && (
              <div className="mt-1 text-[13px] opacity-70">
                {latestPaid.paymentChannel ?? "—"}
              </div>
            )}

            {latestPaid && (
              <div className="mt-6 flex flex-wrap gap-8">
                <div>
                  <div className="text-[11px] uppercase tracking-wider opacity-60">{t("hero.estimatedGross")}</div>
                  <div className="tabular-nums font-display text-[24px] font-extrabold">
                    {fmtMoney(latestPaid.brut)}{" "}
                    <span className="text-[13px] opacity-60">{t("currency")}</span>
                  </div>
                </div>
                <div>
                  <div className="text-[11px] uppercase tracking-wider opacity-60">{t("hero.estimatedNet")}</div>
                  <div className="tabular-nums font-display text-[24px] font-extrabold" style={{ color: "#FFB066" }}>
                    {fmtMoney(latestPaid.net)}{" "}
                    <span className="text-[13px] opacity-60" style={{ color: "#fff" }}>{t("currency")}</span>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ── KPI annuels ──────────────────────────────────────────────── */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {[
          { label: t("ytd.cumulGross", { year: activeYear }), value: fmtMoney(ytd.gross), sub: t("currency"), tone: "orange" },
          { label: t("ytd.cumulNet", { year: activeYear }), value: fmtMoney(ytd.net), sub: t("currency"), tone: "green" },
          { label: t("ytd.irpp"), value: fmtMoney(ytd.irpp), sub: "YTD", tone: "red" },
          { label: t("ytd.cnps"), value: fmtMoney(ytd.cnps), sub: "YTD", tone: "blue" },
        ].map((k, i) => (
          <div key={i} className="rounded-2xl border border-line bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-semibold uppercase tracking-wide text-ink-3">{k.label}</span>
              <span
                className={cn(
                  "h-2 w-2 rounded-full",
                  k.tone === "orange" && "bg-orange-500",
                  k.tone === "green" && "bg-success-500",
                  k.tone === "red" && "bg-error-500",
                  k.tone === "blue" && "bg-info-500",
                )}
              />
            </div>
            <div className="mt-2 tabular-nums font-display text-[20px] font-extrabold text-ink">{k.value}</div>
            <div className="mt-0.5 text-[11px] text-ink-3">{k.sub}</div>
          </div>
        ))}
      </div>

      {/* ── Historique ───────────────────────────────────────────────── */}
      <Card>
        <div className="flex items-center justify-between border-b border-line px-6 py-4">
          <span className="font-display text-[15px] font-bold text-ink">{t("history.title")}</span>
          <div className="flex gap-1.5">
            {(years.length > 0 ? years : [currentYear]).map((y) => (
              <button
                key={y}
                onClick={() => setActiveYear(y)}
                className={cn(
                  "rounded-full px-3 py-1 text-[12px] font-semibold transition-colors",
                  activeYear === y
                    ? "bg-orange-500 text-white"
                    : "bg-bg-dim text-ink-2 hover:bg-orange-50 hover:text-orange-700",
                )}
              >
                {y}
              </button>
            ))}
          </div>
        </div>

        {filteredPayslips.length === 0 ? (
          <div className="flex h-32 items-center justify-center text-[13px] text-ink-3">
            {t("history.noData")}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
              <thead>
                <tr className="border-b border-line bg-bg-dim">
                  {[t("history.period"), t("history.ref"), t("history.gross"), t("history.net"), t("history.paymentDate"), t("history.channel"), ""].map((h, i) => (
                    <th
                      key={i}
                      style={{
                        textAlign: i <= 1 || i === 6 ? "left" : "right",
                        padding: "10px 16px",
                        fontSize: 11,
                        fontWeight: 600,
                        color: "var(--ink-3)",
                        textTransform: "uppercase",
                        letterSpacing: "0.05em",
                        whiteSpace: "nowrap",
                      }}
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filteredPayslips.map((p) => {
                  const isPaid = p.runStatus === "VALIDATED" || p.runStatus === "PAID";
                  const payDate = p.paymentDate ? formatDate(p.paymentDate) : "—";
                  return (
                    <tr
                      key={p.entryId}
                      onClick={() => setSelectedEntry(p)}
                      className="cursor-pointer border-b border-line/60 transition-colors hover:bg-orange-50/40"
                    >
                      <td style={{ padding: "12px 16px" }}>
                        <div className="flex items-center gap-3">
                          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-orange-100 text-orange-600">
                            <FileText size={15} />
                          </div>
                          <div>
                            <div className="text-[13.5px] font-semibold text-ink">
                              {formatPeriodFr(p.periode)}
                            </div>
                            {isPaid && (
                              <div className="text-[11px] text-success-600">{t("history.paid")}</div>
                            )}
                          </div>
                        </div>
                      </td>
                      <td style={{ padding: "12px 16px" }}>
                        <span className="font-mono text-[11px] text-ink-3">{p.entryId.split("-")[0]}</span>
                      </td>
                      <td style={{ padding: "12px 16px", textAlign: "right" }} className="tabular-nums text-ink-2">
                        {fmtMoney(p.brut)}
                      </td>
                      <td style={{ padding: "12px 16px", textAlign: "right" }} className="tabular-nums font-semibold text-orange-700">
                        {fmtMoney(p.net)}
                      </td>
                      <td style={{ padding: "12px 16px", textAlign: "right" }} className="text-ink-3">
                        {payDate}
                      </td>
                      <td style={{ padding: "12px 16px", textAlign: "right" }}>
                        {p.paymentChannel && (
                          <span className="rounded-full bg-bg-dim px-2.5 py-1 text-[11px] font-medium text-ink-2">
                            {p.paymentChannel}
                          </span>
                        )}
                      </td>
                      <td style={{ padding: "12px 16px" }}>
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            className="flex h-7 w-7 items-center justify-center rounded-lg border border-line bg-white text-ink-3 transition-colors hover:border-orange-300 hover:text-orange-600 disabled:opacity-40"
                            disabled={isDownloading}
                            onClick={(e) => { e.stopPropagation(); handleDownloadEntry(p); }}
                          >
                            <Download size={12} />
                          </button>
                          <button className="flex h-7 w-7 items-center justify-center rounded-lg border border-line bg-white text-ink-3 transition-colors hover:border-orange-300 hover:text-orange-600">
                            <ChevronRight size={12} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
