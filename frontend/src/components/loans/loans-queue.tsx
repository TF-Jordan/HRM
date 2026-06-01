"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Check,
  Coins,
  Download,
  Loader2,
  Plus,
  X,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney, formatNumber } from "@/lib/format";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { LoanAdvanceResponse, LoanAdvanceStatus } from "@/server/ksm/modules/loans";

// ── Helpers ─────────────────────────────────────────────────────────────────

function num(v: number | string | null | undefined): number {
  if (v == null) return 0;
  const n = typeof v === "string" ? parseFloat(v) : v;
  return isNaN(n) ? 0 : n;
}

function fmtMoney(v: number | string | null | undefined, locale: "fr" | "en") {
  return formatMoney(num(v), { currency: "XAF", locale, withCurrency: false });
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
  return "personal";
}

function typeBadgeTone(type: string): "warning" | "success" | "info" | "violet" | "orange" {
  switch (type) {
    case "advance": return "warning";
    case "housing": return "success";
    case "vehicle": return "info";
    case "personal": return "violet";
    default: return "orange";
  }
}

type FilterKey = "all" | "pending" | "active" | "repaid";

// ── Reject modal ────────────────────────────────────────────────────────────

function RejectModal({
  onConfirm,
  onCancel,
  isPending,
}: {
  onConfirm: (motif: string) => void;
  onCancel: () => void;
  isPending: boolean;
}) {
  const t = useTranslations("loans.admin");
  const [motif, setMotif] = React.useState("");

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={onCancel}>
      <div
        className="relative w-full max-w-md rounded-2xl border border-line bg-white p-6 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <h3 className="font-display text-[16px] font-bold text-ink">{t("rejectTitle")}</h3>
        <p className="mt-1 text-[13px] text-ink-3">{t("rejectSubtitle")}</p>
        <textarea
          value={motif}
          onChange={(e) => setMotif(e.target.value)}
          placeholder={t("rejectPlaceholder")}
          rows={3}
          className="mt-4 w-full resize-none rounded-xl border border-line bg-white px-3.5 py-2.5 text-[14px] text-ink outline-none focus:border-orange-400 focus:ring-2 focus:ring-orange-100"
          required
        />
        <div className="mt-4 flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={onCancel}>
            {t("cancel")}
          </Button>
          <Button
            variant="danger"
            size="sm"
            disabled={!motif.trim() || isPending}
            onClick={() => onConfirm(motif.trim())}
          >
            {isPending ? <Loader2 size={13} className="animate-spin" /> : <X size={13} />}
            {t("confirmReject")}
          </Button>
        </div>
      </div>
    </div>
  );
}

// ── Main component ──────────────────────────────────────────────────────────

