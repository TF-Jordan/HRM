"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { ArrowLeft, Loader2, Save, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Link, useRouter } from "@/i18n/navigation";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  useRole,
  useUpdateRole,
  useDeleteRole,
} from "@/hooks/modules/useAdmin";

export function AdminRoleDetailClient({ roleId }: { roleId: string }) {
  const t = useTranslations("admin.roleDetail");
  const tNav = useTranslations("navigation");
  const router = useRouter();
  const role = useRole(roleId);
  const update = useUpdateRole(roleId);
  const remove = useDeleteRole();
  const [name, setName] = React.useState<string | null>(null);
  const [permsText, setPermsText] = React.useState<string | null>(null);

  const initialName = role.data?.name ?? "";
  const initialPerms = role.data?.permissions.join("\n") ?? "";
  const effectiveName = name ?? initialName;
  const effectivePerms = permsText ?? initialPerms;

  const save = () => {
    const perms = effectivePerms
      .split(/[\s,;\n]+/)
      .map((p) => p.trim())
      .filter(Boolean);
    update.mutate(
      { name: effectiveName, permissions: perms },
      {
        onSuccess: () => toast.success(t("saveButton")),
        onError: (err) => toast.error((err as Error).message),
      },
    );
  };

  const del = () => {
    if (!confirm("Supprimer ce rôle ? Les assignations existantes seront révoquées.")) return;
    remove.mutate(roleId, {
      onSuccess: () => {
        toast.success("Rôle supprimé");
        router.push("/admin/roles");
      },
      onError: (err) => toast.error((err as Error).message),
    });
  };

  if (role.isLoading) return <Skeleton className="h-64 w-full" />;
  if (!role.data) return null;

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        crumbs={[
          { label: tNav("sections.administration") },
          { label: tNav("items.adminRoles"), href: "/admin/roles" },
          { label: role.data.code },
        ]}
        title={role.data.name}
        subtitle={role.data.code}
        actions={
          <Link
            href="/admin/roles"
            className="inline-flex items-center gap-1.5 text-[13px] text-ink-3 hover:text-ink"
          >
            <ArrowLeft className="size-4" />
            {t("back")}
          </Link>
        }
      />

      <Card>
        <CardContent className="space-y-4 p-5">
          <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
            <div className="space-y-1.5 md:col-span-2">
              <Label htmlFor="name">Nom</Label>
              <Input id="name" value={effectiveName} onChange={(e) => setName(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>Code (lecture seule)</Label>
              <Input value={role.data.code} disabled />
            </div>
            <div className="space-y-1.5">
              <Label>Portée (lecture seule)</Label>
              <Badge tone="gray">{role.data.scopeType}</Badge>
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="permissions">{t("permissionsTitle")}</Label>
            <textarea
              id="permissions"
              rows={14}
              value={effectivePerms}
              onChange={(e) => setPermsText(e.target.value)}
              className="w-full rounded-xl border border-line bg-white px-3 py-2 font-mono text-[12.5px] text-ink focus:border-brand-400 focus:outline-none dark:border-dark-line dark:bg-dark-2"
            />
            <p className="text-[11.5px] text-ink-3">Une permission par ligne.</p>
          </div>
          <div className="flex justify-between gap-2">
            <Button variant="secondary" onClick={del} disabled={remove.isPending}>
              <Trash2 className="size-4" />
              {t("deleteButton")}
            </Button>
            <Button onClick={save} disabled={update.isPending}>
              {update.isPending && <Loader2 className="size-4 animate-spin" />}
              <Save className="size-4" />
              {t("saveButton")}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
