"use client";

import { useQuery } from "@tanstack/react-query";
import { Loader2, Plus } from "lucide-react";
import { useTranslations } from "next-intl";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Column, DataTable } from "@/components/ui/data-table";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import type { AdministrationUser } from "@/server/ksm/modules/admin";

export function UsersList() {
  const t = useTranslations("admin");
  const tUsers = useTranslations("admin.users");
  const tCommon = useTranslations("common");

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "users"],
    queryFn: () => apiFetch<AdministrationUser[]>("/api/admin/users"),
  });

  const columns: Column<AdministrationUser>[] = [
    {
      key: "name",
      header: tUsers("columns.name"),
      cell: (u) => (
        <div className="flex items-center gap-3">
          <Avatar name={u.username} size="md" />
          <div>
            <div className="font-semibold text-ink">{u.username}</div>
            <div className="font-mono-tabular text-[11px] text-ink-3">
              {u.actorId.slice(0, 8)}…
            </div>
          </div>
        </div>
      ),
    },
    {
      key: "email",
      header: tUsers("columns.email"),
      cell: (u) => <span className="text-ink-2">{u.email}</span>,
    },
    {
      key: "status",
      header: tUsers("columns.status"),
      cell: (u) =>
        u.forcePasswordChange ? (
          <Badge tone="warning">Mot de passe à changer</Badge>
        ) : u.status === "ACTIVE" ? (
          <Badge tone="success">{tCommon("status.active")}</Badge>
        ) : (
          <Badge tone="gray">{u.status}</Badge>
        ),
    },
    {
      key: "createdAt",
      header: tUsers("columns.createdAt"),
      cell: (u) => formatDate(u.createdAt, { locale: "fr" }),
    },
    {
      key: "actions",
      header: "",
      cell: () => <span className="text-[12px] text-ink-3">→</span>,
      className: "text-right",
    },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: "Admin" }, { label: tUsers("title") }]}
        title={tUsers("title")}
        subtitle={tUsers("subtitle")}
        actions={
          <Link href="/admin/users/new">
            <Button>
              <Plus className="h-4 w-4" />
              {tUsers("new")}
            </Button>
          </Link>
        }
      />

      {isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : error || !data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {error instanceof BffApiError ? error.message : "Failed to load users"}
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={data}
          rowKey={(u) => u.id}
          empty={tCommon("actions.loading")}
        />
      )}
    </>
  );
}
