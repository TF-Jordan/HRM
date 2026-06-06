"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ArrowLeft,
  Coins,
  Download,
  Loader2,
  Plus,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { apiFetch } from "@/lib/api-client";
import { formatMoney, formatNumber } from "@/lib/format";
import { cn } from "@/lib/utils";
import type {
  LoanAdvanceResponse,
  LoanAdvanceStatus,
  LoanRepaymentResponse,
} from "@/server/ksm/modules/loans";

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

function addMonths(iso: string | null, n: number): Date | null {
  if (!iso) return null;
  const d = new Date(iso);
  if (isNaN(d.getTime())) return null;
  const r = new Date(d);
  r.setMonth(r.getMonth() + n);
  return r;
}

function fmtDate(d: Date | null, locale: "fr" | "en"): string {
  return d
    ? d.toLocaleDateString(locale === "fr" ? "fr-FR" : "en-US", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      })
    : "—";
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

type ScheduleRow = {
  n: number;
  dueDate: Date | null;
  installment: number;
  capitalRemaining: number;
  status: "paid" | "current" | "upcoming";
  /** True when the row reflects a real payroll deduction (vs a projected installment). */
  real: boolean;
};

/**
 * Amortization schedule. Installments already withheld are taken from the real repayment history
 * (actual date, amount and resulting balance from payroll); the remainder is projected from a
 * constant monthly deduction.
 */
function buildSchedule(loan: LoanAdvanceResponse, repayments: LoanRepaymentResponse[]): ScheduleRow[] {
  const total = num(loan.montant);
  const n = Math.max(1, loan.nbEcheances);
  const monthly = num(loan.mensualite) || total / n;
  const realPaid = Math.min(repayments.length, n);
  const basePaid = monthly > 0 ? Math.round((total - num(loan.soldeRestant)) / monthly) : 0;
  const paidInstallments =
    realPaid > 0
      ? realPaid
      : loan.status === "FULLY_REPAID"
        ? n
        : loan.status === "PENDING" || loan.status === "REJECTED"
          ? 0
          : Math.max(0, Math.min(basePaid, n));

  return Array.from({ length: n }, (_, idx) => {
    const i = idx + 1;
    const real = repayments[idx];
    if (real) {
      return {
        n: i,
        dueDate: real.recordedAt ? new Date(real.recordedAt) : addMonths(loan.dateDebut, i),
        installment: num(real.montant),
        capitalRemaining: num(real.soldeApres),
        status: "paid" as const,
        real: true,
      };
    }
    const isLast = i === n;
    const installment = isLast ? total - monthly * (n - 1) : monthly;
    const capitalRemaining = isLast ? 0 : Math.max(total - monthly * i, 0);
    const status: ScheduleRow["status"] =
      i <= paidInstallments
        ? "paid"
        : loan.status === "IN_REPAYMENT" && i === paidInstallments + 1
          ? "current"
          : "upcoming";
    return { n: i, dueDate: addMonths(loan.dateDebut, i), installment, capitalRemaining, status, real: false };
  });
}

function loanWorkflow(
  status: LoanAdvanceStatus,
  t: ReturnType<typeof useTranslations<"loans">>,
): WorkflowStep[] {
  if (status === "REJECTED") {
    return [
      { key: "requested", label: t("detail.workflow.requested"), state: "done" },
      { key: "rejected", label: t("detail.workflow.rejected"), state: "active" },
    ];
  }
  const idxByStatus: Record<LoanAdvanceStatus, number> = {
    PENDING: 0,
    APPROVED: 1,
    IN_REPAYMENT: 2,
    FULLY_REPAID: 3,
    REJECTED: 0,
  };
  const cur = idxByStatus[status];
  const mk = (key: string, label: string, pos: number): WorkflowStep => ({
    key,
    label,
    state: pos < cur ? "done" : pos === cur ? "active" : "pending",
  });
  return [
    mk("requested", t("detail.workflow.requested"), 0),
    mk("approved", t("detail.workflow.approved"), 1),
    mk("inRepayment", t("detail.workflow.inRepayment"), 2),
    mk("repaid", t("detail.workflow.repaid"), 3),
  ];
}

// ── Loan request form ─────────────────────────────────────────────────────────

