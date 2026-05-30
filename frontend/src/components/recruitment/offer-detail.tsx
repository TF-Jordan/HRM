"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Briefcase,
  CheckCircle2,
  ChevronLeft,
  FileText,
  Loader2,
  Plus,
  Send,
  Upload,
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
import { Field, Input, Label } from "@/components/ui/input";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { useCan } from "@/hooks/use-can";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { applicationStatusTone, jobOfferStatusTone } from "@/lib/recruitment-status";
import type { StoredFileResponse } from "@/server/ksm/modules/files";
import type {
  ApplicationResponse,
  JobOfferResponse,
  JobOfferStatus,
} from "@/server/ksm/modules/recruitment";

export function OfferDetail({ offerId }: { offerId: string }) {
  const t = useTranslations("recruitment");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const router = useRouter();
  const canManage = useCan("hrm:recruitment:manage");
  const canCreate = useCan("hrm:recruitment:create");
  const [showAdd, setShowAdd] = React.useState(false);

  const offerQuery = useQuery({
    queryKey: ["hrm", "job-offer", offerId],
    queryFn: () => apiFetch<JobOfferResponse>(`/api/hrm/job-offers/${offerId}`),
  });
  const appsQuery = useQuery({
    queryKey: ["hrm", "job-offer", offerId, "applications"],
    queryFn: () =>
      apiFetch<ApplicationResponse[]>(`/api/hrm/job-offers/${offerId}/applications`),
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "job-offer", offerId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "job-offers"] });
  }, [queryClient, offerId]);
  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }
  const action = (path: string) =>
    apiFetch(`/api/hrm/job-offers/${offerId}/${path}`, { method: "POST" });

  const publishM = useMutation({
    mutationFn: () => action("publish"),
    onSuccess: () => {
      toast.success(t("detail.publishSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const closeM = useMutation({
    mutationFn: () => action("close"),
    onSuccess: () => {
      toast.success(t("detail.closeSuccess"));
      invalidate();
    },
    onError: handleError,
  });

  if (offerQuery.isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (offerQuery.error || !offerQuery.data) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {offerQuery.error instanceof BffApiError ? offerQuery.error.message : "—"}
      </div>
    );
  }

  const offer = offerQuery.data;
  const apps = appsQuery.data ?? [];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/recruitment" },
          { label: shortRef(offer.id) },
        ]}
        title={
          <span className="flex items-center gap-3">
            <Briefcase className="h-6 w-6 text-orange-500" />
            {offer.poste}
          </span>
        }
        subtitle={
          <span className="flex flex-wrap items-center gap-3">
            <Badge tone={jobOfferStatusTone(offer.status)}>{t(`jobOfferStatus.${offer.status}`)}</Badge>
            <span className="font-mono-tabular text-[12px] text-ink-3">{shortRef(offer.id)}</span>
            {offer.dateLimite && (
              <span className="text-[12.5px] text-ink-3">
                {t("detail.deadline")} · {formatDate(offer.dateLimite, { locale })}
              </span>
            )}
          </span>
        }
        actions={
          <>
            <Link href="/recruitment">
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("detail.back")}
              </Button>
            </Link>
            {canCreate && offer.status !== "CLOSED" && (
              <Button type="button" variant="secondary" onClick={() => setShowAdd(true)}>
                <Plus className="h-4 w-4" />
                {t("detail.addCandidate")}
              </Button>
            )}
            {canManage && offer.status === "DRAFT" && (
              <Button type="button" onClick={() => publishM.mutate()} disabled={publishM.isPending}>
                {publishM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
                {t("detail.publish")}
              </Button>
            )}
            {canManage && offer.status === "PUBLISHED" && (
              <Button type="button" onClick={() => closeM.mutate()} disabled={closeM.isPending}>
                {closeM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <CheckCircle2 className="h-4 w-4" />}
                {t("detail.close")}
              </Button>
            )}
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <WorkflowStepper steps={workflowSteps(offer.status, t)} />
        </CardContent>
      </Card>

      <Card className="mb-6">
        <CardContent padding="lg">
          <dl className="grid grid-cols-2 gap-x-8 gap-y-4 md:grid-cols-4">
            <Detail label={t("detail.departement")} value={offer.departement ?? "—"} />
            <Detail label={t("detail.site")} value={offer.localisation ?? "—"} />
            <Detail label={t("detail.package")} value={offer.packageSalarial ?? "—"} mono />
            <Detail
              label={t("detail.deadline")}
              value={offer.dateLimite ? formatDate(offer.dateLimite, { locale }) : "—"}
              mono
            />
          </dl>
          {offer.competencesRequises && (
            <div className="mt-6">
              <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                {t("detail.competences")}
              </Label>
              <p className="mt-1 whitespace-pre-line text-[13.5px] text-ink-2">
                {offer.competencesRequises}
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="mb-3 text-[13px] font-bold tracking-tight text-ink">{t("detail.applications")}</div>
      <Card>
        {appsQuery.isLoading ? (
          <div className="grid place-items-center py-10">
            <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
          </div>
        ) : apps.length === 0 ? (
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("offers.empty")}</p>
          </CardContent>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-bg-dim">
                <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("offers.columns.poste")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("candidate.email")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("candidate.cv")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("offers.columns.status")}
                </th>
              </tr>
            </thead>
            <tbody>
              {apps.map((a) => (
                <tr
                  key={a.id}
                  className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                  onClick={() => router.push(`/recruitment/candidates/${a.id}`)}
                >
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-2.5">
                      <Avatar name={`${a.candidatPrenom} ${a.candidatNom}`} size="sm" />
                      <span className="text-[13.5px] font-semibold text-ink">
                        {a.candidatPrenom} {a.candidatNom}
                      </span>
                    </div>
                  </td>
                  <td className="px-3 py-3 font-mono-tabular text-[12px] text-ink-2">
                    {a.candidatEmail ?? "—"}
                  </td>
                  <td className="px-3 py-3">
                    {a.cvFileId ? (
                      <a
                        href={`/api/files/${a.cvFileId}`}
                        target="_blank"
                        rel="noreferrer"
                        onClick={(e) => e.stopPropagation()}
                        className="inline-flex"
                      >
                        <Badge tone="success">
                          <FileText className="h-3 w-3" />
                          {t("candidate.viewFile")}
                        </Badge>
                      </a>
                    ) : (
                      <Badge tone="gray">{t("candidate.noFile")}</Badge>
                    )}
                  </td>
                  <td className="px-3 py-3">
                    <Badge tone={applicationStatusTone(a.status)}>
                      {t(`applicationStatus.${a.status}`)}
                    </Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <AddCandidateDialog
        open={showAdd}
        onClose={() => setShowAdd(false)}
        offerId={offer.id}
        onCreated={() => {
          setShowAdd(false);
          queryClient.invalidateQueries({ queryKey: ["hrm", "job-offer", offerId, "applications"] });
          queryClient.invalidateQueries({ queryKey: ["hrm", "job-offers", "applications"] });
        }}
      />
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

function workflowSteps(status: JobOfferStatus, t: (k: string) => string): WorkflowStep[] {
  const order: JobOfferStatus[] = ["DRAFT", "PUBLISHED", "CLOSED"];
  const idx = order.indexOf(status);
  const mk = (k: string, l: string, p: number): WorkflowStep => ({
    key: k,
    label: l,
    state: p < idx ? "done" : p === idx ? "active" : "pending",
  });
  return [
    mk("draft", t("detail.workflow.draft"), 0),
    mk("published", t("detail.workflow.published"), 1),
    mk("closed", t("detail.workflow.closed"), 2),
  ];
}

function shortRef(uuid: string): string {
  return `JO-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}

function AddCandidateDialog({
  open,
  onClose,
  offerId,
  onCreated,
}: {
  open: boolean;
  onClose: () => void;
  offerId: string;
  onCreated: () => void;
}) {
  const t = useTranslations("recruitment.candidate.add");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");

  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{
    nom: string;
    prenom: string;
    candidatEmail: string;
    candidatTelephone: string;
  }>({ mode: "onChange" });
  const [cvFileId, setCvFileId] = React.useState<string>("");
  const [coverFileId, setCoverFileId] = React.useState<string>("");
  const [uploading, setUploading] = React.useState<"cv" | "cover" | null>(null);

  React.useEffect(() => {
    if (!open) {
      reset({ nom: "", prenom: "", candidatEmail: "", candidatTelephone: "" });
      setCvFileId("");
      setCoverFileId("");
      setUploading(null);
    }
  }, [open, reset]);

  async function upload(kind: "cv" | "cover", file: File) {
    setUploading(kind);
    try {
      const fd = new FormData();
      fd.append("file", file, file.name);
      const res = await fetch("/api/files/upload", { method: "POST", body: fd });
      const json = (await res.json()) as { ok: boolean; data?: StoredFileResponse; message?: string };
      if (!res.ok || !json.ok || !json.data) throw new Error(json.message ?? "upload failed");
      if (kind === "cv") setCvFileId(json.data.id);
      else setCoverFileId(json.data.id);
    } catch (cause) {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    } finally {
      setUploading(null);
    }
  }

  const create = useMutation({
    mutationFn: (v: { nom: string; prenom: string; candidatEmail: string; candidatTelephone: string }) =>
      apiFetch<ApplicationResponse>("/api/hrm/applications", {
        method: "POST",
        body: {
          jobOfferId: offerId,
          candidatNom: v.nom.trim(),
          candidatPrenom: v.prenom.trim(),
          candidatEmail: v.candidatEmail.trim() || null,
          candidatTelephone: v.candidatTelephone.trim() || null,
          cvFileId: cvFileId || null,
          lettreMotivationFileId: coverFileId || null,
        },
      }),
    onSuccess: () => {
      toast.success(t("title"));
      onCreated();
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
      title={t("title")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || create.isPending || !!uploading}
            onClick={handleSubmit((v) => create.mutate(v))}
          >
            {create.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
            {t("submit")}
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-2 gap-3">
        <Field label={t("fields.prenom")}>
          <Input {...register("prenom", { required: true, minLength: 2 })} />
        </Field>
        <Field label={t("fields.nom")}>
          <Input {...register("nom", { required: true, minLength: 2 })} />
        </Field>
        <Field label={t("fields.email")}>
          <Input type="email" {...register("candidatEmail")} />
        </Field>
        <Field label={t("fields.telephone")}>
          <Input {...register("candidatTelephone")} />
        </Field>
        <FileUpload
          label={t("fields.cv")}
          uploaded={!!cvFileId}
          uploading={uploading === "cv"}
          uploadingLabel={t("uploading")}
          onPick={(f) => upload("cv", f)}
        />
        <FileUpload
          label={t("fields.cover")}
          uploaded={!!coverFileId}
          uploading={uploading === "cover"}
          uploadingLabel={t("uploading")}
          onPick={(f) => upload("cover", f)}
        />
      </div>
    </Dialog>
  );
}

function FileUpload({
  label,
  uploaded,
  uploading,
  uploadingLabel,
  onPick,
}: {
  label: string;
  uploaded: boolean;
  uploading: boolean;
  uploadingLabel: string;
  onPick: (f: File) => void;
}) {
  return (
    <Field label={label}>
      <label className="inline-flex cursor-pointer items-center gap-2 rounded-[10px] border border-dashed border-line bg-bg-soft px-3 py-2.5 text-[12.5px] text-ink-2 hover:border-orange-300">
        {uploading ? (
          <Loader2 className="h-3.5 w-3.5 animate-spin" />
        ) : uploaded ? (
          <CheckCircle2 className="h-3.5 w-3.5 text-success-500" />
        ) : (
          <Upload className="h-3.5 w-3.5" />
        )}
        {uploading ? uploadingLabel : uploaded ? "✓" : label}
        <input
          type="file"
          className="hidden"
          onChange={(e) => {
            const f = e.target.files?.[0];
            if (f) onPick(f);
          }}
        />
      </label>
    </Field>
  );
}
