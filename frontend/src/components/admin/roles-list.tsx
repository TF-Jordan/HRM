"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Loader2, ShieldCheck, Sparkles } from "lucide-react";
import { useTranslations } from "next-intl";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Column, DataTable } from "@/components/ui/data-table";
import { SectionTitle } from "@/components/ui/section-title";
import { apiFetch, BffApiError } from "@/lib/api-client";
import type { AdministrationRole } from "@/server/ksm/modules/admin";

export function RolesList() {
  const t = useTranslations("admin");
  const tRoles = useTranslations("admin.roles");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();

  const rolesQuery = useQuery({
    queryKey: ["admin", "roles"],
    queryFn: () => apiFetch<AdministrationRole[]>("/api/admin/roles"),
  });

  const provision = useMutation({
    mutationFn: () =>
      apiFetch<AdministrationRole[]>("/api/admin/roles/defaults", { method: "POST" }),
    onSuccess: () => {
      toast.success(tRoles("provisioned_success"));
      queryClient.invalidateQueries({ queryKey: ["admin", "roles"] });
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) {
        toast.error(cause.message);
      } else {
        toast.error(tErrors("unknown"));
      }
    },
  });

  const columns: Column<AdministrationRole>[] = [
    {
      key: "code",
      header: tRoles("code"),
      cell: (r) => <span className="font-mono-tabular text-ink-2">{r.code}</span>,
    },
    {
      key: "name",
      header: tRoles("name"),
      cell: (r) => <span className="text-ink">{r.name}</span>,
    },
    {
      key: "scope",
      header: tRoles("scopeType"),
      cell: (r) => <Badge tone={r.scopeType === "TENANT" ? "orange" : "info"}>{r.scopeType}</Badge>,
    },
    {
      key: "permissions",
      header: tRoles("permissionsCount"),
      cell: (r) => (
        <span className="font-mono-tabular text-ink-3">{r.permissions.length}</span>
      ),
      className: "text-right",
      headClassName: "text-right",
    },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: "Admin" }, { label: tRoles("title") }]}
        title={tRoles("title")}
        subtitle={tRoles("subtitle")}
        actions={
          <Button onClick={() => provision.mutate()} disabled={provision.isPending}>
            {provision.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Sparkles className="h-4 w-4" />
            )}
            {tRoles("provisionDefaults")}
          </Button>
        }
      />

      <SectionTitle>{tRoles("provisioned")}</SectionTitle>
      {rolesQuery.isLoading ? (
        <div className="grid place-items-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : rolesQuery.error || !rolesQuery.data ? (
        <Card>
          <CardContent padding="lg">
            <p className="flex items-center gap-2 text-ink-3">
              <ShieldCheck className="h-4 w-4" />
              {rolesQuery.error instanceof BffApiError ? rolesQuery.error.message : "Failed to load"}
            </p>
          </CardContent>
        </Card>
      ) : (
        <DataTable columns={columns} data={rolesQuery.data} rowKey={(r) => r.id} />
      )}
    </>
  );
}
