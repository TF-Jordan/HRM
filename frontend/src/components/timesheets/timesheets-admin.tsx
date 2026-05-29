"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { PeriodPicker } from "@/components/timesheets/period-picker";
import { Badge } from "@/components/ui/badge";
import { Column, DataTable } from "@/components/ui/data-table";
import { StatCard, StatCardGrid } from "@/components/ui/stat-card";
import { useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { timesheetStatusTone, timesheetTotalHours } from "@/lib/timesheet-status";
import type { TimesheetResponse } from "@/server/ksm/modules/timesheets";

function currentPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

export function TimesheetsAdmin() {
  const t = useTranslations("timesheets");
  const tAdmin = useTranslations("timesheets.admin");
  const tCommon = useTranslations("common");
  const router = useRouter();
  const [periode, setPeriode] = React.useState(currentPeriode());

  const query = useQuery({
    queryKey: ["hrm", "timesheets", "org", periode],
    queryFn: () =>
      apiFetch<TimesheetResponse[]>(`/api/hrm/timesheets?periode=${encodeURIComponent(periode)}`),
  });

  const num = (v: number | string) => Number(v).toFixed(1);

  const columns: Column<TimesheetResponse>[] = [
    {
      key: "employee",
      header: tAdmin("columns.employee"),
      cell: (ts) => (
        <span className="font-mono-tabular text-[12px] text-ink-3">
          {ts.employeeId.slice(0, 8)}…
        </span>
      ),
    },
    {
      key: "normales",
      header: tAdmin("columns.normales"),
      cell: (ts) => <span className="font-mono-tabular text-ink-2">{num(ts.heuresNormales)}</span>,
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "supp",
      header: tAdmin("columns.supp"),
      cell: (ts) => (
        <span className="font-mono-tabular text-ink-2">{num(ts.heuresSupplementaires)}</span>
      ),
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "nuit",
      header: tAdmin("columns.nuit"),
      cell: (ts) => <span className="font-mono-tabular text-ink-2">{num(ts.heuresNuit)}</span>,
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "weekend",
      header: tAdmin("columns.weekend"),
      cell: (ts) => <span className="font-mono-tabular text-ink-2">{num(ts.heuresWeekend)}</span>,
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "absences",
      header: tAdmin("columns.absences"),
      cell: (ts) => (
        <span className="font-mono-tabular text-danger-600">{num(ts.absencesNonJustifiees)}</span>
      ),
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "total",
      header: tAdmin("columns.total"),
      cell: (ts) => (
        <span className="font-mono-tabular font-bold text-ink">
          {timesheetTotalHours(ts).toFixed(1)}
        </span>
      ),
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "status",
      header: tAdmin("columns.status"),
      cell: (ts) => <Badge tone={timesheetStatusTone(ts.status)}>{t(`status.${ts.status}`)}</Badge>,
    },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: tAdmin("title") }]}
        title={tAdmin("title")}
        subtitle={tAdmin("subtitle")}
        actions={<PeriodPicker value={periode} onChange={setPeriode} maxPeriode={currentPeriode()} />}
      />

      <StatCardGrid>
        <StatCard
          label={tAdmin("title")}
          value={query.data?.length ?? 0}
          sub={tAdmin("period")}
          tone="orange"
        />
        <StatCard
          label={t("status.VALIDATED")}
          value={(query.data ?? []).filter((ts) => ts.status === "VALIDATED").length}
          sub={tCommon("status.validated")}
          tone="green"
        />
        <StatCard
          label={t("status.SUBMITTED")}
          value={(query.data ?? []).filter((ts) => ts.status === "SUBMITTED").length}
          sub={tCommon("status.submitted")}
          tone="blue"
        />
        <StatCard
          label={tAdmin("columns.total")}
          value={(query.data ?? []).reduce((s, ts) => s + timesheetTotalHours(ts), 0).toFixed(0)}
          sub="h"
          tone="violet"
        />
      </StatCardGrid>

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !query.data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "Failed"}
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={query.data}
          rowKey={(ts) => ts.id}
          empty={tAdmin("empty")}
          onRowClick={(ts) => router.push(`/timesheets/${ts.id}`)}
        />
      )}
    </>
  );
}