function LoanRequestForm({
  onBack,
  salaireBase,
}: {
  onBack: () => void;
  salaireBase: number | null;
}) {
  const t = useTranslations("loans");
  const locale = useLocale() as "fr" | "en";
  const { session } = useSession();
  const queryClient = useQueryClient();

  const [montant, setMontant] = React.useState(0);
  const [nbEcheances, setNbEcheances] = React.useState(1);
  const [motif, setMotif] = React.useState("");

  const mensualite = nbEcheances > 0 ? Math.round(montant / nbEcheances) : 0;
  const cap = salaireBase != null ? Math.round(salaireBase / 3) : null;
  const capExceeded = cap != null && mensualite > cap;
  const firstInstallment = addMonths(new Date().toISOString(), 1);
  const endDate = addMonths(new Date().toISOString(), nbEcheances);

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
    if (montant <= 0 || nbEcheances <= 0 || !motif.trim() || capExceeded) return;
    mutation.mutate({ montant, nbEcheances, motif: motif.trim() });
  }

  return (
    <div>
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

      <div className="flex flex-col gap-6 lg:flex-row">
        <form onSubmit={handleSubmit} className="flex-1 space-y-6">
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

          <div className="rounded-2xl border border-line bg-white p-6 shadow-sm">
            <h3 className="mb-4 text-[13px] font-bold uppercase tracking-wide text-ink-3">
              {t("form.sectionFinancial")}
            </h3>
            <div className="grid grid-cols-2 gap-4">
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

          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={onBack}>
              {t("form.cancel")}
            </Button>
            <Button type="submit" disabled={mutation.isPending || capExceeded}>
              {mutation.isPending ? <Loader2 size={14} className="animate-spin" /> : <Plus size={14} />}
              {t("form.submit")}
            </Button>
          </div>
        </form>

        <div className="w-full shrink-0 space-y-4 lg:w-[300px]">
          <div
            className="rounded-2xl p-5"
            style={{ background: "linear-gradient(135deg, #1A150E 0%, #2D2520 100%)", color: "#fff" }}
          >
            <div className="mb-2 text-[11px] font-semibold uppercase tracking-widest" style={{ opacity: 0.7 }}>
              {t("form.simulation")}
            </div>
            <div className="font-display text-[24px] font-extrabold tabular-nums">
              {formatNumber(mensualite, locale)}{" "}
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
                <span className="tabular-nums">{formatNumber(montant, locale)}</span>
              </div>
              <div className="flex justify-between py-0.5">
                <span style={{ opacity: 0.7 }}>{t("form.simInstallments")}</span>
                <span className="tabular-nums">{nbEcheances} {t("form.installmentsSuffix")}</span>
              </div>
              <div className="flex justify-between py-0.5">
                <span style={{ opacity: 0.7 }}>{t("form.simFirstInstallment")}</span>
                <span className="tabular-nums">{fmtDate(firstInstallment, locale)}</span>
              </div>
              <div className="flex justify-between py-0.5">
                <span style={{ opacity: 0.7 }}>{t("form.simEndDate")}</span>
                <span className="tabular-nums">{fmtDate(endDate, locale)}</span>
              </div>
            </div>
          </div>

          {capExceeded ? (
            <div className="rounded-2xl border border-danger-300 bg-danger-50 p-4">
              <div className="text-[12.5px] text-danger-700">
                {t("form.capExceeded", { monthly: formatNumber(mensualite, locale), cap: formatNumber(cap ?? 0, locale) })}
              </div>
            </div>
          ) : (
            <div className="rounded-2xl border border-amber-300 bg-amber-50 p-4">
              <div className="text-[12.5px] text-ink-2">{t("form.salaryCapWarning")}</div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// ── Loan detail with amortization schedule ────────────────────────────────────

function LoanDetail({ loan, onBack }: { loan: LoanAdvanceResponse; onBack: () => void }) {
  const t = useTranslations("loans");
  const locale = useLocale() as "fr" | "en";

  const type = loanType(loan.motif, loan.nbEcheances);
  const total = num(loan.montant);
  const repaid = total - num(loan.soldeRestant);
  const progress = total > 0 ? Math.round((repaid / total) * 100) : 0;
  const endDate = addMonths(loan.dateDebut, loan.nbEcheances);

  const repaymentsQuery = useQuery({
    queryKey: ["hrm", "loans", loan.id, "repayments"],
    queryFn: () => apiFetch<LoanRepaymentResponse[]>(`/api/hrm/loans/${loan.id}/repayments`),
  });
  const repayments = React.useMemo(() => repaymentsQuery.data ?? [], [repaymentsQuery.data]);
  const hasReal = repayments.length > 0;
  const schedule = React.useMemo(() => buildSchedule(loan, repayments), [loan, repayments]);

  function handleExportCsv() {
    const header = [
      t("detail.schedule.n"),
      t("detail.schedule.dueDate"),
      t("detail.schedule.installment"),
      t("detail.schedule.capitalRemaining"),
      t("detail.schedule.status"),
    ];
    const rows = schedule.map((r) =>
      [
        r.n,
        fmtDate(r.dueDate, locale),
        Math.round(r.installment),
        Math.round(r.capitalRemaining),
        t(`detail.scheduleStatus.${r.status}`),
      ].join(";"),
    );
    const csv = ["\ufeff" + header.join(";"), ...rows].join("\n");
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `echeancier-${loan.id.slice(0, 8)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  const scheduleTone: Record<ScheduleRow["status"], "success" | "orange" | "gray"> = {
    paid: "success",
    current: "orange",
    upcoming: "gray",
  };

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-3">
        <button
          onClick={onBack}
          className="grid h-9 w-9 place-items-center rounded-xl border border-line bg-white text-ink-2 shadow-sm transition-colors hover:bg-bg-dim"
        >
          <ArrowLeft size={16} />
        </button>
        <div className="flex-1">
          <h1 className="font-display text-[22px] font-extrabold tracking-tight text-ink">
            {t(`type.${type}`)} · {fmtMoneyCompact(total)} XAF
          </h1>
          <p className="font-mono text-[12px] text-ink-3">
            {t("detail.reference")} {loan.id.split("-").slice(0, 2).join("-")}
          </p>
        </div>
        {loan.nbEcheances > 1 && (
          <Button variant="secondary" size="sm" onClick={handleExportCsv}>
            <Download size={14} /> {t("detail.schedule.exportCsv")}
          </Button>
        )}
      </div>

      {/* Workflow */}
      <div className="rounded-2xl border border-line bg-white p-6 shadow-sm">
        <WorkflowStepper steps={loanWorkflow(loan.status, t)} />
      </div>

      {/* Summary + progress */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="rounded-2xl border border-line bg-white p-5 shadow-sm lg:col-span-2">
          <div className="grid grid-cols-2 gap-x-6 gap-y-4 sm:grid-cols-3">
            <Stat label={t("detail.amount")} value={`${fmtMoney(loan.montant)} XAF`} />
            <Stat label={t("detail.monthly")} value={`${fmtMoney(loan.mensualite)} XAF`} />
            <Stat label={t("detail.remaining")} value={`${fmtMoney(loan.soldeRestant)} XAF`} accent />
            <Stat label={t("detail.duration")} value={t("table.months", { n: loan.nbEcheances })} />
            <Stat label={t("detail.startDate")} value={fmtDate(loan.dateDebut ? new Date(loan.dateDebut) : null, locale)} />
            <Stat label={t("detail.endDate")} value={fmtDate(endDate, locale)} />
          </div>
          {loan.motif && (
            <div className="mt-4 border-t border-line pt-4">
              <div className="text-[11px] font-semibold uppercase tracking-wide text-ink-3">{t("detail.reason")}</div>
              <div className="mt-1 text-[13px] text-ink-2">{loan.motif}</div>
            </div>
          )}
        </div>

        <div className="rounded-2xl border border-line bg-white p-5 shadow-sm">
          <div className="text-[11px] font-semibold uppercase tracking-wide text-ink-3">{t("detail.progress")}</div>
          <div className="mt-2 font-display text-[28px] font-extrabold tabular-nums text-ink">{progress}%</div>
          <div className="mb-3 mt-1 text-[11px] text-ink-3">
            {t("detail.repaidOf", { paid: fmtMoney(repaid), total: fmtMoney(total) })}
          </div>
          <div className="h-2.5 overflow-hidden rounded-full bg-bg-dim">
            <div className="h-full rounded-full bg-orange-500 transition-all" style={{ width: `${progress}%` }} />
          </div>
        </div>
      </div>

      {/* Amortization schedule */}
      <div className="rounded-2xl border border-line bg-white shadow-sm">
        <div className="border-b border-line px-6 py-4">
          <div className="font-display text-[15px] font-bold text-ink">{t("detail.schedule.title")}</div>
          <div className="text-[12px] text-ink-3">
            {hasReal ? t("detail.schedule.realNote") : t("detail.schedule.projectedNote")}
          </div>
        </div>
        <div className="overflow-x-auto">
          <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
            <thead>
              <tr className="border-b border-line bg-bg-dim">
                {[
                  t("detail.schedule.n"),
                  t("detail.schedule.dueDate"),
                  t("detail.schedule.installment"),
                  t("detail.schedule.capitalRemaining"),
                  t("detail.schedule.status"),
                ].map((h, i) => (
                  <th
                    key={i}
                    style={{
                      textAlign: i >= 2 && i <= 3 ? "right" : i === 4 ? "right" : "left",
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
              {schedule.map((r) => (
                <tr key={r.n} className="border-b border-line/60">
                  <td style={{ padding: "10px 16px" }} className="font-mono text-ink-3">{r.n}</td>
                  <td style={{ padding: "10px 16px" }} className="text-ink-2">{fmtDate(r.dueDate, locale)}</td>
                  <td style={{ padding: "10px 16px", textAlign: "right" }} className="tabular-nums font-semibold text-ink">
                    {fmtMoney(r.installment)}
                  </td>
                  <td style={{ padding: "10px 16px", textAlign: "right" }} className="tabular-nums text-ink-2">
                    {fmtMoney(r.capitalRemaining)}
                  </td>
                  <td style={{ padding: "10px 16px", textAlign: "right" }}>
                    <Badge tone={scheduleTone[r.status]} showDot={false}>
                      {t(`detail.scheduleStatus.${r.status}`)}
                    </Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function Stat({ label, value, accent }: { label: string; value: React.ReactNode; accent?: boolean }) {
  return (
    <div>
      <div className="text-[11px] font-semibold uppercase tracking-wide text-ink-3">{label}</div>
      <div className={cn("mt-1 tabular-nums text-[15px] font-bold", accent ? "text-orange-600" : "text-ink")}>
        {value}
      </div>
    </div>
  );
}

// ── Main component ──────────────────────────────────────────────────────────

type Filter = "all" | LoanAdvanceStatus;

export function MyLoans() {
  const t = useTranslations("loans");
  const locale = useLocale() as "fr" | "en";
  const [showForm, setShowForm] = React.useState(false);
  const [selectedLoan, setSelectedLoan] = React.useState<LoanAdvanceResponse | null>(null);
  const [filter, setFilter] = React.useState<Filter>("all");

  const query = useQuery({
    queryKey: ["hrm", "loans", "mine"],
    queryFn: () => apiFetch<LoansPageData>("/api/hrm/loans/mine"),
  });

  const allLoans = query.data?.loans ?? [];
  const salaireBase = query.data?.salaireBase ?? null;

  const activeLoan =
    allLoans.find((l) => l.status === "IN_REPAYMENT") ??
    allLoans.find((l) => l.status === "APPROVED") ??
    null;

  const pendingCount = allLoans.filter((l) => l.status === "PENDING").length;
  const totalBorrowed = allLoans.reduce((s, l) => s + num(l.montant), 0);
  const rejectedCount = allLoans.filter((l) => l.status === "REJECTED").length;
  const activeMonthly = allLoans
    .filter((l) => l.status === "IN_REPAYMENT" || l.status === "APPROVED")
    .reduce((s, l) => s + num(l.mensualite), 0);
  const capacity = salaireBase != null ? Math.max(0, Math.round(salaireBase / 3) - activeMonthly) : null;

  const filters: { key: Filter; label: string }[] = [
    { key: "all", label: t("filters.all") },
    { key: "PENDING", label: t("filters.PENDING") },
    { key: "IN_REPAYMENT", label: t("filters.IN_REPAYMENT") },
    { key: "FULLY_REPAID", label: t("filters.FULLY_REPAID") },
    { key: "REJECTED", label: t("filters.REJECTED") },
  ];
  const visible = allLoans.filter((l) => (filter === "all" ? true : l.status === filter));

  if (showForm) {
    return <LoanRequestForm onBack={() => setShowForm(false)} salaireBase={salaireBase} />;
  }
  if (selectedLoan) {
    return <LoanDetail loan={selectedLoan} onBack={() => setSelectedLoan(null)} />;
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
        <div
          className="pointer-events-none absolute"
          style={{
            top: -100, right: -100, width: 400, height: 400,
            background: "radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)",
          }}
        />

        {activeLoan ? (
          <div className="relative flex flex-wrap items-start justify-between gap-4">
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <span className="inline-flex items-center rounded-full px-2.5 py-0.5 text-[11px] font-semibold" style={{ background: "rgba(249,115,22,0.2)", color: "#FFB066" }}>
                  {t("hero.activeLoan")}
                </span>
                <span className="font-mono text-[11px]" style={{ opacity: 0.6 }}>
                  {activeLoan.id.split("-").slice(0, 2).join("-")}
                </span>
              </div>

              <div className="mt-2 font-display text-[28px] font-extrabold text-white">
                {t(`type.${loanType(activeLoan.motif, activeLoan.nbEcheances)}`)} · {fmtMoneyCompact(num(activeLoan.montant))} XAF
              </div>

              <div className="mt-1 text-[13px]" style={{ opacity: 0.7 }}>
                {activeLoan.dateDebut && (
                  <>{t("hero.startedIn", { date: new Date(activeLoan.dateDebut).toLocaleDateString(locale === "fr" ? "fr-FR" : "en-US", { month: "short", year: "numeric" }) })} · </>
                )}
                {t("hero.installments", { n: activeLoan.nbEcheances })}
              </div>

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
                      const totalAmt = num(activeLoan.montant);
                      const remaining = num(activeLoan.soldeRestant);
                      const monthly = num(activeLoan.mensualite);
                      const paidInstallments = monthly > 0 ? Math.round((totalAmt - remaining) / monthly) : 0;
                      return `${activeLoan.nbEcheances - paidInstallments} / ${activeLoan.nbEcheances}`;
                    })()}
                  </div>
                </div>
              </div>
            </div>

            <Button variant="secondary" size="sm" className="relative shrink-0" onClick={() => setSelectedLoan(activeLoan)}>
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
          { label: t("kpi.pending"), value: `${pendingCount}`, sub: t("kpi.pendingSub"), tone: "amber" as const },
          { label: t("kpi.capacity"), value: capacity != null ? fmtMoneyCompact(capacity) : "—", sub: t("kpi.capacitySub"), tone: "green" as const },
          { label: t("kpi.totalBorrowed"), value: fmtMoneyCompact(totalBorrowed), sub: t("kpi.totalBorrowedSub"), tone: "blue" as const },
          { label: t("kpi.incidents"), value: `${rejectedCount}`, sub: t("kpi.incidentsSub"), tone: "orange" as const },
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
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line px-6 py-4">
          <span className="font-display text-[15px] font-bold text-ink">{t("table.title")}</span>
          <div className="flex flex-wrap gap-1.5">
            {filters.map((f) => (
              <button
                key={f.key}
                type="button"
                onClick={() => setFilter(f.key)}
                className={cn(
                  "rounded-full px-3 py-1 text-[12px] font-semibold transition-colors",
                  filter === f.key
                    ? "bg-orange-500 text-white"
                    : "bg-bg-dim text-ink-2 hover:bg-orange-50 hover:text-orange-700",
                )}
              >
                {f.label}
              </button>
            ))}
          </div>
        </div>

        {visible.length === 0 ? (
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
                {visible.map((l) => {
                  const type = loanType(l.motif, l.nbEcheances);
                  return (
                    <tr
                      key={l.id}
                      onClick={() => setSelectedLoan(l)}
                      className="cursor-pointer border-b border-line/60 transition-colors hover:bg-orange-50/40"
                    >
                      <td style={{ padding: "12px 16px" }}>
                        <span className="font-mono text-[11px] text-ink-3">
                          {l.id.split("-").slice(0, 2).join("-")}
                        </span>
                      </td>
                      <td style={{ padding: "12px 16px" }}>
                        <Badge tone={typeBadgeTone(type)} showDot={false}>
                          {t(`type.${type}`)}
                        </Badge>
                      </td>
                      <td style={{ padding: "12px 16px", textAlign: "right" }} className="tabular-nums font-bold text-ink">
                        {fmtMoney(l.montant)}
                      </td>
                      <td style={{ padding: "12px 16px", textAlign: "right" }} className="text-ink-3">
                        {t("table.months", { n: l.nbEcheances })}
                      </td>
                      <td style={{ padding: "12px 16px", textAlign: "right" }} className="tabular-nums text-ink-2">
                        {fmtMoney(l.soldeRestant)}
                      </td>
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
