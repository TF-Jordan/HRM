"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Link } from "@/i18n/navigation";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { useRoles, useCreateRole } from "@/hooks/modules/useAdmin";

export function AdminRolesClient() {
  const t = useTranslations("admin.roles");
  const tCommon = useTranslations("common");
  const tNav = useTranslations("navigation");
  const roles = useRoles();
  const create = useCreateRole();
  const [open, setOpen] = React.useState(false);
  const [form, setForm] = React.useState({ code: "", name: "", permissions: "" });

  const submit = () => {
    const perms = form.permissions
      .split(/[\s,;\n]+/)
      .map((p) => p.trim())
      .filter(Boolean);
    if (!form.code || !form.name || perms.length === 0) {
      toast.error("Code, nom et au moins 1 permission requis");
      return;
    }
    create.mutate(
      { code: form.code, name: form.name, permissions: perms, scopeType: "TENANT" },
      {
        onSuccess: () => {
          toast.success(t("form.submit"));
          setOpen(false);
          setForm({ code: "", name: "", permissions: "" });
        },
        onError: (err) => toast.error((err as Error).message),
      },
    );
  };

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        crumbs={[{ label: tNav("sections.administration") }, { label: tNav("items.adminRoles") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus className="size-4" />
            {t("newButton")}
          </Button>
        }
      />

      {roles.isLoading && <Skeleton className="h-32 w-full" />}

      {!roles.isLoading && roles.data && roles.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!roles.isLoading && roles.data && roles.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("table.code"),
                    t("table.name"),
                    t("table.scopeType"),
                    t("table.permissionsCount"),
                  ].map((h, i) => (
                    <th
                      key={i}
                      className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {roles.data.map((r) => (
                  <tr key={r.id} className="hover:bg-brand-50/40 dark:hover:bg-dark-3">
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-mono text-ink">
                      <Link
                        href={`/admin/roles/${r.id}` as never}
                        className="hover:text-brand-700 hover:underline"
                      >
                        {r.code}
                      </Link>
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">
                      {r.name}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
                      <Badge tone="gray">{r.scopeType}</Badge>
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {r.permissions.length}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("newButton")}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label htmlFor="code">{t("form.code")}</Label>
              <Input
                id="code"
                value={form.code}
                onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="name">{t("form.name")}</Label>
              <Input
                id="name"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="permissions">{t("form.permissions")}</Label>
              <textarea
                id="permissions"
                rows={5}
                placeholder="hrm:leave:create&#10;hrm:loan:read&#10;hrm:expense:create"
                value={form.permissions}
                onChange={(e) => setForm({ ...form, permissions: e.target.value })}
                className="w-full rounded-xl border border-line bg-white px-3 py-2 font-mono text-[12.5px] text-ink focus:border-brand-400 focus:outline-none dark:border-dark-line dark:bg-dark-2"
              />
              <p className="text-[11.5px] text-ink-3">Une permission par ligne ou séparées par virgules.</p>
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
              {tCommon("actions.cancel")}
            </Button>
            <Button type="button" onClick={submit} disabled={create.isPending}>
              {create.isPending && <Loader2 className="size-4 animate-spin" />}
              {t("form.submit")}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
