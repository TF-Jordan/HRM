"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { Check, X, Loader2, Plus } from "lucide-react";
import { toast } from "sonner";
import { useAllLoans, useApproveLoan, useRejectLoan } from "@/hooks/modules/useLoans";
import { useFormat } from "@/hooks/useFormat";
import { Link } from "@/i18n/navigation";
import { PageHeader } from "@/components/shell/PageHeader";
import { StatCard } from "@/components/ui-tokens/StatCard";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge, type BadgeProps } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { cn } from "@/lib/utils";
import type { LoanAdvanceStatus, LoanAdvanceWithEmployee } from "@/lib/types/hrm/loan-advance";

const STATUS_TONE: Record<LoanAdvanceStatus, BadgeProps["tone"]> = {
  PENDING: "amber",
  IN_REPAYMENT: "blue",
  FULLY_REPAID: "green",
  REJECTED: "red",
};
const STATUS_HEX: Record<string, string> = {
  PENDING: "#f59e0b",
  IN_REPAYMENT: "#3b82f6",
  FULLY_REPAID: "#10b981",
};

export function ApproveLoansClient() {
  const t = useTranslations("accounting.loansAdmin");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const { data, isLoading } = useAllLoans();
  const approve = useApproveLoan();
  const reject = useRejectLoan();
  const [statusFilter, setStatusFilter] = React.useState<LoanAdvanceStatus | null>(null);
  const [rejectingId, setRejectingId] = React.useState<string | null>(null);
  const form = useForm<{ motif: string }>({ defaultValues: { motif: "" } });

  const loans = React.useMemo<LoanAdvanceWithEmployee[]>(() => data ?? [], [data]);
  const filtered = statusFilter ? loans.filter((l) => l.status === statusFilter) : loans;

  const stats = React.useMemo(() => {
    const pending = loans.filter((l) => l.status === "PENDING");
    const repaying = loans.filter((l) => l.status === "IN_REPAYMENT");
    const pendingSum = pending.reduce((s, l) => s + Number(l.montant), 0);
    const encours = repaying.reduce((s, l) => s + Number(l.soldeRestant), 0);
    const monthly = repaying.reduce((s, l) => s + Number(l.mensualite), 0);
    const byStatus: Record<string, number> = {};
    for (const l of loans) byStatus[l.status] = (byStatus[l.status] ?? 0) + 1;
    return { pendingCount: pending.length, pendingSum, activeCount: repaying.length, encours, monthly, byStatus };
  }, [loans]);

  const donut = React.useMemo(() => {
    const order = ["IN_REPAYMENT", "FULLY_REPAID", "PENDING"];
    const total = loans.length || 1;
    let acc = 0;
    const parts: string[] = [];
    for (const s of order) {
      const n = stats.byStatus[s] ?? 0;
      if (n === 0) continue;
      const start = (acc / total) * 100;
      acc += n;
      parts.push(`${STATUS_HEX[s]} ${start}% ${(acc / total) * 100}%`);
    }
    return parts.length ? `conic-gradient(${parts.join(", ")})` : "conic-gradient(var(--color-cream-2) 0 100%)";
  }, [loans.length, stats.byStatus]);

  const chips: Array<{ key: LoanAdvanceStatus | null; label: string }> = [
    { key: null, label: `${t("all")} (${loans.length})` },
    { key: "PENDING", label: `${t("statusLabel.PENDING")} (${stats.byStatus.PENDING ?? 0})` },
    { key: "IN_REPAYMENT", label: `${t("statusLabel.IN_REPAYMENT")} (${stats.byStatus.IN_REPAYMENT ?? 0})` },
    { key: "FULLY_REPAID", label: `${t("statusLabel.FULLY_REPAID")} (${stats.byStatus.FULLY_REPAID ?? 0})` },
  ];

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        crumbs={[{ label: t("eyebrow") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button asChild>
            <Link href="/loans/my">
              <Plus className="size-4" />
              {t("newButton")}
            </Link>
          </Button>
        }
      />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="grid grid-cols-2 gap-4 lg:col-span-2">
          {isLoading ? (
            Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-[104px] rounded-[18px]" />)
          ) : (
            <>
              <StatCard tone="amber" label={t("kpi.pending")} value={stats.pendingCount} footer={t("kpi.pendingFooter", { sum: fmt.moneyShort(stats.pendingSum) })} />
              <StatCard tone="orange" label={t("kpi.active")} value={stats.activeCount} footer={t("kpi.activeFooter", { sum: fmt.moneyShort(stats.encours) })} />
              <StatCard tone="blue" label={t("kpi.monthly")} value={fmt.moneyShort(stats.monthly)} footer={t("kpi.monthlyFooter")} />
              <StatCard tone="green" label={t("kpi.incidents")} value="0%" footer={t("kpi.incidentsFooter")} />
            </>
          )}
        </div>

        <Card>
          <CardHeader>
            <CardTitle>{t("distribution")}</CardTitle>
          </CardHeader>
          <CardContent className="flex items-center gap-5">
            <div className="relative grid size-28 shrink-0 place-items-center rounded-full" style={{ background: donut }}>
              <div className="grid size-20 place-items-center rounded-full bg-white text-center">
                <div>
                  <div className="font-display text-[15px] font-extrabold leading-none text-ink tabular">
                    {fmt.moneyShort(stats.encours)}
                  </div>
                  <div className="text-[9px] text-ink-4">{t("encours")}</div>
                </div>
              </div>
            </div>
            <ul className="flex-1 space-y-1.5 text-[12.5px]">
              {(["IN_REPAYMENT", "FULLY_REPAID", "PENDING"] as const).map((s) => (
                <li key={s} className="flex items-center justify-between">
                  <span className="flex items-center gap-2 text-ink-2">
                    <span className="size-2.5 rounded-full" style={{ background: STATUS_HEX[s] }} />
                    {t(`statusLabel.${s}`)}
                  </span>
                  <span className="font-semibold text-ink tabular">{stats.byStatus[s] ?? 0}</span>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      </div>

      <Card className="overflow-hidden p-0">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line-soft p-3">
          <h3 className="px-1 font-display text-[15px] font-bold text-ink">
            {t("allLoans")} ({loans.length})
          </h3>
          <div className="flex flex-wrap gap-2">
            {chips.map((c) => (
              <button
                key={c.label}
                type="button"
                onClick={() => setStatusFilter(c.key)}
                className={cn(
                  "rounded-full px-3 py-1.5 text-[12.5px] font-semibold transition-colors",
                  statusFilter === c.key
                    ? "bg-grad-orange text-white shadow-brand"
                    : "border border-line bg-white text-ink-2 hover:border-brand-300 hover:text-brand-700",
                )}
              >
                {c.label}
              </button>
            ))}
          </div>
        </div>

        {isLoading ? (
          <div className="space-y-2 p-4">
            {Array.from({ length: 5 }).map((_, i) => <Skeleton key={i} className="h-12 w-full" />)}
          </div>
        ) : loans.length === 0 ? (
          <div className="py-10 text-center text-sm text-ink-3">—</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-[13px]">
              <thead>
                <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                  <th className="px-4 py-2.5 text-left font-semibold">{t("table.employee")}</th>
                  <th className="px-3 py-2.5 text-right font-semibold">{t("table.total")}</th>
                  <th className="px-3 py-2.5 text-center font-semibold">{t("table.echeances")}</th>
                  <th className="px-3 py-2.5 text-right font-semibold">{t("table.mensualite")}</th>
                  <th className="px-3 py-2.5 text-right font-semibold">{t("table.restant")}</th>
                  <th className="px-3 py-2.5 text-left font-semibold">{t("table.progression")}</th>
                  <th className="px-3 py-2.5 text-left font-semibold">{t("table.status")}</th>
                  <th className="w-24 px-3 py-2.5" />
                </tr>
              </thead>
              <tbody>
                {filtered.map((l) => {
                  const progress =
                    Number(l.montant) > 0
                      ? Math.round(((Number(l.montant) - Number(l.soldeRestant)) / Number(l.montant)) * 100)
                      : 0;
                  return (
                    <tr key={l.id} className="border-b border-line-soft/70 last:border-0">
                      <td className="px-4 py-2.5">
                        <div className="font-semibold text-ink">{l.employeeName}</div>
                        <div className="font-mono text-[11px] text-ink-4">{l.employeeMatricule}</div>
                      </td>
                      <td className="px-3 py-2.5 text-right tabular text-ink-2">{fmt.money(l.montant)}</td>
                      <td className="px-3 py-2.5 text-center tabular text-ink-3">{l.nbEcheances}</td>
                      <td className="px-3 py-2.5 text-right tabular text-ink-2">{fmt.money(l.mensualite)}</td>
                      <td className="px-3 py-2.5 text-right tabular text-ink-2">{fmt.money(l.soldeRestant)}</td>
                      <td className="px-3 py-2.5">
                        <div className="flex items-center gap-2">
                          <div className="h-1.5 w-20 overflow-hidden rounded-full bg-cream-2">
                            <div className="h-full rounded-full bg-grad-orange" style={{ width: `${progress}%` }} />
                          </div>
                          <span className="text-[11px] tabular text-ink-4">{progress}%</span>
                        </div>
                      </td>
                      <td className="px-3 py-2.5">
                        <Badge tone={STATUS_TONE[l.status]}>{t(`statusLabel.${l.status}`)}</Badge>
                      </td>
                      <td className="px-3 py-2.5 text-right">
                        {l.status === "PENDING" && (
                          <div className="flex justify-end gap-1">
                            <Button
                              variant="ghost"
                              size="icon"
                              aria-label={t("approve")}
                              disabled={approve.isPending}
                              onClick={() =>
                                approve.mutate(l.id, {
                                  onSuccess: () => toast.success(t("approve")),
                                  onError: (e) => toast.error((e as Error).message),
                                })
                              }
                            >
                              <Check className="size-4 text-status-green-600" />
                            </Button>
                            <Button variant="ghost" size="icon" aria-label={t("reject")} onClick={() => setRejectingId(l.id)}>
                              <X className="size-4 text-status-red-600" />
                            </Button>
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

      <Dialog open={!!rejectingId} onOpenChange={(o) => !o && setRejectingId(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("rejectTitle")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={form.handleSubmit((v) => {
              if (!rejectingId) return;
              reject.mutate(
                { loanId: rejectingId, motif: v.motif },
                {
                  onSuccess: () => {
                    toast.success(t("reject"));
                    setRejectingId(null);
                    form.reset();
                  },
                  onError: (e) => toast.error((e as Error).message),
                },
              );
            })}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label>{t("rejectReason")}</Label>
              <Input {...form.register("motif")} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setRejectingId(null)}>
                {tCommon("actions.cancel" as never)}
              </Button>
              <Button type="submit" variant="destructive" disabled={reject.isPending}>
                {reject.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("reject")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
