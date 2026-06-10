"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ArrowLeft,
  Banknote,
  CheckCircle2,
  ChevronRight,
  Download,
  Loader2,
  Lock,
  RefreshCw,
  ShieldCheck,
  Stamp,
  Undo2,
  Users,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { useCan } from "@/hooks/use-can";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatMoney } from "@/lib/format";
import {
  formatPeriodFr,
  nextPayrollAction,
  payrollStatusTone,
  payrollStepperState,
  type PayrollRunAction,
} from "@/lib/payroll-status";
import { cn } from "@/lib/utils";
import type {
  EmployeeResponse,
} from "@/server/ksm/modules/employees";
import type {
  PayrollEntryResponse,
  PayrollRunResponse,
  PayrollRunStatus,
} from "@/server/ksm/modules/payroll";

export function PayrollRunDetail({ runId }: { runId: string }) {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  // Validation/rejection is HR-admin only; the payroll manager keeps the recalculate ability.
  const canValidate = useCan("hrm:payroll:validate");
  const canRun = useCan("hrm:payroll:run");
  const qc = useQueryClient();
  const [rejectOpen, setRejectOpen] = React.useState(false);
  const [rejectReason, setRejectReason] = React.useState("");

  const runQuery = useQuery({
    queryKey: ["hrm", "payroll", runId],
    queryFn: () => apiFetch<PayrollRunResponse>(`/api/hrm/payroll/${runId}`),
  });
  const entriesQuery = useQuery({
    queryKey: ["hrm", "payroll", runId, "entries"],
    queryFn: () => apiFetch<PayrollEntryResponse[]>(`/api/hrm/payroll/${runId}/entries`),
  });
  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "all-for-payroll"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const transition = useMutation({
    mutationFn: (action: PayrollRunAction) =>
      apiFetch<PayrollRunResponse>(`/api/hrm/payroll/${runId}/${action}`, { method: "POST" }),
    onSuccess: (data) => {
      toast.success(t(`status.${data.status as PayrollRunStatus}`));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", runId] });
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", runId, "entries"] });
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "runs"] });
    },
    onError: (cause) => {
      toast.error(cause instanceof BffApiError ? cause.message : "—");
    },
  });

  const invalidateRun = React.useCallback(() => {
    qc.invalidateQueries({ queryKey: ["hrm", "payroll", runId] });
    qc.invalidateQueries({ queryKey: ["hrm", "payroll", runId, "entries"] });
    qc.invalidateQueries({ queryKey: ["hrm", "payroll", "runs"] });
  }, [qc, runId]);

  const reject = useMutation({
    mutationFn: (reason: string) =>
      apiFetch<PayrollRunResponse>(`/api/hrm/payroll/${runId}/reject`, {
        method: "POST",
        body: { reason },
      }),
    onSuccess: () => {
      toast.success(t("reject.success"));
      setRejectOpen(false);
      setRejectReason("");
      invalidateRun();
    },
    onError: (cause) => {
      toast.error(cause instanceof BffApiError ? cause.message : "—");
    },
  });

  const recalculate = useMutation({
    mutationFn: (body: { period: string; runType?: string }) =>
      apiFetch<PayrollRunResponse>("/api/hrm/payroll", { method: "POST", body }),
    onSuccess: (data) => {
      toast.success(t(`status.${data.status as PayrollRunStatus}`));
      invalidateRun();
    },
    onError: (cause) => {
      toast.error(cause instanceof BffApiError ? cause.message : "—");
    },
  });

  const run = runQuery.data;
  const action = run ? nextPayrollAction(run.status) : null;
  const canReject = !!run && canValidate && (run.status === "CALCULATED" || run.status === "REVIEW");
  const canRecalculate =
    !!run &&
    canRun &&
    (run.status === "CALCULATED" || run.status === "REVIEW" || run.status === "REJECTED");
  const entries = entriesQuery.data ?? [];
  const employeesById = React.useMemo(() => {
    const m = new Map<string, EmployeeResponse>();
    for (const e of employeesQuery.data ?? []) m.set(e.id, e);
    return m;
  }, [employeesQuery.data]);

  const stepStates = run ? payrollStepperState(run.status) : null;
  const stepEntries: { key: keyof NonNullable<typeof stepStates>; label: string }[] = [
    { key: "variables", label: t("stepper.variables") },
    { key: "calculation", label: t("stepper.calculation") },
    { key: "review", label: t("stepper.review") },
    { key: "validation", label: t("stepper.validation") },
    { key: "payment", label: t("stepper.payment") },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("uc")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/payroll" },
          { label: run ? formatPeriodFr(run.periode) : "…" },
        ]}
        title={run ? t("detail.title", { periode: formatPeriodFr(run.periode) }) : t("title")}
        subtitle={
          run
            ? t("detail.subtitle", {
                status: t(`status.${run.status as PayrollRunStatus}`),
                count: run.nbEmployes,
              })
            : undefined
        }
        actions={
          <>
            <Link href="/payroll">
              <Button variant="secondary">
                <ArrowLeft className="h-4 w-4" /> {t("actions.back")}
              </Button>
            </Link>
            {canRecalculate && (
              <Button
                variant="secondary"
                onClick={() =>
                  run && recalculate.mutate({ period: run.periode, runType: run.runType })
                }
                disabled={recalculate.isPending}
              >
                {recalculate.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <RefreshCw className="h-4 w-4" />
                )}
                {recalculate.isPending ? t("actions.calculating") : t("actions.recalculate")}
              </Button>
            )}
            {canReject && (
              <Button
                variant="secondary"
                className="border-danger-200 text-danger-600 hover:bg-danger-50"
                onClick={() => setRejectOpen(true)}
                disabled={reject.isPending}
              >
                <Undo2 className="h-4 w-4" />
                {t("actions.reject")}
              </Button>
            )}
            {run && action && canValidate && (
              <ActionButton
                action={action}
                isPending={transition.isPending}
                onRun={() => transition.mutate(action)}
                t={t}
              />
            )}
          </>
        }
      />

      <Dialog
        open={rejectOpen}
        onClose={() => {
          if (!reject.isPending) setRejectOpen(false);
        }}
        size="sm"
        title={t("reject.title")}
        subtitle={t("reject.subtitle")}
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() => setRejectOpen(false)}
              disabled={reject.isPending}
            >
              {t("reject.cancel")}
            </Button>
            <Button
              className="bg-danger-600 hover:bg-danger-700"
              onClick={() => reject.mutate(rejectReason)}
              disabled={reject.isPending || rejectReason.trim().length === 0}
            >
              {reject.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Undo2 className="h-4 w-4" />
              )}
              {t("reject.confirm")}
            </Button>
          </>
        }
      >
        <label className="text-[12px] font-semibold uppercase tracking-[0.05em] text-ink-3">
          {t("reject.reasonLabel")}
        </label>
        <textarea
          autoFocus
          value={rejectReason}
          onChange={(e) => setRejectReason(e.target.value)}
          rows={4}
          placeholder={t("reject.reasonPlaceholder")}
          className="mt-2 w-full resize-y rounded-[12px] border border-line bg-white px-3.5 py-2.5 text-[13px] text-ink outline-none focus:border-orange-400 focus:ring-2 focus:ring-orange-100"
        />
      </Dialog>

      {runQuery.isLoading ? (
        <div className="grid place-items-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : runQuery.error || !run ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {runQuery.error instanceof BffApiError ? runQuery.error.message : "—"}
        </div>
      ) : (
        <div className="flex flex-col gap-5">
          {run.status === "REJECTED" && run.rejectionReason && (
            <div className="flex items-start gap-3 rounded-[16px] border border-danger-200 bg-danger-50 px-5 py-4">
              <Undo2 className="mt-0.5 h-5 w-5 shrink-0 text-danger-600" />
              <div className="min-w-0">
                <div className="text-[13px] font-bold text-danger-700">
                  {t("reject.bannerTitle")}
                </div>
                <p className="mt-1 whitespace-pre-wrap text-[13px] text-danger-700/90">
                  {run.rejectionReason}
                </p>
                {run.rejectedAt && (
                  <div className="mt-1.5 text-[11px] text-danger-600/70">
                    {t("reject.rejectedAt", {
                      date: formatDate(run.rejectedAt, { locale }),
                    })}
                  </div>
                )}
                {canRun && (
                  <p className="mt-2 text-[12px] text-ink-3">{t("reject.recalcHint")}</p>
                )}
              </div>
            </div>
          )}

          {/* Stepper */}
          <Card>
            <CardContent padding="md">
              <div className="flex flex-wrap items-center gap-2">
                {stepEntries.map((s, idx) => (
                  <React.Fragment key={s.key}>
                    <span
                      className={cn(
                        "rounded-full px-3.5 py-1.5 text-[11px] font-semibold tracking-wide",
                        stepStates?.[s.key] === "done" && "bg-success-50 text-success-600",
                        stepStates?.[s.key] === "active" && "bg-grad-orange text-white shadow-orange-brand",
                        stepStates?.[s.key] === "pending" && "border border-line bg-white text-ink-3",
                      )}
                    >
                      {s.label}
                    </span>
                    {idx < stepEntries.length - 1 && (
                      <ChevronRight className="h-3.5 w-3.5 text-ink-4" />
                    )}
                  </React.Fragment>
                ))}
                <div className="ml-auto">
                  <Badge tone={payrollStatusTone(run.status)} showDot={false}>
                    {t(`status.${run.status as PayrollRunStatus}`)}
                  </Badge>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Totals */}
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
            <TotalTile
              icon={Users}
              label={t("detail.employee") + "s"}
              value={String(run.nbEmployes)}
              tone="orange"
            />
            <TotalTile
              icon={Download}
              label={t("detail.brut")}
              value={formatMoney(Number(run.totalGross ?? 0), { locale, withCurrency: false })}
              sub="XAF"
              tone="info"
            />
            <TotalTile
              icon={Download}
              label={t("detail.totalDeductions")}
              value={formatMoney(Number(run.totalEmployeeDeductions ?? 0), {
                locale,
                withCurrency: false,
              })}
              sub="XAF"
              tone="warning"
            />
            <TotalTile
              icon={CheckCircle2}
              label={t("detail.net")}
              value={formatMoney(Number(run.totalNet ?? 0), { locale, withCurrency: false })}
              sub="XAF"
              tone="success"
            />
          </div>

          {/* Entries */}
          <Card>
            <div className="flex items-center justify-between border-b border-line-soft px-6 py-4">
              <h3 className="text-[15px] font-bold tracking-tight text-ink">
                {t("detail.entriesTitle")}
              </h3>
              <div className="text-[12px] text-ink-3">
                {entries.length > 0 ? `${entries.length}` : ""}
              </div>
            </div>
            <EntriesTable
              entries={entries}
              employeesById={employeesById}
              loading={entriesQuery.isLoading}
              t={t}
              locale={locale}
              runId={run.id}
            />
          </Card>
        </div>
      )}
    </>
  );
}

