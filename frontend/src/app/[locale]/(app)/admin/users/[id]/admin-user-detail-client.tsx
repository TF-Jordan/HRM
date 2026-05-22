"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { ArrowLeft, Plus, Trash2, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Link } from "@/i18n/navigation";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  useAssignRole,
  useRevokeAssignment,
  useRoles,
  useUserAssignments,
  useUsers,
} from "@/hooks/modules/useAdmin";

export function AdminUserDetailClient({ userId }: { userId: string }) {
  const t = useTranslations("admin.userDetail");
  const tCommon = useTranslations("common");
  const tNav = useTranslations("navigation");
  const users = useUsers();
  const roles = useRoles();
  const assignments = useUserAssignments(userId);
  const assign = useAssignRole(userId);
  const revoke = useRevokeAssignment(userId);
  const [open, setOpen] = React.useState(false);
  const [selectedRole, setSelectedRole] = React.useState<string>("");

  const user = users.data?.find((u) => u.id === userId);
  const roleName = (roleId: string) => roles.data?.find((r) => r.id === roleId)?.name ?? roleId;
  const roleCode = (roleId: string) => roles.data?.find((r) => r.id === roleId)?.code ?? "";
  const assignedRoleIds = new Set(assignments.data?.map((a) => a.roleId) ?? []);
  const availableRoles = (roles.data ?? []).filter((r) => !assignedRoleIds.has(r.id));

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        crumbs={[
          { label: tNav("sections.administration") },
          { label: tNav("items.adminUsers"), href: "/admin/users" },
          { label: user?.username ?? userId },
        ]}
        title={user?.username ?? "—"}
        subtitle={user?.email ?? ""}
        actions={
          <Link
            href="/admin/users"
            className="inline-flex items-center gap-1.5 text-[13px] text-ink-3 hover:text-ink"
          >
            <ArrowLeft className="size-4" />
            {t("back")}
          </Link>
        }
      />

      <Card>
        <CardContent className="space-y-4 p-5">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-[16px] font-semibold text-ink">{t("rolesTitle")}</h2>
            <Button
              size="sm"
              onClick={() => {
                setSelectedRole("");
                setOpen(true);
              }}
              disabled={availableRoles.length === 0}
            >
              <Plus className="size-4" />
              {t("assignButton")}
            </Button>
          </div>

          {assignments.isLoading && <Skeleton className="h-20 w-full" />}

          {!assignments.isLoading && assignments.data && assignments.data.length === 0 && (
            <div className="rounded-xl border border-dashed border-line bg-cream-soft/40 p-4 text-[13px] text-ink-3 dark:border-dark-line dark:bg-dark-3">
              {t("noRoles")}
            </div>
          )}

          {!assignments.isLoading && assignments.data && assignments.data.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {assignments.data.map((a) => (
                <div
                  key={a.id}
                  className="flex items-center gap-2 rounded-xl border border-line bg-white px-3 py-2 text-[13px] dark:border-dark-line dark:bg-dark-2"
                >
                  <Badge tone="blue" className="font-mono">
                    {roleCode(a.roleId)}
                  </Badge>
                  <span className="text-ink">{roleName(a.roleId)}</span>
                  <span className="text-[11px] text-ink-4">({a.scope})</span>
                  <button
                    type="button"
                    aria-label={t("revokeButton")}
                    title={t("revokeButton")}
                    onClick={() =>
                      revoke.mutate(a.id, {
                        onSuccess: () => toast.success(t("revokeButton")),
                        onError: (err) => toast.error((err as Error).message),
                      })
                    }
                    className="text-ink-3 hover:text-status-red-600"
                  >
                    <Trash2 className="size-4" />
                  </button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("assignDialog.title")}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <Label>{t("assignDialog.role")}</Label>
            <Select value={selectedRole} onValueChange={setSelectedRole}>
              <SelectTrigger>
                <SelectValue placeholder="—" />
              </SelectTrigger>
              <SelectContent>
                {availableRoles.map((r) => (
                  <SelectItem key={r.id} value={r.id}>
                    {r.code} — {r.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <DialogFooter>
            <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
              {tCommon("actions.cancel")}
            </Button>
            <Button
              type="button"
              disabled={!selectedRole || assign.isPending}
              onClick={() =>
                assign.mutate(
                  { userId, roleId: selectedRole, scope: "TENANT" },
                  {
                    onSuccess: () => {
                      toast.success(t("assignDialog.submit"));
                      setOpen(false);
                    },
                    onError: (err) => toast.error((err as Error).message),
                  },
                )
              }
            >
              {assign.isPending && <Loader2 className="size-4 animate-spin" />}
              {t("assignDialog.submit")}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
