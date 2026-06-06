"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ArrowLeft,
  CalendarDays,
  CalendarRange,
  Clock,
  Loader2,
  Plane,
  Plus,
  UserX,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { workingDaysBetween } from "@/lib/holidays-cm";
import { leaveStatusTone } from "@/lib/leave-status";
import { cn } from "@/lib/utils";
import type {
  EmployeeResponse,
  LeaveBalanceResponse,
  LeaveType,
} from "@/server/ksm/modules/employees";
import type { LeaveResponse, LeaveStatus } from "@/server/ksm/modules/leaves";

type MinePayload = {
  employee: EmployeeResponse | null;
  leaves: LeaveResponse[];
  balances: LeaveBalanceResponse[];
  year: number;
};

const LEAVE_TYPES: LeaveType[] = [
  "ANNUAL",
  "SICK",
  "MATERNITY",
  "PATERNITY",
  "UNPAID",
  "SPECIAL",
];

// ── Helpers ─────────────────────────────────────────────────────────────────

function num(v: number | string | null | undefined): number {
  if (v == null) return 0;
  const n = typeof v === "string" ? parseFloat(v) : v;
  return isNaN(n) ? 0 : n;
}

function parseDate(iso: string | null | undefined): Date | null {
  if (!iso) return null;
  const d = new Date(iso);
  return isNaN(d.getTime()) ? null : d;
}

function fmtDate(iso: string | null | undefined, locale: "fr" | "en"): string {
  const d = parseDate(iso);
  return d
    ? d.toLocaleDateString(locale === "fr" ? "fr-FR" : "en-US", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      })
    : "—";
}

function startOfToday(): Date {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  return d;
}

function daysUntil(iso: string): number {
  const d = parseDate(iso);
  if (!d) return Infinity;
  d.setHours(0, 0, 0, 0);
  return Math.round((d.getTime() - startOfToday().getTime()) / 86_400_000);
}

function leaveTypeTone(type: LeaveType): "orange" | "danger" | "violet" | "teal" | "gray" | "info" {
  switch (type) {
    case "ANNUAL": return "orange";
    case "SICK": return "danger";
    case "MATERNITY": return "violet";
    case "PATERNITY": return "teal";
    case "UNPAID": return "gray";
    case "SPECIAL": return "info";
  }
}

function leaveWorkflow(
  status: LeaveStatus,
  t: ReturnType<typeof useTranslations<"leaves">>,
): WorkflowStep[] {
  const reviewState: WorkflowStep["state"] =
    status === "APPROVED" || status === "REJECTED"
      ? "done"
      : status === "PENDING"
        ? "active"
        : "pending";
  const result =
    status === "APPROVED"
      ? t("status.APPROVED")
      : status === "REJECTED"
        ? t("status.REJECTED")
        : status === "CANCELLED"
          ? t("status.CANCELLED")
          : "—";
  return [
    { key: "submit", label: t("status.PENDING"), state: "done" },
    { key: "review", label: t("detail.validatedBy"), state: reviewState },
    { key: "result", label: result, state: status === "PENDING" ? "pending" : "done" },
  ];
}

// ── New leave request form (inline) ───────────────────────────────────────────

