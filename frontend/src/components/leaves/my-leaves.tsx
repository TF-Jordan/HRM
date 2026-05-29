"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2, Plus, UserX } from "lucide-react";
import { useTranslations } from "next-intl";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Column, DataTable } from "@/components/ui/data-table";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { leaveStatusTone } from "@/lib/leave-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { LeaveResponse } from "@/server/ksm/modules/leaves";

type MinePayload = {
  employee: EmployeeResponse | null;
  leaves: LeaveResponse[];
};

export function MyLeaves() {
  const t = useTranslations("leaves");
  const tMy = useTranslations("leaves.my");
  const tEmpType = useTranslations("employees.leaveType");
  const router = useRouter();

  const query = useQuery({
    queryKey: ["hrm", "leaves", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/leaves/mine"),
  });

  const columns: Column<LeaveResponse>[] = [
    {
      key: "type",
      header: tMy("columns.type"),
      cell: (l) => <Badge tone="orange">{tEmpType(l.type)}</Badge>,
    },
    {
      key: "period",
      header: tMy("columns.period"),
      cell: (l) => (
        <span className="font-mono-tabular text-[12.5px] text-ink-2">
          {formatDate(l.dateDebut, { locale: "fr" })} → {formatDate(l.dateFin, { locale: "fr" })}
        </span>
      ),
    },
    {
      key: "days",
      header: tMy("columns.days"),
      cell: (l) => (
        <span className="font-mono-tabular font-bold text-ink">
          {Number(l.nbJours).toFixed(1)}
        </span>
      ),
      className: "text-right",
      headClassName: "text-right",
    },
    {
      key: "status",
      header: tMy("columns.status"),
      cell: (l) => <Badge tone={leaveStatusTone(l.status)}>{t(`status.${l.status}`)}</Badge>,
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
          query.data?.employee && (
            <Link href="/leaves/new">
              <Button>
                <Plus className="h-4 w-4" />
                {tMy("new")}
              </Button>
            </Link>
          )
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
          data={query.data.leaves}
          rowKey={(l) => l.id}
          empty={tMy("empty")}
          onRowClick={(l) => router.push(`/leaves/${l.id}`)}
        />
      )}
    </>
  );
}
