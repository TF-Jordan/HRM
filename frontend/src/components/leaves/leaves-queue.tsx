"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, Clock, Loader2, Search, XCircle } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Textarea } from "@/components/ui/input";
import { StatCard, StatCardGrid } from "@/components/ui/stat-card";
import { useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { leaveStatusTone } from "@/lib/leave-status";
import { cn } from "@/lib/utils";
import type { EnrichedLeaveResponse } from "@/server/ksm/modules/leaves";

type FilterKey = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED" | "ALL";

const URGENT_DAYS = 7;

/** Days from today until the leave starts (negative = already started). Module-level to keep render pure. */
function daysUntilStart(dateDebut: string): number {
  const start = new Date(dateDebut).getTime();
  const today = new Date().setHours(0, 0, 0, 0);
  return Math.round((start - today) / 86_400_000);
}

const norm = (s: string) => s.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();

export function LeavesQueue() {
  const t = useTranslations("leaves");
  const tQ = useTranslations("leaves.queue");
  const tDetail = useTranslations("leaves.detail");
  const tEmpType = useTranslations("employees.leaveType");
  const tErrors = useTranslations("errors");
  const router = useRouter();
  const queryClient = useQueryClient();

  const [filter, setFilter] = React.useState<FilterKey>("PENDING");
  const [search, setSearch] = React.useState("");
  const [rejectTarget, setRejectTarget] = React.useState<EnrichedLeaveResponse | null>(null);
  const [actingId, setActingId] = React.useState<string | null>(null);

  const query = useQuery({
    queryKey: ["hrm", "leaves", "org"],
    queryFn: () => apiFetch<EnrichedLeaveResponse[]>("/api/hrm/leaves"),
    refetchInterval: 60_000,
  });

  const all = React.useMemo(() => query.data ?? [], [query.data]);

  const counts = React.useMemo(() => {
    const c = { PENDING: 0, APPROVED: 0, REJECTED: 0, CANCELLED: 0, ALL: all.length, daysPending: 0 };
    for (const l of all) {
      c[l.status] += 1;
      if (l.status === "PENDING") c.daysPending += Number(l.nbJours);
    }
    return c;
  }, [all]);

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "leaves"] });
  }, [queryClient]);

  const approveMutation = useMutation({
    mutationFn: (id: string) =>
      apiFetch<EnrichedLeaveResponse>(`/api/hrm/leaves/${id}/approve`, { method: "POST" }),
    onMutate: (id) => setActingId(id),
    onSuccess: () => {
      toast.success(tDetail("approveSuccess"));
      invalidate();
    },
    onError: (cause) => toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown")),
    onSettled: () => setActingId(null),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, commentaire }: { id: string; commentaire: string }) =>
      apiFetch<EnrichedLeaveResponse>(`/api/hrm/leaves/${id}/reject`, {
        method: "POST",
        body: { commentaire },
      }),
    onSuccess: () => {
      toast.success(tDetail("rejectSuccess"));
      invalidate();
      setRejectTarget(null);
    },
    onError: (cause) => toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown")),
  });

  const filtered = React.useMemo(() => {
    const q = norm(search.trim());
    let rows = filter === "ALL" ? all : all.filter((l) => l.status === filter);
    if (q) {
      rows = rows.filter((l) => {
        const hay = norm(
          `${l.employeeName ?? ""} ${l.employeeMatricule ?? ""} ${l.employeeDepartment ?? ""} ${l.type}`,
        );
        return hay.includes(q);
      });
    }
    return [...rows].sort((a, b) => {
      // Pending → most urgent first (earliest start). Others → most recent start first.
      if (a.status === "PENDING" && b.status === "PENDING") {
        return new Date(a.dateDebut).getTime() - new Date(b.dateDebut).getTime();
      }
      return new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime();
    });
  }, [all, filter, search]);

  const tabs: { key: FilterKey; label: string; count: number }[] = [
    { key: "PENDING", label: tQ("filters.toProcess"), count: counts.PENDING },
    { key: "APPROVED", label: t("status.APPROVED"), count: counts.APPROVED },
    { key: "REJECTED", label: t("status.REJECTED"), count: counts.REJECTED },
    { key: "CANCELLED", label: t("status.CANCELLED"), count: counts.CANCELLED },
    { key: "ALL", label: tQ("filters.all"), count: counts.ALL },
  ];

  const employeeLabel = (l: EnrichedLeaveResponse) =>
    l.employeeName ?? l.employeeMatricule ?? `${l.employeeId.slice(0, 8)}…`;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: tQ("consoleTitle") }]}
        title={tQ("consoleTitle")}
        subtitle={tQ("consoleSubtitle")}
      />

      <StatCardGrid>
        <StatCard label={tQ("kpi.pending")} value={counts.PENDING} sub={tQ("kpi.pendingSub")} tone="amber" />
        <StatCard
          label={tQ("kpi.daysPending")}
          value={counts.daysPending.toFixed(1)}
          sub={tQ("kpi.daysPendingSub")}
          tone="orange"
        />
        <StatCard label={tQ("kpi.approved")} value={counts.APPROVED} sub={tQ("kpi.approvedSub")} tone="green" />
        <StatCard label={tQ("kpi.rejected")} value={counts.REJECTED} sub={tQ("kpi.rejectedSub")} tone="red" />
      </StatCardGrid>

      {/* Toolbar: filter tabs + search */}
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
            placeholder={tQ("searchPlaceholder")}
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
            {filter === "PENDING" && !search ? tQ("empty") : tQ("emptyFiltered")}
          </p>
        </div>
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-bg-dim">
                  <Th>{tQ("columns.employee")}</Th>
                  <Th>{tQ("columns.type")}</Th>
                  <Th>{tQ("columns.period")}</Th>
                  <Th className="text-right">{tQ("columns.days")}</Th>
                  <Th>{tQ("columns.reason")}</Th>
                  <Th>{tQ("columns.status")}</Th>
                  <Th className="text-right">{tQ("columns.actions")}</Th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((l) => {
                  const dleft = daysUntilStart(l.dateDebut);
                  const urgent = l.status === "PENDING" && dleft <= URGENT_DAYS;
                  const busy = actingId === l.id && approveMutation.isPending;
                  return (
                    <tr
                      key={l.id}
                      className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                      onClick={() => router.push(`/leaves/${l.id}`)}
                    >
                      <td className="px-5 py-3">
                        <div className="flex items-center gap-2.5">
                          <Avatar name={employeeLabel(l)} size="sm" />
                          <div className="min-w-0">
                            <div className="truncate text-[13px] font-semibold text-ink">{employeeLabel(l)}</div>
                            <div className="truncate font-mono-tabular text-[11px] text-ink-4">
                              {l.employeeMatricule ?? l.employeeId.slice(0, 8)}
                              {l.employeeDepartment ? ` · ${l.employeeDepartment}` : ""}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-3 py-3">
                        <Badge tone="orange">{tEmpType(l.type)}</Badge>
                      </td>
                      <td className="px-3 py-3">
                        <div className="font-mono-tabular text-[12.5px] text-ink-2">
                          {formatDate(l.dateDebut, { locale: "fr" })} → {formatDate(l.dateFin, { locale: "fr" })}
                        </div>
                        {urgent && (
                          <div className="mt-0.5 flex items-center gap-1 text-[11px] font-semibold text-amber-600">
                            <Clock className="h-3 w-3" />
                            {dleft < 0
                              ? tQ("started")
                              : dleft === 0
                                ? tQ("startsToday")
                                : tQ("startsIn", { n: dleft })}
                          </div>
                        )}
                      </td>
                      <td className="px-3 py-3 text-right font-mono-tabular text-[13px] font-bold text-ink">
                        {Number(l.nbJours).toFixed(1)}
                      </td>
                      <td className="max-w-[200px] px-3 py-3">
                        <span className="line-clamp-1 text-[12.5px] text-ink-3">{l.motif ?? "—"}</span>
                      </td>
                      <td className="px-3 py-3">
                        <Badge tone={leaveStatusTone(l.status)}>{t(`status.${l.status}`)}</Badge>
                      </td>
                      <td className="px-5 py-3">
                        {l.status === "PENDING" ? (
                          <div className="flex items-center justify-end gap-1.5" onClick={(e) => e.stopPropagation()}>
                            <Button
                              type="button"
                              size="sm"
                              variant="secondary"
                              disabled={rejectMutation.isPending || busy}
                              onClick={() => setRejectTarget(l)}
                            >
                              <XCircle className="h-3.5 w-3.5" />
                              {tDetail("actions.reject")}
                            </Button>
                            <Button
                              type="button"
                              size="sm"
                              disabled={busy || rejectMutation.isPending}
                              onClick={() => approveMutation.mutate(l.id)}
                            >
                              {busy ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <CheckCircle2 className="h-3.5 w-3.5" />}
                              {tDetail("actions.approve")}
                            </Button>
                          </div>
                        ) : (
                          <div className="flex justify-end">
                            <span className="text-[11.5px] text-ink-4">{tQ("viewDetail")}</span>
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

      <RejectModal
        target={rejectTarget}
        employeeLabel={rejectTarget ? employeeLabel(rejectTarget) : ""}
        onClose={() => setRejectTarget(null)}
        onConfirm={(commentaire) =>
          rejectTarget && rejectMutation.mutate({ id: rejectTarget.id, commentaire })
        }
        loading={rejectMutation.isPending}
      />
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

function RejectModal({
  target,
  employeeLabel,
  onClose,
  onConfirm,
  loading,
}: {
  target: EnrichedLeaveResponse | null;
  employeeLabel: string;
  onClose: () => void;
  onConfirm: (commentaire: string) => void;
  loading: boolean;
}) {
  const t = useTranslations("leaves.detail.reject");
  const tCommon = useTranslations("common");
  const tEmpType = useTranslations("employees.leaveType");
  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{ commentaire: string }>({ mode: "onChange" });
  const open = target !== null;
  React.useEffect(() => {
    if (!open) reset({ commentaire: "" });
  }, [open, reset]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={
        <span className="flex items-center gap-2 text-danger-600">
          <XCircle className="h-5 w-5" />
          {t("title")}
        </span>
      }
      subtitle={
        target
          ? `${employeeLabel} · ${tEmpType(target.type)} · ${Number(target.nbJours).toFixed(1)} j`
          : t("subtitle")
      }
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            variant="danger"
            disabled={!isValid || loading}
            onClick={handleSubmit((v) => onConfirm(v.commentaire))}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : t("confirm")}
          </Button>
        </>
      }
    >
      <Field label={t("comment")}>
        <Textarea rows={3} {...register("commentaire", { required: true, minLength: 5 })} />
      </Field>
    </Dialog>
  );
}
