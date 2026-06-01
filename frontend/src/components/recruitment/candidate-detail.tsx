"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Calendar,
  CheckCircle2,
  ChevronLeft,
  FileText,
  Loader2,
  Mail,
  Phone,
  Plus,
  Send,
  Star,
  X,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Label, Textarea } from "@/components/ui/input";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { useCan } from "@/hooks/use-can";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDateTime } from "@/lib/format";
import { applicationStatusTone, interviewResultTone } from "@/lib/recruitment-status";
import type {
  ApplicationResponse,
  ApplicationStatus,
  ConvertApplicationRequest,
  ConvertedEmployeeResponse,
  InterviewResponse,
  InterviewResult,
  InterviewType,
  JobOfferResponse,
} from "@/server/ksm/modules/recruitment";

export function CandidateDetail({ applicationId }: { applicationId: string }) {
  const t = useTranslations("recruitment");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const canManage = useCan("hrm:recruitment:manage");
  const canConvert = useCan("hrm:employee:create");
  const [showSchedule, setShowSchedule] = React.useState(false);
  const [showConvert, setShowConvert] = React.useState(false);
  const [completeInterview, setCompleteInterview] = React.useState<InterviewResponse | null>(null);

  const query = useQuery({
    queryKey: ["hrm", "application", applicationId],
    queryFn: () => apiFetch<ApplicationResponse>(`/api/hrm/applications/${applicationId}`),
  });
  const interviewsQuery = useQuery({
    queryKey: ["hrm", "application", applicationId, "interviews"],
    queryFn: () =>
      apiFetch<InterviewResponse[]>(`/api/hrm/applications/${applicationId}/interviews`),
  });
  const offerQuery = useQuery({
    queryKey: ["hrm", "job-offer", query.data?.jobOfferId],
    queryFn: () =>
      apiFetch<JobOfferResponse>(`/api/hrm/job-offers/${query.data?.jobOfferId}`),
    enabled: !!query.data?.jobOfferId,
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "application", applicationId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "job-offers"] });
  }, [queryClient, applicationId]);
  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }
  const action = (to: string) =>
    apiFetch(`/api/hrm/applications/${applicationId}/${to}`, { method: "POST" });

  const transitionM = useMutation({
    mutationFn: (to: "shortlist" | "interview" | "offer" | "hire" | "reject") => action(to),
    onSuccess: () => {
      toast.success(t("candidate.transitionSuccess"));
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

  const app = query.data;
  const offer = offerQuery.data;
  const interviews = interviewsQuery.data ?? [];
  const fullName = `${app.candidatPrenom} ${app.candidatNom}`;

  const canShortlist = canManage && app.status === "NEW";
  const canInterview = canManage && (app.status === "SHORTLISTED" || app.status === "INTERVIEWING");
  const canOffer = canManage && app.status === "INTERVIEWING";
  const canHire = canManage && (app.status === "OFFERED" || app.status === "INTERVIEWING");
  const canReject =
    canManage && app.status !== "REJECTED" && app.status !== "HIRED";

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/recruitment" },
          { label: offer?.poste ?? "…", href: offer ? `/recruitment/offers/${offer.id}` : undefined },
          { label: fullName },
        ]}
        title={
          <span className="flex items-center gap-3">
            <Avatar name={fullName} size="md" />
            {fullName}
          </span>
        }
        subtitle={
          <span className="flex flex-wrap items-center gap-3">
            <Badge tone={applicationStatusTone(app.status)}>
              {t(`applicationStatus.${app.status}`)}
            </Badge>
            {offer && (
              <span className="text-[12.5px] text-ink-3">
                {t("candidate.workflow.new")} · {offer.poste}
              </span>
            )}
          </span>
        }
        actions={
          <>
            <Link href={offer ? `/recruitment/offers/${offer.id}` : "/recruitment"}>
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("candidate.back")}
              </Button>
            </Link>
            {canReject && (
              <Button type="button" variant="ghost" onClick={() => transitionM.mutate("reject")}>
                <X className="h-4 w-4" />
                {t("candidate.actions.reject")}
              </Button>
            )}
            {canShortlist && (
              <Button type="button" variant="secondary" onClick={() => transitionM.mutate("shortlist")}>
                <Star className="h-4 w-4" />
                {t("candidate.actions.shortlist")}
              </Button>
            )}
            {canInterview && (
              <Button type="button" onClick={() => setShowSchedule(true)}>
                <Calendar className="h-4 w-4" />
                {t("candidate.actions.interview")}
              </Button>
            )}
            {canOffer && (
              <Button type="button" onClick={() => transitionM.mutate("offer")}>
                <Send className="h-4 w-4" />
                {t("candidate.actions.offer")}
              </Button>
            )}
            {canHire && app.status === "OFFERED" && canConvert && (
              <Button type="button" onClick={() => setShowConvert(true)}>
                <CheckCircle2 className="h-4 w-4" />
                {t("candidate.actions.convertToEmployee")}
              </Button>
            )}
            {canHire && !(app.status === "OFFERED" && canConvert) && (
              <Button type="button" onClick={() => transitionM.mutate("hire")}>
                <CheckCircle2 className="h-4 w-4" />
                {t("candidate.actions.hire")}
              </Button>
            )}
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <WorkflowStepper steps={workflowSteps(app.status, t)} />
        </CardContent>
      </Card>

      <Card className="mb-6">
        <CardContent padding="lg">
          <dl className="grid grid-cols-2 gap-x-8 gap-y-4 md:grid-cols-4">
            <Detail
              label={t("candidate.email")}
              icon={<Mail className="h-3.5 w-3.5 text-ink-3" />}
              value={app.candidatEmail ?? "—"}
            />
            <Detail
              label={t("candidate.phone")}
              icon={<Phone className="h-3.5 w-3.5 text-ink-3" />}
              value={app.candidatTelephone ?? "—"}
            />
            <Detail
              label={t("candidate.cv")}
              icon={<FileText className="h-3.5 w-3.5 text-ink-3" />}
              value={
                app.cvFileId ? (
                  <a
                    href={`/api/files/${app.cvFileId}`}
                    target="_blank"
                    rel="noreferrer"
                    className="text-orange-600 hover:text-orange-700"
                  >
                    {t("candidate.viewFile")} →
                  </a>
                ) : (
                  t("candidate.noFile")
                )
              }
            />
            <Detail
              label={t("candidate.cover")}
              value={
                app.lettreMotivationFileId ? (
                  <a
                    href={`/api/files/${app.lettreMotivationFileId}`}
                    target="_blank"
                    rel="noreferrer"
                    className="text-orange-600 hover:text-orange-700"
                  >
                    {t("candidate.viewFile")} →
                  </a>
                ) : (
                  t("candidate.noFile")
                )
              }
            />
          </dl>
        </CardContent>
      </Card>

      <div className="mb-3 flex items-center justify-between">
        <span className="text-[13px] font-bold tracking-tight text-ink">
          {t("candidate.interviews.title")}
        </span>
        {canManage && (
          <Button
            type="button"
            variant="secondary"
            className="!h-8 !px-3 !text-[12px]"
            onClick={() => setShowSchedule(true)}
          >
            <Plus className="h-3.5 w-3.5" />
            {t("candidate.interviews.schedule")}
          </Button>
        )}
      </div>
      <Card>
        {interviewsQuery.isLoading ? (
          <div className="grid place-items-center py-10">
            <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
          </div>
        ) : interviews.length === 0 ? (
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">
              {t("candidate.interviews.empty")}
            </p>
          </CardContent>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-bg-dim">
                <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("candidate.interviews.fields.type")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("candidate.interviews.fields.dateHeure")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("candidate.interviews.fields.interviewer")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("candidate.interviews.completeFields.result")}
                </th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody>
              {interviews.map((iv) => (
                <tr key={iv.id} className="border-t border-line-soft">
                  <td className="px-5 py-3">
                    <Badge tone="info">{t(`interviewType.${iv.type}`)}</Badge>
                  </td>
                  <td className="px-3 py-3 font-mono-tabular text-[12.5px] text-ink-2">
                    {formatDateTime(iv.dateHeure, locale)}
                  </td>
                  <td className="px-3 py-3 text-[12.5px] text-ink-2">
                    {iv.interviewerDisplayName ?? "—"}
                  </td>
                  <td className="px-3 py-3">
                    <Badge tone={interviewResultTone(iv.resultat)}>
                      {t(`interviewResult.${iv.resultat}`)}
                    </Badge>
                  </td>
                  <td className="px-5 py-3 text-right">
                    {canManage && iv.resultat === "PENDING" && (
                      <Button
                        type="button"
                        variant="ghost"
                        className="!h-7 !px-2.5 !text-[11px]"
                        onClick={() => setCompleteInterview(iv)}
                      >
                        {t("candidate.interviews.complete")}
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <ScheduleInterviewDialog
        open={showSchedule}
        onClose={() => setShowSchedule(false)}
        applicationId={applicationId}
        onScheduled={() => {
          setShowSchedule(false);
          queryClient.invalidateQueries({ queryKey: ["hrm", "application", applicationId, "interviews"] });
          invalidate();
        }}
      />
      <CompleteInterviewDialog
        interview={completeInterview}
        onClose={() => setCompleteInterview(null)}
        onCompleted={() => {
          setCompleteInterview(null);
          queryClient.invalidateQueries({ queryKey: ["hrm", "application", applicationId, "interviews"] });
        }}
      />
      <ConvertDialog
        open={showConvert}
        applicationId={applicationId}
        candidateName={`${app.candidatPrenom} ${app.candidatNom}`}
        onClose={() => setShowConvert(false)}
        onConverted={() => {
          setShowConvert(false);
          invalidate();
        }}
      />
    </>
  );
}

function Detail({
  label,
  value,
  icon,
}: {
  label: string;
  value: React.ReactNode;
  icon?: React.ReactNode;
}) {
  return (
    <div>
      <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">{label}</Label>
      <p className="mt-0.5 flex items-center gap-1.5 text-[13.5px] font-medium text-ink">
        {icon}
        {value}
      </p>
    </div>
  );
}

function workflowSteps(status: ApplicationStatus, t: (k: string) => string): WorkflowStep[] {
  if (status === "REJECTED") {
    return [
      { key: "new", label: t("candidate.workflow.new"), state: "done" },
      { key: "rejected", label: t("applicationStatus.REJECTED"), state: "active" },
    ];
  }
  const order: ApplicationStatus[] = ["NEW", "SHORTLISTED", "INTERVIEWING", "OFFERED", "HIRED"];
  const idx = order.indexOf(status);
  const mk = (k: string, l: string, p: number): WorkflowStep => ({
    key: k,
    label: l,
    state: p < idx ? "done" : p === idx ? "active" : "pending",
  });
  return [
    mk("new", t("candidate.workflow.new"), 0),
    mk("shortlisted", t("candidate.workflow.shortlisted"), 1),
    mk("interviewing", t("candidate.workflow.interviewing"), 2),
    mk("decision", t("candidate.workflow.decision"), 3),
    mk("hired", t("candidate.workflow.hired"), 4),
  ];
}

function ScheduleInterviewDialog({
  open,
  onClose,
  applicationId,
  onScheduled,
}: {
  open: boolean;
  onClose: () => void;
  applicationId: string;
  onScheduled: () => void;
}) {
  const t = useTranslations("recruitment.candidate.interviews");
  const tType = useTranslations("recruitment.interviewType");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");

  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{
    type: InterviewType;
    dateHeure: string;
    lieu: string;
    interviewerDisplayName: string;
  }>({
    mode: "onChange",
    defaultValues: {
      type: "RH",
      dateHeure: new Date().toISOString().slice(0, 16),
      lieu: "",
      interviewerDisplayName: "",
    },
  });

  React.useEffect(() => {
    if (!open) {
      reset({
        type: "RH",
        dateHeure: new Date().toISOString().slice(0, 16),
        lieu: "",
        interviewerDisplayName: "",
      });
    }
  }, [open, reset]);

  const create = useMutation({
    mutationFn: (v: {
      type: InterviewType;
      dateHeure: string;
      lieu: string;
      interviewerDisplayName: string;
    }) =>
      apiFetch("/api/hrm/interviews", {
        method: "POST",
        body: {
          applicationId,
          type: v.type,
          dateHeure: new Date(v.dateHeure).toISOString(),
          lieu: v.lieu.trim() || null,
          interviewerDisplayName: v.interviewerDisplayName.trim() || null,
        },
      }),
    onSuccess: () => {
      toast.success(t("scheduleSuccess"));
      onScheduled();
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={t("schedule")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || create.isPending}
            onClick={handleSubmit((v) => create.mutate(v))}
          >
            {create.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Calendar className="h-4 w-4" />}
            {t("scheduleConfirm")}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-2 gap-3">
        <Field label={t("fields.type")}>
          <select
            {...register("type", { required: true })}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            <option value="RH">{tType("RH")}</option>
            <option value="TECHNIQUE">{tType("TECHNIQUE")}</option>
            <option value="FINAL">{tType("FINAL")}</option>
          </select>
        </Field>
        <Field label={t("fields.dateHeure")}>
          <Input type="datetime-local" {...register("dateHeure", { required: true })} />
        </Field>
        <Field label={t("fields.lieu")} className="col-span-2">
          <Input {...register("lieu")} />
        </Field>
        <Field label={t("fields.interviewer")} className="col-span-2">
          <Input {...register("interviewerDisplayName")} />
        </Field>
      </div>
    </Dialog>
  );
}

function CompleteInterviewDialog({
  interview,
  onClose,
  onCompleted,
}: {
  interview: InterviewResponse | null;
  onClose: () => void;
  onCompleted: () => void;
}) {
  const t = useTranslations("recruitment.candidate.interviews");
  const tResult = useTranslations("recruitment.interviewResult");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");

  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{ notes: string; resultat: InterviewResult }>({
    mode: "onChange",
    defaultValues: { notes: "", resultat: "PASS" },
  });

  React.useEffect(() => {
    if (!interview) reset({ notes: "", resultat: "PASS" });
  }, [interview, reset]);

  const complete = useMutation({
    mutationFn: (v: { notes: string; resultat: InterviewResult }) =>
      apiFetch(`/api/hrm/interviews/${interview!.id}/complete`, {
        method: "POST",
        body: { notes: v.notes.trim(), resultat: v.resultat },
      }),
    onSuccess: () => {
      toast.success(t("completeSuccess"));
      onCompleted();
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  return (
    <Dialog
      open={!!interview}
      onClose={onClose}
      title={t("complete")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || complete.isPending}
            onClick={handleSubmit((v) => complete.mutate(v))}
          >
            {complete.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <CheckCircle2 className="h-4 w-4" />}
            {t("completeConfirm")}
          </Button>
        </>
      }
    >
      <Field label={t("completeFields.result")}>
        <select
          {...register("resultat", { required: true })}
          className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
        >
          <option value="PASS">{tResult("PASS")}</option>
          <option value="FAIL">{tResult("FAIL")}</option>
        </select>
      </Field>
      <Field label={t("completeFields.notes")}>
        <Textarea rows={4} {...register("notes", { required: true, minLength: 5 })} />
      </Field>
    </Dialog>
  );
}

function ConvertDialog({
  open,
  applicationId,
  candidateName,
  onClose,
  onConverted,
}: {
  open: boolean;
  applicationId: string;
  candidateName: string;
  onClose: () => void;
  onConverted: () => void;
}) {
  const t = useTranslations("recruitment.candidate.convert");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const today = new Date().toISOString().slice(0, 10);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isValid },
  } = useForm<{
    categorie: string;
    echelon: string;
    departmentCode: string;
    dateEmbauche: string;
    contractType: ConvertApplicationRequest["contractType"];
    contractDateDebut: string;
    contractDateFin: string;
    salaireBase: string;
    avantagesNature: string;
    periodeEssai: string;
    modePaiement: ConvertApplicationRequest["modePaiement"];
    compteBancaire: string;
    numCnps: string;
  }>({
    mode: "onChange",
    defaultValues: {
      categorie: "5",
      echelon: "A",
      departmentCode: "",
      dateEmbauche: today,
      contractType: "CDI",
      contractDateDebut: today,
      contractDateFin: "",
      salaireBase: "",
      avantagesNature: "",
      periodeEssai: "90",
      modePaiement: "BANK_TRANSFER",
      compteBancaire: "",
      numCnps: "",
    },
  });

  React.useEffect(() => {
    if (!open) {
      reset();
    }
  }, [open, reset]);

  const contractType = watch("contractType");
  const showCdEnd = contractType === "CDD" || contractType === "STAGE" || contractType === "INTERIM";

  const convert = useMutation({
    mutationFn: (v: Parameters<typeof handleSubmit>[0] extends (vals: infer V) => unknown ? V : never) => {
      const body: ConvertApplicationRequest = {
        categorie: Number(v.categorie),
        echelon: v.echelon.trim() || null,
        departmentCode: v.departmentCode.trim() || null,
        dateEmbauche: v.dateEmbauche,
        contractType: v.contractType,
        contractDateDebut: v.contractDateDebut,
        contractDateFin: showCdEnd && v.contractDateFin ? v.contractDateFin : null,
        salaireBase: Number(v.salaireBase),
        avantagesNature: v.avantagesNature ? Number(v.avantagesNature) : null,
        periodeEssai: v.periodeEssai ? Number(v.periodeEssai) : null,
        modePaiement: v.modePaiement,
        compteBancaire: v.compteBancaire.trim() || null,
        numCnps: v.numCnps.trim() || null,
      };
      return apiFetch<ConvertedEmployeeResponse>(
        `/api/hrm/applications/${applicationId}/convert-to-employee`,
        { method: "POST", body },
      );
    },
    onSuccess: (emp) => {
      toast.success(t("success", { matricule: emp.matricule }));
      onConverted();
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  return (
    <Dialog
      open={open}
      onClose={onClose}
      size="lg"
      title={t("title", { name: candidateName })}
      subtitle={t("subtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || convert.isPending}
            onClick={handleSubmit((v) => convert.mutate(v))}
          >
            {convert.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <CheckCircle2 className="h-4 w-4" />}
            {t("confirm")}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-2 gap-3">
        <Field label={t("fields.categorie")} error={errors.categorie && tCommon("actions.create")}>
          <Input type="number" min={1} max={12} step={1} {...register("categorie", { required: true })} />
        </Field>
        <Field label={t("fields.echelon")}>
          <Input {...register("echelon")} />
        </Field>
        <Field label={t("fields.departmentCode")}>
          <Input placeholder="IT, COMMERCIAL, …" {...register("departmentCode")} />
        </Field>
        <Field label={t("fields.dateEmbauche")}>
          <Input type="date" {...register("dateEmbauche", { required: true })} />
        </Field>
        <Field label={t("fields.contractType")}>
          <select
            {...register("contractType", { required: true })}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            <option value="CDI">CDI</option>
            <option value="CDD">CDD</option>
            <option value="STAGE">Stage</option>
            <option value="INTERIM">Intérim</option>
          </select>
        </Field>
        <Field label={t("fields.contractDateDebut")}>
          <Input type="date" {...register("contractDateDebut", { required: true })} />
        </Field>
        {showCdEnd && (
          <Field label={t("fields.contractDateFin")} className="col-span-2">
            <Input type="date" {...register("contractDateFin")} />
          </Field>
        )}
        <Field label={t("fields.salaireBase")}>
          <Input type="number" min={0} step={1000} {...register("salaireBase", { required: true, min: 1 })} />
        </Field>
        <Field label={t("fields.avantagesNature")}>
          <Input type="number" min={0} step={1000} {...register("avantagesNature")} />
        </Field>
        <Field label={t("fields.periodeEssai")}>
          <Input type="number" min={0} max={365} step={1} {...register("periodeEssai")} />
        </Field>
        <Field label={t("fields.modePaiement")}>
          <select
            {...register("modePaiement", { required: true })}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            <option value="BANK_TRANSFER">Virement bancaire</option>
            <option value="MTN_MOBILE_MONEY">MTN MoMo</option>
            <option value="ORANGE_MONEY">Orange Money</option>
            <option value="CASH">Espèces</option>
          </select>
        </Field>
        <Field label={t("fields.compteBancaire")} className="col-span-2">
          <Input {...register("compteBancaire")} />
        </Field>
        <Field label={t("fields.numCnps")} className="col-span-2">
          <Input {...register("numCnps")} />
        </Field>
      </div>
    </Dialog>
  );
}
