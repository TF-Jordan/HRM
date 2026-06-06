"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Banknote,
  CheckCircle2,
  ChevronDown,
  ChevronRight,
  Coins,
  FileText,
  Loader2,
  type LucideIcon,
  Plus,
  Wallet,
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
  CalculateFinalSettlementRequest,
  FinalSettlementResponse,
  FinalSettlementStatus,
  TerminationReason,
} from "@/server/ksm/modules/payroll";

const REASONS: TerminationReason[] = [
  "RESIGNATION",
  "DISMISSAL",
  "DISMISSAL_GROSS_MISCONDUCT",
  "END_OF_CONTRACT",
  "RETIREMENT",
  "MUTUAL_AGREEMENT",
  "DEATH",
];

function statusTone(s: FinalSettlementStatus | string): "warning" | "success" {
  return s === "PAID" ? "success" : "warning";
}

export function FinalSettlements() {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const canManage = useCan("hrm:payroll:run");
  const qc = useQueryClient();

  const [creating, setCreating] = React.useState(false);
  const [toPay, setToPay] = React.useState<FinalSettlementResponse | null>(null);
  const [expanded, setExpanded] = React.useState<string | null>(null);

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });
  const listQuery = useQuery({
    queryKey: ["hrm", "payroll", "final-settlements"],
    queryFn: () => apiFetch<FinalSettlementResponse[]>("/api/hrm/payroll/final-settlements"),
  });

  const employeeName = React.useMemo(() => {
    const map = new Map<string, EmployeeResponse>();
    for (const e of employeesQuery.data ?? []) map.set(e.id, e);
    return map;
  }, [employeesQuery.data]);

  const rows = React.useMemo(() => {
    const order: Record<string, number> = { CALCULATED: 0, PAID: 1 };
    return (listQuery.data ?? [])
      .slice()
      .sort(
        (a, b) =>
          (order[a.status] ?? 0) - (order[b.status] ?? 0) ||
          b.departureDate.localeCompare(a.departureDate),
      );
  }, [listQuery.data]);

  const stats = React.useMemo(() => {
    const data = listQuery.data ?? [];
    const calc = data.filter((s) => s.status === "CALCULATED");
    const paid = data.filter((s) => s.status === "PAID");
    const dueNet = calc.reduce((acc, s) => acc + Number(s.netSettlement ?? 0), 0);
    return { calculated: calc.length, paid: paid.length, dueNet };
  }, [listQuery.data]);

  const calculate = useMutation({
    mutationFn: (body: CalculateFinalSettlementRequest) =>
      apiFetch<FinalSettlementResponse>("/api/hrm/payroll/final-settlements", {
        method: "POST",
        body,
      }),
    onSuccess: () => {
      toast.success(t("stc.calculated"));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "final-settlements"] });
      setCreating(false);
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("stc.calcError")),
  });

  const pay = useMutation({
    mutationFn: (id: string) =>
      apiFetch(`/api/hrm/payroll/final-settlements/${id}/pay`, { method: "POST" }),
    onSuccess: () => {
      toast.success(t("stc.paid"));
      qc.invalidateQueries({ queryKey: ["hrm", "payroll", "final-settlements"] });
      setToPay(null);
    },
    onError: (e) => toast.error(e instanceof BffApiError ? e.message : t("stc.payError")),
  });

  const isLoading = listQuery.isLoading || employeesQuery.isLoading;

  return (
    <>
      <PageHeader
        ucBadge={t("stc.uc")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }, { label: t("stc.title") }]}
        title={t("stc.title")}
        subtitle={t("stc.subtitle")}
        actions={
          canManage ? (
            <Button onClick={() => setCreating(true)}>
              <Plus className="h-4 w-4" />
              {t("stc.new")}
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
            <StatCard icon={Coins} tone="warning" label={t("stc.stats.calculated")} value={String(stats.calculated)} sub={t("stc.stats.calculatedSub")} />
            <StatCard icon={CheckCircle2} tone="success" label={t("stc.stats.paid")} value={String(stats.paid)} sub={t("stc.stats.paidSub")} />
            <StatCard icon={Wallet} tone="orange" label={t("stc.stats.dueNet")} value={formatMoney(stats.dueNet, { locale, withCurrency: false })} sub={t("stc.stats.dueNetSub")} />
          </div>

          <Card>
            <div className="border-b border-line-soft px-6 py-4">
              <h3 className="text-[15px] font-bold tracking-tight text-ink">{t("stc.table.title")}</h3>
              <p className="text-[12px] text-ink-3">{t("stc.table.subtitle")}</p>
            </div>
            {rows.length === 0 ? (
              <div className="px-6 py-12 text-center text-[13px] text-ink-3">{t("stc.table.empty")}</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-[13px]">
                  <thead>
                    <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
                      <th className="px-6 py-3">{t("stc.cols.employee")}</th>
                      <th className="px-3 py-3">{t("stc.cols.departure")}</th>
                      <th className="px-3 py-3">{t("stc.cols.reason")}</th>
                      <th className="px-3 py-3 text-right">{t("stc.cols.gross")}</th>
                      <th className="px-3 py-3 text-right">{t("stc.cols.net")}</th>
                      <th className="px-3 py-3">{t("stc.cols.status")}</th>
                      <th className="px-6 py-3" />
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line-soft">
                    {rows.map((s) => {
                      const emp = employeeName.get(s.employeeId);
                      const isOpen = expanded === s.id;
                      return (
                        <React.Fragment key={s.id}>
                          <tr className="hover:bg-bg-soft">
                            <td className="px-6 py-3">
                              <button
                                type="button"
                                onClick={() => setExpanded(isOpen ? null : s.id)}
                                className="flex items-center gap-2 text-left"
                              >
                                {isOpen ? (
                                  <ChevronDown className="h-4 w-4 shrink-0 text-ink-3" />
                                ) : (
                                  <ChevronRight className="h-4 w-4 shrink-0 text-ink-3" />
                                )}
                                <span>
                                  <span className="block text-[13.5px] font-semibold text-ink">
                                    {emp?.actorDisplayName ?? s.employeeId.slice(0, 8)}
                                  </span>
                                  <span className="font-mono-tabular block text-[11px] text-ink-3">
                                    {emp?.matricule ?? ""}
                                  </span>
                                </span>
                              </button>
                            </td>
                            <td className="px-3 py-3 text-[12.5px] text-ink-2">{s.departureDate}</td>
                            <td className="px-3 py-3">
                              <Badge tone="gray" showDot={false}>
                                {t(`stc.reasons.${s.reason as TerminationReason}`)}
                              </Badge>
                            </td>
                            <td className="font-mono-tabular px-3 py-3 text-right text-ink-2">
                              {formatMoney(Number(s.grossSettlement), { locale, withCurrency: false })}
                            </td>
                            <td className="font-mono-tabular px-3 py-3 text-right font-semibold text-ink">
                              {formatMoney(Number(s.netSettlement), { locale, withCurrency: false })}
                            </td>
                            <td className="px-3 py-3">
                              <Badge tone={statusTone(s.status)} showDot={false}>
                                {t(`stc.status.${s.status as FinalSettlementStatus}`)}
                              </Badge>
                            </td>
                            <td className="px-6 py-3 text-right">
                              {canManage && s.status === "CALCULATED" && (
                                <Button variant="secondary" size="sm" onClick={() => setToPay(s)}>
                                  <Banknote className="h-3.5 w-3.5" />
                                  {t("stc.pay")}
                                </Button>
                              )}
                            </td>
                          </tr>
                          {isOpen && (
                            <tr className="bg-bg-soft/40">
                              <td colSpan={7} className="px-6 py-4">
                                <Breakdown s={s} locale={locale} t={t} />
                              </td>
                            </tr>
                          )}
                        </React.Fragment>
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
          employees={employeesQuery.data ?? []}
          isSaving={calculate.isPending}
          onClose={() => setCreating(false)}
          onSubmit={(body) => calculate.mutate(body)}
          t={t}
        />
      )}

      {toPay && (
        <Dialog
          open
          onClose={() => setToPay(null)}
          size="sm"
          title={t("stc.payTitle")}
          subtitle={`${employeeName.get(toPay.employeeId)?.actorDisplayName ?? toPay.employeeId.slice(0, 8)} · ${formatMoney(Number(toPay.netSettlement), { locale, withCurrency: false })}`}
          footer={
            <>
              <Button variant="secondary" onClick={() => setToPay(null)} disabled={pay.isPending}>
                {t("stc.dialog.cancel")}
              </Button>
              <Button onClick={() => pay.mutate(toPay.id)} disabled={pay.isPending}>
                {pay.isPending ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    {t("stc.dialog.processing")}
                  </>
                ) : (
                  t("stc.pay")
                )}
              </Button>
            </>
          }
        >
          <p className="text-[13.5px] text-ink-2">{t("stc.payConfirm")}</p>
        </Dialog>
      )}
    </>
  );
}

function Breakdown({
  s,
  locale,
  t,
}: {
  s: FinalSettlementResponse;
  locale: "fr" | "en";
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const money = (v: number | string) => formatMoney(Number(v), { locale, withCurrency: false });
  const lines: { label: string; value: number | string; sign?: "minus" }[] = [
    { label: t("stc.breakdown.proratedSalary"), value: s.proratedSalary },
    { label: t("stc.breakdown.leaveCompensation"), value: s.leaveCompensation },
    { label: t("stc.breakdown.noticeIndemnity"), value: s.noticeIndemnity },
    { label: t("stc.breakdown.severanceIndemnity"), value: s.severanceIndemnity },
    { label: t("stc.breakdown.gratification"), value: s.gratification },
  ];
  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <div className="rounded-[12px] border border-line-soft bg-white p-4">
        <div className="mb-2 flex items-center gap-2 text-[12px] font-semibold uppercase tracking-[0.05em] text-ink-3">
          <FileText className="h-3.5 w-3.5" />
          {t("stc.breakdown.components")}
        </div>
        <dl className="flex flex-col gap-1.5 text-[12.5px]">
          {lines.map((l) => (
            <div key={l.label} className="flex items-center justify-between">
              <dt className="text-ink-2">{l.label}</dt>
              <dd className="font-mono-tabular text-ink">{money(l.value)}</dd>
            </div>
          ))}
          <div className="mt-1 flex items-center justify-between border-t border-line-soft pt-1.5 font-semibold">
            <dt className="text-ink">{t("stc.breakdown.gross")}</dt>
            <dd className="font-mono-tabular text-ink">{money(s.grossSettlement)}</dd>
          </div>
          <div className="flex items-center justify-between text-danger-600">
            <dt>{t("stc.breakdown.loanDeducted")}</dt>
            <dd className="font-mono-tabular">- {money(s.loanDeducted)}</dd>
          </div>
          <div className="mt-1 flex items-center justify-between border-t border-line pt-1.5 text-[14px] font-bold">
            <dt className="text-ink">{t("stc.breakdown.net")}</dt>
            <dd className="font-mono-tabular text-orange-600">{money(s.netSettlement)}</dd>
          </div>
        </dl>
      </div>
      <div className="rounded-[12px] border border-line-soft bg-white p-4">
        <div className="mb-2 text-[12px] font-semibold uppercase tracking-[0.05em] text-ink-3">
          {t("stc.breakdown.context")}
        </div>
        <dl className="flex flex-col gap-1.5 text-[12.5px]">
          <Row label={t("stc.breakdown.seniority")} value={t("stc.breakdown.years", { count: s.seniorityYears })} />
          <Row label={t("stc.breakdown.period")} value={s.periode} />
          <Row label={t("stc.cols.departure")} value={s.departureDate} />
          <Row label={t("stc.cols.reason")} value={t(`stc.reasons.${s.reason as TerminationReason}`)} />
        </dl>
        <p className="mt-3 text-[11.5px] text-ink-4">{t("stc.breakdown.docHint")}</p>
      </div>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between">
      <dt className="text-ink-2">{label}</dt>
      <dd className="text-ink">{value}</dd>
    </div>
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
  onSubmit: (body: CalculateFinalSettlementRequest) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const [employeeId, setEmployeeId] = React.useState("");
  const [departureDate, setDepartureDate] = React.useState(
    new Date().toISOString().slice(0, 10),
  );
  const [reason, setReason] = React.useState<TerminationReason>("DISMISSAL");
  const [unusedLeaveDays, setUnusedLeaveDays] = React.useState("0");
  const [noticeMonths, setNoticeMonths] = React.useState("0");
  const [accruedGratification, setAccruedGratification] = React.useState("0");
  const [error, setError] = React.useState<string | null>(null);

  function submit() {
    if (!employeeId || !departureDate || !reason) {
      setError(t("stc.validation.required"));
      return;
    }
    onSubmit({
      employeeId,
      departureDate,
      reason,
      unusedLeaveDays: Number(unusedLeaveDays) || 0,
      noticeMonths: Number(noticeMonths) || 0,
      accruedGratification: Number(accruedGratification) || 0,
    });
  }

  return (
    <Dialog
      open
      onClose={onClose}
      size="md"
      title={t("stc.dialog.title")}
      subtitle={t("stc.dialog.subtitle")}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isSaving}>
            {t("stc.dialog.cancel")}
          </Button>
          <Button onClick={submit} disabled={isSaving}>
            {isSaving ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("stc.dialog.calculating")}
              </>
            ) : (
              t("stc.dialog.calculate")
            )}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label={t("stc.fields.employee")} className="sm:col-span-2">
          <select
            value={employeeId}
            onChange={(e) => setEmployeeId(e.target.value)}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            <option value="">{t("stc.fields.employeePlaceholder")}</option>
            {employees.map((e) => (
              <option key={e.id} value={e.id}>
                {(e.actorDisplayName ?? e.matricule) + " · " + e.matricule}
              </option>
            ))}
          </select>
        </Field>
        <Field label={t("stc.fields.departureDate")}>
          <Input type="date" value={departureDate} onChange={(e) => setDepartureDate(e.target.value)} />
        </Field>
        <Field label={t("stc.fields.reason")}>
          <select
            value={reason}
            onChange={(e) => setReason(e.target.value as TerminationReason)}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            {REASONS.map((r) => (
              <option key={r} value={r}>
                {t(`stc.reasons.${r}`)}
              </option>
            ))}
          </select>
        </Field>
        <Field label={t("stc.fields.unusedLeaveDays")} hint={t("stc.hints.unusedLeaveDays")}>
          <Input type="number" min={0} step="0.5" value={unusedLeaveDays} onChange={(e) => setUnusedLeaveDays(e.target.value)} />
        </Field>
        <Field label={t("stc.fields.noticeMonths")} hint={t("stc.hints.noticeMonths")}>
          <Input type="number" min={0} value={noticeMonths} onChange={(e) => setNoticeMonths(e.target.value)} />
        </Field>
        <Field label={t("stc.fields.accruedGratification")} hint={t("stc.hints.accruedGratification")} className="sm:col-span-2">
          <Input type="number" min={0} value={accruedGratification} onChange={(e) => setAccruedGratification(e.target.value)} />
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
          <div className={cn("font-display font-mono-tabular mt-1.5 text-[24px] font-extrabold tracking-tight text-ink")}>
            {value}
          </div>
          <div className="mt-1 text-[11.5px] text-ink-3">{sub}</div>
        </div>
        <IconTile icon={Icon} tone={tone} size="sm" />
      </div>
    </Card>
  );
}
