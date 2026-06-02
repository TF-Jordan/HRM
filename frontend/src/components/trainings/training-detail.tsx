"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, ChevronLeft, GraduationCap, Loader2, Play, Plus, UserPlus } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Label } from "@/components/ui/input";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatNumber } from "@/lib/format";
import { enrollmentStatusTone, trainingStatusTone } from "@/lib/training-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { EnrollmentResponse, TrainingResponse, TrainingStatus } from "@/server/ksm/modules/trainings";

export function TrainingDetail({ trainingId }: { trainingId: string }) {
  const t = useTranslations("trainings");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const canManage = useCan("hrm:training:manage");
  const [showEnroll, setShowEnroll] = React.useState(false);
  const [picked, setPicked] = React.useState("");

  const query = useQuery({
    queryKey: ["hrm", "training", trainingId],
    queryFn: () => apiFetch<TrainingResponse>(`/api/hrm/trainings/${trainingId}`),
  });
  const enrollmentsQuery = useQuery({
    queryKey: ["hrm", "training", trainingId, "enrollments"],
    queryFn: () =>
      apiFetch<EnrollmentResponse[]>(`/api/hrm/trainings/${trainingId}/enrollments`),
  });
  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
    enabled: canManage,
  });
  const nameOf = React.useCallback(
    (employeeId: string) => {
      const e = employeesQuery.data?.find((x) => x.id === employeeId);
      return e?.actorDisplayName ?? e?.matricule ?? `${employeeId.slice(0, 8)}…`;
    },
    [employeesQuery.data],
  );

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "training", trainingId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "trainings"] });
  }, [queryClient, trainingId]);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }
  const action = (path: string) =>
    apiFetch(`/api/hrm/trainings/${trainingId}/${path}`, { method: "POST" });

  const startM = useMutation({ mutationFn: () => action("start"), onSuccess: () => { toast.success(t("detail.startSuccess")); invalidate(); }, onError: handleError });
  const completeM = useMutation({ mutationFn: () => action("complete"), onSuccess: () => { toast.success(t("detail.completeSuccess")); invalidate(); }, onError: handleError });
  const cancelM = useMutation({ mutationFn: () => action("cancel"), onSuccess: () => { toast.success(t("detail.cancelSuccess")); invalidate(); }, onError: handleError });
  const enrollM = useMutation({
    mutationFn: (employeeId: string) =>
      apiFetch(`/api/hrm/trainings/${trainingId}/enrollments`, { method: "POST", body: { employeeId } }),
    onSuccess: () => {
      toast.success(t("enrollSuccess"));
      setShowEnroll(false);
      setPicked("");
      invalidate();
    },
    onError: handleError,
  });

  if (query.isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (query.error || !query.data) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {query.error instanceof BffApiError ? query.error.message : "—"}
      </div>
    );
  }

  const tr = query.data;
  const enrollments = enrollmentsQuery.data ?? [];
  const eligible = (employeesQuery.data ?? []).filter(
    (e) => !enrollments.some((en) => en.employeeId === e.id),
  );

  const showStart = canManage && tr.status === "PLANNED";
  const showComplete = canManage && tr.status === "IN_PROGRESS";
  const showCancel = canManage && tr.status !== "COMPLETED" && tr.status !== "CANCELLED";

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/trainings" },
          { label: tr.intitule },
        ]}
        title={
          <span className="flex items-center gap-3">
            <GraduationCap className="h-6 w-6 text-orange-500" />
            {tr.intitule}
          </span>
        }
        subtitle={
          <span className="flex flex-wrap items-center gap-3">
            <Badge tone={trainingStatusTone(tr.status)}>{t(`status.${tr.status}`)}</Badge>
            {tr.dateDebut && (
              <span className="text-[12.5px] text-ink-3">
                {formatDate(tr.dateDebut, { locale })}
                {tr.dateFin ? ` → ${formatDate(tr.dateFin, { locale })}` : ""}
              </span>
            )}
          </span>
        }
        actions={
          <>
            <Link href="/trainings">
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("detail.back")}
              </Button>
            </Link>
            {showCancel && (
              <Button type="button" variant="ghost" onClick={() => cancelM.mutate()}>
                {t("detail.actions.cancel")}
              </Button>
            )}
            {showStart && (
              <Button type="button" onClick={() => startM.mutate()} disabled={startM.isPending}>
                {startM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Play className="h-4 w-4" />}
                {t("detail.actions.start")}
              </Button>
            )}
            {showComplete && (
              <Button type="button" onClick={() => completeM.mutate()} disabled={completeM.isPending}>
                {completeM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <CheckCircle2 className="h-4 w-4" />}
                {t("detail.actions.complete")}
              </Button>
            )}
            {canManage && (
              <Button type="button" onClick={() => setShowEnroll(true)}>
                <UserPlus className="h-4 w-4" />
                {t("detail.actions.enroll")}
              </Button>
            )}
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <WorkflowStepper steps={workflowSteps(tr.status, t)} />
        </CardContent>
      </Card>

      <Card className="mb-6">
        <CardContent padding="lg">
          <dl className="grid grid-cols-2 gap-x-8 gap-y-4 md:grid-cols-4">
            <Detail label={t("detail.organisme")} value={tr.organisme ?? "—"} />
            <Detail
              label={t("detail.period")}
              value={
                tr.dateDebut
                  ? `${formatDate(tr.dateDebut, { locale })}${tr.dateFin ? ` → ${formatDate(tr.dateFin, { locale })}` : ""}`
                  : "—"
              }
              mono
            />
            <Detail label={t("detail.location")} value={tr.lieu ?? "—"} />
            <Detail
              label={t("detail.cost")}
              value={tr.cout != null ? `${formatNumber(Number(tr.cout), locale)} XAF` : "—"}
              mono
            />
          </dl>
        </CardContent>
      </Card>

      <div className="mb-3 text-[13px] font-bold tracking-tight text-ink">{t("detail.enrollments")}</div>
      <Card>
        {enrollmentsQuery.isLoading ? (
          <div className="grid place-items-center py-10">
            <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
          </div>
        ) : enrollments.length === 0 ? (
          <div className="px-5 py-10 text-center text-[13px] text-ink-3">
            {t("detail.enrollmentsEmpty")}
          </div>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-bg-dim">
                <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("detail.columns.employee")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("detail.columns.status")}
                </th>
                <th className="px-3 py-3 text-right text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("detail.columns.note")}
                </th>
              </tr>
            </thead>
            <tbody>
              {enrollments.map((en) => (
                <tr key={en.id} className="border-t border-line-soft">
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-2.5">
                      <Avatar name={nameOf(en.employeeId)} size="sm" />
                      <span className="text-[13px] font-semibold text-ink">{nameOf(en.employeeId)}</span>
                    </div>
                  </td>
                  <td className="px-3 py-3">
                    <Badge tone={enrollmentStatusTone(en.status)}>
                      {t(`enrollmentStatus.${en.status}`)}
                    </Badge>
                  </td>
                  <td className="px-3 py-3 text-right font-mono-tabular text-[13px] text-ink">
                    {en.noteEvaluation != null ? Number(en.noteEvaluation).toFixed(1) : "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <Dialog
        open={showEnroll}
        onClose={() => setShowEnroll(false)}
        title={t("detail.actions.enroll")}
        footer={
          <>
            <Button type="button" variant="ghost" onClick={() => setShowEnroll(false)}>
              {tCommon("actions.cancel")}
            </Button>
            <Button
              type="button"
              disabled={!picked || enrollM.isPending}
              onClick={() => enrollM.mutate(picked)}
            >
              {enrollM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
              {t("detail.enrollEmployee")}
            </Button>
          </>
        }
      >
        <Field label={t("detail.columns.employee")}>
          <select
            value={picked}
            onChange={(e) => setPicked(e.target.value)}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            <option value="">—</option>
            {eligible.map((e) => (
              <option key={e.id} value={e.id}>
                {e.matricule} · {e.actorDisplayName ?? e.actorId.slice(0, 8)}
              </option>
            ))}
          </select>
        </Field>
      </Dialog>
    </>
  );
}

function Detail({ label, value, mono }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div>
      <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">{label}</Label>
      <p className={mono ? "mt-0.5 font-mono-tabular text-[13.5px] font-bold text-ink" : "mt-0.5 text-[14px] font-medium text-ink"}>
        {value}
      </p>
    </div>
  );
}

function workflowSteps(status: TrainingStatus, t: (k: string) => string): WorkflowStep[] {
  if (status === "CANCELLED") {
    return [{ key: "cancelled", label: t("status.CANCELLED"), state: "active" }];
  }
  const order: TrainingStatus[] = ["PLANNED", "IN_PROGRESS", "COMPLETED"];
  const idx = order.indexOf(status);
  const mk = (k: string, l: string, p: number): WorkflowStep => ({
    key: k,
    label: l,
    state: p < idx ? "done" : p === idx ? "active" : "pending",
  });
  return [
    mk("planned", t("status.PLANNED"), 0),
    mk("in_progress", t("status.IN_PROGRESS"), 1),
    mk("completed", t("status.COMPLETED"), 2),
  ];
}
