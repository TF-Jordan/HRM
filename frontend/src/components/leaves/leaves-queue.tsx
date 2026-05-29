"use client";

import { useQuery } from "@tanstack/react-query";
import { CheckCircle2, Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Column, DataTable } from "@/components/ui/data-table";
import { useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { leaveStatusTone } from "@/lib/leave-status";
import type { LeaveResponse } from "@/server/ksm/modules/leaves";

export function LeavesQueue() {
  const t = useTranslations("leaves");
  const tQ = useTranslations("leaves.queue");
  const tCommon = useTranslations("common");
  const tEmpType = useTranslations("employees.leaveType");
  const router = useRouter();

  const query = useQuery({
    queryKey: ["hrm", "leaves", "pending"],
    queryFn: () => apiFetch<LeaveResponse[]>("/api/hrm/leaves/pending"),
    refetchInterval: 60_000,
  });

  const columns: Column<LeaveResponse>[] = [
    {
      key: "employee",
      header: tQ("columns.employee"),
      cell: (l) => (
        <span className="font-mono-tabular text-[12px] text-ink-3">
          {l.employeeId.slice(0, 8)}…
        </span>
      ),
    },
    {
      key: "type",
      header: tQ("columns.type"),
      cell: (l) => <Badge tone="orange">{tEmpType(l.type)}</Badge>,
    },
    {
      key: "period",
      header: tQ("columns.period"),
      cell: (l) => (
        <span className="font-mono-tabular text-[12.5px] text-ink-2">
          {formatDate(l.dateDebut, { locale: "fr" })} → {formatDate(l.dateFin, { locale: "fr" })}
        </span>
      ),
    },
    {
      key: "days",
      header: tQ("columns.days"),
      cell: (l) => (
        <span className="font-mono-tabular font-bold text-ink">
          {Number(l.nbJours).toFixed(1)}
        </span>
      ),
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "reason",
      header: tQ("columns.reason"),
      cell: (l) => <span className="text-ink-3">{l.motif ?? "—"}</span>,
    },
    {
      key: "status",
      header: tCommon("status.pending"),
      cell: (l) => <Badge tone={leaveStatusTone(l.status)}>{t(`status.${l.status}`)}</Badge>,
    },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: tQ("title") }]}
        title={tQ("title")}
        subtitle={tQ("subtitle")}
      />
      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !query.data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "Failed"}
        </div>
      ) : query.data.length === 0 ? (
        <div className="rounded-[20px] border border-line bg-white p-12 text-center">
          <CheckCircle2 className="mx-auto h-10 w-10 text-success-500" />
          <p className="mt-3 text-[14px] text-ink-2">{tQ("empty")}</p>
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={query.data}
          rowKey={(l) => l.id}
          onRowClick={(l) => router.push(`/leaves/${l.id}`)}
        />
      )}
    </>
  );
}
