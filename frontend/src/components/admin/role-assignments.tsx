"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  AlertTriangle,
  Briefcase,
  Calculator,
  Check,
  Gauge,
  Loader2,
  Search,
  ShieldCheck,
  Stethoscope,
  UserCog,
  UserMinus,
  UserPlus,
  Wallet,
} from "lucide-react";
import { useTranslations } from "next-intl";
import { useMemo, useState } from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { IconTile } from "@/components/ui/icon-tile";
import { Input } from "@/components/ui/input";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { cn } from "@/lib/utils";

type Holder = {
  userId: string;
  username: string;
  email: string;
  assignmentId: string;
};

type FunctionRow = {
  code: string;
  roleId: string;
  roleName: string;
  scopeType: string;
  slug: string | null;
  permissionCount: number;
  holders: Holder[];
};

type Account = { id: string; username: string; email: string; status: string };

type Payload = { functions: FunctionRow[]; accounts: Account[] };

const ICONS: Record<string, typeof UserCog> = {
  HR_ADMIN: ShieldCheck,
  HR_DIRECTOR: Briefcase,
  PAYROLL_MANAGER: Wallet,
  RECRUITER: UserPlus,
  HR_CONTROLLER: Gauge,
  OCCUPATIONAL_DOCTOR: Stethoscope,
  ACCOUNTANT: Calculator,
};

const TONES: Record<
  string,
  "orange" | "info" | "violet" | "success" | "warning" | "teal" | "gray"
> = {
  HR_ADMIN: "violet",
  HR_DIRECTOR: "orange",
  PAYROLL_MANAGER: "success",
  RECRUITER: "info",
  HR_CONTROLLER: "teal",
  OCCUPATIONAL_DOCTOR: "warning",
  ACCOUNTANT: "gray",
};

export function RoleAssignments() {
  const t = useTranslations("admin.roleAssignments");
  const tAdmin = useTranslations("admin");
  const queryClient = useQueryClient();

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "role-assignments"],
    queryFn: () => apiFetch<Payload>("/api/admin/role-assignments"),
  });

  const [dialogRole, setDialogRole] = useState<FunctionRow | null>(null);

  const mutation = useMutation({
    mutationFn: (body: { roleId: string; userId: string | null }) =>
      apiFetch<{ ok: boolean }>("/api/admin/role-assignments", {
        method: "POST",
        body: JSON.stringify(body),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "role-assignments"] });
      setDialogRole(null);
    },
  });

  const functionLabel = (code: string) => ({
    label: t(`functions.${code}.title`),
    description: t(`functions.${code}.description`),
  });

  const assignedCount = data?.functions.filter((f) => f.holders.length > 0).length ?? 0;
  const totalFunctions = data?.functions.length ?? 0;

  return (
    <>
      <PageHeader
        ucBadge={tAdmin("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: tAdmin("landing.title") },
          { label: t("title") },
        ]}
        title={t("title")}
        subtitle={t("subtitle")}
      />

      {isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : error || !data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {error instanceof BffApiError ? error.message : t("loadError")}
        </div>
      ) : (
        <>
          <div className="mb-5 flex flex-wrap items-center gap-3 rounded-[16px] border border-line bg-white px-5 py-4 shadow-xs-brand">
            <div className="flex items-center gap-2.5">
              <IconTile icon={UserCog} tone="orange" size="md" />
              <div>
                <div className="font-display text-[15px] font-bold text-ink">
                  {t("summary.title")}
                </div>
                <div className="text-[12.5px] text-ink-3">
                  {t("summary.coverage", { assigned: assignedCount, total: totalFunctions })}
                </div>
              </div>
            </div>
            <div className="ml-auto flex items-center gap-2 rounded-[10px] bg-bg-soft px-3 py-2 text-[12px] text-ink-2">
              <AlertTriangle className="h-3.5 w-3.5 text-orange-500" />
              {t("summary.rule")}
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
            {data.functions.map((fn) => {
              const { label, description } = functionLabel(fn.code);
              const holder = fn.holders[0];
              const conflict = fn.holders.length > 1;
              return (
                <Card key={fn.roleId} className="h-full">
                  <CardContent padding="lg" className="flex h-full flex-col">
                    <div className="mb-3 flex items-start gap-3">
                      <IconTile
                        icon={ICONS[fn.code] ?? UserCog}
                        tone={TONES[fn.code] ?? "gray"}
                        size="lg"
                      />
                      <div className="min-w-0 flex-1">
                        <h3 className="font-display text-[16px] font-bold tracking-tight text-ink">
                          {label}
                        </h3>
                        <p className="mt-0.5 line-clamp-2 text-[12.5px] text-ink-3">
                          {description}
                        </p>
                      </div>
                    </div>

                    <div className="mb-4 flex flex-wrap gap-1.5">
                      <Badge tone="gray">{t(`scope.${fn.scopeType}`)}</Badge>
                      <Badge tone="info">
                        {t("permissionCount", { count: fn.permissionCount })}
                      </Badge>
                    </div>

                    <div className="mt-auto rounded-[12px] border border-line-soft bg-bg-soft/60 p-3">
                      {holder ? (
                        <div className="flex items-center gap-2.5">
                          <Avatar name={holder.username} size="sm" />
                          <div className="min-w-0 flex-1">
                            <div className="truncate text-[13px] font-semibold text-ink">
                              {holder.username}
                            </div>
                            <div className="truncate text-[11.5px] text-ink-3">{holder.email}</div>
                          </div>
                          {conflict ? (
                            <Badge tone="danger">
                              {t("conflict", { count: fn.holders.length })}
                            </Badge>
                          ) : (
                            <Badge tone="success">{t("assigned")}</Badge>
                          )}
                        </div>
                      ) : (
                        <div className="flex items-center gap-2 text-[12.5px] text-ink-3">
                          <UserMinus className="h-4 w-4 text-ink-4" />
                          {t("unassigned")}
                        </div>
                      )}
                    </div>

                    <div className="mt-3 flex items-center gap-2">
                      <Button
                        variant={holder ? "secondary" : "primary"}
                        size="sm"
                        className="flex-1"
                        onClick={() => setDialogRole(fn)}
                      >
                        <UserCog className="h-3.5 w-3.5" />
                        {holder ? t("reassign") : t("assign")}
                      </Button>
                      {holder && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() =>
                            mutation.mutate({ roleId: fn.roleId, userId: null })
                          }
                          disabled={mutation.isPending}
                        >
                          <UserMinus className="h-3.5 w-3.5" />
                          {t("vacate")}
                        </Button>
                      )}
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </>
      )}

      {dialogRole && (
        <ReassignDialog
          fn={dialogRole}
          accounts={data?.accounts ?? []}
          onClose={() => setDialogRole(null)}
          onConfirm={(userId) => mutation.mutate({ roleId: dialogRole.roleId, userId })}
          pending={mutation.isPending}
          functionLabel={functionLabel(dialogRole.code).label}
        />
      )}
    </>
  );
}

