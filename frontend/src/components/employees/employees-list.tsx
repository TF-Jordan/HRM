"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2, Plus, Search } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Chip } from "@/components/ui/chip";
import { Column, DataTable } from "@/components/ui/data-table";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { employeeStatusTone } from "@/lib/employee-status";
import { formatDate } from "@/lib/format";
import { cn, initials } from "@/lib/utils";
import type { EmployeeResponse, EmployeeStatus } from "@/server/ksm/modules/employees";

type StatusFilter = "all" | EmployeeStatus;

export function EmployeesList() {
  const t = useTranslations("employees");
  const tList = useTranslations("employees.list");
  const tStatus = useTranslations("employees.status");
  const router = useRouter();
  const [statusFilter, setStatusFilter] = React.useState<StatusFilter>("all");
  const [search, setSearch] = React.useState("");

  const query = useQuery({
    queryKey: ["hrm", "employees"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const filtered = React.useMemo(() => {
    if (!query.data) return [];
    return query.data.filter((e) => {
      if (statusFilter !== "all" && e.status !== statusFilter) return false;
      if (search) {
        const needle = search.toLowerCase();
        return (
          (e.actorDisplayName ?? "").toLowerCase().includes(needle) ||
          e.matricule.toLowerCase().includes(needle) ||
          (e.departmentCode ?? "").toLowerCase().includes(needle)
        );
      }
      return true;
    });
  }, [query.data, statusFilter, search]);

  const columns: Column<EmployeeResponse>[] = [
    {
      key: "employee",
      header: tList("columns.employee"),
      cell: (e) => (
        <div className="flex items-center gap-3">
          <Avatar
            name={e.actorDisplayName ?? e.matricule}
            initials={initials(e.actorDisplayName ?? e.matricule, 2)}
            size="md"
            tone={
              (["orange", "blue", "green", "violet", "amber", "teal"] as const)[
                e.id.charCodeAt(0) % 6
              ]
            }
          />
          <div>
            <div className="font-semibold text-ink">{e.actorDisplayName ?? "—"}</div>
            <div className="font-mono-tabular text-[11.5px] text-ink-3">{e.matricule}</div>
          </div>
        </div>
      ),
    },
    {
      key: "department",
      header: tList("columns.department"),
      cell: (e) => <span className="text-ink-2">{e.departmentCode ?? "—"}</span>,
    },
    {
      key: "categoryEchelon",
      header: tList("columns.categoryEchelon"),
      cell: (e) => (
        <span className="font-mono-tabular text-ink-2">
          {e.categorie}
          {e.echelon ? ` / ${e.echelon}` : ""}
        </span>
      ),
    },
    {
      key: "hireDate",
      header: tList("columns.hireDate"),
      cell: (e) => <span className="text-ink-2">{formatDate(e.dateEmbauche, { locale: "fr" })}</span>,
    },
    {
      key: "status",
      header: tList("columns.status"),
      cell: (e) => <Badge tone={employeeStatusTone(e.status)}>{tStatus(e.status)}</Badge>,
    },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: tList("title") }]}
        title={tList("title")}
        subtitle={tList("subtitle")}
        actions={
          <Link href="/employees/new">
            <Button>
              <Plus className="h-4 w-4" />
              {tList("new")}
            </Button>
          </Link>
        }
      />

      {/* Search + filters */}
      <div className="mb-4 flex flex-wrap items-center gap-3">
        <div
          className={cn(
            "flex flex-1 min-w-[280px] max-w-[420px] items-center gap-2.5 rounded-xl border border-line bg-white px-4 py-2 text-ink-3 shadow-xs-brand",
            "focus-within:border-orange-400 focus-within:ring-4 focus-within:ring-orange-500/12",
          )}
        >
          <Search className="h-4 w-4 shrink-0" />
          <input
            type="search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder={tList("search")}
            className="flex-1 border-none bg-transparent text-[13.5px] text-ink outline-none placeholder:text-ink-4"
          />
        </div>
        <div className="flex flex-wrap gap-1.5">
          <Chip active={statusFilter === "all"} tone="orange" onClick={() => setStatusFilter("all")}>
            {tList("filters.all")}
          </Chip>
          <Chip active={statusFilter === "ACTIVE"} onClick={() => setStatusFilter("ACTIVE")}>
            {tList("filters.active")}
          </Chip>
          <Chip active={statusFilter === "ON_LEAVE"} onClick={() => setStatusFilter("ON_LEAVE")}>
            {tList("filters.onLeave")}
          </Chip>
          <Chip active={statusFilter === "SUSPENDED"} onClick={() => setStatusFilter("SUSPENDED")}>
            {tList("filters.suspended")}
          </Chip>
          <Chip active={statusFilter === "TERMINATED"} onClick={() => setStatusFilter("TERMINATED")}>
            {tList("filters.terminated")}
          </Chip>
        </div>
      </div>

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !query.data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "Failed to load employees"}
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={filtered}
          rowKey={(e) => e.id}
          empty={tList("empty")}
          onRowClick={(e) => router.push(`/employees/${e.id}`)}
        />
      )}
    </>
  );
}
