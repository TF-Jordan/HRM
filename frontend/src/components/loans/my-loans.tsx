"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ArrowLeft,
  Coins,
  Download,
  Loader2,
  Plus,
} from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { apiFetch } from "@/lib/api-client";
import { formatMoney, formatNumber } from "@/lib/format";
import { cn } from "@/lib/utils";
import type { LoanAdvanceResponse, LoanAdvanceStatus } from "@/server/ksm/modules/loans";

type LoansPageData = {
  loans: LoanAdvanceResponse[];
  salaireBase: number | null;
};

// ── Helpers ─────────────────────────────────────────────────────────────────

function num(v: number | string | null | undefined): number {
  if (v == null) return 0;
  const n = typeof v === "string" ? parseFloat(v) : v;
  return isNaN(n) ? 0 : n;
}

function fmtMoney(v: number | string | null | undefined) {
  return formatMoney(num(v), { currency: "XAF", locale: "fr", withCurrency: false });
}

function fmtMoneyCompact(v: number) {
  if (v >= 1_000_000) return `${(v / 1_000_000).toFixed(v % 1_000_000 === 0 ? 0 : 1).replace(".", ",")}M`;
  if (v >= 1_000) return `${Math.round(v / 1_000)}K`;
  return formatNumber(v, "fr");
}

function statusTone(s: LoanAdvanceStatus): "warning" | "info" | "orange" | "success" | "danger" {
  switch (s) {
    case "PENDING": return "warning";
    case "APPROVED": return "info";
    case "IN_REPAYMENT": return "orange";
    case "FULLY_REPAID": return "success";
    case "REJECTED": return "danger";
  }
}

function loanType(motif: string | null | undefined, nbEcheances: number): string {
  if (nbEcheances <= 1) return "advance";
  if (!motif) return "personal";
  const m = motif.toLowerCase();
  if (m.includes("logement") || m.includes("housing")) return "housing";
  if (m.includes("véhicule") || m.includes("vehicle") || m.includes("voiture")) return "vehicle";
  if (m.includes("formation") || m.includes("training")) return "training";
  if (m.includes("personnel") || m.includes("personal")) return "personal";
  return "personal";
}

function typeBadgeTone(type: string): "warning" | "orange" {
  return type === "advance" ? "warning" : "orange";
}

// ── Loan request form (full-page, matches FormRequestLoanAdvance design) ───

