"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  CheckCircle2,
  ChevronLeft,
  FileText,
  Loader2,
  Send,
  ShieldCheck,
  Upload,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Label } from "@/components/ui/input";
import { WorkflowStepper, type WorkflowStep } from "@/components/ui/workflow-stepper";
import { useCan } from "@/hooks/use-can";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { declarationStatusTone, declarationTypeTone } from "@/lib/declaration-status";
import { formatDateTime } from "@/lib/format";
import type { StoredFileResponse } from "@/server/ksm/modules/files";
import type {
  DeclarationStatus,
  SocialDeclarationResponse,
} from "@/server/ksm/modules/declarations";

export function DeclarationDetail({ declarationId }: { declarationId: string }) {
  const t = useTranslations("declarations");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const canManage = useCan("hrm:declaration:manage");
  const [showGenerate, setShowGenerate] = React.useState(false);

  const query = useQuery({
    queryKey: ["hrm", "declaration", declarationId],
    queryFn: () =>
      apiFetch<SocialDeclarationResponse>(`/api/hrm/declarations/${declarationId}`),
  });

  const invalidate = React.useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["hrm", "declaration", declarationId] });
    queryClient.invalidateQueries({ queryKey: ["hrm", "declarations"] });
  }, [queryClient, declarationId]);

  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }
  const action = (path: string) =>
    apiFetch(`/api/hrm/declarations/${declarationId}/${path}`, { method: "POST" });

  const submitM = useMutation({
    mutationFn: () => action("submit"),
    onSuccess: () => {
      toast.success(t("detail.submitSuccess"));
      invalidate();
    },
    onError: handleError,
  });
  const ackM = useMutation({
    mutationFn: () => action("acknowledge"),
    onSuccess: () => {
      toast.success(t("detail.acknowledgeSuccess"));
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

  const d = query.data;
  const reference = `DS-${d.id.slice(0, 4).toUpperCase()}-${d.id.slice(4, 8).toUpperCase()}`;
  const showGenerateBtn = canManage && d.statut === "DRAFT";
  const showSubmitBtn = canManage && d.statut === "GENERATED";
  const showAckBtn = canManage && d.statut === "SUBMITTED";

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/declarations" },
          { label: reference },
        ]}
        title={
          <span className="flex items-center gap-3">
            <ShieldCheck className="h-6 w-6 text-orange-500" />
            {t(`type.${d.type}`)} · {d.periode}
          </span>
        }
        subtitle={
          <span className="flex flex-wrap items-center gap-3">
            <Badge tone={declarationStatusTone(d.statut)}>{t(`status.${d.statut}`)}</Badge>
            <Badge tone={declarationTypeTone(d.type)}>{t(`typeName.${d.type}`)}</Badge>
            <span className="font-mono-tabular text-[12px] text-ink-3">{reference}</span>
          </span>
        }
        actions={
          <>
            <Link href="/declarations">
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("detail.back")}
              </Button>
            </Link>
            {showAckBtn && (
              <Button type="button" onClick={() => ackM.mutate()} disabled={ackM.isPending}>
                {ackM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <CheckCircle2 className="h-4 w-4" />}
                {t("detail.actions.acknowledge")}
              </Button>
            )}
            {showSubmitBtn && (
              <Button type="button" onClick={() => submitM.mutate()} disabled={submitM.isPending}>
                {submitM.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send className="h-4 w-4" />}
                {t("detail.actions.submit")}
              </Button>
            )}
            {showGenerateBtn && (
              <Button type="button" onClick={() => setShowGenerate(true)}>
                <Upload className="h-4 w-4" />
                {t("detail.actions.generate")}
              </Button>
            )}
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <WorkflowStepper steps={workflowSteps(d.statut, t)} />
        </CardContent>
      </Card>

      <Card>
        <CardContent padding="lg">
          <dl className="grid grid-cols-2 gap-x-8 gap-y-4 md:grid-cols-4">
            <Detail label={t("detail.type")} value={t(`type.${d.type}`)} />
            <Detail label={t("detail.periode")} value={d.periode} mono />
            <Detail label={t("detail.format")} value={d.format} mono />
            <Detail
              label={t("detail.fichier")}
              value={
                d.fichierId ? (
                  <a
                    href={`/api/files/${d.fichierId}`}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-1.5 text-orange-600 hover:text-orange-700"
                  >
                    <FileText className="h-3 w-3" />
                    {t("detail.viewFile")}
                  </a>
                ) : (
                  <span className="text-ink-4">{t("detail.fichierEmpty")}</span>
                )
              }
            />
            {d.generatedAt && (
              <Detail
                label={t("detail.generatedAt")}
                value={formatDateTime(d.generatedAt, locale)}
                mono
              />
            )}
            {d.submittedAt && (
              <Detail
                label={t("detail.submittedAt")}
                value={formatDateTime(d.submittedAt, locale)}
                mono
              />
            )}
          </dl>
          <p className="mt-6 text-[12px] text-ink-4">
            ID · <span className="font-mono-tabular">{d.id}</span>
          </p>
        </CardContent>
      </Card>

      <GenerateDialog
        open={showGenerate}
        format={d.format}
        declarationId={declarationId}
        onClose={() => setShowGenerate(false)}
        onSuccess={() => {
          setShowGenerate(false);
          toast.success(t("detail.generateSuccess"));
          invalidate();
        }}
        cancelLabel={tCommon("actions.cancel")}
      />
    </>
  );
}

