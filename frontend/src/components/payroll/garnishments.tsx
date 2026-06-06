"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Coins,
  Loader2,
  type LucideIcon,
  PauseCircle,
  PlayCircle,
  Plus,
  Scale,
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
import { Field, Input } from "@/components/ui/input";
import { useCan } from "@/hooks/use-can";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney } from "@/lib/format";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  CreateGarnishmentRequest,
  GarnishmentResponse,
  GarnishmentStatus,
  GarnishmentType,
} from "@/server/ksm/modules/payroll";

const TYPES: GarnishmentType[] = ["ALIMONY", "TAX_LEVY", "CREDITOR"];

function typeTone(type: GarnishmentType | string): "warning" | "info" | "gray" {
  switch (type) {
    case "ALIMONY":
      return "warning";
    case "TAX_LEVY":
      return "info";
    default:
      return "gray";
  }
}

function statusTone(s: GarnishmentStatus | string): "success" | "warning" | "info" | "gray" {
  switch (s) {
    case "ACTIVE":
      return "success";
    case "SUSPENDED":
      return "warning";
    case "COMPLETED":
      return "info";
    default:
      return "gray";
  }
}

export function Garnishments() {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const canManage = useCan("hrm:payroll:run");
  const qc = useQueryClient();

  const [creating, setCreating] = React.useState(false);
  const [toCancel, setToCancel] = React.useState<GarnishmentResponse | null>(null);

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });
  const listQuery = useQuery({
    queryKey: ["hrm", "payroll", "garnishments"],
    queryFn: () => apiFetch<GarnishmentResponse[]>("/api/hrm/payroll/garnishments"),
  });

  const employeeName = React.useMemo(() => {
    const map = new Map<string, EmployeeResponse>();
    for (const e of employeesQuery.data ?? []) map.set(e.id, e);
    return map;
  }, [employeesQuery.data]);

  const rows = React.useMemo(() => {
    const statusOrder: Record<string, number> = { ACTIVE: 0, SUSPENDED: 1, COMPLETED: 2, CANCELLED: 3 };
    const typeOrder: Record<string, number> = { ALIMONY: 0, TAX_LEVY: 1, CREDITOR: 2 };
    return (listQuery.data ?? [])
      .slice()
      .sort(
        (a, b) =>
          (statusOrder[a.status] ?? 0) - (statusOrder[b.status] ?? 0) ||
          (typeOrder[a.type] ?? 0) - (typeOrder[b.type] ?? 0),
      );
  }, [listQuery.data]);

  const stats = React.useMemo(() => {
    const data = listQuery.data ?? [];
    const active = data.filter((g) => g.status === "ACTIVE");
    const remaining = active.reduce((acc, g) => acc + Number(g.remainingBalance ?? 0), 0);
    const monthly = active.reduce((acc, g) => acc + Number(g.monthlyAmount ?? 0), 0);
    return { active: active.length, remaining, monthly };
  }, [listQuery.data]);

  const create = useMutation({
    mutationFn: (body: Omit<CreateGarnishmentRequest, "organizationId">) =>
      apiFetch<GarnishmentResponse>("/api/hrm/payroll/garnishments", { method: "POST", body }),
    onSuccess: () => {
      toast.success(t("garnish.created"));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "garnishments"] });
      setCreating(false);
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("garnish.createError")),
  });

  const cancel = useMutation({
    mutationFn: (id: string) => apiFetch(`/api/hrm/payroll/garnishments/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      toast.success(t("garnish.cancelled"));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "garnishments"] });
      setToCancel(null);
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("garnish.cancelError")),
  });

  const suspend = useMutation({
    mutationFn: (id: string) =>
      apiFetch(`/api/hrm/payroll/garnishments/${id}/suspend`, { method: "PUT" }),
    onSuccess: () => {
      toast.success(t("garnish.suspended"));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "garnishments"] });
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("garnish.suspendError")),
  });

  const resume = useMutation({
    mutationFn: (id: string) =>
      apiFetch(`/api/hrm/payroll/garnishments/${id}/resume`, { method: "PUT" }),
    onSuccess: () => {
      toast.success(t("garnish.resumed"));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "garnishments"] });
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("garnish.resumeError")),
  });

  const isLoading = listQuery.isLoading || employeesQuery.isLoading;

  return (
    <>
      <PageHeader
        ucBadge={t("garnish.uc")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }, { label: t("garnish.title") }]}
        title={t("garnish.title")}
        subtitle={t("garnish.subtitle")}
        actions={
          canManage ? (
            <Button onClick={() => setCreating(true)}>
              <Plus className="h-4 w-4" />
              {t("garnish.new")}
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
            <StatCard icon={Scale} tone="orange" label={t("garnish.stats.active")} value={String(stats.active)} sub={t("garnish.stats.activeSub")} />
            <StatCard icon={Wallet} tone="warning" label={t("garnish.stats.remaining")} value={formatMoney(stats.remaining, { locale, withCurrency: false })} sub={t("garnish.stats.remainingSub")} />
            <StatCard icon={Coins} tone="info" label={t("garnish.stats.monthly")} value={formatMoney(stats.monthly, { locale, withCurrency: false })} sub={t("garnish.stats.monthlySub")} />
          </div>

          <Card>
            <div className="border-b border-line-soft px-6 py-4">
              <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("garnish.table.title")}</h3>
              <p className="text-[12px] text-ink-3">{t("garnish.table.subtitle")}</p>
            </div>
            {rows.length === 0 ? (
              <div className="px-6 py-12 text-center text-[13px] text-ink-3">{t("garnish.table.empty")}</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-[13px]">
                  <thead>
                    <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
                      <th className="px-6 py-3">{t("garnish.cols.employee")}</th>
                      <th className="px-3 py-3">{t("garnish.cols.type")}</th>
                      <th className="px-3 py-3">{t("garnish.cols.beneficiary")}</th>
                      <th className="px-3 py-3 text-right">{t("garnish.cols.monthly")}</th>
                      <th className="px-3 py-3">{t("garnish.cols.progress")}</th>
                      <th className="px-3 py-3">{t("garnish.cols.status")}</th>
                      <th className="px-6 py-3" />
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line-soft">
                    {rows.map((g) => {
                      const emp = employeeName.get(g.employeeId);
                      const total = Number(g.totalAmount);
                      const remaining = Number(g.remainingBalance);
                      const recovered = Math.max(0, total - remaining);
                      const pct = total > 0 ? Math.min(100, Math.round((recovered / total) * 100)) : 0;
                      const isClosed = g.status === "COMPLETED" || g.status === "CANCELLED";
                      return (
                        <tr key={g.id} className={cn("hover:bg-bg-soft", isClosed && "opacity-60")}>
                          <td className="px-6 py-3">
                            <div className="text-[13.5px] font-semibold text-ink">
                              {emp?.actorDisplayName ?? g.employeeId.slice(0, 8)}
                            </div>
                            <div className="font-mono-tabular text-[11px] text-ink-3">{emp?.matricule ?? ""}</div>
                          </td>
                          <td className="px-3 py-3">
                            <Badge tone={typeTone(g.type)} showDot={false}>
                              {t(`garnish.types.${g.type as GarnishmentType}`)}
                            </Badge>
                          </td>
                          <td className="px-3 py-3">
                            <div className="text-[12.5px] text-ink-2">{g.beneficiary}</div>
                            {g.reference && (
                              <div className="font-mono-tabular text-[11px] text-ink-4">{g.reference}</div>
                            )}
                          </td>
                          <td className="font-mono-tabular px-3 py-3 text-right text-ink-2">
                            {formatMoney(Number(g.monthlyAmount), { locale, withCurrency: false })}
                          </td>
                          <td className="px-3 py-3">
                            <div className="flex items-center gap-2">
                              <div className="h-1.5 w-24 overflow-hidden rounded-full bg-line-soft">
                                <div
                                  className={cn(
                                    "h-full rounded-full",
                                    pct >= 100 ? "bg-success-500" : "bg-orange-500",
                                  )}
                                  style={{ width: `${pct}%` }}
                                />
                              </div>
                              <span className="font-mono-tabular text-[11.5px] text-ink-3">{pct}%</span>
                            </div>
                            <div className="font-mono-tabular mt-1 text-[11px] text-ink-4">
                              {t("garnish.remainingLabel", {
                                amount: formatMoney(remaining, { locale, withCurrency: false }),
                              })}
                            </div>
                          </td>
                          <td className="px-3 py-3">
                            <Badge tone={statusTone(g.status)} showDot={false}>
                              {t(`garnish.status.${g.status as GarnishmentStatus}`)}
                            </Badge>
                          </td>
                          <td className="px-6 py-3 text-right">
                            {canManage && (g.status === "ACTIVE" || g.status === "SUSPENDED") && (
                              <div className="flex items-center justify-end gap-1">
                                {g.status === "ACTIVE" ? (
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    className="text-warning-600 hover:bg-warning-50"
                                    onClick={() => suspend.mutate(g.id)}
                                    disabled={suspend.isPending || resume.isPending}
                                    title={t("garnish.suspend")}
                                  >
                                    <PauseCircle className="h-3.5 w-3.5" />
                                  </Button>
                                ) : (
                                  <Button
                                    variant="ghost"
                                    size="sm"
                                    className="text-success-600 hover:bg-success-50"
                                    onClick={() => resume.mutate(g.id)}
                                    disabled={suspend.isPending || resume.isPending}
                                    title={t("garnish.resume")}
                                  >
                                    <PlayCircle className="h-3.5 w-3.5" />
                                  </Button>
                                )}
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="text-danger-600 hover:bg-danger-50"
                                  onClick={() => setToCancel(g)}
                                  title={t("garnish.cancel")}
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
        <CreateDialog
          employees={employeesQuery.data ?? []}
          isSaving={create.isPending}
          onClose={() => setCreating(false)}
          onSubmit={(body) => create.mutate(body)}
          t={t}
        />
      )}

      {toCancel && (
        <Dialog
          open
          onClose={() => setToCancel(null)}
          size="sm"
          title={t("garnish.cancelTitle")}
          subtitle={`${employeeName.get(toCancel.employeeId)?.actorDisplayName ?? toCancel.employeeId.slice(0, 8)} · ${toCancel.beneficiary}`}
          footer={
            <>
              <Button variant="secondary" onClick={() => setToCancel(null)} disabled={cancel.isPending}>
                {t("garnish.dialog.cancel")}
              </Button>
              <Button
                className="bg-danger-600 hover:bg-danger-600/90"
                onClick={() => cancel.mutate(toCancel.id)}
                disabled={cancel.isPending}
              >
                {cancel.isPending ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    {t("garnish.dialog.processing")}
                  </>
                ) : (
                  t("garnish.cancel")
                )}
              </Button>
            </>
          }
        >
          <p className="text-[13.5px] text-ink-2">{t("garnish.cancelConfirm")}</p>
        </Dialog>
      )}
    </>
  );
}

function CreateDialog({
  employees,
  isSaving,
  onClose,
  onSubmit,
  t,
}: {
  employees: EmployeeResponse[];
  isSaving: boolean;
  onClose: () => void;
  onSubmit: (body: Omit<CreateGarnishmentRequest, "organizationId">) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const [employeeId, setEmployeeId] = React.useState("");
  const [type, setType] = React.useState<GarnishmentType>("ALIMONY");
  const [beneficiary, setBeneficiary] = React.useState("");
  const [reference, setReference] = React.useState("");
  const [totalAmount, setTotalAmount] = React.useState("");
  const [monthlyAmount, setMonthlyAmount] = React.useState("");
  const [error, setError] = React.useState<string | null>(null);

  function submit() {
    if (!employeeId || !beneficiary.trim() || totalAmount.trim() === "" || monthlyAmount.trim() === "") {
      setError(t("garnish.validation.required"));
      return;
    }
    if (Number(monthlyAmount) <= 0 || Number(totalAmount) <= 0) {
      setError(t("garnish.validation.positive"));
      return;
    }
    onSubmit({
      employeeId,
      type,
      beneficiary: beneficiary.trim(),
      reference: reference.trim() || null,
      totalAmount: Number(totalAmount),
      monthlyAmount: Number(monthlyAmount),
    });
  }

  return (
    <Dialog
      open
      onClose={onClose}
      size="md"
      title={t("garnish.dialog.title")}
      subtitle={t("garnish.dialog.subtitle")}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isSaving}>
            {t("garnish.dialog.cancel")}
          </Button>
          <Button onClick={submit} disabled={isSaving}>
            {isSaving ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("garnish.dialog.saving")}
              </>
            ) : (
              t("garnish.dialog.save")
            )}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label={t("garnish.fields.employee")} className="sm:col-span-2">
          <select
            value={employeeId}
            onChange={(e) => setEmployeeId(e.target.value)}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            <option value="">{t("garnish.fields.employeePlaceholder")}</option>
            {employees.map((e) => (
              <option key={e.id} value={e.id}>
                {(e.actorDisplayName ?? e.matricule) + " · " + e.matricule}
              </option>
            ))}
          </select>
        </Field>
        <Field label={t("garnish.fields.type")} hint={t("garnish.hints.type")}>
          <select
            value={type}
            onChange={(e) => setType(e.target.value as GarnishmentType)}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            {TYPES.map((tp) => (
              <option key={tp} value={tp}>
                {t(`garnish.types.${tp}`)}
              </option>
            ))}
          </select>
        </Field>
        <Field label={t("garnish.fields.reference")} hint={t("garnish.hints.reference")}>
          <Input value={reference} onChange={(e) => setReference(e.target.value)} placeholder={t("garnish.fields.referencePlaceholder")} />
        </Field>
        <Field label={t("garnish.fields.beneficiary")} className="sm:col-span-2">
          <Input value={beneficiary} onChange={(e) => setBeneficiary(e.target.value)} placeholder={t("garnish.fields.beneficiaryPlaceholder")} />
        </Field>
        <Field label={t("garnish.fields.totalAmount")} hint={t("garnish.hints.totalAmount")}>
          <Input type="number" min={0} value={totalAmount} onChange={(e) => setTotalAmount(e.target.value)} placeholder="0" />
        </Field>
        <Field label={t("garnish.fields.monthlyAmount")} hint={t("garnish.hints.monthlyAmount")}>
          <Input type="number" min={0} value={monthlyAmount} onChange={(e) => setMonthlyAmount(e.target.value)} placeholder="0" />
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
  tone: "orange" | "warning" | "info";
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