function LoanRequestForm({ onBack }: { onBack: () => void }) {
  const t = useTranslations("loans");
  const { session } = useSession();
  const queryClient = useQueryClient();

  const [montant, setMontant] = React.useState(0);
  const [nbEcheances, setNbEcheances] = React.useState(1);
  const [motif, setMotif] = React.useState("");

  const mensualite = nbEcheances > 0 ? Math.round(montant / nbEcheances) : 0;

  const mutation = useMutation({
    mutationFn: (body: { montant: number; nbEcheances: number; motif: string }) =>
      apiFetch("/api/hrm/loans/mine", { method: "POST", body: JSON.stringify(body) }),
    onSuccess: () => {
      toast.success(t("form.success"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "loans", "mine"] });
      onBack();
    },
    onError: () => {
      toast.error(t("form.error"));
    },
  });

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (montant <= 0 || nbEcheances <= 0 || !motif.trim()) return;
    mutation.mutate({ montant, nbEcheances, motif: motif.trim() });
  }

  return (
    <div>
      {/* Header */}
      <div className="mb-6 flex items-center gap-3">
        <button
          onClick={onBack}
          className="grid h-9 w-9 place-items-center rounded-xl border border-line bg-white text-ink-2 shadow-sm transition-colors hover:bg-bg-dim"
        >
          <ArrowLeft size={16} />
        </button>
        <div>
          <h1 className="font-display text-[22px] font-extrabold tracking-tight text-ink">
            {t("form.title")}
          </h1>
          <p className="text-[13px] text-ink-3">{t("form.subtitle")}</p>
        </div>
      </div>

      <div className="flex gap-6">
        {/* Main form */}
        <form onSubmit={handleSubmit} className="flex-1 space-y-6">
          {/* Section: Demandeur */}
          <div className="rounded-2xl border border-line bg-white p-6 shadow-sm">
            <h3 className="mb-4 text-[13px] font-bold uppercase tracking-wide text-ink-3">
              {t("form.sectionApplicant")}
            </h3>
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
                {t("form.employee")}
              </label>
              <div className="flex items-center gap-3 rounded-xl border border-line bg-bg-dim px-4 py-3">
                <div className="grid h-9 w-9 place-items-center rounded-lg bg-orange-100 font-display text-[14px] font-bold text-orange-600">
                  {session?.user.fullName?.charAt(0) ?? "?"}
                </div>
                <div>
                  <div className="text-[13px] font-semibold text-ink">{session?.user.fullName}</div>
                  <div className="text-[11px] text-ink-3">{session?.user.roles[0] ?? ""}</div>
                </div>
              </div>
            </div>
          </div>

          {/* Section: Conditions financières */}
          <div className="rounded-2xl border border-line bg-white p-6 shadow-sm">
            <h3 className="mb-4 text-[13px] font-bold uppercase tracking-wide text-ink-3">
              {t("form.sectionFinancial")}
            </h3>
            <div className="grid grid-cols-2 gap-4">
              {/* Montant */}
              <div>
                <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
                  {t("form.amount")}
                </label>
                <div className="relative">
                  <input
                    type="number"
                    min={0}
                    value={montant || ""}
                    onChange={(e) => setMontant(Number(e.target.value) || 0)}
                    className="w-full rounded-xl border border-line bg-white px-3.5 py-2.5 pr-14 text-[14px] text-ink outline-none transition-colors focus:border-orange-400 focus:ring-2 focus:ring-orange-100"
                    required
                  />
                  <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[12px] font-semibold text-ink-3">
                    {t("form.amountSuffix")}
                  </span>
                </div>
              </div>

              {/* Nb échéances */}
              <div>
                <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
                  {t("form.installments")}
                </label>
                <div className="relative">
                  <input
                    type="number"
                    min={1}
                    max={48}
                    value={nbEcheances}
                    onChange={(e) => setNbEcheances(Number(e.target.value) || 1)}
                    className="w-full rounded-xl border border-line bg-white px-3.5 py-2.5 pr-14 text-[14px] text-ink outline-none transition-colors focus:border-orange-400 focus:ring-2 focus:ring-orange-100"
                    required
                  />
                  <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[12px] font-semibold text-ink-3">
                    {t("form.installmentsSuffix")}
                  </span>
                </div>
                <p className="mt-1 text-[11px] text-ink-3">{t("form.installmentsHint")}</p>
              </div>

              {/* Motif */}
              <div className="col-span-2">
                <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
                  {t("form.reason")}
                </label>
                <textarea
                  value={motif}
                  onChange={(e) => setMotif(e.target.value)}
                  placeholder={t("form.reasonPlaceholder")}
                  rows={4}
                  className="w-full resize-none rounded-xl border border-line bg-white px-3.5 py-2.5 text-[14px] text-ink outline-none transition-colors focus:border-orange-400 focus:ring-2 focus:ring-orange-100"
                  required
                />
                <p className="mt-1 text-[11px] text-ink-3">{t("form.reasonHint")}</p>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={onBack}>
              {t("form.cancel")}
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? <Loader2 size={14} className="animate-spin" /> : <Plus size={14} />}
              {t("form.submit")}
            </Button>
          </div>
        </form>

        {/* Sidebar */}
        <div className="w-[300px] shrink-0 space-y-4">
          {/* Simulation card (dark) */}
          <div
            className="rounded-2xl p-5"
            style={{ background: "linear-gradient(135deg, #1A150E 0%, #2D2520 100%)", color: "#fff" }}
          >
            <div className="mb-2 text-[11px] font-semibold uppercase tracking-widest" style={{ opacity: 0.7 }}>
              {t("form.simulation")}
            </div>
            <div className="font-display text-[24px] font-extrabold tabular-nums">
              {formatNumber(mensualite, "fr")}{" "}
              <span className="text-[12px] font-medium" style={{ opacity: 0.6 }}>
                {t("form.simMonthly")}
              </span>
            </div>
            <div className="mt-1 text-[12px]" style={{ opacity: 0.7 }}>
              {t("form.simMonthlyLabel")}
            </div>
            <div className="mt-4 space-y-1 text-[12px]">
              <div className="flex justify-between py-0.5">
                <span style={{ opacity: 0.7 }}>{t("form.simCapital")}</span>
                <span className="tabular-nums">{formatNumber(montant, "fr")}</span>
              </div>
              <div className="flex justify-between py-0.5">
                <span style={{ opacity: 0.7 }}>{t("form.simInstallments")}</span>
                <span className="tabular-nums">{nbEcheances} {t("form.installmentsSuffix")}</span>
              </div>
              <div className="flex justify-between py-0.5">
                <span style={{ opacity: 0.7 }}>{t("form.simRate")}</span>
                <span className="tabular-nums">{t("form.simRateValue")}</span>
              </div>
            </div>
          </div>

          {/* Warning card */}
          <div className="rounded-2xl border border-amber-300 bg-amber-50 p-4">
            <div className="text-[12.5px] text-ink-2">
              {t("form.salaryCapWarning")}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Main component ──────────────────────────────────────────────────────────

