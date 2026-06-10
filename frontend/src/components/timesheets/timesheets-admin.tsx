"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, Loader2, Search, XCircle } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { PeriodPicker } from "@/components/timesheets/period-picker";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { StatCard, StatCardGrid } from "@/components/ui/stat-card";
import { useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { timesheetStatusTone, timesheetTotalHours } from "@/lib/timesheet-status";
import { cn } from "@/lib/utils";
import type { EnrichedTimesheetResponse } from "@/server/ksm/modules/timesheets";

type FilterKey = "SUBMITTED" | "VALIDATED" | "REJECTED" | "DRAFT" | "ALL";

function currentPeriode(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

const norm = (s: string) => s.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
const num = (v: number | string) => Number(v).toFixed(1);

export function TimesheetsAdmin() {
  const t = useTranslations("timesheets");
  const tAdmin = useTranslations("timesheets.admin");
  const tDetail = useTranslations("timesheets.detail");
  const tErrors = useTranslations("errors");
  const router = useRouter();
  const queryClient = useQueryClient();

  const [periode, setPeriode] = React.useState(currentPeriode());
  const [filter, setFilter] = React.useState<FilterKey>("SUBMITTED");
  const [search, setSearch] = React.useState("");
  const [actingId, setActingId] = React.useState<string | null>(null);

  // Reject modal state
  const [rejectTarget, setRejectTarget] = React.useState<string | null>(null);
  const [rejectComment, setRejectComment] = React.useState("");
  const [rejectError, setRejectError] = React.useState(false);

  const query = useQuery({
    queryKey: ["hrm", "timesheets", "org", periode],
    queryFn: () =>
      apiFetch<EnrichedTimesheetResponse[]>(`/api/hrm/timesheets?periode=${encodeURIComponent(periode)}`),
    refetchInterval: 60_000,
  });

  const all = React.useMemo(() => query.data ?? [], [query.data]);

  const counts = React.useMemo(() => {
    const c = { DRAFT: 0, SUBMITTED: 0, VALIDATED: 0, REJECTED: 0, ALL: all.length, totalHours: 0, absences: 0 };
    for (const ts of all) {
      c[ts.status] += 1;
      c.totalHours += timesheetTotalHours(ts);
      c.absences += Number(ts.absencesNonJustifiees);
    }
    return c;
  }, [all]);

  const validateMutation = useMutation({
    mutationFn: (id: string) =>
      apiFetch<EnrichedTimesheetResponse>(`/api/hrm/timesheets/${id}/validate`, { method: "POST" }),
    onMutate: (id) => setActingId(id),
    onSuccess: () => {
      toast.success(tDetail("validateSuccess"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "timesheets"] });
    },
    onError: (cause) => toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown")),
    onSettled: () => setActingId(null),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, comment }: { id: string; comment: string }) =>
      apiFetch<EnrichedTimesheetResponse>(`/api/hrm/timesheets/${id}/reject`, {
        method: "POST",
        body: { comment },
      }),
    onMutate: ({ id }) => setActingId(id),
    onSuccess: () => {
      toast.success(tDetail("rejectSuccess"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "timesheets"] });
      setRejectTarget(null);
      setRejectComment("");
    },
    onError: (cause) => toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown")),
    onSettled: () => setActingId(null),
  });

  function handleRejectSubmit() {
    if (!rejectComment.trim()) {
      setRejectError(true);
      return;
    }
    if (rejectTarget) {
      rejectMutation.mutate({ id: rejectTarget, comment: rejectComment.trim() });
    }
  }

  const filtered = React.useMemo(() => {
    const q = norm(search.trim());
    let rows = filter === "ALL" ? all : all.filter((ts) => ts.status === filter);
    if (q) {
      rows = rows.filter((ts) =>
        norm(`${ts.employeeName ?? ""} ${ts.employeeMatricule ?? ""} ${ts.employeeDepartment ?? ""}`).includes(q),
      );
    }
    return [...rows].sort((a, b) => timesheetTotalHours(b) - timesheetTotalHours(a));
  }, [all, filter, search]);

  const tabs: { key: FilterKey; label: string; count: number }[] = [
    { key: "SUBMITTED", label: tAdmin("filters.toValidate"), count: counts.SUBMITTED },
    { key: "VALIDATED", label: t("status.VALIDATED"), count: counts.VALIDATED },
    { key: "REJECTED", label: tAdmin("filters.rejected"), count: counts.REJECTED },
    { key: "DRAFT", label: t("status.DRAFT"), count: counts.DRAFT },
    { key: "ALL", label: tAdmin("filters.all"), count: counts.ALL },
  ];

  const employeeLabel = (ts: EnrichedTimesheetResponse) =>
    ts.employeeName ?? ts.employeeMatricule ?? `${ts.employeeId.slice(0, 8)}…`;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: tAdmin("consoleTitle") }]}
        title={tAdmin("consoleTitle")}
        subtitle={tAdmin("subtitle")}
        actions={<PeriodPicker value={periode} onChange={setPeriode} maxPeriode={currentPeriode()} />}
      />

      <StatCardGrid>
        <StatCard label={tAdmin("kpi.toValidate")} value={counts.SUBMITTED} sub={tAdmin("kpi.toValidateSub")} tone="amber" />
        <StatCard label={tAdmin("kpi.validated")} value={counts.VALIDATED} sub={tAdmin("kpi.validatedSub")} tone="green" />
        <StatCard label={tAdmin("kpi.rejected")} value={counts.REJECTED} sub={tAdmin("kpi.rejectedSub")} tone="red" />
        <StatCard label={tAdmin("kpi.totalHours")} value={counts.totalHours.toFixed(0)} sub={tAdmin("kpi.totalHoursSub")} tone="violet" />
      </StatCardGrid>

      {/* Toolbar */}
      <div className="mt-5 mb-4 flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-1 rounded-full border border-line bg-white p-1 shadow-xs-brand">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setFilter(tab.key)}
              className={cn(
                "flex items-center gap-1.5 rounded-full px-3.5 py-1.5 text-[12.5px] font-semibold transition-colors",
                filter === tab.key ? "bg-grad-orange text-white shadow-orange-brand" : "text-ink-2 hover:bg-bg-soft",
              )}
            >
              {tab.label}
              <span
                className={cn(
                  "rounded-full px-1.5 py-0.5 text-[10.5px] font-bold tabular-nums",
                  filter === tab.key ? "bg-white/25 text-white" : "bg-bg-dim text-ink-3",
                )}
              >
                {tab.count}
              </span>
            </button>
          ))}
        </div>
        <div className="relative">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-4" />
          <Input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder={tAdmin("searchPlaceholder")}
            className="w-64 pl-9"
          />
        </div>
      </div>

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : filtered.length === 0 ? (
        <div className="rounded-[20px] border border-line bg-white p-12 text-center">
          <CheckCircle2 className="mx-auto h-10 w-10 text-success-500" />
          <p className="mt-3 text-[14px] text-ink-2">
            {filter === "ALL" && !search ? tAdmin("empty") : tAdmin("emptyFiltered")}
          </p>
        </div>
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-bg-dim">
                  <Th>{tAdmin("columns.employee")}</Th>
                  <Th className="text-right">{tAdmin("columns.normales")}</Th>
                  <Th className="text-right">{tAdmin("columns.supp")}</Th>
                  <Th className="text-right">{tAdmin("columns.nuit")}</Th>
                  <Th className="text-right">{tAdmin("columns.weekend")}</Th>
                  <Th className="text-right">{tAdmin("columns.absences")}</Th>
                  <Th className="text-right">{tAdmin("columns.total")}</Th>
                  <Th>{tAdmin("columns.status")}</Th>
                  <Th className="text-right">{tAdmin("columns.actions")}</Th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((ts) => {
                  const busy = actingId === ts.id && (validateMutation.isPending || rejectMutation.isPending);
                  return (
                    <tr
                      key={ts.id}
                      className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                      onClick={() => router.push(`/timesheets/${ts.id}`)}
                    >
                      <td className="px-5 py-3">
                        <div className="flex items-center gap-2.5">
                          <Avatar name={employeeLabel(ts)} size="sm" />
                          <div className="min-w-0">
                            <div className="truncate text-[13px] font-semibold text-ink">{employeeLabel(ts)}</div>
                            <div className="truncate font-mono-tabular text-[11px] text-ink-4">
                              {ts.employeeMatricule ?? ts.employeeId.slice(0, 8)}
                              {ts.employeeDepartment ? ` · ${ts.employeeDepartment}` : ""}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] text-ink-2">{num(ts.heuresNormales)}</td>
                      <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] text-ink-2">{num(ts.heuresSupplementaires)}</td>
                      <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] text-ink-2">{num(ts.heuresNuit)}</td>
                      <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] text-ink-2">{num(ts.heuresWeekend)}</td>
                      <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px]">
                        <span className={Number(ts.absencesNonJustifiees) > 0 ? "font-bold text-danger-600" : "text-ink-4"}>
                          {num(ts.absencesNonJustifiees)}
                        </span>
                      </td>
                      <td className="px-3 py-3 text-right font-mono-tabular text-[13px] font-bold text-ink">
                        {timesheetTotalHours(ts).toFixed(1)}
                      </td>
                      <td className="px-3 py-3">
                        <Badge tone={timesheetStatusTone(ts.status)}>{t(`status.${ts.status}`)}</Badge>
                      </td>
                      <td className="px-5 py-3">
                        {ts.status === "SUBMITTED" ? (
                          <div className="flex items-center justify-end gap-1.5" onClick={(e) => e.stopPropagation()}>
                            <Button type="button" size="sm" disabled={busy} onClick={() => validateMutation.mutate(ts.id)}>
                              {busy && validateMutation.isPending ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <CheckCircle2 className="h-3.5 w-3.5" />}
                              {tDetail("actions.validate")}
                            </Button>
                            <Button
                              type="button"
                              size="sm"
                              variant="danger"
                              disabled={busy}
                              onClick={() => {
                                setRejectTarget(ts.id);
                                setRejectComment("");
                                setRejectError(false);
                              }}
                            >
                              <XCircle className="h-3.5 w-3.5" />
                              {tDetail("actions.reject")}
                            </Button>
                          </div>
                        ) : (
                          <div className="flex justify-end">
                            <span className="text-[11.5px] text-ink-4">{tAdmin("viewDetail")}</span>
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {/* Reject modal */}
      {rejectTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={() => setRejectTarget(null)}>
          <div
            className="mx-4 w-full max-w-md rounded-[20px] border border-line bg-white p-6 shadow-lg"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="text-[16px] font-semibold text-ink">{tDetail("rejectConfirmTitle")}</h3>
            <p className="mt-1 text-[13px] text-ink-3">{tDetail("rejectConfirmDescription")}</p>
            <label className="mt-4 block text-[12.5px] font-medium text-ink-2">
              {tDetail("rejectionComment")}
            </label>
            <textarea
              className={cn(
                "mt-1.5 w-full rounded-[12px] border bg-bg-soft px-3.5 py-2.5 text-[13.5px] text-ink placeholder:text-ink-4 focus:outline-none focus:ring-2 focus:ring-orange-400",
                rejectError ? "border-danger-500" : "border-line",
              )}
              rows={3}
              placeholder={tDetail("rejectionCommentPlaceholder")}
              value={rejectComment}
              onChange={(e) => {
                setRejectComment(e.target.value);
                if (rejectError && e.target.value.trim()) setRejectError(false);
              }}
              autoFocus
            />
            {rejectError && (
              <p className="mt-1 text-[12px] text-danger-600">{tDetail("rejectionCommentRequired")}</p>
            )}
            <div className="mt-5 flex items-center justify-end gap-2">
              <Button type="button" variant="secondary" onClick={() => setRejectTarget(null)}>
                Annuler
              </Button>
              <Button
                type="button"
                variant="danger"
                disabled={rejectMutation.isPending}
                onClick={handleRejectSubmit}
              >
                {rejectMutation.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <XCircle className="h-4 w-4" />
                )}
                {tDetail("actions.reject")}
              </Button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

function Th({ children, className }: { children?: React.ReactNode; className?: string }) {
  return (
    <th
      className={cn(
        "px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3 first:pl-5 last:pr-5",
        className,
      )}
    >
      {children}
    </th>
  );
}
