"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  CalendarClock,
  ClipboardList,
  Coins,
  Loader2,
  Lock,
  type LucideIcon,
  Pencil,
  Search,
  TimerReset,
  Users,
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
import { formatPeriodFr } from "@/lib/payroll-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { PayVariableResponse } from "@/server/ksm/modules/payroll";

/** Numeric fields captured per employee/period — mirrors CapturePayVariableRequest. */
type VariableForm = {
  overtimeHoursDay: string;
  overtimeHoursNight: string;
  overtimeHoursSundayHoliday: string;
  bonuses: string;
  unpaidAbsenceDays: string;
  advances: string;
  workedDaysOverride: string;
};

const EMPTY_FORM: VariableForm = {
  overtimeHoursDay: "",
  overtimeHoursNight: "",
  overtimeHoursSundayHoliday: "",
  bonuses: "",
  unpaidAbsenceDays: "",
  advances: "",
  workedDaysOverride: "",
};

function currentPeriod(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

export function PayVariables() {
  const t = useTranslations("payroll");
  const locale = useLocale() as "fr" | "en";
  const canEdit = useCan("hrm:payroll:run");
  const queryClient = useQueryClient();

  const [period, setPeriod] = React.useState<string>(currentPeriod);
  const [search, setSearch] = React.useState("");
  const [editing, setEditing] = React.useState<EmployeeResponse | null>(null);

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const variablesQuery = useQuery({
    queryKey: ["hrm", "payroll", "variables", period],
    queryFn: () =>
      apiFetch<PayVariableResponse[]>(
        `/api/hrm/payroll/variables?period=${encodeURIComponent(period)}`,
      ),
  });

  const variablesByEmployee = React.useMemo(() => {
    const map = new Map<string, PayVariableResponse>();
    for (const v of variablesQuery.data ?? []) map.set(v.employeeId, v);
    return map;
  }, [variablesQuery.data]);

  const employees = React.useMemo(() => {
    const list = (employeesQuery.data ?? []).filter((e) => e.status !== "TERMINATED");
    const q = search.trim().toLowerCase();
    const filtered = q
      ? list.filter(
          (e) =>
            (e.actorDisplayName ?? "").toLowerCase().includes(q) ||
            e.matricule.toLowerCase().includes(q) ||
            (e.departmentCode ?? "").toLowerCase().includes(q),
        )
      : list;
    return filtered.sort((a, b) =>
      (a.actorDisplayName ?? a.matricule).localeCompare(b.actorDisplayName ?? b.matricule),
    );
  }, [employeesQuery.data, search]);

  const stats = React.useMemo(() => {
    const vars = variablesQuery.data ?? [];
    const totalEmployees = (employeesQuery.data ?? []).filter(
      (e) => e.status !== "TERMINATED",
    ).length;
    const captured = vars.length;
    const totalBonuses = vars.reduce((acc, v) => acc + Number(v.bonuses ?? 0), 0);
    const totalOvertime = vars.reduce(
      (acc, v) =>
        acc +
        Number(v.overtimeHoursDay ?? 0) +
        Number(v.overtimeHoursNight ?? 0) +
        Number(v.overtimeHoursSundayHoliday ?? 0),
      0,
    );
    return { totalEmployees, captured, totalBonuses, totalOvertime };
  }, [variablesQuery.data, employeesQuery.data]);

  const capture = useMutation({
    mutationFn: (input: { employeeId: string; form: VariableForm }) => {
      const f = input.form;
      const num = (s: string) => (s.trim() === "" ? 0 : Number(s));
      const workedDays = f.workedDaysOverride.trim() === "" ? null : Number(f.workedDaysOverride);
      return apiFetch<PayVariableResponse>("/api/hrm/payroll/variables", {
        method: "POST",
        body: {
          employeeId: input.employeeId,
          period,
          overtimeHoursDay: num(f.overtimeHoursDay),
          overtimeHoursNight: num(f.overtimeHoursNight),
          overtimeHoursSundayHoliday: num(f.overtimeHoursSundayHoliday),
          bonuses: num(f.bonuses),
          unpaidAbsenceDays: num(f.unpaidAbsenceDays),
          advances: num(f.advances),
          workedDaysOverride: workedDays,
        },
      });
    },
    onSuccess: () => {
      toast.success(t("variables.saved"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "payroll", "variables", period] });
      setEditing(null);
    },
    onError: (err) => {
      toast.error(err instanceof BffApiError ? err.message : t("variables.saveError"));
    },
  });

  const isLoading = employeesQuery.isLoading || variablesQuery.isLoading;
  const error = employeesQuery.error ?? variablesQuery.error;

  return (
    <>
      <PageHeader
        ucBadge={t("variables.uc")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }, { label: t("variables.title") }]}
        title={t("variables.title")}
        subtitle={t("variables.subtitle")}
        actions={
          <div className="flex items-center gap-2">
            <div className="flex items-center gap-2 rounded-[11px] border border-line bg-white px-3 py-1.5 shadow-xs-brand">
              <CalendarClock className="h-4 w-4 text-ink-3" />
              <input
                type="month"
                value={period}
                onChange={(e) => setPeriod(e.target.value || currentPeriod())}
                className="bg-transparent text-[13px] font-semibold text-ink outline-none"
                aria-label={t("variables.period")}
              />
            </div>
          </div>
        }
      />

      {isLoading ? (
        <div className="grid place-items-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {error instanceof BffApiError ? error.message : "—"}
        </div>
      ) : (
        <div className="flex flex-col gap-5">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <StatCard
              icon={Users}
              tone="orange"
              label={t("variables.stats.employees")}
              value={String(stats.totalEmployees)}
              sub={t("variables.stats.employeesSub", { count: stats.captured })}
            />
            <StatCard
              icon={ClipboardList}
              tone="info"
              label={t("variables.stats.captured")}
              value={`${stats.captured}/${stats.totalEmployees}`}
              sub={t("variables.stats.capturedSub")}
            />
            <StatCard
              icon={TimerReset}
              tone="violet"
              label={t("variables.stats.overtime")}
              value={`${formatHours(stats.totalOvertime, locale)} h`}
              sub={t("variables.stats.overtimeSub")}
            />
            <StatCard
              icon={Coins}
              tone="success"
              label={t("variables.stats.bonuses")}
              value={formatMoney(stats.totalBonuses, { locale, withCurrency: false })}
              sub={t("variables.stats.bonusesSub")}
            />
          </div>

          <Card>
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line-soft px-6 py-4">
              <div>
                <h3 className="text-[15px] font-bold tracking-tight text-ink">
                  {t("variables.table.title")}
                </h3>
                <p className="text-[12px] text-ink-3">
                  {t("variables.table.subtitle", { period: formatPeriodFr(period) })}
                </p>
              </div>
              <div className="flex items-center gap-2 rounded-[11px] border border-line bg-white px-3 py-1.5 shadow-xs-brand">
                <Search className="h-4 w-4 text-ink-3" />
                <input
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder={t("variables.searchPlaceholder")}
                  className="w-44 bg-transparent text-[13px] text-ink outline-none placeholder:text-ink-4"
                />
              </div>
            </div>

            {employees.length === 0 ? (
              <div className="px-6 py-10 text-center text-[13px] text-ink-3">
                {t("variables.table.empty")}
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-[13px]">
                  <thead>
                    <tr className="border-b border-line-soft text-left text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
                      <th className="px-6 py-3">{t("variables.table.employee")}</th>
                      <th className="px-3 py-3 text-right">{t("variables.cols.overtimeDay")}</th>
                      <th className="px-3 py-3 text-right">{t("variables.cols.overtimeNight")}</th>
                      <th className="px-3 py-3 text-right">{t("variables.cols.overtimeSunday")}</th>
                      <th className="px-3 py-3 text-right">{t("variables.cols.bonuses")}</th>
                      <th className="px-3 py-3 text-right">{t("variables.cols.absences")}</th>
                      <th className="px-3 py-3 text-right">{t("variables.cols.advances")}</th>
                      <th className="px-3 py-3 text-right">{t("variables.cols.workedDays")}</th>
                      <th className="px-4 py-3">{t("variables.cols.state")}</th>
                      <th className="px-6 py-3" />
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line-soft">
                    {employees.map((emp) => {
                      const v = variablesByEmployee.get(emp.id) ?? null;
                      return (
                        <tr key={emp.id} className="hover:bg-bg-soft">
                          <td className="px-6 py-3">
                            <div className="flex items-center gap-2.5">
                              <Initials name={emp.actorDisplayName ?? emp.matricule} />
                              <div className="min-w-0">
                                <div className="truncate text-[13.5px] font-semibold text-ink">
                                  {emp.actorDisplayName ?? emp.matricule}
                                </div>
                                <div className="font-mono-tabular text-[11px] text-ink-3">
                                  {emp.matricule}
                                  {emp.departmentCode ? ` · ${emp.departmentCode}` : ""}
                                </div>
                              </div>
                            </div>
                          </td>
                          <NumCell value={v?.overtimeHoursDay} suffix="h" locale={locale} />
                          <NumCell value={v?.overtimeHoursNight} suffix="h" locale={locale} />
                          <NumCell value={v?.overtimeHoursSundayHoliday} suffix="h" locale={locale} />
                          <MoneyCell value={v?.bonuses} locale={locale} />
                          <NumCell value={v?.unpaidAbsenceDays} suffix="j" locale={locale} />
                          <MoneyCell value={v?.advances} locale={locale} />
                          <td className="font-mono-tabular px-3 py-3 text-right text-ink-2">
                            {v?.workedDaysOverride != null ? v.workedDaysOverride : "—"}
                          </td>
                          <td className="px-4 py-3">
                            {v?.locked ? (
                              <Badge tone="gray" showDot={false}>
                                <Lock className="h-3 w-3" /> {t("variables.state.locked")}
                              </Badge>
                            ) : v ? (
                              <Badge tone="success" showDot={false}>
                                {t("variables.state.captured")}
                              </Badge>
                            ) : (
                              <Badge tone="warning" showDot={false}>
                                {t("variables.state.empty")}
                              </Badge>
                            )}
                          </td>
                          <td className="px-6 py-3 text-right">
                            <Button
                              variant="secondary"
                              size="sm"
                              disabled={!canEdit || v?.locked}
                              onClick={() => setEditing(emp)}
                              title={v?.locked ? t("variables.state.lockedHint") : undefined}
                            >
                              <Pencil className="h-3 w-3" />
                              {v ? t("variables.edit") : t("variables.capture")}
                            </Button>
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

      {editing && (
        <CaptureDialog
          employee={editing}
          period={period}
          existing={variablesByEmployee.get(editing.id) ?? null}
          isSaving={capture.isPending}
          onClose={() => setEditing(null)}
          onSubmit={(form) => capture.mutate({ employeeId: editing.id, form })}
          t={t}
        />
      )}
    </>
  );
}

function CaptureDialog({
  employee,
  period,
  existing,
  isSaving,
  onClose,
  onSubmit,
  t,
}: {
  employee: EmployeeResponse;
  period: string;
  existing: PayVariableResponse | null;
  isSaving: boolean;
  onClose: () => void;
  onSubmit: (form: VariableForm) => void;
  t: ReturnType<typeof useTranslations<"payroll">>;
}) {
  const [form, setForm] = React.useState<VariableForm>(() =>
    existing
      ? {
          overtimeHoursDay: toInput(existing.overtimeHoursDay),
          overtimeHoursNight: toInput(existing.overtimeHoursNight),
          overtimeHoursSundayHoliday: toInput(existing.overtimeHoursSundayHoliday),
          bonuses: toInput(existing.bonuses),
          unpaidAbsenceDays: toInput(existing.unpaidAbsenceDays),
          advances: toInput(existing.advances),
          workedDaysOverride:
            existing.workedDaysOverride != null ? String(existing.workedDaysOverride) : "",
        }
      : EMPTY_FORM,
  );

  const set = (key: keyof VariableForm) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((prev) => ({ ...prev, [key]: e.target.value }));

  return (
    <Dialog
      open
      onClose={onClose}
      size="lg"
      title={existing ? t("variables.dialog.editTitle") : t("variables.dialog.captureTitle")}
      subtitle={`${employee.actorDisplayName ?? employee.matricule} · ${formatPeriodFr(period)}`}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isSaving}>
            {t("variables.dialog.cancel")}
          </Button>
          <Button onClick={() => onSubmit(form)} disabled={isSaving}>
            {isSaving ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                {t("variables.dialog.saving")}
              </>
            ) : (
              t("variables.dialog.save")
            )}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label={t("variables.cols.overtimeDay")} hint={t("variables.hints.hours")}>
          <Input type="number" min={0} step={0.5} value={form.overtimeHoursDay} onChange={set("overtimeHoursDay")} />
        </Field>
        <Field label={t("variables.cols.overtimeNight")} hint={t("variables.hints.hours")}>
          <Input type="number" min={0} step={0.5} value={form.overtimeHoursNight} onChange={set("overtimeHoursNight")} />
        </Field>
        <Field label={t("variables.cols.overtimeSunday")} hint={t("variables.hints.hours")}>
          <Input
            type="number"
            min={0}
            step={0.5}
            value={form.overtimeHoursSundayHoliday}
            onChange={set("overtimeHoursSundayHoliday")}
          />
        </Field>
        <Field label={t("variables.cols.absences")} hint={t("variables.hints.days")}>
          <Input type="number" min={0} step={0.5} value={form.unpaidAbsenceDays} onChange={set("unpaidAbsenceDays")} />
        </Field>
        <Field label={t("variables.cols.bonuses")} hint={t("variables.hints.money")}>
          <Input type="number" min={0} step={1} value={form.bonuses} onChange={set("bonuses")} />
        </Field>
        <Field label={t("variables.cols.advances")} hint={t("variables.hints.money")}>
          <Input type="number" min={0} step={1} value={form.advances} onChange={set("advances")} />
        </Field>
        <Field
          label={t("variables.cols.workedDays")}
          hint={t("variables.hints.workedDays")}
          className="sm:col-span-2"
        >
          <Input
            type="number"
            min={0}
            max={31}
            step={1}
            placeholder={t("variables.hints.workedDaysPlaceholder")}
            value={form.workedDaysOverride}
            onChange={set("workedDaysOverride")}
          />
        </Field>
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
  tone: "orange" | "info" | "violet" | "success";
  label: string;
  value: string;
  sub: string;
}) {
  return (
    <Card className="p-5">
      <div className="flex items-start justify-between">
        <div className="min-w-0">
          <div className="text-[11px] font-semibold uppercase tracking-[0.05em] text-ink-3">
            {label}
          </div>
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

function Initials({ name }: { name: string }) {
  const initials = name
    .split(/\s+/)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase() ?? "")
    .join("");
  return (
    <span className="grid h-9 w-9 shrink-0 place-items-center rounded-[10px] bg-bg-soft text-[12px] font-bold text-ink-2">
      {initials || "?"}
    </span>
  );
}

function NumCell({
  value,
  suffix,
  locale,
}: {
  value: number | string | null | undefined;
  suffix: string;
  locale: "fr" | "en";
}) {
  return (
    <td
      className={cn(
        "font-mono-tabular px-3 py-3 text-right",
        value == null ? "text-ink-4" : "text-ink-2",
      )}
    >
      {value == null ? "—" : `${formatHours(Number(value), locale)} ${suffix}`}
    </td>
  );
}

function MoneyCell({
  value,
  locale,
}: {
  value: number | string | null | undefined;
  locale: "fr" | "en";
}) {
  return (
    <td
      className={cn(
        "font-mono-tabular px-3 py-3 text-right",
        value == null ? "text-ink-4" : "text-ink",
      )}
    >
      {value == null ? "—" : formatMoney(Number(value), { locale, withCurrency: false })}
    </td>
  );
}

function toInput(value: number | string | null | undefined): string {
  if (value == null) return "";
  const n = Number(value);
  return n === 0 ? "0" : String(n);
}

function formatHours(n: number, locale: "fr" | "en"): string {
  return n.toLocaleString(locale === "fr" ? "fr-FR" : "en-US", {
    maximumFractionDigits: 1,
  });
}