function LeaveForm({
  employeeId,
  balances,
  onBack,
}: {
  employeeId: string;
  balances: LeaveBalanceResponse[];
  onBack: () => void;
}) {
  const t = useTranslations("leaves");
  const tMy = useTranslations("leaves.my");
  const tNew = useTranslations("leaves.new");
  const tType = useTranslations("employees.leaveType");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();

  const today = new Date().toISOString().slice(0, 10);
  const [type, setType] = React.useState<LeaveType>("ANNUAL");
  const [dateDebut, setDateDebut] = React.useState(today);
  const [dateFin, setDateFin] = React.useState(today);
  const [motif, setMotif] = React.useState("");

  const days = React.useMemo(() => workingDaysBetween(dateDebut, dateFin), [dateDebut, dateFin]);
  const datesInvalid = !!dateDebut && !!dateFin && new Date(dateFin) < new Date(dateDebut);

  const balance = balances.find((b) => b.type === type) ?? null;
  const remaining = balance ? num(balance.soldeRestant) : null;
  const insufficient = remaining != null && days > remaining;

  const mutation = useMutation({
    mutationFn: () =>
      apiFetch<LeaveResponse>("/api/hrm/leaves", {
        method: "POST",
        body: {
          employeeId,
          type,
          dateDebut,
          dateFin,
          motif: motif.trim() || undefined,
        },
      }),
    onSuccess: () => {
      toast.success(tNew("success"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "leaves", "mine"] });
      onBack();
    },
    onError: (cause) => {
      toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown"));
    },
  });

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (days <= 0 || datesInvalid || insufficient) return;
    mutation.mutate();
  }

  return (
    <div>
      <div className="mb-6 flex items-center gap-3">
        <button
          onClick={onBack}
          className="grid h-9 w-9 place-items-center rounded-xl border border-line bg-white text-ink-2 shadow-sm transition-colors hover:bg-bg-dim"
        >
          <ArrowLeft size={16} />
        </button>
        <div>
          <h1 className="font-display text-[22px] font-extrabold tracking-tight text-ink">
            {tNew("title")}
          </h1>
          <p className="text-[13px] text-ink-3">{tNew("subtitle")}</p>
        </div>
      </div>

      <div className="flex flex-col gap-6 lg:flex-row">
        <form onSubmit={handleSubmit} className="flex-1 space-y-6">
          <div className="rounded-2xl border border-line bg-white p-6 shadow-sm">
            <div className="grid grid-cols-2 gap-4">
              <div className="col-span-2">
                <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
                  {tNew("fields.type")}
                </label>
                <select
                  value={type}
                  onChange={(e) => setType(e.target.value as LeaveType)}
                  className="w-full rounded-xl border border-line bg-white px-3.5 py-2.5 text-[14px] text-ink outline-none transition-colors focus:border-orange-400 focus:ring-2 focus:ring-orange-100"
                >
                  {LEAVE_TYPES.map((lt) => (
                    <option key={lt} value={lt}>
                      {tType(lt)}
                    </option>
                  ))}
                </select>
                {type === "UNPAID" ? (
                  <p className="mt-1.5 text-[11px] text-amber-600">{tMy("form.unpaidNote")}</p>
                ) : remaining == null ? (
                  <p className="mt-1.5 text-[11px] text-ink-3">{tMy("form.noBalanceNote")}</p>
                ) : (
                  <p className="mt-1.5 text-[11px] text-ink-3">
                    {tMy("form.balanceRemaining", { n: remaining.toFixed(1) })}
                  </p>
                )}
              </div>

              <div>
                <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
                  {tNew("fields.dateDebut")}
                </label>
                <input
                  type="date"
                  min={today}
                  value={dateDebut}
                  onChange={(e) => setDateDebut(e.target.value)}
                  className="w-full rounded-xl border border-line bg-white px-3.5 py-2.5 text-[14px] text-ink outline-none transition-colors focus:border-orange-400 focus:ring-2 focus:ring-orange-100"
                  required
                />
              </div>
              <div>
                <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
                  {tNew("fields.dateFin")}
                </label>
                <input
                  type="date"
                  min={dateDebut || today}
                  value={dateFin}
                  onChange={(e) => setDateFin(e.target.value)}
                  className="w-full rounded-xl border border-line bg-white px-3.5 py-2.5 text-[14px] text-ink outline-none transition-colors focus:border-orange-400 focus:ring-2 focus:ring-orange-100"
                  required
                />
              </div>

              <div className="col-span-2">
                <label className="mb-1.5 block text-[12px] font-semibold text-ink-2">
                  {tNew("fields.motif")}
                </label>
                <textarea
                  value={motif}
                  onChange={(e) => setMotif(e.target.value)}
                  rows={3}
                  className="w-full resize-none rounded-xl border border-line bg-white px-3.5 py-2.5 text-[14px] text-ink outline-none transition-colors focus:border-orange-400 focus:ring-2 focus:ring-orange-100"
                />
                <p className="mt-1 text-[11px] text-ink-3">{tNew("hint")}</p>
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={onBack}>
              {t("detail.actions.cancel")}
            </Button>
            <Button type="submit" disabled={mutation.isPending || days <= 0 || datesInvalid || insufficient}>
              {mutation.isPending ? <Loader2 size={14} className="animate-spin" /> : <Plus size={14} />}
              {tNew("submit")}
            </Button>
          </div>
        </form>

        <div className="w-full shrink-0 space-y-4 lg:w-[300px]">
          <div
            className="rounded-2xl p-5"
            style={{ background: "linear-gradient(135deg, #1A150E 0%, #2D2520 100%)", color: "#fff" }}
          >
            <div className="mb-2 flex items-center gap-2 text-[11px] font-semibold uppercase tracking-widest" style={{ opacity: 0.7 }}>
              <CalendarRange size={14} /> {tNew("fields.duration")}
            </div>
            <div className="font-display text-[34px] font-extrabold tabular-nums">
              {days.toFixed(1)}
            </div>
            <div className="mt-1 text-[12px]" style={{ opacity: 0.7 }}>
              {fmtDate(dateDebut, "fr")} → {fmtDate(dateFin, "fr")}
            </div>
          </div>

          {insufficient && (
            <div className="rounded-2xl border border-danger-300 bg-danger-50 p-4">
              <div className="text-[12.5px] text-danger-700">
                {tMy("form.insufficient", {
                  requested: days.toFixed(1),
                  available: (remaining ?? 0).toFixed(1),
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// ── Leave detail (inline) ──────────────────────────────────────────────────────

function LeaveDetailView({ leave, onBack }: { leave: LeaveResponse; onBack: () => void }) {
  const t = useTranslations("leaves");
  const tMy = useTranslations("leaves.my");
  const tDetail = useTranslations("leaves.detail");
  const tType = useTranslations("employees.leaveType");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const [confirmCancel, setConfirmCancel] = React.useState(false);

  const isPending = leave.status === "PENDING";
  const isApproved = leave.status === "APPROVED";
  const startInFuture = daysUntil(leave.dateDebut) > 0;
  const cancellable = isPending || (isApproved && startInFuture);

  const cancelMutation = useMutation({
    mutationFn: () =>
      apiFetch<LeaveResponse>(`/api/hrm/leaves/${leave.id}/cancel`, { method: "POST" }),
    onSuccess: () => {
      toast.success(tDetail("cancelSuccess"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "leaves", "mine"] });
      setConfirmCancel(false);
      onBack();
    },
    onError: (cause) => {
      toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown"));
    },
  });

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center gap-3">
        <button
          onClick={onBack}
          className="grid h-9 w-9 place-items-center rounded-xl border border-line bg-white text-ink-2 shadow-sm transition-colors hover:bg-bg-dim"
        >
          <ArrowLeft size={16} />
        </button>
        <div className="flex-1">
          <h1 className="flex items-center gap-2 font-display text-[22px] font-extrabold tracking-tight text-ink">
            <CalendarRange className="text-orange-500" size={22} />
            {tType(leave.type)} · {fmtDate(leave.dateDebut, locale)} → {fmtDate(leave.dateFin, locale)}
          </h1>
          <div className="mt-1 flex items-center gap-2">
            <Badge tone={leaveStatusTone(leave.status)} showDot={false}>
              {t(`status.${leave.status}`)}
            </Badge>
            <span className="font-mono text-[12px] text-ink-3">{Number(leave.nbJours).toFixed(1)} j</span>
          </div>
        </div>
        {cancellable && (
          <Button variant="danger" size="sm" onClick={() => setConfirmCancel(true)}>
            {tDetail("actions.cancel")}
          </Button>
        )}
      </div>

      <div className="rounded-2xl border border-line bg-white p-6 shadow-sm">
        <WorkflowStepper steps={leaveWorkflow(leave.status, t)} />
      </div>

      <div className="rounded-2xl border border-line bg-white p-6 shadow-sm">
        <dl className="grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
          <Detail label={t("queue.columns.type")} value={tType(leave.type)} />
          <Detail label={t("queue.columns.days")} value={`${Number(leave.nbJours).toFixed(1)} j`} />
          <Detail
            label={t("queue.columns.period")}
            value={`${fmtDate(leave.dateDebut, locale)} → ${fmtDate(leave.dateFin, locale)}`}
          />
          <Detail label={t("queue.columns.reason")} value={leave.motif || "—"} />
          {leave.valideurDisplayName && (
            <Detail label={tDetail("validatedBy")} value={leave.valideurDisplayName} />
          )}
          {leave.dateValidation && (
            <Detail label={tDetail("validatedAt")} value={fmtDate(leave.dateValidation, locale)} />
          )}
          {leave.commentaireValideur && (
            <div className="sm:col-span-2">
              <Detail label={tDetail("comment")} value={leave.commentaireValideur} />
            </div>
          )}
        </dl>
      </div>

      <Dialog
        open={confirmCancel}
        onClose={() => setConfirmCancel(false)}
        title={tDetail("cancel.title")}
        subtitle={tDetail("cancel.subtitle")}
        footer={
          <>
            <Button type="button" variant="ghost" onClick={() => setConfirmCancel(false)}>
              {tMy("cancel")}
            </Button>
            <Button
              type="button"
              variant="danger"
              disabled={cancelMutation.isPending}
              onClick={() => cancelMutation.mutate()}
            >
              {cancelMutation.isPending ? <Loader2 size={14} className="animate-spin" /> : tDetail("cancel.confirm")}
            </Button>
          </>
        }
      >
        <p className="text-[13.5px] text-ink-3">{tDetail("cancel.subtitle")}</p>
      </Dialog>
    </div>
  );
}

function Detail({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <div className="text-[11px] font-semibold uppercase tracking-wide text-ink-3">{label}</div>
      <div className="mt-1 text-[14px] text-ink-2">{value}</div>
    </div>
  );
}

// ── Main component ────────────────────────────────────────────────────────────

type Filter = "all" | LeaveStatus;

export function MyLeaves() {
  const t = useTranslations("leaves");
  const tMy = useTranslations("leaves.my");
  const tType = useTranslations("employees.leaveType");
  const locale = useLocale() as "fr" | "en";

  const currentYear = new Date().getFullYear();
  const [year, setYear] = React.useState(currentYear);
  const [view, setView] = React.useState<"list" | "new" | { leave: LeaveResponse }>("list");
  const [filter, setFilter] = React.useState<Filter>("all");

  const query = useQuery({
    queryKey: ["hrm", "leaves", "mine", year],
    queryFn: () => apiFetch<MinePayload>(`/api/hrm/leaves/mine?year=${year}`),
  });

  const employee = query.data?.employee ?? null;
  const allLeaves = query.data?.leaves ?? [];
  const balances = query.data?.balances ?? [];

  const annual = balances.find((b) => b.type === "ANNUAL") ?? null;
  const annualRemaining = annual ? num(annual.soldeRestant) : 0;
  const annualAcquired = annual ? num(annual.acquis) : 0;
  const annualTaken = annual ? num(annual.pris) : 0;
  const annualPct = annualAcquired > 0 ? Math.round((annualTaken / annualAcquired) * 100) : 0;
  const monthsElapsed = year === currentYear ? new Date().getMonth() + 1 : 12;
  const accrualRate = annualAcquired > 0 ? annualAcquired / Math.max(monthsElapsed, 1) : 0;

  const inYear = allLeaves.filter((l) => (parseDate(l.dateDebut)?.getFullYear() ?? 0) === year);
  const pendingCount = allLeaves.filter((l) => l.status === "PENDING").length;
  const takenDays = inYear
    .filter((l) => l.status === "APPROVED")
    .reduce((s, l) => s + num(l.nbJours), 0);

  const upcoming = allLeaves
    .filter((l) => l.status === "APPROVED" && daysUntil(l.dateDebut) >= 0)
    .sort((a, b) => (parseDate(a.dateDebut)?.getTime() ?? 0) - (parseDate(b.dateDebut)?.getTime() ?? 0));
  const nextLeave = upcoming[0] ?? null;

  const visible = inYear
    .filter((l) => (filter === "all" ? true : l.status === filter))
    .sort((a, b) => (parseDate(b.dateDebut)?.getTime() ?? 0) - (parseDate(a.dateDebut)?.getTime() ?? 0));

  const years = Array.from(
    new Set([currentYear, currentYear - 1, ...allLeaves.map((l) => parseDate(l.dateDebut)?.getFullYear() ?? currentYear)]),
  ).sort((a, b) => b - a);

  const filters: { key: Filter; label: string }[] = [
    { key: "all", label: tMy("filters.all") },
    { key: "PENDING", label: tMy("filters.PENDING") },
    { key: "APPROVED", label: tMy("filters.APPROVED") },
    { key: "REJECTED", label: tMy("filters.REJECTED") },
    { key: "CANCELLED", label: tMy("filters.CANCELLED") },
  ];

  if (view === "new" && employee) {
    return <LeaveForm employeeId={employee.id} balances={balances} onBack={() => setView("list")} />;
  }
  if (typeof view === "object") {
    return <LeaveDetailView leave={view.leave} onBack={() => setView("list")} />;
  }

  if (query.isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="animate-spin text-ink-3" size={28} />
      </div>
    );
  }

  if (!employee) {
    return (
      <>
        <PageHeader ucBadge={t("ucBadge")} title={tMy("title")} subtitle={tMy("subtitle")} />
        <div className="rounded-2xl border border-line bg-white p-8">
          <div className="flex items-start gap-4">
            <span className="grid h-12 w-12 place-items-center rounded-[14px] bg-warning-50 text-warning-600">
              <UserX className="h-6 w-6" />
            </span>
            <p className="text-[14px] text-ink-2">{tMy("noEmployee")}</p>
          </div>
        </div>
      </>
    );
  }

  return (
    <div className="space-y-5">
      <PageHeader
        ucBadge={t("ucBadge")}
        title={tMy("title")}
        subtitle={tMy("subtitle")}
        actions={
          <Button size="sm" onClick={() => setView("new")}>
            <Plus size={13} /> {tMy("new")}
          </Button>
        }
      />

      {/* ── Annual balance hero ─────────────────────────────────────── */}
      <div
        className="relative overflow-hidden rounded-2xl p-6 shadow-lg"
        style={{ background: "linear-gradient(135deg, #1A150E 0%, #2D2520 100%)", color: "#fff" }}
      >
        <div
          className="pointer-events-none absolute"
          style={{
            top: -100, right: -100, width: 400, height: 400,
            background: "radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)",
          }}
        />
        <div className="relative flex flex-wrap items-start justify-between gap-6">
          <div className="flex-1">
            <span className="inline-flex items-center rounded-full px-2.5 py-0.5 text-[11px] font-semibold" style={{ background: "rgba(249,115,22,0.2)", color: "#FFB066" }}>
              {tMy("hero.badge")} · {year}
            </span>
            <div className="mt-2 flex items-end gap-2">
              <span className="font-display text-[44px] font-extrabold leading-none tabular-nums">
                {annualRemaining.toFixed(1)}
              </span>
              <span className="mb-1 text-[13px]" style={{ opacity: 0.7 }}>{tMy("hero.remaining")}</span>
            </div>

            <div className="mb-1 mt-5 flex max-w-md items-center justify-between text-[12px]" style={{ opacity: 0.75 }}>
              <span>{tMy("hero.taken")}: {annualTaken.toFixed(1)} j</span>
              <span>{tMy("hero.acquired")}: {annualAcquired.toFixed(1)} j</span>
            </div>
            <div className="h-2 max-w-md overflow-hidden rounded-full" style={{ background: "rgba(255,255,255,0.2)" }}>
              <div className="h-full rounded-full transition-all" style={{ width: `${Math.min(annualPct, 100)}%`, background: "#FFB066" }} />
            </div>
            {accrualRate > 0 && (
              <div className="mt-2 text-[11px]" style={{ opacity: 0.55 }}>
                {tMy("hero.accrual", { rate: accrualRate.toFixed(1) })}
              </div>
            )}
          </div>

          <div className="min-w-[180px]">
            <div className="text-[11px] uppercase tracking-widest" style={{ opacity: 0.6 }}>
              {tMy("hero.nextLeave")}
            </div>
            {nextLeave ? (
              <div className="mt-2">
                <div className="font-display text-[16px] font-bold">{tType(nextLeave.type)}</div>
                <div className="mt-0.5 text-[12px]" style={{ opacity: 0.7 }}>
                  {fmtDate(nextLeave.dateDebut, locale)}
                </div>
                <div className="mt-1 inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-semibold" style={{ background: "rgba(255,255,255,0.12)" }}>
                  <Clock size={11} />
                  {daysUntil(nextLeave.dateDebut) === 0
                    ? tMy("hero.startsToday")
                    : tMy("hero.startsIn", { n: daysUntil(nextLeave.dateDebut) })}
                </div>
              </div>
            ) : (
              <div className="mt-2 flex items-center gap-2 text-[13px]" style={{ opacity: 0.6 }}>
                <Plane size={15} /> {tMy("hero.nextLeaveNone")}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ── KPI cards ─────────────────────────────────────────────── */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {[
          { label: tMy("kpi.available"), value: annualRemaining.toFixed(1), sub: tMy("kpi.availableSub", { year }), tone: "green" as const },
          { label: tMy("kpi.taken"), value: takenDays.toFixed(1), sub: tMy("kpi.takenSub", { year }), tone: "orange" as const },
          { label: tMy("kpi.pending"), value: `${pendingCount}`, sub: tMy("kpi.pendingSub"), tone: "amber" as const },
          { label: tMy("kpi.upcoming"), value: `${upcoming.length}`, sub: tMy("kpi.upcomingSub"), tone: "blue" as const },
        ].map((k, i) => (
          <div key={i} className="rounded-2xl border border-line bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-semibold uppercase tracking-wide text-ink-3">{k.label}</span>
              <span
                className={cn(
                  "h-2 w-2 rounded-full",
                  k.tone === "amber" && "bg-amber-500",
                  k.tone === "green" && "bg-success-500",
                  k.tone === "blue" && "bg-info-500",
                  k.tone === "orange" && "bg-orange-500",
                )}
              />
            </div>
            <div className="mt-2 font-display text-[20px] font-extrabold tabular-nums text-ink">{k.value}</div>
            <div className="mt-0.5 text-[11px] text-ink-3">{k.sub}</div>
          </div>
        ))}
      </div>

      {/* ── Balances + upcoming ───────────────────────────────────── */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="rounded-2xl border border-line bg-white p-5 shadow-sm lg:col-span-2">
          <div className="mb-4 font-display text-[15px] font-bold text-ink">{tMy("balances.title", { year })}</div>
          {balances.length === 0 ? (
            <div className="py-6 text-center text-[13px] text-ink-3">{tMy("balances.empty", { year })}</div>
          ) : (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              {balances.map((b) => {
                const acq = num(b.acquis);
                const taken = num(b.pris);
                const rem = num(b.soldeRestant);
                const pct = acq > 0 ? Math.min(Math.round((taken / acq) * 100), 100) : 0;
                return (
                  <div key={b.id} className="rounded-xl border border-line bg-bg-dim/40 p-4">
                    <div className="flex items-center justify-between">
                      <Badge tone={leaveTypeTone(b.type)} showDot={false}>{tType(b.type)}</Badge>
                      <span className="font-display text-[18px] font-extrabold tabular-nums text-ink">
                        {rem.toFixed(1)}<span className="ml-0.5 text-[11px] font-medium text-ink-3">{tMy("balances.days")}</span>
                      </span>
                    </div>
                    <div className="mt-3 h-1.5 overflow-hidden rounded-full bg-line">
                      <div className="h-full rounded-full bg-orange-500" style={{ width: `${pct}%` }} />
                    </div>
                    <div className="mt-2 flex justify-between text-[11px] text-ink-3">
                      <span>{tMy("balances.taken")}: {taken.toFixed(1)}</span>
                      <span>{tMy("balances.acquired")}: {acq.toFixed(1)}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <div className="rounded-2xl border border-line bg-white p-5 shadow-sm">
          <div className="mb-4 flex items-center gap-2 font-display text-[15px] font-bold text-ink">
            <CalendarDays size={16} className="text-orange-500" /> {tMy("upcoming.title")}
          </div>
          {upcoming.length === 0 ? (
            <div className="py-6 text-center text-[13px] text-ink-3">{tMy("upcoming.empty")}</div>
          ) : (
            <div className="space-y-2.5">
              {upcoming.slice(0, 5).map((l) => (
                <button
                  key={l.id}
                  onClick={() => setView({ leave: l })}
                  className="flex w-full items-center gap-3 rounded-xl border border-line p-3 text-left transition-colors hover:bg-orange-50/40"
                >
                  <span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-orange-100 text-orange-600">
                    <CalendarRange size={16} />
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="truncate text-[13px] font-semibold text-ink">{tType(l.type)}</div>
                    <div className="text-[11px] text-ink-3">{fmtDate(l.dateDebut, locale)}</div>
                  </div>
                  <span className="shrink-0 text-[11px] font-semibold text-ink-3">
                    {daysUntil(l.dateDebut) === 0 ? tMy("hero.startsToday") : tMy("hero.startsIn", { n: daysUntil(l.dateDebut) })}
                  </span>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* ── History ───────────────────────────────────────────────── */}
      <div className="rounded-2xl border border-line bg-white shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line px-6 py-4">
          <span className="font-display text-[15px] font-bold text-ink">{tMy("history.title")}</span>
          <div className="flex flex-wrap items-center gap-3">
            <div className="flex flex-wrap gap-1.5">
              {filters.map((f) => (
                <button
                  key={f.key}
                  type="button"
                  onClick={() => setFilter(f.key)}
                  className={cn(
                    "rounded-full px-3 py-1 text-[12px] font-semibold transition-colors",
                    filter === f.key
                      ? "bg-orange-500 text-white"
                      : "bg-bg-dim text-ink-2 hover:bg-orange-50 hover:text-orange-700",
                  )}
                >
                  {f.label}
                </button>
              ))}
            </div>
            <select
              value={year}
              onChange={(e) => setYear(Number(e.target.value))}
              className="rounded-lg border border-line bg-white px-2.5 py-1 text-[12px] font-semibold text-ink-2 outline-none focus:border-orange-400"
            >
              {years.map((y) => (
                <option key={y} value={y}>{y}</option>
              ))}
            </select>
          </div>
        </div>

        {visible.length === 0 ? (
          <div className="flex h-32 items-center justify-center text-[13px] text-ink-3">{tMy("empty")}</div>
        ) : (
          <div className="overflow-x-auto">
            <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
              <thead>
                <tr className="border-b border-line bg-bg-dim">
                  {[tMy("columns.type"), tMy("columns.period"), tMy("columns.days"), tMy("columns.status"), tMy("columns.actions")].map((h, i) => (
                    <th
                      key={i}
                      style={{
                        textAlign: i === 2 ? "right" : i >= 3 ? "right" : "left",
                        padding: "10px 16px",
                        fontSize: 11,
                        fontWeight: 600,
                        color: "var(--ink-3)",
                        textTransform: "uppercase",
                        letterSpacing: "0.05em",
                        whiteSpace: "nowrap",
                      }}
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {visible.map((l) => (
                  <tr
                    key={l.id}
                    onClick={() => setView({ leave: l })}
                    className="cursor-pointer border-b border-line/60 transition-colors hover:bg-orange-50/40"
                  >
                    <td style={{ padding: "12px 16px" }}>
                      <Badge tone={leaveTypeTone(l.type)} showDot={false}>{tType(l.type)}</Badge>
                    </td>
                    <td style={{ padding: "12px 16px" }} className="font-mono text-[12.5px] text-ink-2">
                      {fmtDate(l.dateDebut, locale)} → {fmtDate(l.dateFin, locale)}
                    </td>
                    <td style={{ padding: "12px 16px", textAlign: "right" }} className="tabular-nums font-bold text-ink">
                      {Number(l.nbJours).toFixed(1)}
                    </td>
                    <td style={{ padding: "12px 16px", textAlign: "right" }}>
                      <Badge tone={leaveStatusTone(l.status)} showDot={false}>{t(`status.${l.status}`)}</Badge>
                    </td>
                    <td style={{ padding: "12px 16px", textAlign: "right" }}>
                      <span className="text-[12px] font-semibold text-orange-600">{tMy("viewDetail")}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
