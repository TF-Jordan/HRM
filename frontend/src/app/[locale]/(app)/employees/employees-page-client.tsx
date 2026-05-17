"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Plus, Users, AlertTriangle } from "lucide-react";
import { Link, useRouter } from "@/i18n/navigation";
import { PageHeader } from "@/components/shell/PageHeader";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { DataTable, type Column } from "@/components/ui-tokens/DataTable";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import type { Employee } from "@/lib/types/hrm/employee";

export function EmployeesPageClient() {
  const t = useTranslations("employees");
  const tNav = useTranslations("navigation");
  const router = useRouter();
  const fmt = useFormat();
  const { data, isLoading, isError, error, refetch } = useEmployees();

  const columns: Column<Employee>[] = React.useMemo(
    () => [
      {
        id: "matricule",
        header: t("table.matricule"),
        accessor: (e) => e.matricule,
        sortable: true,
        cell: (e) => <span className="font-mono text-[12.5px] text-ink-2">{e.matricule}</span>,
      },
      {
        id: "name",
        header: t("table.name"),
        accessor: (e) => e.actorDisplayName,
        sortable: true,
        cell: (e) => <span className="font-medium text-ink">{e.actorDisplayName}</span>,
      },
      {
        id: "department",
        header: t("table.department"),
        accessor: (e) => e.departmentCode ?? "—",
        sortable: true,
      },
      {
        id: "categorie",
        header: t("table.category"),
        accessor: (e) => e.categorie,
        sortable: true,
        align: "center",
      },
      {
        id: "hireDate",
        header: t("table.hireDate"),
        accessor: (e) => e.dateEmbauche,
        cell: (e) => <span className="tabular">{fmt.date(e.dateEmbauche)}</span>,
        sortable: true,
      },
      {
        id: "status",
        header: t("table.status"),
        accessor: (e) => e.status,
        cell: (e) => <StatusBadge kind="employee" status={e.status} />,
        sortable: true,
      },
    ],
    [t, fmt],
  );

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-01..05"
        crumbs={[{ label: tNav("items.employees") }]}
        title={t("list.title")}
        subtitle={t("list.subtitle")}
        actions={
          <Button asChild>
            <Link href="/employees/new">
              <Plus className="size-4" />
              {t("list.newButton")}
            </Link>
          </Button>
        }
      />

      {isError && (
        <Card>
          <CardContent className="flex items-start gap-3">
            <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
            <div className="flex-1">
              <div className="font-semibold text-ink">Failed to load employees</div>
              <div className="text-sm text-ink-3">
                {(error as Error | null)?.message ?? "Unknown error"}
              </div>
            </div>
            <Button variant="secondary" onClick={() => refetch()}>
              Retry
            </Button>
          </CardContent>
        </Card>
      )}

      {isLoading && (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      )}

      {!isLoading && !isError && data && data.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center gap-3 py-12 text-center">
            <div className="grid size-12 place-items-center rounded-2xl bg-brand-50 text-brand-600">
              <Users className="size-6" />
            </div>
            <div className="font-display text-lg font-bold text-ink">{t("list.empty")}</div>
            <div className="max-w-md text-sm text-ink-3">{t("list.emptySubtitle")}</div>
            <Button asChild className="mt-2">
              <Link href="/employees/new">
                <Plus className="size-4" />
                {t("list.newButton")}
              </Link>
            </Button>
          </CardContent>
        </Card>
      )}

      {!isLoading && !isError && data && data.length > 0 && (
        <DataTable
          data={data}
          columns={columns}
          rowKey={(e) => e.id}
          onRowClick={(e) => router.push(`/employees/${e.id}` as never)}
        />
      )}
    </div>
  );
}
