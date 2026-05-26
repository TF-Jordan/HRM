"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useQuery } from "@tanstack/react-query";
import { Check, X, Banknote, Download } from "lucide-react";
import { toast } from "sonner";
import { bffFetch } from "@/lib/api-client";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useExpenseTransition } from "@/hooks/modules/useExpenses";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { StatCard } from "@/components/ui-tokens/StatCard";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import type { ExpenseReport, ExpenseStatus } from "@/lib/types/hrm/expense";

type ExpenseRow = ExpenseReport & { employeeName: string; employeeMatricule: string };

const STATUS_HEX: Record<string, string> = {
  SUBMITTED: "#f59e0b",
  APPROVED: "#3b82f6",
  REIMBURSED: "#10b981",
  REJECTED: "#ef4444",
};

export function ApproveExpensesClient() {
  const t = useTranslations("manager.expensesAdmin");
  const fmt = useFormat();
  const employees = useEmployees();
  const transition = useExpenseTransition();
  const [statusFilter, setStatusFilter] = React.useState<ExpenseStatus | null>(null);

  const expenses = useQuery({
    queryKey: ["hrm", "expenses", "all"],
    enabled: !!employees.data,
    queryFn: async () => {
      const emps = employees.data ?? [];
      const all = await Promise.all(
        emps.map((e) =>
          bffFetch<ExpenseReport[]>(`/api/hrm/expenses?employeeId=${e.id}`)
            .then((rows) =>
              rows.map((r): ExpenseRow => ({ ...r, employeeName: e.actorDisplayName, employeeMatricule: e.matricule })),
            )
            .catch(() => [] as ExpenseRow[]),
        ),
      );
      return all.flat();
    },
  });

  const rows = React.useMemo(() => expenses.data ?? [], [expenses.data]);
  const filtered = statusFilter ? rows.filter((r) => r.status === statusFilter) : rows;

  const stats = React.useMemo(() => {
    const sum = (st: ExpenseStatus) =>
      rows.filter((r) => r.status === st).reduce((s, r) => s + Number(r.totalMontant), 0);
    const count = (st: ExpenseStatus) => rows.filter((r) => r.status === st).length;
    const byStatus: Record<string, number> = {};
    for (const r of rows) byStatus[r.status] = (byStatus[r.status] ?? 0) + 1;
    return {
      pending: count("SUBMITTED"),
      pendingSum: sum("SUBMITTED"),
      approved: count("APPROVED"),
      approvedSum: sum("APPROVED"),
      reimbursed: count("REIMBURSED"),
      rejected: count("REJECTED"),
      byStatus,
    };
  }, [rows]);

  const donut = React.useMemo(() => {
    const order = ["SUBMITTED", "APPROVED", "REIMBURSED", "REJECTED"];
    const total = rows.length || 1;
    let acc = 0;
    const parts: string[] = [];
    for (const s of order) {
      const n = stats.byStatus[s] ?? 0;
      if (!n) continue;
      const start = (acc / total) * 100;
      acc += n;
      parts.push(`${STATUS_HEX[s]} ${start}% ${(acc / total) * 100}%`);
    }
    return parts.length ? `conic-gradient(${parts.join(", ")})` : "conic-gradient(var(--color-cream-2) 0 100%)";
  }, [rows.length, stats.byStatus]);

  const run = (id: string, action: "approve" | "reject" | "reimburse") =>
    transition.mutate(
      { id, action },
      {
        onSuccess: () => {
          toast.success(t(action));
          expenses.refetch();
        },
        onError: (err) => toast.error((err as Error).message),
      },
    );

  const chips: Array<{ key: ExpenseStatus | null; label: string }> = [
    { key: null, label: `${t("all")} (${rows.length})` },
    { key: "SUBMITTED", label: `${t("statusLabel.SUBMITTED")} (${stats.byStatus.SUBMITTED ?? 0})` },
    { key: "APPROVED", label: `${t("statusLabel.APPROVED")} (${stats.byStatus.APPROVED ?? 0})` },
    { key: "REIMBURSED", label: `${t("statusLabel.REIMBURSED")} (${stats.byStatus.REIMBURSED ?? 0})` },
    { key: "REJECTED", label: `${t("statusLabel.REJECTED")} (${stats.byStatus.REJECTED ?? 0})` },
  ];

  const isLoading = employees.isLoading || expenses.isLoading;

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        crumbs={[{ label: t("eyebrow") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button variant="secondary">
            <Download className="size-4" />
            {t("exportButton")}
          </Button>
        }
      />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="grid grid-cols-2 gap-4 lg:col-span-2">
          {isLoading ? (
            Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-[104px] rounded-[18px]" />)
          ) : (
            <>
              <StatCard tone="amber" label={t("kpi.pending")} value={stats.pending} footer={t("kpi.pendingFooter", { sum: fmt.moneyShort(stats.pendingSum) })} />
              <StatCard tone="blue" label={t("kpi.approved")} value={stats.approved} footer={t("kpi.approvedFooter", { sum: fmt.moneyShort(stats.approvedSum) })} />
              <StatCard tone="green" label={t("kpi.reimbursed")} value={stats.reimbursed} footer={t("kpi.reimbursedFooter")} />
              <StatCard tone="red" label={t("kpi.rejected")} value={stats.rejected} footer={t("kpi.rejectedFooter")} />
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
                <div className="font-display text-[20px] font-extrabold leading-none text-ink tabular">{rows.length}</div>
              </div>
            </div>
            <ul className="flex-1 space-y-1.5 text-[12.5px]">
              {(["SUBMITTED", "APPROVED", "REIMBURSED", "REJECTED"] as const).map((s) => (
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
            {t("allNotes")} ({rows.length})
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
        ) : rows.length === 0 ? (
          <div className="py-10 text-center text-sm text-ink-3">—</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-[13px]">
              <thead>
                <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                  <th className="px-4 py-2.5 text-left font-semibold">{t("table.author")}</th>
                  <th className="px-3 py-2.5 text-left font-semibold">{t("table.periode")}</th>
                  <th className="px-3 py-2.5 text-right font-semibold">{t("table.montant")}</th>
                  <th className="px-3 py-2.5 text-left font-semibold">{t("table.status")}</th>
                  <th className="w-44 px-3 py-2.5" />
                </tr>
              </thead>
              <tbody>
                {filtered.map((e) => (
                  <tr key={e.id} className="border-b border-line-soft/70 last:border-0">
                    <td className="px-4 py-2.5">
                      <div className="font-semibold text-ink">{e.employeeName}</div>
                      <div className="text-[11px] text-ink-4">{e.motif ?? e.employeeMatricule}</div>
                    </td>
                    <td className="px-3 py-2.5 text-ink-2 tabular">{fmt.date(e.periode)}</td>
                    <td className="px-3 py-2.5 text-right font-semibold text-ink tabular">{fmt.money(e.totalMontant)}</td>
                    <td className="px-3 py-2.5">
                      <StatusBadge kind="expense" status={e.status} />
                    </td>
                    <td className="px-3 py-2.5 text-right">
                      <div className="flex justify-end gap-1.5">
                        {e.status === "SUBMITTED" && (
                          <>
                            <Button size="sm" onClick={() => run(e.id, "approve")}>
                              <Check className="size-4" />
                              {t("approve")}
                            </Button>
                            <Button size="sm" variant="secondary" onClick={() => run(e.id, "reject")}>
                              <X className="size-4" />
                            </Button>
                          </>
                        )}
                        {e.status === "APPROVED" && (
                          <Button size="sm" onClick={() => run(e.id, "reimburse")}>
                            <Banknote className="size-4" />
                            {t("reimburse")}
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
