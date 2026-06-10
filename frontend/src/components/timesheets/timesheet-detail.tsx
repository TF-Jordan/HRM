"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AlertTriangle, CheckCircle2, ChevronLeft, Clock, Loader2, Send, XCircle } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/input";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatPeriod } from "@/lib/format";
import { timesheetStatusTone, timesheetTotalHours } from "@/lib/timesheet-status";
import { cn } from "@/lib/utils";
import type { TimesheetResponse } from "@/server/ksm/modules/timesheets";

export function TimesheetDetail({ timesheetId }: { timesheetId: string }) {
  const t = useTranslations("timesheets");
  const tDetail = useTranslations("timesheets.detail");
  const tHours = useTranslations("timesheets.hours");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();

  const canCreate = useCan("hrm:timesheet:create");
  const canValidate = useCan("hrm:timesheet:validate");

  // Reject modal state
  const [showRejectModal, setShowRejectModal] = React.useState(false);
  const [rejectComment, setRejectComment] = React.useState("");
  const [rejectError, setRejectError] = React.useState(false);

  const query = useQuery({
    queryKey: ["hrm", "timesheet", timesheetId],
    queryFn: () => apiFetch<TimesheetResponse>(`/api/hrm/timesheets/${timesheetId}`),
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "timesheet", timesheetId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "timesheets"] });
  }, [queryClient, timesheetId]);

  function handleApiError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const submitMutation = useMutation({
    mutationFn: () =>
      apiFetch<TimesheetResponse>(`/api/hrm/timesheets/${timesheetId}/submit`, { method: "POST" }),
    onSuccess: () => {
      toast.success(tDetail("submitSuccess"));
      invalidate();
    },
    onError: handleApiError,
  });

  const validateMutation = useMutation({
    mutationFn: () =>
      apiFetch<TimesheetResponse>(`/api/hrm/timesheets/${timesheetId}/validate`, {
        method: "POST",
      }),
    onSuccess: () => {
      toast.success(tDetail("validateSuccess"));
      invalidate();
    },
    onError: handleApiError,
  });

  const rejectMutation = useMutation({
    mutationFn: (comment: string) =>
      apiFetch<TimesheetResponse>(`/api/hrm/timesheets/${timesheetId}/reject`, {
        method: "POST",
        body: { comment },
      }),
    onSuccess: () => {
      toast.success(tDetail("rejectSuccess"));
      setShowRejectModal(false);
      setRejectComment("");
      invalidate();
    },
    onError: handleApiError,
  });

  function handleRejectSubmit() {
    if (!rejectComment.trim()) {
      setRejectError(true);
      return;
    }
    rejectMutation.mutate(rejectComment.trim());
  }

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
        {query.error instanceof BffApiError ? query.error.message : "Not found"}
      </div>
    );
  }

  const ts = query.data;
  const isDraft = ts.status === "DRAFT";
  const isSubmitted = ts.status === "SUBMITTED";
  const isRejected = ts.status === "REJECTED";

  const steps: WorkflowStep[] = [
    { key: "draft", label: t("status.DRAFT"), state: ts.status === "DRAFT" ? "active" : "done" },
    {
      key: "submitted",
      label: t("status.SUBMITTED"),
      state:
        ts.status === "SUBMITTED"
          ? "active"
          : ts.status === "VALIDATED" || ts.status === "REJECTED"
            ? "done"
            : "pending",
    },
    ...(isRejected
      ? [
          {
            key: "rejected" as const,
            label: t("status.REJECTED"),
            state: "active" as const,
          },
        ]
      : [
          {
            key: "validated" as const,
            label: t("status.VALIDATED"),
            state: (ts.status === "VALIDATED" ? "done" : "pending") as "done" | "pending",
          },
        ]),
  ];

  const num = (v: number | string) => Number(v).toFixed(1);
  const rows: Array<[string, string, boolean?]> = [
    [tHours("normales"), num(ts.heuresNormales)],
    [tHours("supplementaires"), num(ts.heuresSupplementaires)],
    [tHours("nuit"), num(ts.heuresNuit)],
    [tHours("weekend"), num(ts.heuresWeekend)],
    [tHours("absences"), num(ts.absencesNonJustifiees), true],
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("admin.title"), href: "/timesheets" },
          { label: formatPeriod(ts.periode, "fr") },
        ]}
        title={
          <span className="flex items-center gap-3 capitalize">
            <Clock className="h-6 w-6 text-orange-500" />
            {formatPeriod(ts.periode, "fr")}
          </span>
        }
        subtitle={
          <span className="flex items-center gap-2">
            <Badge tone={timesheetStatusTone(ts.status)}>{t(`status.${ts.status}`)}</Badge>
            <span className="font-mono-tabular text-ink-3">
              {timesheetTotalHours(ts).toFixed(1)} h
            </span>
          </span>
        }
        actions={
          <>
            <Link href="/timesheets">
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("my.title")}
              </Button>
            </Link>
            {canCreate && isDraft && (
              <Button
                type="button"
                onClick={() => submitMutation.mutate()}
                disabled={submitMutation.isPending}
              >
                {submitMutation.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Send className="h-4 w-4" />
                )}
                {tDetail("actions.submit")}
              </Button>
            )}
            {canValidate && isSubmitted && (
              <>
                <Button
                  type="button"
                  onClick={() => validateMutation.mutate()}
                  disabled={validateMutation.isPending}
                >
                  {validateMutation.isPending ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <CheckCircle2 className="h-4 w-4" />
                  )}
                  {tDetail("actions.validate")}
                </Button>
                <Button
                  type="button"
                  variant="danger"
                  onClick={() => {
                    setShowRejectModal(true);
                    setRejectComment("");
                    setRejectError(false);
                  }}
                  disabled={rejectMutation.isPending}
                >
                  <XCircle className="h-4 w-4" />
                  {tDetail("actions.reject")}
                </Button>
              </>
            )}
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <WorkflowStepper steps={steps} />
        </CardContent>
      </Card>

      {/* Rejection banner */}
      {isRejected && ts.rejectionComment && (
        <Card className="mb-6">
          <CardContent padding="lg">
            <div className="flex items-start gap-3">
              <span className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-danger-50 text-danger-600">
                <AlertTriangle className="h-4.5 w-4.5" />
              </span>
              <div>
                <p className="text-[13px] font-semibold text-danger-700">{tDetail("rejectionComment")}</p>
                <p className="mt-1 text-[13.5px] text-ink-2">{ts.rejectionComment}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardContent padding="lg">
          <p className="mb-4 text-[11px] font-bold uppercase tracking-[0.1em] text-ink-3">
            {tDetail("breakdown")}
          </p>
          <div className="divide-y divide-line-soft">
            {rows.map(([label, value, danger]) => (
              <div key={label} className="flex items-center justify-between py-3">
                <Label className="text-[13.5px] text-ink-2">{label}</Label>
                <span
                  className={`font-mono-tabular text-[15px] font-semibold ${
                    danger && Number(value) > 0 ? "text-danger-600" : "text-ink"
                  }`}
                >
                  {value}
                </span>
              </div>
            ))}
            <div className="flex items-center justify-between py-3.5">
              <span className="text-[13.5px] font-bold uppercase tracking-wider text-ink">
                {tHours("total")}
              </span>
              <span className="font-mono-tabular text-[18px] font-extrabold text-orange-700">
                {timesheetTotalHours(ts).toFixed(1)}
              </span>
            </div>
          </div>
          <p className="mt-5 text-[12px] text-ink-4">
            ID · <span className="font-mono-tabular">{ts.id}</span>
          </p>
        </CardContent>
      </Card>

      {/* Reject modal */}
      {showRejectModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={() => setShowRejectModal(false)}>
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
              <Button type="button" variant="secondary" onClick={() => setShowRejectModal(false)}>
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