function Detail({ label, value, mono }: { label: string; value: React.ReactNode; mono?: boolean }) {
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
  status: DeclarationStatus,
  t: (k: string) => string,
): WorkflowStep[] {
  const order: DeclarationStatus[] = ["DRAFT", "GENERATED", "SUBMITTED", "ACKNOWLEDGED"];
  const idx = order.indexOf(status);
  const mk = (k: string, l: string, p: number): WorkflowStep => ({
    key: k,
    label: l,
    state: p < idx ? "done" : p === idx ? "active" : "pending",
  });
  return [
    mk("draft", t("detail.workflow.draft"), 0),
    mk("generated", t("detail.workflow.generated"), 1),
    mk("submitted", t("detail.workflow.submitted"), 2),
    mk("acknowledged", t("detail.workflow.acknowledged"), 3),
  ];
}

function GenerateDialog({
  open,
  format,
  declarationId,
  onClose,
  onSuccess,
  cancelLabel,
}: {
  open: boolean;
  format: string;
  declarationId: string;
  onClose: () => void;
  onSuccess: () => void;
  cancelLabel: string;
}) {
  const t = useTranslations("declarations.generate");
  const tErrors = useTranslations("errors");
  const [fileId, setFileId] = React.useState("");
  const [fileName, setFileName] = React.useState("");
  const [uploading, setUploading] = React.useState(false);
  const [generating, setGenerating] = React.useState(false);

  React.useEffect(() => {
    if (!open) {
      setFileId("");
      setFileName("");
      setUploading(false);
      setGenerating(false);
    }
  }, [open]);

  async function upload(file: File) {
    setUploading(true);
    try {
      const fd = new FormData();
      fd.append("file", file, file.name);
      const res = await fetch("/api/files/upload", { method: "POST", body: fd });
      const json = (await res.json()) as { ok: boolean; data?: StoredFileResponse; message?: string };
      if (!res.ok || !json.ok || !json.data) throw new Error(json.message ?? "upload failed");
      setFileId(json.data.id);
      setFileName(file.name);
    } catch (cause) {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    } finally {
      setUploading(false);
    }
  }

  async function confirm() {
    if (!fileId) return;
    setGenerating(true);
    try {
      const res = await fetch(`/api/hrm/declarations/${declarationId}/generate`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fichierId: fileId }),
      });
      const json = (await res.json()) as { ok: boolean; message?: string };
      if (!res.ok || !json.ok) throw new Error(json.message ?? "generate failed");
      onSuccess();
    } catch (cause) {
      if (cause instanceof Error) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    } finally {
      setGenerating(false);
    }
  }

  const acceptMap: Record<string, string> = {
    PDF: "application/pdf",
    CSV: "text/csv,application/vnd.ms-excel",
    XML: "application/xml,text/xml",
  };
  const accept = acceptMap[format] ?? "*";

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={t("title")}
      subtitle={t("subtitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose} disabled={generating}>
            {cancelLabel}
          </Button>
          <Button type="button" disabled={!fileId || generating} onClick={confirm}>
            {generating ? <Loader2 className="h-4 w-4 animate-spin" /> : <Upload className="h-4 w-4" />}
            {t("confirm")}
          </Button>
        </>
      }
    >
      <Field label={format}>
        <label className="inline-flex w-full cursor-pointer items-center gap-2 rounded-[11px] border border-dashed border-line bg-bg-soft px-4 py-4 text-[13px] text-ink-2 hover:border-orange-300">
          {uploading ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : fileId ? (
            <CheckCircle2 className="h-4 w-4 text-success-500" />
          ) : (
            <Upload className="h-4 w-4" />
          )}
          <span className="flex-1">
            {uploading
              ? t("uploading")
              : fileId
                ? `${t("uploaded")} · ${fileName}`
                : t("uploadCta")}
          </span>
          <input
            type="file"
            accept={accept}
            className="hidden"
            onChange={(e) => {
              const f = e.target.files?.[0];
              if (f) upload(f);
            }}
          />
        </label>
      </Field>
    </Dialog>
  );
}
