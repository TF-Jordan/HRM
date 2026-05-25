"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import {
  CalendarDays,
  Check,
  ChevronLeft,
  ChevronRight,
  Eye,
  Loader2,
  Plus,
  X,
} from "lucide-react";
import { toast } from "sonner";

import {
  usePendingLeaves,
  useApproveLeave,
  useRejectLeave,
} from "@/hooks/modules/useLeaves";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import { StatCard } from "@/components/ui-tokens/StatCard";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Avatar } from "@/components/ui/avatar";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Link } from "@/i18n/navigation";
import { initials } from "@/lib/utils";
import type { LeaveRequest, LeaveRequestStatus } from "@/lib/types/hrm/leave-request";
import type { Employee } from "@/lib/types/hrm/employee";

/* ========================================================================== */
/* Main component                                                              */
/* ========================================================================== */

export function LeavesClient() {
  const t = useTranslations("leaves");
  const tNav = useTranslations("navigation");
  const tStatuses = useTranslations("statuses.leaveType");
  const tLeaveStatuses = useTranslations("statuses.leave");
  const fmt = useFormat();

  const pendingQuery = usePendingLeaves();
  const employeesQuery = useEmployees();
  const approve = useApproveLeave();
  const reject = useRejectLeave();

  const [rejectingId, setRejectingId] = React.useState<string | null>(null);
  const rejectForm = useForm<{ commentaire: string }>({
    defaultValues: { commentaire: "" },
  });

  // Build employee lookup map
  const employeeMap = React.useMemo(() => {
    const map = new Map<string, Employee>();
    if (employeesQuery.data) {
      for (const e of employeesQuery.data) {
        map.set(e.id, e);
      }
    }
    return map;
  }, [employeesQuery.data]);

  // All leave requests from the pending endpoint (these are the ones admins manage)
  const allLeaves = pendingQuery.data ?? [];

  // Computed stats
  const pendingCount = allLeaves.filter((l) => l.status === "PENDING").length;
  const approvedOngoing = allLeaves.filter((l) => {
    if (l.status !== "APPROVED") return false;
    const now = new Date();
    return new Date(l.dateDebut) <= now && new Date(l.dateFin) >= now;
  });
  const urgentCount = allLeaves.filter((l) => {
    if (l.status !== "PENDING") return false;
    const daysDiff = Math.ceil(
      (new Date(l.dateDebut).getTime() - Date.now()) / 86_400_000,
    );
    return daysDiff <= 3;
  }).length;

  const totalDays = allLeaves.reduce((sum, l) => sum + Number(l.nbJours), 0);
  const avgDays = allLeaves.length > 0 ? totalDays / allLeaves.length : 0;

  const totalEmployees = employeesQuery.data?.length ?? 0;
  const absentToday = approvedOngoing.length;
  const absentRate =
    totalEmployees > 0 ? (absentToday / totalEmployees) * 100 : 0;

  // Calendar state
  const [calendarDate, setCalendarDate] = React.useState(() => new Date());

  // Filter tabs: Toutes, En attente, Approuvees
  const filterFn = React.useCallback(
    (tab: string, leaves: LeaveRequest[]) => {
      switch (tab) {
        case "pending":
          return leaves.filter((l) => l.status === "PENDING");
        case "approved":
          return leaves.filter((l) => l.status === "APPROVED");
        default:
          return leaves;
      }
    },
    [],
  );

  const isLoading = pendingQuery.isLoading || employeesQuery.isLoading;

  return (
    <div className="space-y-6 animate-fade-up">
      {/* Header */}
      <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-[11px] font-semibold uppercase tracking-widest text-brand-500">
            Activite
          </p>
          <h1 className="font-display text-[34px] font-extrabold leading-tight tracking-tight text-ink">
            Conges
          </h1>
          <p className="mt-1 text-[14.5px] text-ink-3">
            Gestion des demandes, soldes et calendrier des absences
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" size="sm">
            <CalendarDays className="size-4" />
            Calendrier
          </Button>
          <Button asChild size="sm">
            <Link href="/leaves/my">
              <Plus className="size-4" />
              Nouvelle demande
            </Link>
          </Button>
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {isLoading ? (
          Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="h-[110px] rounded-2xl" />
          ))
        ) : (
          <>
            <StatCard
              tone="amber"
              label="A APPROUVER"
              value={pendingCount}
              footer={
                urgentCount > 0
                  ? `${urgentCount} urgente${urgentCount > 1 ? "s" : ""}`
                  : "aucune urgente"
              }
            />
            <StatCard
              tone="blue"
              label="EN COURS"
              value={absentToday}
              footer={`employe${absentToday !== 1 ? "s" : ""} absent${absentToday !== 1 ? "s" : ""} aujourd'hui`}
            />
            <StatCard
              tone="green"
              label="SOLDE MOYEN"
              value={`${avgDays.toFixed(1)} j`}
              footer="par demande"
            />
            <StatCard
              tone="red"
              label="TAUX D'ABSENTEISME"
              value={`${absentRate.toFixed(1)}%`}
              footer={`${absentToday} absent${absentToday !== 1 ? "s" : ""} sur ${totalEmployees}`}
            />
          </>
        )}
      </div>

      {/* Main content: Demandes + Calendar */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_340px]">
        {/* Demandes table with filter tabs */}
        <Card>
          <CardHeader className="flex-row items-center justify-between">
            <CardTitle>Demandes</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <Tabs defaultValue="all">
              <div className="border-b border-line px-4 pt-1">
                <TabsList>
                  <TabsTrigger value="all">
                    Toutes ({allLeaves.length})
                  </TabsTrigger>
                  <TabsTrigger value="pending">
                    En attente ({allLeaves.filter((l) => l.status === "PENDING").length})
                  </TabsTrigger>
                  <TabsTrigger value="approved">
                    Approuvees ({allLeaves.filter((l) => l.status === "APPROVED").length})
                  </TabsTrigger>
                </TabsList>
              </div>
              {(["all", "pending", "approved"] as const).map((tab) => (
                <TabsContent key={tab} value={tab} className="mt-0">
                  <LeaveRequestsTable
                    leaves={filterFn(tab, allLeaves)}
                    employeeMap={employeeMap}
                    fmt={fmt}
                    tStatuses={tStatuses}
                    onApprove={(id) =>
                      approve.mutate(id, {
                        onSuccess: () => toast.success("Demande approuvee"),
                        onError: (err) => toast.error((err as Error).message),
                      })
                    }
                    onReject={(id) => setRejectingId(id)}
                    isLoading={isLoading}
                  />
                </TabsContent>
              ))}
            </Tabs>
          </CardContent>
        </Card>

        {/* Calendar sidebar */}
        <div className="space-y-4">
          <Card>
            <CardHeader className="flex-row items-center justify-between">
              <CardTitle>Calendrier</CardTitle>
              <div className="flex items-center gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="size-7"
                  onClick={() =>
                    setCalendarDate(
                      new Date(
                        calendarDate.getFullYear(),
                        calendarDate.getMonth() - 1,
                        1,
                      ),
                    )
                  }
                >
                  <ChevronLeft className="size-4" />
                </Button>
                <span className="min-w-[100px] text-center text-[13px] font-semibold text-ink">
                  {calendarDate.toLocaleDateString("fr-FR", {
                    month: "long",
                    year: "numeric",
                  })}
                </span>
                <Button
                  variant="ghost"
                  size="icon"
                  className="size-7"
                  onClick={() =>
                    setCalendarDate(
                      new Date(
                        calendarDate.getFullYear(),
                        calendarDate.getMonth() + 1,
                        1,
                      ),
                    )
                  }
                >
                  <ChevronRight className="size-4" />
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <MiniCalendar
                date={calendarDate}
                absences={allLeaves.filter((l) => l.status === "APPROVED")}
              />
            </CardContent>
          </Card>

          {/* Cette semaine - absents */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Cette semaine</CardTitle>
            </CardHeader>
            <CardContent>
              {approvedOngoing.length === 0 ? (
                <p className="text-center text-[13px] text-ink-3">
                  Aucun employe absent
                </p>
              ) : (
                <div className="space-y-2.5">
                  {approvedOngoing.slice(0, 5).map((l) => {
                    const emp = employeeMap.get(l.employeeId);
                    return (
                      <div
                        key={l.id}
                        className="flex items-center gap-2.5"
                      >
                        <Avatar
                          size="sm"
                          name={emp?.actorDisplayName}
                        />
                        <div className="min-w-0 flex-1">
                          <p className="truncate text-[12.5px] font-medium text-ink">
                            {emp?.actorDisplayName ?? l.employeeId.slice(0, 8)}
                          </p>
                          <p className="text-[11px] text-ink-3">
                            {tStatuses(l.type)} -- {fmt.date(l.dateFin)}
                          </p>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Soldes de conges par employe */}
      <Card>
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle>Soldes de conges par employe</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <LeaveBalancesTable
            employees={employeesQuery.data ?? []}
            leaves={allLeaves}
            employeeMap={employeeMap}
            fmt={fmt}
            isLoading={isLoading}
          />
        </CardContent>
      </Card>

      {/* Reject dialog */}
      <Dialog
        open={!!rejectingId}
        onOpenChange={(v) => !v && setRejectingId(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Rejeter la demande</DialogTitle>
            <DialogDescription>
              Veuillez indiquer le motif du rejet.
            </DialogDescription>
          </DialogHeader>
          <form
            onSubmit={rejectForm.handleSubmit((values) => {
              if (!rejectingId) return;
              reject.mutate(
                { leaveId: rejectingId, commentaire: values.commentaire },
                {
                  onSuccess: () => {
                    toast.success("Demande rejetee");
                    setRejectingId(null);
                    rejectForm.reset();
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              );
            })}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="commentaire">Commentaire</Label>
              <Input
                id="commentaire"
                {...rejectForm.register("commentaire", {
                  required: true,
                  minLength: 1,
                })}
              />
            </div>
            <DialogFooter>
              <Button
                type="button"
                variant="secondary"
                onClick={() => setRejectingId(null)}
              >
                Annuler
              </Button>
              <Button
                type="submit"
                variant="destructive"
                disabled={reject.isPending}
              >
                {reject.isPending && (
                  <Loader2 className="size-4 animate-spin" />
                )}
                Rejeter
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

/* ========================================================================== */
/* Leave Requests Table                                                        */
/* ========================================================================== */

function LeaveRequestsTable({
  leaves,
  employeeMap,
  fmt,
  tStatuses,
  onApprove,
  onReject,
  isLoading,
}: {
  leaves: LeaveRequest[];
  employeeMap: Map<string, Employee>;
  fmt: ReturnType<typeof useFormat>;
  tStatuses: (key: string) => string;
  onApprove: (id: string) => void;
  onReject: (id: string) => void;
  isLoading: boolean;
}) {
  if (isLoading) {
    return (
      <div className="p-6">
        <Skeleton className="h-32 w-full" />
      </div>
    );
  }

  if (leaves.length === 0) {
    return (
      <div className="py-8 text-center text-sm text-ink-3">
        Aucune demande
      </div>
    );
  }

  const typeTone: Record<string, "orange" | "red" | "blue" | "violet" | "green" | "amber" | "teal"> = {
    ANNUAL: "green",
    SICK: "red",
    UNPAID: "amber",
    MATERNITY: "violet",
    PATERNITY: "blue",
    SPECIAL: "teal",
  };

  return (
    <table className="w-full border-separate border-spacing-0">
      <thead>
        <tr>
          {["EMPLOYE", "TYPE", "PERIODE", "JOURS", "STATUT", ""].map(
            (h, i) => (
              <th
                key={i}
                className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
              >
                {h}
              </th>
            ),
          )}
        </tr>
      </thead>
      <tbody>
        {leaves.map((l) => {
          const emp = employeeMap.get(l.employeeId);
          const isPending = l.status === "PENDING";
          return (
            <tr
              key={l.id}
              className="transition-colors hover:bg-cream-dim/40"
            >
              {/* Employee */}
              <td className="border-b border-line-soft px-4 py-3">
                <div className="flex items-center gap-2.5">
                  <Avatar
                    size="sm"
                    name={emp?.actorDisplayName}
                  />
                  <div>
                    <p className="text-[13.5px] font-medium text-ink">
                      {emp?.actorDisplayName ??
                        l.employeeId.slice(0, 8)}
                    </p>
                    <p className="text-[11px] text-ink-3">
                      {emp?.matricule ?? "---"}
                    </p>
                  </div>
                </div>
              </td>

              {/* Type */}
              <td className="border-b border-line-soft px-4 py-3">
                <Badge
                  tone={typeTone[l.type] ?? "gray"}
                  withDot={false}
                >
                  {tStatuses(l.type)}
                </Badge>
              </td>

              {/* Period */}
              <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                {fmt.date(l.dateDebut)} - {fmt.date(l.dateFin)}
              </td>

              {/* Days */}
              <td className="border-b border-line-soft px-4 py-3 text-center text-[13.5px] font-semibold text-ink tabular">
                {fmt.number(Number(l.nbJours))}
              </td>

              {/* Status */}
              <td className="border-b border-line-soft px-4 py-3">
                <StatusBadge kind="leave" status={l.status} />
              </td>

              {/* Actions */}
              <td className="border-b border-line-soft px-4 py-3">
                <div className="flex justify-end gap-1">
                  {isPending && (
                    <>
                      <Button
                        size="sm"
                        variant="ghost"
                        className="size-8 p-0 text-status-green-600 hover:bg-status-green-50 hover:text-status-green-700"
                        onClick={() => onApprove(l.id)}
                        aria-label="Approuver"
                      >
                        <Check className="size-4" />
                      </Button>
                      <Button
                        size="sm"
                        variant="ghost"
                        className="size-8 p-0 text-status-red-600 hover:bg-status-red-50 hover:text-status-red-700"
                        onClick={() => onReject(l.id)}
                        aria-label="Rejeter"
                      >
                        <X className="size-4" />
                      </Button>
                    </>
                  )}
                  <Button
                    size="sm"
                    variant="ghost"
                    className="size-8 p-0 text-ink-3 hover:text-ink"
                    aria-label="Voir"
                  >
                    <Eye className="size-4" />
                  </Button>
                </div>
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}

/* ========================================================================== */
/* Mini Calendar                                                               */
/* ========================================================================== */

function MiniCalendar({
  date,
  absences,
}: {
  date: Date;
  absences: LeaveRequest[];
}) {
  const year = date.getFullYear();
  const month = date.getMonth();
  const firstDay = new Date(year, month, 1).getDay(); // 0=Sun
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const today = new Date();

  // Build a set of days that have absences
  const absentDays = React.useMemo(() => {
    const days = new Set<number>();
    for (const l of absences) {
      const start = new Date(l.dateDebut);
      const end = new Date(l.dateFin);
      for (
        let d = new Date(Math.max(start.getTime(), new Date(year, month, 1).getTime()));
        d <= end && d.getMonth() === month;
        d.setDate(d.getDate() + 1)
      ) {
        days.add(d.getDate());
      }
    }
    return days;
  }, [absences, year, month]);

  const weekDays = ["Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di"];
  // Adjust firstDay: JS uses 0=Sun, we want 0=Mon
  const startOffset = firstDay === 0 ? 6 : firstDay - 1;

  const cells: React.ReactNode[] = [];
  // Empty cells for offset
  for (let i = 0; i < startOffset; i++) {
    cells.push(<div key={`empty-${i}`} />);
  }
  // Day cells
  for (let day = 1; day <= daysInMonth; day++) {
    const isToday =
      day === today.getDate() &&
      month === today.getMonth() &&
      year === today.getFullYear();
    const hasAbsence = absentDays.has(day);
    cells.push(
      <div
        key={day}
        className={`relative flex size-8 items-center justify-center rounded-lg text-[12px] ${
          isToday
            ? "bg-brand-500 font-bold text-white"
            : "text-ink-2 hover:bg-cream-soft"
        }`}
      >
        {day}
        {hasAbsence && !isToday && (
          <span className="absolute bottom-0.5 size-1.5 rounded-full bg-status-red-500" />
        )}
        {hasAbsence && isToday && (
          <span className="absolute bottom-0.5 size-1.5 rounded-full bg-white" />
        )}
      </div>,
    );
  }

  return (
    <div>
      <div className="mb-1 grid grid-cols-7 gap-0.5">
        {weekDays.map((d) => (
          <div
            key={d}
            className="flex size-8 items-center justify-center text-[10px] font-semibold uppercase text-ink-4"
          >
            {d}
          </div>
        ))}
      </div>
      <div className="grid grid-cols-7 gap-0.5">{cells}</div>
    </div>
  );
}

/* ========================================================================== */
/* Leave Balances Table                                                        */
/* ========================================================================== */

function LeaveBalancesTable({
  employees,
  leaves,
  employeeMap,
  fmt,
  isLoading,
}: {
  employees: Employee[];
  leaves: LeaveRequest[];
  employeeMap: Map<string, Employee>;
  fmt: ReturnType<typeof useFormat>;
  isLoading: boolean;
}) {
  if (isLoading) {
    return (
      <div className="p-6">
        <Skeleton className="h-32 w-full" />
      </div>
    );
  }

  // Compute per-employee balance from the leave data we have
  const balances = React.useMemo(() => {
    const map = new Map<
      string,
      {
        annualTaken: number;
        sickTaken: number;
        rttTaken: number;
        totalTaken: number;
      }
    >();

    for (const l of leaves) {
      if (l.status === "REJECTED" || l.status === "CANCELLED") continue;
      const prev = map.get(l.employeeId) ?? {
        annualTaken: 0,
        sickTaken: 0,
        rttTaken: 0,
        totalTaken: 0,
      };
      const days = Number(l.nbJours);
      if (l.type === "ANNUAL") prev.annualTaken += days;
      else if (l.type === "SICK") prev.sickTaken += days;
      else if (l.type === "SPECIAL") prev.rttTaken += days;
      prev.totalTaken += days;
      map.set(l.employeeId, prev);
    }

    return map;
  }, [leaves]);

  // Only show employees who have leaves or active employees (limited to first 10)
  const activeEmployees = employees
    .filter((e) => e.status === "ACTIVE" || e.status === "ON_LEAVE")
    .slice(0, 10);

  if (activeEmployees.length === 0) {
    return (
      <div className="py-8 text-center text-sm text-ink-3">
        Aucun employe
      </div>
    );
  }

  const ANNUAL_TOTAL = 28; // Cameroon standard annual leave
  const SICK_TOTAL = 15;
  const RTT_TOTAL = 5;

  return (
    <table className="w-full border-separate border-spacing-0">
      <thead>
        <tr>
          {[
            "EMPLOYE",
            "ANNUEL ACQUIS",
            "ANNUEL PRIS",
            "ANNUEL RESTANT",
            "MALADIE",
            "RTT",
            "SOLDE TOTAL (JOURS)",
          ].map((h, i) => (
            <th
              key={i}
              className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
            >
              {h}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {activeEmployees.map((emp) => {
          const b = balances.get(emp.id) ?? {
            annualTaken: 0,
            sickTaken: 0,
            rttTaken: 0,
            totalTaken: 0,
          };
          const annualRemaining = ANNUAL_TOTAL - b.annualTaken;
          const sickRemaining = SICK_TOTAL - b.sickTaken;
          const rttRemaining = RTT_TOTAL - b.rttTaken;
          const totalRemaining =
            annualRemaining + sickRemaining + rttRemaining;
          const progressPct = Math.max(
            0,
            Math.min(100, (annualRemaining / ANNUAL_TOTAL) * 100),
          );

          return (
            <tr
              key={emp.id}
              className="transition-colors hover:bg-cream-dim/40"
            >
              {/* Employee */}
              <td className="border-b border-line-soft px-4 py-3">
                <div className="flex items-center gap-2.5">
                  <Avatar size="sm" name={emp.actorDisplayName} />
                  <span className="text-[13.5px] font-medium text-ink">
                    {emp.actorDisplayName}
                  </span>
                </div>
              </td>

              {/* Annual acquired */}
              <td className="border-b border-line-soft px-4 py-3 text-center text-[13.5px] text-ink-2 tabular">
                {ANNUAL_TOTAL}
              </td>

              {/* Annual taken */}
              <td className="border-b border-line-soft px-4 py-3 text-center text-[13.5px] text-ink-2 tabular">
                {b.annualTaken}
              </td>

              {/* Annual remaining with progress bar */}
              <td className="border-b border-line-soft px-4 py-3">
                <div className="flex items-center gap-2">
                  <span className="w-8 text-center text-[13.5px] font-semibold text-ink tabular">
                    {annualRemaining}
                  </span>
                  <div className="h-2 flex-1 rounded-full bg-cream-soft">
                    <div
                      className="h-full rounded-full bg-brand-500 transition-all"
                      style={{ width: `${progressPct}%` }}
                    />
                  </div>
                </div>
              </td>

              {/* Sick */}
              <td className="border-b border-line-soft px-4 py-3 text-center text-[13.5px] text-ink-2 tabular">
                {sickRemaining}
              </td>

              {/* RTT */}
              <td className="border-b border-line-soft px-4 py-3 text-center text-[13.5px] text-ink-2 tabular">
                {rttRemaining}
              </td>

              {/* Total */}
              <td className="border-b border-line-soft px-4 py-3 text-center text-[13.5px] font-bold text-ink tabular">
                {totalRemaining}
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}
