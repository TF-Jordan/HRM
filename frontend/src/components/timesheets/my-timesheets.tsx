"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2, Plus, UserX } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { PeriodPicker } from "@/components/timesheets/period-picker";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Column, DataTable } from "@/components/ui/data-table";
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatPeriod } from "@/lib/format";
import { timesheetStatusTone, timesheetTotalHours } from "@/lib/timesheet-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { TimesheetResponse } from "@/server/ksm/modules/timesheets";

type MinePayload = {
  employee: EmployeeResponse | null;
  periode: string;
  timesheets: TimesheetResponse[];
};

function currentPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

export function MyTimesheets() {
  const t = useTranslations("timesheets");
  const tMy = useTranslations("timesheets.my");
  const tAdmin = useTranslations("timesheets.admin");
  const router = useRouter();
  const [periode, setPeriode] = React.useState(currentPeriode());

  const query = useQuery({
    queryKey: ["hrm", "timesheets", "mine", periode],
    queryFn: () =>
      apiFetch<MinePayload>(`/api/hrm/timesheets/mine?periode=${encodeURIComponent(periode)}`),
  });

  const num = (v: number | string) => Number(v).toFixed(1);
  const columns: Column<TimesheetResponse>[] = [
    {
      key: "periode",
      header: tMy("period"),
      cell: (ts) => <span className="capitalize text-ink">{formatPeriod(ts.periode, "fr")}</span>,
    },
    {
      key: "normales",
      header: tAdmin("columns.normales"),
      cell: (ts) => <span className="font-mono-tabular text-ink-2">{num(ts.heuresNormales)}</span>,
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
        breadcrumb={[{ label: "HR Core" }, { label: tMy("title") }]}
        title={tMy("title")}
        subtitle={tMy("subtitle")}
        actions={
          <div className="flex items-center gap-2">
            <PeriodPicker value={periode} onChange={setPeriode} maxPeriode={currentPeriode()} />
            {query.data?.employee && (
              <Link href={`/timesheets/new?periode=${periode}`}>
                <Button>
                  <Plus className="h-4 w-4" />
                  {tMy("new")}
                </Button>
              </Link>
            )}
          </div>
        }
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !query.data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "Failed"}
        </div>
      ) : !query.data.employee ? (
        <Card>
          <CardContent padding="lg">
            <div className="flex items-start gap-4">
              <span className="grid h-12 w-12 place-items-center rounded-[14px] bg-warning-50 text-warning-600">
                <UserX className="h-6 w-6" />
              </span>
              <p className="text-[14px] text-ink-2">{tMy("noEmployee")}</p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <DataTable
          columns={columns}
          data={query.data.timesheets}
          rowKey={(ts) => ts.id}
          empty={tMy("empty")}
          onRowClick={(ts) => router.push(`/timesheets/${ts.id}`)}
        />
      )}
    </>
  );
}
