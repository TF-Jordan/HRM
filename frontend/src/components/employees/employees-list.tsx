"use client";

import { useQuery } from "@tanstack/react-query";
import { Download, Loader2, Plus, Search, SlidersHorizontal, Upload } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Chip } from "@/components/ui/chip";
import { Column, DataTable } from "@/components/ui/data-table";
import { StatCard, StatCardGrid } from "@/components/ui/stat-card";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { employeeStatusTone } from "@/lib/employee-status";
import { formatDate } from "@/lib/format";
import { cn, initials } from "@/lib/utils";
import type { EmployeeResponse, EmployeeStatus } from "@/server/ksm/modules/employees";

type StatusFilter = "all" | EmployeeStatus;

const AVATAR_TONES = ["orange", "blue", "green", "violet", "amber", "teal"] as const;

export function EmployeesList() {
  const t = useTranslations("employees");
  const tList = useTranslations("employees.list");
  const tStatus = useTranslations("employees.status");
  const tCommon = useTranslations("common");
  const router = useRouter();
  const [statusFilter, setStatusFilter] = React.useState<StatusFilter>("all");
  const [search, setSearch] = React.useState("");

  const query = useQuery({
    queryKey: ["hrm", "employees"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const all = query.data ?? [];
  const counts = React.useMemo(() => {
    return {
      total: all.length,
      active: all.filter((e) => e.status === "ACTIVE").length,
      onLeave: all.filter((e) => e.status === "ON_LEAVE").length,
      suspended: all.filter((e) => e.status === "SUSPENDED").length,
      terminated: all.filter((e) => e.status === "TERMINATED").length,
    };
  }, [all]);

  const filtered = React.useMemo(() => {
    return all.filter((e) => {
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
  }, [all, statusFilter, search]);

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
            tone={AVATAR_TONES[e.id.charCodeAt(0) % AVATAR_TONES.length]}
          />
          <div>
            <div className="text-[13.5px] font-semibold text-ink">{e.actorDisplayName ?? "—"}</div>
            <div className="font-mono-tabular text-[11px] text-ink-3">{e.matricule}</div>
          </div>
        </div>
      ),
    },
    {
      key: "department",
      header: tList("columns.department"),
      cell: (e) =>
        e.departmentCode ? (
          <span className="inline-flex items-center rounded-[7px] bg-bg-soft px-2.5 py-1 text-[11px] font-medium text-ink-2">
            {e.departmentCode}
          </span>
        ) : (
          <span className="text-ink-4">—</span>
        ),
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
          <>
            <Button variant="secondary" disabled>
              <Upload className="h-4 w-4" />
              {tCommon("actions.import")}
            </Button>
            <Button variant="secondary" disabled>
              <Download className="h-4 w-4" />
              {tCommon("actions.export")}
            </Button>
            <Link href="/employees/new">
              <Button>
                <Plus className="h-4 w-4" />
                {tList("new")}
              </Button>
            </Link>
          </>
        }
      />

      {/* KPI stat row */}
      <StatCardGrid>
        <StatCard
          label={tList("filters.all")}
          value={counts.total}
          sub={tList("subtitle")}
          tone="green"
        />
        <StatCard
          label={tList("filters.active")}
          value={counts.active}
          sub={counts.total ? `${Math.round((counts.active / counts.total) * 100)}%` : "—"}
          tone="orange"
        />
        <StatCard
          label={tList("filters.onLeave")}
          value={counts.onLeave}
          sub={tStatus("ON_LEAVE")}
          tone="blue"
        />
        <StatCard
          label={tList("filters.suspended")}
          value={counts.suspended + counts.terminated}
          sub={`${counts.terminated} ${tStatus("TERMINATED").toLowerCase()}`}
          tone="amber"
        />
      </StatCardGrid>

      {/* Filter bar */}
      <div className="mb-4 rounded-[20px] border border-line bg-white p-4 shadow-sm-brand">
        <div className="flex flex-wrap items-center gap-3">
          <div
            className={cn(
              "flex min-w-[280px] flex-1 items-center gap-2.5 rounded-[10px] border border-line bg-white px-3 py-2 text-ink-3",
              "focus-within:border-orange-400 focus-within:ring-4 focus-within:ring-orange-500/12",
            )}
          >
            <Search className="h-3.5 w-3.5 shrink-0" />
            <input
              type="search"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder={tList("search")}
              className="flex-1 border-none bg-transparent text-[13px] text-ink outline-none placeholder:text-ink-4"
            />
          </div>
          <Chip active={statusFilter === "all"} tone="orange" onClick={() => setStatusFilter("all")}>
            {tList("filters.all")} ({counts.total})
          </Chip>
          <Chip active={statusFilter === "ACTIVE"} onClick={() => setStatusFilter("ACTIVE")}>
            {tList("filters.active")} ({counts.active})
          </Chip>
          <Chip active={statusFilter === "ON_LEAVE"} onClick={() => setStatusFilter("ON_LEAVE")}>
            {tList("filters.onLeave")} ({counts.onLeave})
          </Chip>
          <Chip active={statusFilter === "SUSPENDED"} onClick={() => setStatusFilter("SUSPENDED")}>
            {tList("filters.suspended")} ({counts.suspended})
          </Chip>
          <Chip active={statusFilter === "TERMINATED"} onClick={() => setStatusFilter("TERMINATED")}>
            {tList("filters.terminated")} ({counts.terminated})
          </Chip>
          <div className="h-6 w-px bg-line" />
          <Button variant="secondary" size="sm" disabled>
            <SlidersHorizontal className="h-3.5 w-3.5" />
            {tCommon("actions.filter")}
          </Button>
        </div>
      </div>

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
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