const ACTION_CONFIG: Record<
  PayrollRunAction,
  { icon: React.ComponentType<{ className?: string }>; labelKey: string; pendingKey: string }
> = {
  validate: { icon: ShieldCheck, labelKey: "actions.validate", pendingKey: "actions.validating" },
  approve: { icon: Stamp, labelKey: "actions.approve", pendingKey: "actions.approving" },
  "initiate-payment": {
    icon: Banknote,
    labelKey: "actions.initiatePayment",
    pendingKey: "actions.initiatingPayment",
  },
  close: { icon: Lock, labelKey: "actions.close", pendingKey: "actions.closing" },
};

function ActionButton({
  action,
  isPending,
  onRun,
  t,
}: {
  action: PayrollRunAction;
  isPending: boolean;
  onRun: () => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const { icon: Icon, labelKey, pendingKey } = ACTION_CONFIG[action];
  return (
    <Button onClick={onRun} disabled={isPending}>
      {isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Icon className="h-4 w-4" />}
      {isPending ? t(pendingKey) : t(labelKey)}
    </Button>
  );
}

function TotalTile({
  icon: Icon,
  label,
  value,
  sub,
  tone,
}: {
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  value: string;
  sub?: string;
  tone: "orange" | "info" | "warning" | "success";
}) {
  const iconBg = {
    orange: "bg-orange-50 text-orange-600",
    info: "bg-info-50 text-info-600",
    warning: "bg-warning-50 text-warning-600",
    success: "bg-success-50 text-success-600",
  }[tone];
  return (
    <Card>
      <CardContent padding="lg">
        <div className="flex items-start justify-between">
          <div>
            <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
              {label}
            </div>
            <div className="font-display font-mono-tabular mt-2 text-[26px] font-extrabold tracking-tight text-ink">
              {value}
            </div>
            {sub && <div className="mt-1 text-[11px] text-ink-3">{sub}</div>}
          </div>
          <span className={cn("grid h-10 w-10 place-items-center rounded-[12px]", iconBg)}>
            <Icon className="h-5 w-5" />
          </span>
        </div>
      </CardContent>
    </Card>
  );
}

function EntriesTable({
  entries,
  employeesById,
  loading,
  t,
  locale,
  runId,
}: {
  entries: PayrollEntryResponse[];
  employeesById: Map<string, EmployeeResponse>;
  loading: boolean;
  t: ReturnType<typeof useTranslations<"payroll">>;
  locale: "fr" | "en";
  runId: string;
}) {
  if (loading) {
    return (
      <div className="grid place-items-center py-12">
        <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
      </div>
    );
  }
  if (entries.length === 0) {
    return <div className="px-6 py-10 text-center text-[13px] text-ink-3">{t("detail.entriesEmpty")}</div>;
  }
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-[13px]">
        <thead>
          <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
            <th className="px-6 py-3">{t("detail.employee")}</th>
            <th className="px-4 py-3 text-right">{t("detail.salaireBase")}</th>
            <th className="px-4 py-3 text-right">{t("detail.brut")}</th>
            <th className="px-4 py-3 text-right">{t("detail.retenues")}</th>
            <th className="px-4 py-3 text-right">{t("detail.net")}</th>
            <th className="px-4 py-3">{t("detail.channel")}</th>
            <th className="px-4 py-3">{t("detail.paymentStatus")}</th>
            <th className="px-6 py-3" />
          </tr>
        </thead>
        <tbody className="divide-y divide-line-soft">
          {entries.map((e) => {
            const emp = employeesById.get(e.employeeId);
            const name = emp?.actorDisplayName ?? emp?.matricule ?? t("payslip.noEmployee");
            const matricule = emp?.matricule ?? e.employeeId.slice(0, 8);
            return (
              <tr key={e.id} className="hover:bg-bg-soft">
                <td className="px-6 py-3">
                  <div className="flex items-center gap-2.5">
                    <Avatar size="md" name={name} />
                    <div>
                      <div className="text-[13px] font-semibold text-ink">{name}</div>
                      <div className="font-mono-tabular text-[11px] text-ink-3">{matricule}</div>
                    </div>
                  </div>
                </td>
                <td className="font-mono-tabular px-4 py-3 text-right text-ink-2">
                  {formatMoney(Number(e.salaireBase ?? 0), { locale, withCurrency: false })}
                </td>
                <td className="font-mono-tabular px-4 py-3 text-right font-semibold text-ink">
                  {formatMoney(Number(e.brut ?? 0), { locale, withCurrency: false })}
                </td>
                <td className="font-mono-tabular px-4 py-3 text-right text-danger-600">
                  −{formatMoney(Number(e.totalDeductions ?? 0), { locale, withCurrency: false })}
                </td>
                <td className="font-mono-tabular px-4 py-3 text-right font-bold text-ink">
                  {formatMoney(Number(e.net ?? 0), { locale, withCurrency: false })}
                </td>
                <td className="px-4 py-3 text-[12px] text-ink-3">
                  {channelLabel(e.paymentChannel, t)}
                </td>
                <td className="px-4 py-3">
                  <Badge tone={paymentTone(e.paymentStatus)} showDot={false}>
                    {paymentLabel(e.paymentStatus, t)}
                  </Badge>
                </td>
                <td className="px-6 py-3 text-right">
                  <Link href={`/payroll/${runId}/entries/${e.id}`}>
                    <Button variant="secondary" size="sm">
                      {t("actions.viewPayslip")} <ChevronRight className="h-3 w-3" />
                    </Button>
                  </Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

function paymentLabel(
  s: string,
  t: ReturnType<typeof useTranslations<"payroll">>,
): string {
  const keys = ["PENDING", "PROCESSING", "COMPLETED", "FAILED"];
  return keys.includes(s) ? t(`payment.${s as "PENDING"}`) : s;
}

function channelLabel(
  c: string | null,
  t: ReturnType<typeof useTranslations<"payroll">>,
): string {
  if (!c) return "—";
  const keys = ["BANK_TRANSFER", "MTN_MOBILE_MONEY", "ORANGE_MONEY", "CASH"];
  return keys.includes(c) ? t(`channel.${c as "CASH"}`) : c;
}

function paymentTone(s: string): "warning" | "info" | "success" | "danger" | "gray" {
  switch (s) {
    case "PENDING": return "warning";
    case "PROCESSING": return "info";
    case "COMPLETED": return "success";
    case "FAILED": return "danger";
    default: return "gray";
  }
}
