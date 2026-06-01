"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Check, Loader2, Plus, X } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useSearchParams } from "next/navigation";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useCan } from "@/hooks/use-can";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatNumber } from "@/lib/format";
import { expenseStatusTone } from "@/lib/expense-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { ExpenseReportResponse, ExpenseReportStatus } from "@/server/ksm/modules/expenses";

type Filter = "ALL" | "SUBMITTED" | "APPROVED" | "REIMBURSED" | "REJECTED";

const FILTERS: { key: Filter; tKey: string }[] = [
  { key: "ALL", tKey: "filters.all" },
  { key: "SUBMITTED", tKey: "filters.submitted" },
  { key: "APPROVED", tKey: "filters.approved" },
  { key: "REIMBURSED", tKey: "filters.reimbursed" },
  { key: "REJECTED", tKey: "filters.rejected" },
];

export function ExpensesQueue() {
  const t = useTranslations("expenses");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const queryClient = useQueryClient();
  const canManage = useCan("hrm:expense:manage");
  const canCreate = useCan("hrm:expense:create");
  const searchParams = useSearchParams();
  const initialFilter = (searchParams.get("status") as Filter | null) ?? "ALL";
  const [filter, setFilter] = React.useState<Filter>(
    FILTERS.some((f) => f.key === initialFilter) ? initialFilter : "ALL",
  );

  const query = useQuery({
    queryKey: ["hrm", "expenses", "list", filter],
    queryFn: () => {
      const qs = filter === "ALL" ? "" : `?status=${filter}`;
      return apiFetch<ExpenseReportResponse[]>(`/api/hrm/expenses${qs}`);
    },
    refetchInterval: 60_000,
  });

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });
  const nameOf = React.useCallback(
    (employeeId: string) => {
      const e = employeesQuery.data?.find((x) => x.id === employeeId);
      return e?.actorDisplayName ?? e?.matricule ?? `${employeeId.slice(0, 8)}…`;
    },
    [employeesQuery.data],
  );

  const all = React.useMemo(() => query.data ?? [], [query.data]);
  const counts = React.useMemo(() => {
    const c: Partial<Record<ExpenseReportStatus, number>> = {};
    let approvedAmount = 0;
    for (const r of all) {
      c[r.status] = (c[r.status] ?? 0) + 1;
      if (r.status === "APPROVED") approvedAmount += Number(r.totalMontant ?? 0);
    }
    return { c, approvedAmount };
  }, [all]);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["hrm", "expenses"] });

  const approveM = useMutation({
    mutationFn: (id: string) =>
      apiFetch(`/api/hrm/expenses/${id}/approve`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("detail.approveSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const rejectM = useMutation({
    mutationFn: (id: string) =>
      apiFetch(`/api/hrm/expenses/${id}/reject`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("detail.rejectSuccess"));
      invalidate();
    },
    onError: handleError,
  });

  // Real status breakdown for the side card (no fabricated category data).
  const breakdown: { key: ExpenseReportStatus; tone: string }[] = [
    { key: "SUBMITTED", tone: "bg-warning-500" },
    { key: "APPROVED", tone: "bg-info-500" },
    { key: "REIMBURSED", tone: "bg-success-500" },
    { key: "REJECTED", tone: "bg-danger-500" },
  ];
  const maxCount = Math.max(1, ...breakdown.map((b) => counts.c[b.key] ?? 0));

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          canCreate ? (
            <Link href="/expenses/new">
              <Button>
                <Plus className="h-4 w-4" />
                {t("new.title")}
              </Button>
            </Link>
          ) : undefined
        }
      />

      <div className="mb-5 grid grid-cols-4 gap-4">
        <StatTile label={t("kpi.toApprove")} value={counts.c.SUBMITTED ?? 0} tone="amber" sub={t("status.SUBMITTED")} />
        <StatTile
          label={t("kpi.approved")}
          value={counts.c.APPROVED ?? 0}
          tone="blue"
          sub={`${formatNumber(counts.approvedAmount, locale)} XAF`}
        />
        <StatTile label={t("kpi.reimbursed")} value={counts.c.REIMBURSED ?? 0} tone="green" sub={t("status.REIMBURSED")} />
        <StatTile label={t("kpi.rejected")} value={counts.c.REJECTED ?? 0} tone="red" sub={t("status.REJECTED")} />
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[2fr_1fr]">
        <Card>
          <div className="relative">
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line px-5 py-4">
              <span className="text-[14px] font-bold tracking-tight text-ink">{t("queue.title")}</span>
              <div className="flex flex-wrap gap-2">
                {FILTERS.map((f) => (
                  <button
                    key={f.key}
                    type="button"
                    onClick={() => setFilter(f.key)}
                    className={cn(
                      "rounded-full px-3 py-1.5 text-[12px] font-semibold transition-colors",
                      filter === f.key
                        ? "bg-grad-orange text-white shadow-orange-brand"
                        : "border border-line bg-white text-ink-2 hover:bg-bg-soft",
                    )}
                  >
                    {t(f.tKey)}
                  </button>
                ))}
              </div>
            </div>

            {query.isLoading ? (
              <div className="grid place-items-center py-16">
                <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
              </div>
            ) : query.error ? (
              <div className="px-5 py-10 text-center text-ink-3">
                {query.error instanceof BffApiError ? query.error.message : "—"}
              </div>
            ) : all.length === 0 ? (
              <div className="px-5 py-12 text-center text-[13px] text-ink-3">{t("queue.empty")}</div>
            ) : (
              <table className="w-full border-collapse">
                <thead>
                  <tr className="border-b border-line bg-[linear-gradient(180deg,var(--color-bg-dim)_0%,var(--color-bg-soft)_100%)]">
                    <Th>{t("queue.columns.reference")}</Th>
                    <Th>{t("queue.columns.author")}</Th>
                    <Th className="text-right">{t("queue.columns.amount")}</Th>
                    <Th>{t("queue.columns.status")}</Th>
                    <Th className="text-right" />
                  </tr>
                </thead>
                <tbody>
                  {all.map((r) => (
                    <tr
                      key={r.id}
                      className="cursor-pointer border-b border-line-soft last:border-0 hover:bg-bg-soft"
                      onClick={() => router.push(`/expenses/${r.id}`)}
                    >
                      <td className="px-5 py-3 font-mono-tabular text-[11px] text-ink-3">{shortRef(r.id)}</td>
                      <td className="px-3 py-3">
                        <div className="flex items-center gap-2.5">
                          <Avatar name={nameOf(r.employeeId)} size="sm" />
                          <div className="min-w-0">
                            <div className="text-[13px] font-semibold text-ink">{nameOf(r.employeeId)}</div>
                            <div className="truncate text-[11px] text-ink-3">{r.motif ?? r.periode}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-3 py-3 text-right font-mono-tabular text-[13px] font-bold text-ink">
                        {formatNumber(Number(r.totalMontant ?? 0), locale)}
                      </td>
                      <td className="px-3 py-3">
                        <Badge tone={expenseStatusTone(r.status)}>{t(`status.${r.status}`)}</Badge>
                      </td>
                      <td className="px-5 py-3 text-right" onClick={(e) => e.stopPropagation()}>
                        {canManage && r.status === "SUBMITTED" ? (
                          <div className="flex justify-end gap-2">
                            <button
                              type="button"
                              aria-label={t("detail.actions.reject")}
                              onClick={() => rejectM.mutate(r.id)}
                              className="grid h-7 w-7 place-items-center rounded-md border border-line bg-white text-ink-3 hover:border-danger-300 hover:text-danger-600"
                            >
                              <X className="h-3.5 w-3.5" />
                            </button>
                            <button
                              type="button"
                              aria-label={t("detail.actions.approve")}
                              onClick={() => approveM.mutate(r.id)}
                              className="grid h-7 w-7 place-items-center rounded-md bg-grad-orange text-white shadow-orange-brand"
                            >
                              <Check className="h-3.5 w-3.5" />
                            </button>
                          </div>
                        ) : canManage && r.status === "APPROVED" ? (
                          <Button
                            type="button"
                            variant="secondary"
                            className="!h-7 !px-2.5 !text-[11px]"
                            onClick={() =>
                              apiFetch(`/api/hrm/expenses/${r.id}/reimburse`, { method: "POST" })
                                .then(() => {
                                  toast.success(t("detail.reimburseSuccess"));
                                  invalidate();
                                })
                                .catch(handleError)
                            }
                          >
                            {t("detail.actions.reimburse")}
                          </Button>
                        ) : null}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </Card>

        <Card>
          <CardContent padding="lg">
            <div className="mb-4 text-[14px] font-bold tracking-tight text-ink">
              {t("title")} · {all.length}
            </div>
            <div className="flex flex-col gap-3">
              {breakdown.map((b) => {
                const n = counts.c[b.key] ?? 0;
                return (
                  <div key={b.key}>
                    <div className="mb-1 flex items-center justify-between text-[12px]">
                      <span className="text-ink-2">{t(`status.${b.key}`)}</span>
                      <span className="font-mono-tabular font-semibold text-ink">{n}</span>
                    </div>
                    <div className="h-2 overflow-hidden rounded-full bg-bg-soft">
                      <div
                        className={cn("h-full rounded-full", b.tone)}
                        style={{ width: `${(n / maxCount) * 100}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}

function StatTile({
  label,
  value,
  sub,
  tone,
}: {
  label: string;
  value: React.ReactNode;
  sub: React.ReactNode;
  tone: "amber" | "blue" | "green" | "red";
}) {
  const dot = {
    amber: "bg-warning-500",
    blue: "bg-info-500",
    green: "bg-success-500",
    red: "bg-danger-500",
  }[tone];
  return (
    <div className="relative flex flex-col gap-1 overflow-hidden rounded-[16px] border border-line bg-white px-[18px] py-4 shadow-xs-brand">
      <div className="flex items-center justify-between">
        <span className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">{label}</span>
        <span className={cn("inline-block h-2 w-2 rounded-full", dot)} />
      </div>
      <div className="font-display font-mono-tabular text-[24px] font-extrabold tracking-tight text-ink">
        {value}
      </div>
      <div className="text-[11px] text-ink-3">{sub}</div>
    </div>
  );
}

function Th({ children, className }: { children?: React.ReactNode; className?: string }) {
  return (
    <th
      className={cn(
        "px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3 first:pl-5 last:pr-5",
        className,
      )}
    >
      {children}
    </th>
  );
}

function shortRef(uuid: string): string {
  return `NF-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}
