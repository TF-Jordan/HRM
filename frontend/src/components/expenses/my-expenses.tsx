"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2, Plus } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Column, DataTable } from "@/components/ui/data-table";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatNumber } from "@/lib/format";
import { expenseStatusTone } from "@/lib/expense-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { ExpenseReportResponse, ExpenseReportStatus } from "@/server/ksm/modules/expenses";

type MinePayload = { employee: EmployeeResponse | null; reports: ExpenseReportResponse[] };
type Filter = "ALL" | "SUBMITTED" | "REIMBURSED";

export function MyExpenses() {
  const t = useTranslations("expenses");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const [filter, setFilter] = React.useState<Filter>("ALL");

  const query = useQuery({
    queryKey: ["hrm", "expenses", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/expenses/mine"),
    refetchInterval: 60_000,
  });

  const reports = React.useMemo(() => query.data?.reports ?? [], [query.data]);
  const visible = reports.filter((r) =>
    filter === "ALL" ? true : filter === "SUBMITTED" ? r.status === "SUBMITTED" : r.status === "REIMBURSED",
  );
  const counts = React.useMemo(() => {
    const c: Partial<Record<ExpenseReportStatus, number>> = {};
    for (const r of reports) c[r.status] = (c[r.status] ?? 0) + 1;
    return c;
  }, [reports]);

  const columns: Column<ExpenseReportResponse>[] = [
    {
      key: "ref",
      header: t("mine.columns.reference"),
      cell: (r) => <span className="font-mono-tabular text-[11px] text-ink-3">{shortRef(r.id)}</span>,
    },
    {
      key: "objet",
      header: t("mine.columns.objet"),
      cell: (r) => (
        <div>
          <div className="text-[13px] font-semibold text-ink">{r.motif ?? "—"}</div>
          <div className="text-[11px] text-ink-3">{r.periode}</div>
        </div>
      ),
    },
    {
      key: "amount",
      header: t("mine.columns.amount"),
      cell: (r) => (
        <span className="font-mono-tabular font-bold text-ink">
          {formatNumber(Number(r.totalMontant ?? 0), locale)}
        </span>
      ),
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "status",
      header: t("mine.columns.status"),
      cell: (r) => <Badge tone={expenseStatusTone(r.status)}>{t(`status.${r.status}`)}</Badge>,
    },
  ];

  const filters: { key: Filter; label: string }[] = [
    { key: "ALL", label: t("filters.all") },
    { key: "SUBMITTED", label: t("filters.submitted") },
    { key: "REIMBURSED", label: t("filters.reimbursed") },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("mine.title") }]}
        title={t("mine.title")}
        subtitle={t("mine.subtitle")}
        actions={
          <Link href="/expenses/new">
            <Button>
              <Plus className="h-4 w-4" />
              {t("new.title")}
            </Button>
          </Link>
        }
      />

      <div className="mb-5 grid grid-cols-4 gap-4">
        <Tile label={t("status.SUBMITTED")} value={counts.SUBMITTED ?? 0} tone="bg-warning-500" />
        <Tile label={t("status.APPROVED")} value={counts.APPROVED ?? 0} tone="bg-info-500" />
        <Tile label={t("status.REIMBURSED")} value={counts.REIMBURSED ?? 0} tone="bg-success-500" />
        <Tile label={t("status.REJECTED")} value={counts.REJECTED ?? 0} tone="bg-danger-500" />
      </div>

      <div className="mb-4 flex flex-wrap gap-2">
        {filters.map((f) => (
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
            {f.label}
          </button>
        ))}
      </div>

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={visible}
          rowKey={(r) => r.id}
          onRowClick={(r) => router.push(`/expenses/${r.id}`)}
          empty={t("mine.empty")}
        />
      )}
    </>
  );
}

function Tile({ label, value, tone }: { label: string; value: React.ReactNode; tone: string }) {
  return (
    <div className="relative flex flex-col gap-1 overflow-hidden rounded-[16px] border border-line bg-white px-[18px] py-4 shadow-xs-brand">
      <div className="flex items-center justify-between">
        <span className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">{label}</span>
        <span className={cn("inline-block h-2 w-2 rounded-full", tone)} />
      </div>
      <div className="font-display font-mono-tabular text-[24px] font-extrabold tracking-tight text-ink">
        {value}
      </div>
    </div>
  );
}

function shortRef(uuid: string): string {
  return `NF-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}
