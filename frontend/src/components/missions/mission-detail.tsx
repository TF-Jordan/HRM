"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  CheckCircle2,
  ChevronLeft,
  Loader2,
  MapPin,
  Play,
  Send,
  XCircle,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Label, Textarea } from "@/components/ui/input";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { useCan } from "@/hooks/use-can";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatDateTime, formatNumber } from "@/lib/format";
import { missionStatusTone } from "@/lib/mission-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  AmendMissionOrderRequest,
  MissionOrderResponse,
} from "@/server/ksm/modules/missions";

export function MissionDetail({ missionOrderId }: { missionOrderId: string }) {
  const t = useTranslations("missions");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const { session } = useSession();
  const [modal, setModal] = React.useState<null | "decline" | "amend" | "cancel">(null);

  const canManage = useCan("hrm:mission:manage");
  const canAccept = useCan("hrm:mission:accept");

  const query = useQuery({
    queryKey: ["hrm", "mission-order", missionOrderId],
    queryFn: () =>
      apiFetch<MissionOrderResponse>(`/api/hrm/mission-orders/${missionOrderId}`),
  });

  const meQuery = useQuery({
    queryKey: ["hrm", "mission-orders", "mine"],
    queryFn: () =>
      apiFetch<{ employee: EmployeeResponse | null }>(`/api/hrm/mission-orders/mine`),
    enabled: !!session,
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "mission-order", missionOrderId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "mission-orders"] });
  }, [queryClient, missionOrderId]);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }

  const mutate = (path: string, body?: unknown) =>
    apiFetch<MissionOrderResponse>(`/api/hrm/mission-orders/${missionOrderId}/${path}`, {
      method: "POST",
      body,
    });

  const issueM = useMutation({
    mutationFn: () => mutate("issue"),
    onSuccess: () => {
      toast.success(t("detail.issueSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const acceptM = useMutation({
    mutationFn: () => mutate("accept"),
    onSuccess: () => {
      toast.success(t("detail.acceptSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const declineM = useMutation({
    mutationFn: (reason: string) => mutate("decline", { reason }),
    onSuccess: () => {
      toast.success(t("detail.declineSuccess"));
      setModal(null);
      invalidate();
    },
    onError: handleError,
  });
  const amendM = useMutation({
    mutationFn: (body: AmendMissionOrderRequest) => mutate("amend", body),
    onSuccess: () => {
      toast.success(t("amend.success"));
      setModal(null);
      invalidate();
    },
    onError: handleError,
  });
  const startM = useMutation({
    mutationFn: () => mutate("start"),
    onSuccess: () => {
      toast.success(t("detail.startSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const completeM = useMutation({
    mutationFn: () => mutate("complete"),
    onSuccess: () => {
      toast.success(t("detail.completeSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const cancelM = useMutation({
    mutationFn: () => mutate("cancel"),
    onSuccess: () => {
      toast.success(t("detail.cancelSuccess"));
      setModal(null);
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

  const o = query.data;
  const isMine = !!meQuery.data?.employee && meQuery.data.employee.id === o.employeeId;
  const steps = workflowSteps(o.status, t);
  const reference = shortRef(o.id);

  // What the current user can do, given role + state ownership.
  const showIssue = canManage && o.status === "DRAFT";
  const showAcceptDecline = canAccept && isMine && o.status === "PENDING_ACCEPTANCE";
  const showAmend = canManage && o.status === "DECLINED";
  const showStart = canManage && o.status === "APPROVED";
  const showComplete = canManage && o.status === "IN_PROGRESS";
  const showCancel =
    canManage &&
    o.status !== "COMPLETED" &&
    o.status !== "CANCELLED" &&
    o.status !== "DECLINED";

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("queue.title"), href: canManage ? "/mission-orders" : "/mission-orders/mine" },
          { label: reference },
        ]}
        title={
          <span className="flex items-center gap-3">
            <MapPin className="h-6 w-6 text-orange-500" />
            {o.destination}
          </span>
        }
        subtitle={
          <span className="flex flex-wrap items-center gap-3">
            <Badge tone={missionStatusTone(o.status)}>{t(`status.${o.status}`)}</Badge>
            <span className="font-mono-tabular text-[12px] text-ink-3">{reference}</span>
            <span className="text-[12.5px] text-ink-3">
              {formatDate(o.dateDebut, { locale })} → {formatDate(o.dateFin, { locale })}
            </span>
          </span>
        }
        actions={
          <>
            <Link href={canManage ? "/mission-orders" : "/mission-orders/mine"}>
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("detail.back")}
              </Button>
            </Link>
            {showCancel && (
              <Button type="button" variant="ghost" onClick={() => cancelM.mutate()}>
                {t("detail.cancelButton")}
              </Button>
            )}
            {showAmend && (
              <Button type="button" onClick={() => setModal("amend")}>
                {t("detail.amendButton")}
              </Button>
            )}
            {showStart && (
              <Button type="button" onClick={() => startM.mutate()} disabled={startM.isPending}>
                {startM.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Play className="h-4 w-4" />
                )}
                {t("detail.startButton")}
              </Button>
            )}
            {showComplete && (
              <Button type="button" onClick={() => completeM.mutate()} disabled={completeM.isPending}>
                {completeM.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <CheckCircle2 className="h-4 w-4" />
                )}
                {t("detail.completeButton")}
              </Button>
            )}
            {showAcceptDecline && (
              <>
                <Button type="button" variant="secondary" onClick={() => setModal("decline")}>
                  <XCircle className="h-4 w-4" />
                  {t("detail.declineButton")}
                </Button>
                <Button type="button" onClick={() => acceptM.mutate()} disabled={acceptM.isPending}>
                  {acceptM.isPending ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <CheckCircle2 className="h-4 w-4" />
                  )}
                  {t("detail.acceptButton")}
                </Button>
              </>
            )}
            {showIssue && (
              <Button type="button" onClick={() => issueM.mutate()} disabled={issueM.isPending}>
                {issueM.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Send className="h-4 w-4" />
                )}
                {t("detail.issueButton")}
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
            <Detail label={t("detail.destination")} value={o.destination} />
            <Detail label={t("detail.centreCout")} value={o.centreCout ?? "—"} />
            <div className="col-span-2">
              <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                {t("detail.objet")}
              </Label>
              <p className="mt-0.5 text-[14px] text-ink-2">{o.objet}</p>
            </div>
            <Detail
              label={t("detail.period")}
              value={`${formatDate(o.dateDebut, { locale })} → ${formatDate(o.dateFin, { locale })}`}
              mono
            />
            <Detail
              label={t("detail.allowance")}
              value={
                o.montantAvance != null ? formatNumber(Number(o.montantAvance), locale) : "—"
              }
              mono
            />
            <Detail
              label={t("detail.employee")}
              value={`${o.employeeId.slice(0, 8)}…`}
              mono
            />
            {o.parentOrderId && (
              <Detail
                label={t("detail.parent")}
                value={
                  <Link
                    href={`/mission-orders/${o.parentOrderId}`}
                    className="font-mono-tabular text-orange-600 hover:text-orange-700"
                  >
                    {t("detail.parentLink")} · {o.parentOrderId.slice(0, 8)}…
                  </Link>
                }
              />
            )}
            {o.status === "DECLINED" && o.decisionReason && (
              <div className="col-span-2 rounded-[12px] border border-danger-200 bg-danger-50 p-4">
                <Label className="text-[11px] uppercase tracking-wider text-danger-600">
                  {t("detail.decisionReason")}
                </Label>
                <p className="mt-1 text-[13.5px] text-danger-700">{o.decisionReason}</p>
                {o.decidedAt && (
                  <p className="mt-2 font-mono-tabular text-[11px] text-danger-600/80">
                    {t("detail.decidedAt")} · {formatDateTime(o.decidedAt, locale)}
                  </p>
                )}
              </div>
            )}
          </dl>
          <p className="mt-6 text-[12px] text-ink-4">
            ID · <span className="font-mono-tabular">{o.id}</span>
          </p>
        </CardContent>
      </Card>

      <DeclineModal
        open={modal === "decline"}
        onClose={() => setModal(null)}
        onConfirm={(r) => declineM.mutate(r)}
        loading={declineM.isPending}
      />
      <AmendModal
        open={modal === "amend"}
        parent={o}
        locale={locale}
        onClose={() => setModal(null)}
        onConfirm={(body) => amendM.mutate(body)}
        loading={amendM.isPending}
      />
    </>
  );
}

function Detail({
  label,
  value,
  mono,
}: {
  label: string;
  value: React.ReactNode;
  mono?: boolean;
}) {
  return (
    <div>
      <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">{label}</Label>
      <p
        className={
          mono
            ? "mt-0.5 font-mono-tabular text-[13.5px] font-bold text-ink"
            : "mt-0.5 text-[14px] font-medium text-ink"
        }
      >
        {value}
      </p>
    </div>
  );
}

function workflowSteps(
  status: MissionOrderResponse["status"],
  t: (k: string) => string,
): WorkflowStep[] {
  const passedTo = (target: MissionOrderResponse["status"][]): "done" | "active" | "pending" =>
    target.includes(status) ? "active" : "pending";

  const isAfter = (s: MissionOrderResponse["status"]): boolean => {
    const order: MissionOrderResponse["status"][] = [
      "DRAFT",
      "PENDING_ACCEPTANCE",
      "APPROVED",
      "IN_PROGRESS",
      "COMPLETED",
    ];
    const here = order.indexOf(status);
    const target = order.indexOf(s);
    return here >= 0 && target >= 0 && here > target;
  };

  if (status === "DECLINED") {
    return [
      { key: "draft", label: t("detail.workflow.draft"), state: "done" },
      { key: "issued", label: t("detail.workflow.issued"), state: "done" },
      { key: "decision", label: t("detail.workflow.declined"), state: "active" },
    ];
  }
  if (status === "CANCELLED") {
    return [{ key: "cancelled", label: t("status.CANCELLED"), state: "active" }];
  }
  return [
    {
      key: "draft",
      label: t("detail.workflow.draft"),
      state: status === "DRAFT" ? "active" : "done",
    },
    {
      key: "issued",
      label: t("detail.workflow.issued"),
      state: status === "DRAFT" ? "pending" : isAfter("PENDING_ACCEPTANCE") ? "done" : "active",
    },
    {
      key: "decision",
      label: t("detail.workflow.decision"),
      state:
        status === "PENDING_ACCEPTANCE"
          ? "active"
          : isAfter("PENDING_ACCEPTANCE")
            ? "done"
            : "pending",
    },
    {
      key: "running",
      label: t("detail.workflow.running"),
      state:
        status === "IN_PROGRESS" ? "active" : isAfter("IN_PROGRESS") ? "done" : passedTo([]),
    },
    {
      key: "done",
      label: t("detail.workflow.done"),
      state: status === "COMPLETED" ? "active" : "pending",
    },
  ];
}

function shortRef(uuid: string): string {
  return `MO-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}

function DeclineModal({
  open,
  onClose,
  onConfirm,
  loading,
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => void;
  loading: boolean;
}) {
  const t = useTranslations("missions.detail.decline");
  const tCommon = useTranslations("common");
  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{ reason: string }>({ mode: "onChange" });
  React.useEffect(() => {
    if (!open) reset({ reason: "" });
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
            onClick={handleSubmit((v) => onConfirm(v.reason))}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : t("confirm")}
          </Button>
        </>
      }
    >
      <Field label={t("reasonLabel")}>
        <Textarea
          rows={4}
          placeholder={t("reasonPlaceholder")}
          {...register("reason", { required: true, minLength: 5 })}
        />
      </Field>
    </Dialog>
  );
}

function AmendModal({
  open,
  parent,
  locale,
  onClose,
  onConfirm,
  loading,
}: {
  open: boolean;
  parent: MissionOrderResponse;
  locale: "fr" | "en";
  onClose: () => void;
  onConfirm: (body: AmendMissionOrderRequest) => void;
  loading: boolean;
}) {
  const t = useTranslations("missions");
  const tNew = useTranslations("missions.new");
  const tCommon = useTranslations("common");
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { isValid },
  } = useForm<AmendMissionOrderRequest>({
    mode: "onChange",
    defaultValues: {
      destination: parent.destination,
      objet: parent.objet,
      dateDebut: parent.dateDebut,
      dateFin: parent.dateFin,
      montantAvance: parent.montantAvance ?? "",
      centreCout: parent.centreCout ?? "",
    },
  });
  React.useEffect(() => {
    if (open) {
      reset({
        destination: parent.destination,
        objet: parent.objet,
        dateDebut: parent.dateDebut,
        dateFin: parent.dateFin,
        montantAvance: parent.montantAvance ?? "",
        centreCout: parent.centreCout ?? "",
      });
    }
  }, [open, parent, reset]);
  const start = watch("dateDebut");
  const end = watch("dateFin");
  const datesInvalid = !!start && !!end && new Date(end as string) < new Date(start as string);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      size="lg"
      title={t("amend.title")}
      subtitle={t("amend.subtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || datesInvalid || loading}
            onClick={handleSubmit((v) =>
              onConfirm({
                destination: v.destination,
                objet: v.objet,
                dateDebut: v.dateDebut,
                dateFin: v.dateFin,
                montantAvance:
                  v.montantAvance === "" || v.montantAvance == null
                    ? null
                    : Number(v.montantAvance),
                centreCout: v.centreCout || null,
              }),
            )}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : t("amend.submit")}
          </Button>
        </>
      }
    >
      {parent.decisionReason && (
        <div className="mb-4 rounded-[12px] border border-danger-200 bg-danger-50 p-3">
          <Label className="text-[11px] uppercase tracking-wider text-danger-600">
            {t("amend.parentReason")}
          </Label>
          <p className="mt-1 text-[13px] text-danger-700">{parent.decisionReason}</p>
        </div>
      )}
      <div className="grid grid-cols-2 gap-3">
        <Field label={tNew("fields.destination")}>
          <Input {...register("destination", { required: true })} />
        </Field>
        <Field label={tNew("fields.centreCout")}>
          <Input {...register("centreCout")} />
        </Field>
        <Field label={tNew("fields.objet")} className="col-span-2">
          <Textarea rows={2} {...register("objet", { required: true })} />
        </Field>
        <Field label={tNew("fields.dateDebut")}>
          <Input type="date" {...register("dateDebut", { required: true })} />
        </Field>
        <Field label={tNew("fields.dateFin")}>
          <Input type="date" {...register("dateFin", { required: true })} />
        </Field>
        <Field label={tNew("fields.montantAvance")} className="col-span-2">
          <Input type="number" step={1000} min={0} {...register("montantAvance")} />
        </Field>
      </div>
      <p className="mt-3 text-[10.5px] text-ink-4 font-mono-tabular">
        {formatDate(parent.dateDebut, { locale })} → {formatDate(parent.dateFin, { locale })}
      </p>
    </Dialog>
  );
}
