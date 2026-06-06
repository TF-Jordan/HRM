"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ArrowRight,
  CheckCircle2,
  Clock,
  Loader2,
  type LucideIcon,
  Plus,
  Wallet,
  XCircle,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { toast } from "sonner";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { IconTile } from "@/components/ui/icon-tile";
import { Field, Input, Textarea } from "@/components/ui/input";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney } from "@/lib/format";
import { formatPeriodFr } from "@/lib/payroll-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  CalculateRetroactiveRequest,
  RetroactiveResponse,
  RetroactiveStatus,
} from "@/server/ksm/modules/payroll";

function statusTone(s: RetroactiveStatus): "warning" | "success" | "gray" {
  switch (s) {
    case "PENDING":
      return "warning";
    case "APPLIED":
      return "success";
    default:
      return "gray";
  }
}

function previousPeriod(): string {
  const d = new Date();
  d.setMonth(d.getMonth() - 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}
function currentPeriod(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}

type ConfirmTarget = { kind: "apply" | "cancel"; row: RetroactiveResponse };

export function Retroactive() {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const canManage = useCan("hrm:payroll:run");
  const qc = useQueryClient();

  const [creating, setCreating] = React.useState(false);
  const [confirm, setConfirm] = React.useState<ConfirmTarget | null>(null);

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });
  const listQuery = useQuery({
    queryKey: ["hrm", "payroll", "retroactive"],
    queryFn: () => apiFetch<RetroactiveResponse[]>("/api/hrm/payroll/retroactive"),
  });

  const employeeName = React.useMemo(() => {
    const map = new Map<string, EmployeeResponse>();
    for (const e of employeesQuery.data ?? []) map.set(e.id, e);
    return map;
  }, [employeesQuery.data]);

  const rows = React.useMemo(() => {
    const order: Record<RetroactiveStatus, number> = { PENDING: 0, APPLIED: 1, CANCELLED: 2 };
    return (listQuery.data ?? [])
      .slice()
      .sort((a, b) => order[a.status] - order[b.status] || b.targetPeriod.localeCompare(a.targetPeriod));
  }, [listQuery.data]);

  const stats = React.useMemo(() => {
    const data = listQuery.data ?? [];
    const pending = data.filter((r) => r.status === "PENDING");
    const applied = data.filter((r) => r.status === "APPLIED");
    const pendingNet = pending.reduce((acc, r) => acc + Number(r.deltaNet ?? 0), 0);
    return { pending: pending.length, applied: applied.length, pendingNet };
  }, [listQuery.data]);

  const calculate = useMutation({
    mutationFn: (body: CalculateRetroactiveRequest) =>
      apiFetch<RetroactiveResponse>("/api/hrm/payroll/retroactive", { method: "POST", body }),
    onSuccess: () => {
      toast.success(t("retro.calculated"));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "retroactive"] });
      setCreating(false);
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("retro.calcError")),
  });

  const transition = useMutation({
    mutationFn: (target: ConfirmTarget) =>
      target.kind === "apply"
        ? apiFetch(`/api/hrm/payroll/retroactive/${target.row.id}/apply`, { method: "POST" })
        : apiFetch(`/api/hrm/payroll/retroactive/${target.row.id}`, { method: "DELETE" }),
    onSuccess: (_d, target) => {
      toast.success(target.kind === "apply" ? t("retro.applied") : t("retro.cancelled"));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "retroactive"] });
      setConfirm(null);
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("retro.transitionError")),
  });

  const isLoading = listQuery.isLoading || employeesQuery.isLoading;

  return (
    <>
      <PageHeader
        ucBadge={t("retro.uc")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }, { label: t("retro.title") }]}
        title={t("retro.title")}
        subtitle={t("retro.subtitle")}
        actions={
          canManage ? (
            <Button onClick={() => setCreating(true)}>
              <Plus className="h-4 w-4" />
              {t("retro.new")}
            </Button>
          ) : undefined
        }
      />

      {isLoading ? (
        <div className="grid place-items-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : listQuery.error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {listQuery.error instanceof BffApiError ? listQuery.error.message : "—"}
        </div>
      ) : (
        <div className="flex flex-col gap-5">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <StatCard icon={Clock} tone="warning" label={t("retro.stats.pending")} value={String(stats.pending)} sub={t("retro.stats.pendingSub")} />
            <StatCard icon={CheckCircle2} tone="success" label={t("retro.stats.applied")} value={String(stats.applied)} sub={t("retro.stats.appliedSub")} />
            <StatCard icon={Wallet} tone="orange" label={t("retro.stats.pendingNet")} value={formatMoney(stats.pendingNet, { locale, withCurrency: false })} sub={t("retro.stats.pendingNetSub")} />
          </div>

          <Card>
            <div className="border-b border-line-soft px-6 py-4">
              <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("retro.table.title")}</h3>
              <p className="text-[12px] text-ink-3">{t("retro.table.subtitle")}</p>
            </div>
            {rows.length === 0 ? (
              <div className="px-6 py-12 text-center text-[13px] text-ink-3">{t("retro.table.empty")}</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-[13px]">
                  <thead>
                    <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
                      <th className="px-6 py-3">{t("retro.cols.employee")}</th>
                      <th className="px-3 py-3">{t("retro.cols.periods")}</th>
                      <th className="px-3 py-3">{t("retro.cols.reason")}</th>
                      <th className="px-3 py-3 text-right">{t("retro.cols.deltaGross")}</th>
                      <th className="px-3 py-3 text-right">{t("retro.cols.deltaNet")}</th>
                      <th className="px-3 py-3">{t("retro.cols.status")}</th>
                      <th className="px-6 py-3" />
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line-soft">
                    {rows.map((r) => {
                      const emp = employeeName.get(r.employeeId);
                      return (
                        <tr key={r.id} className={cn("hover:bg-bg-soft", r.status === "CANCELLED" && "opacity-60")}>
                          <td className="px-6 py-3">
                            <div className="text-[13.5px] font-semibold text-ink">
                              {emp?.actorDisplayName ?? r.employeeId.slice(0, 8)}
                            </div>
                            <div className="font-mono-tabular text-[11px] text-ink-3">
                              {emp?.matricule ?? ""}
                            </div>
                          </td>
                          <td className="px-3 py-3">
                            <div className="flex items-center gap-1.5 text-[12.5px] text-ink-2">
                              <span>{formatPeriodFr(r.originPeriod)}</span>
                              <ArrowRight className="h-3 w-3 text-ink-4" />
                              <span className="font-semibold text-ink">{formatPeriodFr(r.targetPeriod)}</span>
                            </div>
                          </td>
                          <td className="max-w-[220px] px-3 py-3">
                            <span className="line-clamp-2 text-[12.5px] text-ink-3">{r.reason ?? "—"}</span>
                          </td>
                          <DeltaCell value={r.deltaGross} locale={locale} />
                          <DeltaCell value={r.deltaNet} locale={locale} strong />
                          <td className="px-3 py-3">
                            <Badge tone={statusTone(r.status)} showDot={false}>
                              {t(`retro.status.${r.status}`)}
                            </Badge>
                          </td>
                          <td className="px-6 py-3 text-right">
                            {canManage && r.status === "PENDING" && (
                              <div className="flex items-center justify-end gap-1.5">
                                <Button variant="secondary" size="sm" onClick={() => setConfirm({ kind: "apply", row: r })}>
                                  <CheckCircle2 className="h-3.5 w-3.5" />
                                  {t("retro.apply")}
                                </Button>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="text-danger-600 hover:bg-danger-50"
                                  onClick={() => setConfirm({ kind: "cancel", row: r })}
                                  title={t("retro.cancel")}
                                >
                                  <XCircle className="h-3.5 w-3.5" />
                                </Button>
                              </div>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
        </div>
      )}

      {creating && (
        <CalculateDialog
          employees={(employeesQuery.data ?? []).filter((e) => e.status !== "TERMINATED")}
          isSaving={calculate.isPending}
          onClose={() => setCreating(false)}
          onSubmit={(body) => calculate.mutate(body)}
          t={t}
        />
      )}

      {confirm && (
        <Dialog
          open
          onClose={() => setConfirm(null)}
          size="sm"
          title={confirm.kind === "apply" ? t("retro.applyTitle") : t("retro.cancelTitle")}
          subtitle={`${employeeName.get(confirm.row.employeeId)?.actorDisplayName ?? confirm.row.employeeId.slice(0, 8)} · ${formatPeriodFr(confirm.row.targetPeriod)}`}
          footer={
            <>
              <Button variant="secondary" onClick={() => setConfirm(null)} disabled={transition.isPending}>
                {t("retro.dialog.cancel")}
              </Button>
              <Button
                className={confirm.kind === "cancel" ? "bg-danger-600 hover:bg-danger-600/90" : undefined}
                onClick={() => transition.mutate(confirm)}
                disabled={transition.isPending}
              >
                {transition.isPending ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    {t("retro.dialog.processing")}
                  </>
                ) : confirm.kind === "apply" ? (
                  t("retro.apply")
                ) : (
                  t("retro.cancel")
                )}
              </Button>
            </>
          }
        >
          <p className="text-[13.5px] text-ink-2">
            {confirm.kind === "apply"
              ? t("retro.applyConfirm", { period: formatPeriodFr(confirm.row.targetPeriod) })
              : t("retro.cancelConfirm")}
          </p>
        </Dialog>
      )}
    </>
  );
}

function CalculateDialog({
  employees,
  isSaving,
  onClose,
  onSubmit,
  t,
}: {
  employees: EmployeeResponse[];
  isSaving: boolean;
  onClose: () => void;
  onSubmit: (body: CalculateRetroactiveRequest) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const [employeeId, setEmployeeId] = React.useState("");
  const [originPeriod, setOriginPeriod] = React.useState(previousPeriod());
  const [targetPeriod, setTargetPeriod] = React.useState(currentPeriod());
  const [newBaseSalary, setNewBaseSalary] = React.useState("");
  const [reason, setReason] = React.useState("");
  const [error, setError] = React.useState<string | null>(null);

  function submit() {
    if (!employeeId || !originPeriod || !targetPeriod || newBaseSalary.trim() === "") {
      setError(t("retro.validation.required"));
      return;
    }
    onSubmit({
      employeeId,
      originPeriod,
      targetPeriod,
      newBaseSalary: Number(newBaseSalary),
      reason: reason.trim() || null,
    });
  }

  return (
    <Dialog
      open
      onClose={onClose}
      size="md"
      title={t("retro.dialog.title")}
      subtitle={t("retro.dialog.subtitle")}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isSaving}>
            {t("retro.dialog.cancel")}
          </Button>
          <Button onClick={submit} disabled={isSaving}>
            {isSaving ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("retro.dialog.calculating")}
              </>
            ) : (
              t("retro.dialog.calculate")
            )}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label={t("retro.fields.employee")} className="sm:col-span-2">
          <select
            value={employeeId}
            onChange={(e) => setEmployeeId(e.target.value)}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            <option value="">{t("retro.fields.employeePlaceholder")}</option>
            {employees.map((e) => (
              <option key={e.id} value={e.id}>
                {(e.actorDisplayName ?? e.matricule) + " · " + e.matricule}
              </option>
            ))}
          </select>
        </Field>
        <Field label={t("retro.fields.originPeriod")} hint={t("retro.hints.originPeriod")}>
          <Input type="month" value={originPeriod} onChange={(e) => setOriginPeriod(e.target.value)} />
        </Field>
        <Field label={t("retro.fields.targetPeriod")} hint={t("retro.hints.targetPeriod")}>
          <Input type="month" value={targetPeriod} onChange={(e) => setTargetPeriod(e.target.value)} />
        </Field>
        <Field label={t("retro.fields.newBaseSalary")} hint={t("retro.hints.newBaseSalary")} className="sm:col-span-2">
          <Input type="number" min={0} value={newBaseSalary} onChange={(e) => setNewBaseSalary(e.target.value)} placeholder="0" />
        </Field>
        <Field label={t("retro.fields.reason")} className="sm:col-span-2">
          <Textarea value={reason} onChange={(e) => setReason(e.target.value)} placeholder={t("retro.fields.reasonPlaceholder")} />
        </Field>
        {error && <p className="text-[12px] text-danger-600 sm:col-span-2">{error}</p>}
      </div>
    </Dialog>
  );
}

function StatCard({
  icon: Icon,
  tone,
  label,
  value,
  sub,
}: {
  icon: LucideIcon;
  tone: "orange" | "warning" | "success";
  label: string;
  value: string;
  sub: string;
}) {
  return (
    <Card className="p-5">
      <div className="flex items-start justify-between">
        <div className="min-w-0">
          <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">{label}</div>
          <div className="font-display font-mono-tabular mt-1.5 text-[24px] font-extrabold tracking-tight text-ink">
            {value}
          </div>
          <div className="mt-1 text-[11.5px] text-ink-3">{sub}</div>
        </div>
        <IconTile icon={Icon} tone={tone} size="sm" />
      </div>
    </Card>
  );
}

function DeltaCell({
  value,
  locale,
  strong,
}: {
  value: number | string | null | undefined;
  locale: "fr" | "en";
  strong?: boolean;
}) {
  const n = Number(value ?? 0);
  const sign = n > 0 ? "+" : "";
  return (
    <td
      className={cn(
        "font-mono-tabular px-3 py-3 text-right",
        strong && "font-semibold",
        n > 0 ? "text-success-600" : n < 0 ? "text-danger-600" : "text-ink-3",
      )}
    >
      {`${sign}${formatMoney(n, { locale, withCurrency: false })}`}
    </td>
  );
}
