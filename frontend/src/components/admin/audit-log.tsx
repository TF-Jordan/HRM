"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";

import { PageHeader } from "@/components/shell/page-header";
import { Column, DataTable } from "@/components/ui/data-table";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDateTime } from "@/lib/format";
import type { AdministrationAudit } from "@/server/ksm/modules/admin";

export function AuditLog() {
  const t = useTranslations("admin");
  const tAudit = useTranslations("admin.audit");
  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "audit"],
    queryFn: () => apiFetch<AdministrationAudit[]>("/api/admin/audit?limit=100"),
  });

  const columns: Column<AdministrationAudit>[] = [
    {
      key: "when",
      header: tAudit("columns.when"),
      cell: (a) => (
        <span className="font-mono-tabular text-[12px] text-ink-2">
          {formatDateTime(a.occurredAt, "fr")}
        </span>
      ),
    },
    {
      key: "action",
      header: tAudit("columns.action"),
      cell: (a) => <span className="font-mono-tabular text-ink">{a.action}</span>,
    },
    {
      key: "target",
      header: tAudit("columns.target"),
      cell: (a) => (
        <span className="font-mono-tabular text-[12px] text-ink-3">
          {a.targetType}{a.targetId ? `/${a.targetId.slice(0, 8)}` : ""}
        </span>
      ),
    },
    {
      key: "actor",
      header: tAudit("columns.actor"),
      cell: (a) => (
        <span className="font-mono-tabular text-[12px] text-ink-3">
          {a.actorUserId ? a.actorUserId.slice(0, 8) : "system"}
        </span>
      ),
    },
    {
      key: "details",
      header: tAudit("columns.details"),
      cell: (a) => <span className="text-[12.5px] text-ink-3">{a.payloadSummary ?? "—"}</span>,
    },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: "Admin" }, { label: tAudit("title") }]}
        title={tAudit("title")}
        subtitle={tAudit("subtitle")}
      />
      {isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : error || !data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {error instanceof BffApiError ? error.message : "Failed to load audit"}
        </div>
      ) : (
        <DataTable columns={columns} data={data} rowKey={(a) => a.id} empty="—" />
      )}
    </>
  );
}