export function MyLoans() {
  const t = useTranslations("loans");
  const [showForm, setShowForm] = React.useState(false);

  const query = useQuery({
    queryKey: ["hrm", "loans", "mine"],
    queryFn: () => apiFetch<LoansPageData>("/api/hrm/loans/mine"),
  });

  const allLoans = query.data?.loans ?? [];
  const salaireBase = query.data?.salaireBase ?? null;

  // Find the main active loan (IN_REPAYMENT first, then APPROVED)
  const activeLoan =
    allLoans.find((l) => l.status === "IN_REPAYMENT") ??
    allLoans.find((l) => l.status === "APPROVED") ??
    null;

  // KPIs
  const pendingCount = allLoans.filter((l) => l.status === "PENDING").length;
  const totalBorrowed = allLoans.reduce((s, l) => s + num(l.montant), 0);
  const rejectedCount = allLoans.filter((l) => l.status === "REJECTED").length;
  const activeOutstanding = allLoans
    .filter((l) => l.status === "IN_REPAYMENT" || l.status === "APPROVED")
    .reduce((s, l) => s + num(l.soldeRestant), 0);
  const capacity = salaireBase != null ? Math.max(0, salaireBase - activeOutstanding) : null;

  if (showForm) {
    return <LoanRequestForm onBack={() => setShowForm(false)} />;
  }

  if (query.isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="animate-spin text-ink-3" size={28} />
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <PageHeader
        ucBadge={t("title")}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button size="sm" onClick={() => setShowForm(true)}>
            <Plus size={13} /> {t("actions.newRequest")}
          </Button>
        }
      />

      {/* ── Active loan hero card ───────────────────────────────────── */}
      <div
        className="relative overflow-hidden rounded-2xl p-6 shadow-lg"
        style={{ background: "linear-gradient(135deg, #1A150E 0%, #2D2520 100%)", color: "#fff" }}
      >
        {/* Radial glow */}
        <div
          className="pointer-events-none absolute"
          style={{
            top: -100, right: -100, width: 400, height: 400,
            background: "radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)",
          }}
        />

        {activeLoan ? (
          <div className="relative flex items-start justify-between">
            <div className="flex-1">
              {/* Badge + ref */}
              <div className="flex items-center gap-2">
                <span className="inline-flex items-center rounded-full px-2.5 py-0.5 text-[11px] font-semibold" style={{ background: "rgba(249,115,22,0.2)", color: "#FFB066" }}>
                  {t("hero.activeLoan")}
                </span>
                <span className="font-mono text-[11px]" style={{ opacity: 0.6 }}>
                  {activeLoan.id.split("-").slice(0, 2).join("-")}
                </span>
              </div>

              {/* Title */}
              <div className="mt-2 font-display text-[28px] font-extrabold text-white">
                {t(`type.${loanType(activeLoan.motif, activeLoan.nbEcheances)}`)} · {fmtMoneyCompact(num(activeLoan.montant))} XAF
              </div>

              {/* Subtitle */}
              <div className="mt-1 text-[13px]" style={{ opacity: 0.7 }}>
                {activeLoan.dateDebut && (
                  <>{t("hero.startedIn", { date: new Date(activeLoan.dateDebut).toLocaleDateString("fr-FR", { month: "short", year: "numeric" }) })} · </>
                )}
                {t("hero.installments", { n: activeLoan.nbEcheances })} · {t("hero.rate")}
              </div>

              {/* Progress bar */}
              <div className="mb-5 mt-6">
                <div className="mb-2 flex items-center justify-between">
                  <span className="text-[12px]" style={{ opacity: 0.7 }}>{t("hero.repaid")}</span>
                  <span className="text-[13px] font-bold tabular-nums">
                    {fmtMoney(num(activeLoan.montant) - num(activeLoan.soldeRestant))} / {fmtMoney(activeLoan.montant)} XAF
                  </span>
                </div>
                <div className="h-2 overflow-hidden rounded-full" style={{ background: "rgba(255,255,255,0.2)" }}>
                  <div
                    className="h-full rounded-full transition-all"
                    style={{
                      width: `${num(activeLoan.montant) > 0 ? Math.round(((num(activeLoan.montant) - num(activeLoan.soldeRestant)) / num(activeLoan.montant)) * 100) : 0}%`,
                      background: "#FFB066",
                    }}
                  />
                </div>
              </div>

              {/* Metrics row */}
              <div className="flex flex-wrap gap-8">
                <div>
                  <div className="text-[11px] uppercase tracking-widest" style={{ opacity: 0.6 }}>{t("hero.monthly")}</div>
                  <div className="mt-1 font-display text-[20px] font-extrabold tabular-nums">
                    {fmtMoney(activeLoan.mensualite)} <span className="text-[12px] font-medium" style={{ opacity: 0.6 }}>XAF</span>
                  </div>
                </div>
                <div>
                  <div className="text-[11px] uppercase tracking-widest" style={{ opacity: 0.6 }}>{t("hero.remainingDebt")}</div>
                  <div className="mt-1 font-display text-[20px] font-extrabold tabular-nums" style={{ color: "#FFB066" }}>
                    {fmtMoney(activeLoan.soldeRestant)} <span className="text-[12px] font-medium" style={{ opacity: 0.6, color: "#fff" }}>XAF</span>
                  </div>
                </div>
                <div>
                  <div className="text-[11px] uppercase tracking-widest" style={{ opacity: 0.6 }}>{t("hero.remainingInstallments")}</div>
                  <div className="mt-1 font-display text-[20px] font-extrabold tabular-nums">
                    {(() => {
                      const total = num(activeLoan.montant);
                      const remaining = num(activeLoan.soldeRestant);
                      const monthly = num(activeLoan.mensualite);
                      const paidInstallments = monthly > 0 ? Math.round((total - remaining) / monthly) : 0;
                      return `${activeLoan.nbEcheances - paidInstallments} / ${activeLoan.nbEcheances}`;
                    })()}
                  </div>
                </div>
              </div>
            </div>

            <Button variant="secondary" size="sm" className="relative shrink-0">
              <Download size={14} /> {t("hero.amortizationTable")}
            </Button>
          </div>
        ) : (
          <div className="relative py-4 text-center">
            <Coins size={32} className="mx-auto mb-3" style={{ opacity: 0.4 }} />
            <div className="font-display text-[18px] font-bold">{t("hero.noActiveLoan")}</div>
            <div className="mt-1 text-[13px]" style={{ opacity: 0.6 }}>{t("hero.noActiveLoanSub")}</div>
          </div>
        )}
      </div>

      {/* ── KPI cards ──────────────────────────────────────────────── */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {[
          {
            label: t("kpi.pending"),
            value: `${pendingCount}`,
            sub: t("kpi.pendingSub"),
            tone: "amber" as const,
          },
          {
            label: t("kpi.capacity"),
            value: capacity != null ? `${fmtMoneyCompact(capacity)}` : "—",
            sub: t("kpi.capacitySub"),
            tone: "green" as const,
          },
          {
            label: t("kpi.totalBorrowed"),
            value: fmtMoneyCompact(totalBorrowed),
            sub: t("kpi.totalBorrowedSub"),
            tone: "blue" as const,
          },
          {
            label: t("kpi.incidents"),
            value: `${rejectedCount}`,
            sub: t("kpi.incidentsSub"),
            tone: "orange" as const,
          },
        ].map((k, i) => (
          <div key={i} className="rounded-2xl border border-line bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-semibold uppercase tracking-wide text-ink-3">{k.label}</span>
              <span
                className={cn(
                  "h-2 w-2 rounded-full",
                  k.tone === "amber" && "bg-amber-500",
                  k.tone === "green" && "bg-success-500",
                  k.tone === "blue" && "bg-info-500",
                  k.tone === "orange" && "bg-orange-500",
                )}
              />
            </div>
            <div className="mt-2 font-display text-[20px] font-extrabold tabular-nums text-ink">{k.value}</div>
            <div className="mt-0.5 text-[11px] text-ink-3">{k.sub}</div>
          </div>
        ))}
      </div>

      {/* ── History table ────────────────────────────────────────────── */}
      <div className="rounded-2xl border border-line bg-white shadow-sm">
        <div className="border-b border-line px-6 py-4">
          <span className="font-display text-[15px] font-bold text-ink">{t("table.title")}</span>
        </div>

        {allLoans.length === 0 ? (
          <div className="flex h-32 items-center justify-center text-[13px] text-ink-3">
            {t("table.noData")}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
              <thead>
                <tr className="border-b border-line bg-bg-dim">
                  {[
                    t("table.ref"),
                    t("table.type"),
                    t("table.amount"),
                    t("table.duration"),
                    t("table.remaining"),
                    t("table.status"),
                  ].map((h, i) => (
                    <th
                      key={i}
                      style={{
                        textAlign: i <= 1 ? "left" : "right",
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
                {allLoans.map((l) => {
                  const type = loanType(l.motif, l.nbEcheances);
                  return (
                    <tr
                      key={l.id}
                      className="border-b border-line/60 transition-colors hover:bg-orange-50/40"
                    >
                      {/* Ref */}
                      <td style={{ padding: "12px 16px" }}>
                        <span className="font-mono text-[11px] text-ink-3">
                          {l.id.split("-").slice(0, 2).join("-")}
                        </span>
                      </td>
                      {/* Type */}
                      <td style={{ padding: "12px 16px" }}>
                        <Badge tone={typeBadgeTone(type)} showDot={false}>
                          {t(`type.${type}`)}
                        </Badge>
                      </td>
                      {/* Amount */}
                      <td
                        style={{ padding: "12px 16px", textAlign: "right" }}
                        className="tabular-nums font-bold text-ink"
                      >
                        {fmtMoney(l.montant)}
                      </td>
                      {/* Duration */}
                      <td
                        style={{ padding: "12px 16px", textAlign: "right" }}
                        className="text-ink-3"
                      >
                        {t("table.months", { n: l.nbEcheances })}
                      </td>
                      {/* Remaining */}
                      <td
                        style={{ padding: "12px 16px", textAlign: "right" }}
                        className="tabular-nums text-ink-2"
                      >
                        {fmtMoney(l.soldeRestant)}
                      </td>
                      {/* Status */}
                      <td style={{ padding: "12px 16px", textAlign: "right" }}>
                        <Badge tone={statusTone(l.status)} showDot={false}>
                          {t(`status.${l.status}`)}
                        </Badge>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