export function LoansQueue() {
  const t = useTranslations("loans");
  const tAdmin = useTranslations("loans.admin");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const canApprove = useCan("hrm:loan:approve");
  const [filter, setFilter] = React.useState<FilterKey>("all");
  const [rejectingId, setRejectingId] = React.useState<string | null>(null);

  const query = useQuery({
    queryKey: ["hrm", "loans", "admin"],
    queryFn: () => apiFetch<LoanAdvanceResponse[]>("/api/hrm/loans"),
    refetchInterval: 60_000,
  });

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const nameOf = React.useCallback(
    (employeeId: string) => {
      const e = employeesQuery.data?.find((x) => x.id === employeeId);
      return e?.actorDisplayName ?? e?.matricule ?? `${employeeId.slice(0, 8)}...`;
    },
    [employeesQuery.data],
  );

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["hrm", "loans"] });

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const approveM = useMutation({
    mutationFn: (id: string) =>
      apiFetch(`/api/hrm/loans/${id}/approve`, { method: "PUT" }),
    onSuccess: () => {
      toast.success(tAdmin("approveSuccess"));
      invalidate();
    },
    onError: handleError,
  });

  const rejectM = useMutation({
    mutationFn: ({ id, motif }: { id: string; motif: string }) =>
      apiFetch(`/api/hrm/loans/${id}/reject`, { method: "PUT", body: JSON.stringify({ motif }) }),
    onSuccess: () => {
      toast.success(tAdmin("rejectSuccess"));
      setRejectingId(null);
      invalidate();
    },
    onError: handleError,
  });

  const allLoans = query.data ?? [];

  // Filter
  const filtered = allLoans.filter((l) => {
    if (filter === "all") return true;
    if (filter === "pending") return l.status === "PENDING";
    if (filter === "active") return l.status === "IN_REPAYMENT" || l.status === "APPROVED";
    if (filter === "repaid") return l.status === "FULLY_REPAID";
    return true;
  });

  // KPIs
  const pendingLoans = allLoans.filter((l) => l.status === "PENDING");
  const activeLoans = allLoans.filter((l) => l.status === "IN_REPAYMENT" || l.status === "APPROVED");
  const totalOutstanding = activeLoans.reduce((s, l) => s + num(l.soldeRestant), 0);
  const totalMonthly = activeLoans.reduce((s, l) => s + num(l.mensualite), 0);
  const pendingAmount = pendingLoans.reduce((s, l) => s + num(l.montant), 0);

  return (
    <>
      {rejectingId && (
        <RejectModal
          isPending={rejectM.isPending}
          onCancel={() => setRejectingId(null)}
          onConfirm={(motif) => rejectM.mutate({ id: rejectingId, motif })}
        />
      )}

      <PageHeader
        ucBadge={tAdmin("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: tAdmin("title") }]}
        title={tAdmin("title")}
        subtitle={tAdmin("subtitle")}
        actions={
          <Button variant="secondary" size="sm">
            <Download size={13} /> {tAdmin("exportAmortization")}
          </Button>
        }
      />

      {/* KPI cards */}
      <div className="mb-5 grid grid-cols-2 gap-4 lg:grid-cols-4">
        {[
          {
            label: tAdmin("kpi.pending"),
            value: `${pendingLoans.length}`,
            sub: `${tAdmin("kpi.pendingSub")} · ${fmtMoney(pendingAmount, locale)} XAF`,
            tone: "amber" as const,
          },
          {
            label: tAdmin("kpi.active"),
            value: `${activeLoans.length}`,
            sub: `${fmtMoney(totalOutstanding, locale)} XAF ${tAdmin("kpi.activeSub")}`,
            tone: "orange" as const,
          },
          {
            label: tAdmin("kpi.monthly"),
            value: `${fmtMoney(totalMonthly, locale)}`,
            sub: `XAF · ${tAdmin("kpi.monthlySub")}`,
            tone: "green" as const,
          },
          {
            label: tAdmin("kpi.incidents"),
            value: "0%",
            sub: tAdmin("kpi.incidentsSub"),
            tone: "blue" as const,
          },
        ].map((k, i) => (
          <div key={i} className="rounded-2xl border border-line bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-semibold uppercase tracking-wide text-ink-3">{k.label}</span>
              <span
                className={cn(
                  "h-2 w-2 rounded-full",
                  k.tone === "amber" && "bg-amber-500",
                  k.tone === "orange" && "bg-orange-500",
                  k.tone === "green" && "bg-success-500",
                  k.tone === "blue" && "bg-info-500",
                )}
              />
            </div>
            <div className="mt-2 font-display text-[20px] font-extrabold tabular-nums text-ink">{k.value}</div>
            <div className="mt-0.5 text-[11px] text-ink-3">{k.sub}</div>
          </div>
        ))}
      </div>

      {/* Main table */}
      <Card>
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line px-5 py-4">
          <span className="font-display text-[15px] font-bold text-ink">{tAdmin("tableTitle")}</span>
          <div className="flex gap-1.5">
            {(["all", "pending", "active", "repaid"] as FilterKey[]).map((f) => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={cn(
                  "rounded-full px-3 py-1.5 text-[12px] font-semibold transition-colors",
                  filter === f
                    ? "bg-grad-orange text-white shadow-orange-brand"
                    : "border border-line bg-white text-ink-2 hover:bg-bg-soft",
                )}
              >
                {tAdmin(`filter.${f}`)}
              </button>
            ))}
          </div>
        </div>

        {query.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
          </div>
        ) : filtered.length === 0 ? (
          <div className="px-5 py-12 text-center text-[13px] text-ink-3">{tAdmin("noData")}</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-[13px]">
              <thead>
                <tr className="border-b border-line bg-bg-dim">
                  {[
                    tAdmin("col.ref"),
                    tAdmin("col.employee"),
                    tAdmin("col.type"),
                    tAdmin("col.amount"),
                    tAdmin("col.duration"),
                    tAdmin("col.monthly"),
                    tAdmin("col.remaining"),
                    tAdmin("col.progress"),
                    tAdmin("col.status"),
                    "",
                  ].map((h, i) => (
                    <th
                      key={i}
                      className={cn(
                        "whitespace-nowrap px-4 py-2.5 text-[11px] font-semibold uppercase tracking-wider text-ink-3",
                        i >= 3 && i <= 7 ? "text-right" : "text-left",
                      )}
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.map((l) => {
                  const total = num(l.montant);
                  const rem = num(l.soldeRestant);
                  const pct = total > 0 ? Math.round(((total - rem) / total) * 100) : 100;
                  const type = loanType(l.motif, l.nbEcheances);
                  return (
                    <tr
                      key={l.id}
                      className="border-b border-line/60 transition-colors hover:bg-orange-50/40"
                    >
                      <td className="px-4 py-3 font-mono text-[11px] text-ink-3">
                        {l.id.split("-").slice(0, 2).join("-")}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2.5">
                          <Avatar name={nameOf(l.employeeId)} size="sm" />
                          <span className="text-[13px] font-semibold text-ink">
                            {nameOf(l.employeeId)}
                          </span>
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <Badge tone={typeBadgeTone(type)} showDot={false}>
                          {t(`type.${type}`)}
                        </Badge>
                      </td>
                      <td className="px-4 py-3 text-right tabular-nums font-bold text-ink">
                        {fmtMoney(l.montant, locale)}
                      </td>
                      <td className="px-4 py-3 text-right text-ink-3 tabular-nums">
                        {t("table.months", { n: l.nbEcheances })}
                      </td>
                      <td className="px-4 py-3 text-right tabular-nums text-ink-2">
                        {fmtMoney(l.mensualite, locale)}
                      </td>
                      <td className={cn("px-4 py-3 text-right tabular-nums", rem > 0 ? "text-ink-2" : "text-ink-4")}>
                        {fmtMoney(l.soldeRestant, locale)}
                      </td>
                      <td className="px-4 py-3" style={{ minWidth: 120 }}>
                        <div className="flex items-center gap-2">
                          <div className="relative h-1.5 flex-1 overflow-hidden rounded-full bg-line">
                            <div
                              className="absolute inset-y-0 left-0 rounded-full bg-orange-500 transition-all"
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                          <span className="tabular-nums text-[11px] text-ink-3" style={{ minWidth: 32 }}>
                            {pct}%
                          </span>
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <Badge tone={statusTone(l.status)} showDot={false}>
                          {t(`status.${l.status}`)}
                        </Badge>
                      </td>
                      <td className="px-4 py-3 text-right" onClick={(e) => e.stopPropagation()}>
                        {canApprove && l.status === "PENDING" && (
                          <div className="flex justify-end gap-2">
                            <button
                              type="button"
                              aria-label={tAdmin("reject")}
                              onClick={() => setRejectingId(l.id)}
                              className="grid h-7 w-7 place-items-center rounded-md border border-line bg-white text-ink-3 hover:border-danger-300 hover:text-danger-600"
                            >
                              <X className="h-3.5 w-3.5" />
                            </button>
                            <button
                              type="button"
                              aria-label={tAdmin("approve")}
                              onClick={() => approveM.mutate(l.id)}
                              disabled={approveM.isPending}
                              className="grid h-7 w-7 place-items-center rounded-md bg-grad-orange text-white shadow-orange-brand"
                            >
                              {approveM.isPending ? (
                                <Loader2 className="h-3.5 w-3.5 animate-spin" />
                              ) : (
                                <Check className="h-3.5 w-3.5" />
                              )}
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </>
  );
}