function ReassignDialog({
  fn,
  accounts,
  onClose,
  onConfirm,
  pending,
  functionLabel,
}: {
  fn: FunctionRow;
  accounts: Account[];
  onClose: () => void;
  onConfirm: (userId: string) => void;
  pending: boolean;
  functionLabel: string;
}) {
  const t = useTranslations("admin.roleAssignments");
  const tCommon = useTranslations("common");
  const currentHolderId = fn.holders[0]?.userId ?? null;
  const [selected, setSelected] = useState<string | null>(currentHolderId);
  const [search, setSearch] = useState("");

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return accounts;
    return accounts.filter(
      (a) => a.username.toLowerCase().includes(q) || a.email.toLowerCase().includes(q),
    );
  }, [accounts, search]);

  return (
    <Dialog
      open
      onClose={onClose}
      size="md"
      title={t("dialog.title", { function: functionLabel })}
      subtitle={t("dialog.subtitle")}
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={pending}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            onClick={() => selected && onConfirm(selected)}
            disabled={pending || !selected || selected === currentHolderId}
          >
            {pending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Check className="h-4 w-4" />
            )}
            {t("dialog.confirm")}
          </Button>
        </>
      }
    >
      <div className="relative mb-3">
        <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-4" />
        <Input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder={t("dialog.searchPlaceholder")}
          className="pl-9"
        />
      </div>

      <div className="max-h-[320px] space-y-1.5 overflow-y-auto pr-1">
        {filtered.length === 0 ? (
          <p className="py-8 text-center text-[13px] text-ink-3">{t("dialog.noAccounts")}</p>
        ) : (
          filtered.map((a) => {
            const isSelected = selected === a.id;
            const isCurrent = currentHolderId === a.id;
            return (
              <button
                key={a.id}
                type="button"
                onClick={() => setSelected(a.id)}
                className={cn(
                  "flex w-full items-center gap-3 rounded-[12px] border px-3 py-2.5 text-left transition-all",
                  isSelected
                    ? "border-orange-400 bg-orange-50/60 ring-2 ring-orange-500/15"
                    : "border-line bg-white hover:border-line-strong",
                )}
              >
                <Avatar name={a.username} size="sm" />
                <div className="min-w-0 flex-1">
                  <div className="truncate text-[13px] font-semibold text-ink">{a.username}</div>
                  <div className="truncate text-[11.5px] text-ink-3">{a.email}</div>
                </div>
                {isCurrent && <Badge tone="success">{t("dialog.current")}</Badge>}
                {isSelected && !isCurrent && (
                  <Check className="h-4 w-4 shrink-0 text-orange-500" />
                )}
              </button>
            );
          })
        )}
      </div>
    </Dialog>
  );
}
