"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CalendarRange, CheckCircle2, ChevronLeft, Loader2, XCircle } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Label, Textarea } from "@/components/ui/input";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatDateTime } from "@/lib/format";
import { leaveStatusTone } from "@/lib/leave-status";
import type { LeaveResponse } from "@/server/ksm/modules/leaves";

export function LeaveDetail({ leaveRequestId }: { leaveRequestId: string }) {
  const t = useTranslations("leaves");
  const tDetail = useTranslations("leaves.detail");
  const tEmpType = useTranslations("employees.leaveType");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();
  const { session } = useSession();
  const [modal, setModal] = React.useState<null | "reject" | "cancel">(null);

  const canApprove = useCan("hrm:leave:approve");
  const canCreate = useCan("hrm:leave:create");

  const query = useQuery({
    queryKey: ["hrm", "leave", leaveRequestId],
    queryFn: () => apiFetch<LeaveResponse>(`/api/hrm/leaves/${leaveRequestId}`),
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "leave", leaveRequestId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "leaves"] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "leaves", "mine"] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "leaves", "pending"] });
  }, [queryClient, leaveRequestId]);

  function handleApiError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const approveMutation = useMutation({
    mutationFn: () =>
      apiFetch<LeaveResponse>(`/api/hrm/leaves/${leaveRequestId}/approve`, { method: "POST" }),
    onSuccess: () => {
      toast.success(tDetail("approveSuccess"));
      invalidate();
    },
    onError: handleApiError,
  });

  const rejectMutation = useMutation({
    mutationFn: (commentaire: string) =>
      apiFetch<LeaveResponse>(`/api/hrm/leaves/${leaveRequestId}/reject`, {
        method: "POST",
        body: { commentaire },
      }),
    onSuccess: () => {
      toast.success(tDetail("rejectSuccess"));
      invalidate();
      setModal(null);
    },
    onError: handleApiError,
  });

  const cancelMutation = useMutation({
    mutationFn: () =>
      apiFetch<LeaveResponse>(`/api/hrm/leaves/${leaveRequestId}/cancel`, { method: "POST" }),
    onSuccess: () => {
      toast.success(tDetail("cancelSuccess"));
      invalidate();
      setModal(null);
    },
    onError: handleApiError,
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
        {query.error instanceof BffApiError ? query.error.message : "Not found"}
      </div>
    );
  }
  const leave = query.data;
  const isPending = leave.status === "PENDING";
  const isApproved = leave.status === "APPROVED";
  const startInFuture = new Date(leave.dateDebut) > new Date();

  const steps: WorkflowStep[] = [
    { key: "submit", label: tCommon("status.submitted"), state: "done" },
    {
      key: "review",
      label: tDetail("validatedBy"),
      state:
        leave.status === "APPROVED" || leave.status === "REJECTED"
          ? "done"
          : leave.status === "PENDING"
            ? "active"
            : "pending",
    },
    {
      key: "result",
      label:
        leave.status === "APPROVED"
          ? t("status.APPROVED")
          : leave.status === "REJECTED"
            ? t("status.REJECTED")
            : leave.status === "CANCELLED"
              ? t("status.CANCELLED")
              : "—",
      state: leave.status === "PENDING" ? "pending" : "done",
    },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("queue.title"), href: "/leaves" },
          { label: tEmpType(leave.type) },
        ]}
        title={
          <span className="flex items-center gap-3">
            <CalendarRange className="h-6 w-6 text-orange-500" />
            {tEmpType(leave.type)} · {formatDate(leave.dateDebut, { locale: "fr" })} →{" "}
            {formatDate(leave.dateFin, { locale: "fr" })}
          </span>
        }
        subtitle={
          <span className="flex items-center gap-2">
            <Badge tone={leaveStatusTone(leave.status)}>{t(`status.${leave.status}`)}</Badge>
            <span className="font-mono-tabular text-ink-3">
              {Number(leave.nbJours).toFixed(1)} jours
            </span>
          </span>
        }
        actions={
          <>
            <Link href={canApprove ? "/leaves" : "/leaves"}>
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {canApprove ? t("queue.title") : t("my.title")}
              </Button>
            </Link>
            {canApprove && isPending && (
              <>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => setModal("reject")}
                  disabled={rejectMutation.isPending}
                >
                  <XCircle className="h-4 w-4" />
                  {tDetail("actions.reject")}
                </Button>
                <Button
                  type="button"
                  onClick={() => approveMutation.mutate()}
                  disabled={approveMutation.isPending}
                >
                  {approveMutation.isPending ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <CheckCircle2 className="h-4 w-4" />
                  )}
                  {tDetail("actions.approve")}
                </Button>
              </>
            )}
            {canCreate && !canApprove && (isPending || (isApproved && startInFuture)) && (
              <Button type="button" variant="danger" onClick={() => setModal("cancel")}>
                {tDetail("actions.cancel")}
              </Button>
            )}
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <WorkflowStepper steps={steps} />
        </CardContent>
      </Card>

      <Card>
        <CardContent padding="lg">
          <dl className="grid grid-cols-2 gap-x-8 gap-y-4">
            <div>
              <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                {t("queue.columns.employee")}
              </Label>
              <p className="mt-0.5 font-mono-tabular text-[13px] text-ink">
                {leave.employeeId.slice(0, 8)}…
              </p>
            </div>
            <div>
              <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                {t("queue.columns.type")}
              </Label>
              <p className="mt-0.5 text-[14px] font-medium text-ink">{tEmpType(leave.type)}</p>
            </div>
            <div>
              <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                {t("queue.columns.period")}
              </Label>
              <p className="mt-0.5 font-mono-tabular text-[13.5px] text-ink">
                {formatDate(leave.dateDebut, { locale: "fr" })} →{" "}
                {formatDate(leave.dateFin, { locale: "fr" })}
              </p>
            </div>
            <div>
              <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                {t("queue.columns.days")}
              </Label>
              <p className="mt-0.5 font-mono-tabular text-[14px] font-bold text-ink">
                {Number(leave.nbJours).toFixed(1)}
              </p>
            </div>
            <div className="col-span-2">
              <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                {t("queue.columns.reason")}
              </Label>
              <p className="mt-0.5 text-[14px] text-ink-2">{leave.motif ?? "—"}</p>
            </div>
            {leave.valideurDisplayName && (
              <>
                <div>
                  <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                    {tDetail("validatedBy")}
                  </Label>
                  <p className="mt-0.5 text-[14px] font-medium text-ink">
                    {leave.valideurDisplayName}
                  </p>
                </div>
                <div>
                  <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                    {tDetail("validatedAt")}
                  </Label>
                  <p className="mt-0.5 font-mono-tabular text-[12.5px] text-ink-2">
                    {leave.dateValidation
                      ? formatDateTime(leave.dateValidation, "fr")
                      : "—"}
                  </p>
                </div>
              </>
            )}
            {leave.commentaireValideur && (
              <div className="col-span-2">
                <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                  {tDetail("comment")}
                </Label>
                <p className="mt-0.5 text-[14px] text-ink-2">{leave.commentaireValideur}</p>
              </div>
            )}
          </dl>
          <p className="mt-6 text-[12px] text-ink-4">
            ID · <span className="font-mono-tabular">{leave.id}</span>
          </p>
        </CardContent>
      </Card>

      <RejectModal
        open={modal === "reject"}
        onClose={() => setModal(null)}
        onConfirm={(c) => rejectMutation.mutate(c)}
        loading={rejectMutation.isPending}
      />
      <CancelModal
        open={modal === "cancel"}
        onClose={() => setModal(null)}
        onConfirm={() => cancelMutation.mutate()}
        loading={cancelMutation.isPending}
      />
    </>
  );
}

function RejectModal({
  open,
  onClose,
  onConfirm,
  loading,
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: (commentaire: string) => void;
  loading: boolean;
}) {
  const t = useTranslations("leaves.detail.reject");
  const tCommon = useTranslations("common");
  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{ commentaire: string }>({ mode: "onChange" });
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
      subtitle={t("subtitle")}
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

function CancelModal({
  open,
  onClose,
  onConfirm,
  loading,
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  loading: boolean;
}) {
  const t = useTranslations("leaves.detail.cancel");
  const tCommon = useTranslations("common");
  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={t("title")}
      subtitle={t("subtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.close")}
          </Button>
          <Button type="button" variant="danger" onClick={onConfirm} disabled={loading}>
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : t("confirm")}
          </Button>
        </>
      }
    >
      <p className="text-[13.5px] text-ink-3">{t("subtitle")}</p>
    </Dialog>
  );
}
