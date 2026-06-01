"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { CheckCircle2, Loader2, Plus, Upload } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input } from "@/components/ui/input";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type { StoredFileResponse } from "@/server/ksm/modules/files";
import type {
  CreateMedicalCertificateRequest,
  MedicalCertificateResponse,
} from "@/server/ksm/modules/medical";

type FormValues = {
  employeeId: string;
  typeCertificat: string;
  dateEmission: string;
  dateExpiration: string;
  statut: "VALIDE" | "EXPIRE" | "REVOQUE";
};

export function NewCertificateForm() {
  const t = useTranslations("medical.certificates.new");
  const tStatut = useTranslations("medical.certificates.new.statutValues");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const tVal = useTranslations("validation");
  const router = useRouter();
  const today = new Date().toISOString().slice(0, 10);
  const nextYear = new Date(new Date().setFullYear(new Date().getFullYear() + 1))
    .toISOString()
    .slice(0, 10);

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: {
      employeeId: "",
      typeCertificat: "",
      dateEmission: today,
      dateExpiration: nextYear,
      statut: "VALIDE",
    },
  });

  const [fileId, setFileId] = React.useState<string>("");
  const [uploading, setUploading] = React.useState(false);

  async function upload(file: File) {
    setUploading(true);
    try {
      const fd = new FormData();
      fd.append("file", file, file.name);
      const res = await fetch("/api/files/upload", { method: "POST", body: fd });
      const json = (await res.json()) as { ok: boolean; data?: StoredFileResponse; message?: string };
      if (!res.ok || !json.ok || !json.data) throw new Error(json.message ?? "upload failed");
      setFileId(json.data.id);
    } catch (cause) {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    } finally {
      setUploading(false);
    }
  }

  const mutation = useMutation({
    mutationFn: (v: FormValues) => {
      const body: CreateMedicalCertificateRequest = {
        employeeId: v.employeeId,
        typeCertificat: v.typeCertificat.trim(),
        dateEmission: v.dateEmission,
        dateExpiration: v.dateExpiration,
        statut: v.statut,
        fichierId: fileId || null,
      };
      return apiFetch<MedicalCertificateResponse>("/api/hrm/medical/certificates", {
        method: "POST",
        body,
      });
    },
    onSuccess: (cert) => {
      toast.success(t("success"));
      router.push(`/medical/certificates/${cert.id}`);
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  return (
    <form onSubmit={handleSubmit((v) => mutation.mutate(v))}>
      <PageHeader
        ucBadge={useTranslations("medical")("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: useTranslations("medical")("title"), href: "/medical" },
          { label: t("title") },
        ]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <>
            <Link href="/medical">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
            <Button type="submit" disabled={!isValid || uploading || mutation.isPending}>
              {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
              {t("submit")}
            </Button>
          </>
        }
      />

      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field
              label={t("fields.employee")}
              error={errors.employeeId && tVal("required")}
              className="col-span-2"
            >
              <select
                {...register("employeeId", { required: true })}
                className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
              >
                <option value="">—</option>
                {(employeesQuery.data ?? []).map((e) => (
                  <option key={e.id} value={e.id}>
                    {e.matricule} · {e.actorDisplayName ?? e.actorId.slice(0, 8)}
                  </option>
                ))}
              </select>
            </Field>
            <Field label={t("fields.type")} className="col-span-2">
              <Input
                placeholder={t("fields.typePlaceholder")}
                {...register("typeCertificat", { required: true, minLength: 3 })}
              />
            </Field>
            <Field label={t("fields.dateEmission")}>
              <Input type="date" {...register("dateEmission", { required: true })} />
            </Field>
            <Field label={t("fields.dateExpiration")}>
              <Input type="date" {...register("dateExpiration", { required: true })} />
            </Field>
            <Field label={t("fields.statut")}>
              <select
                {...register("statut", { required: true })}
                className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
              >
                <option value="VALIDE">{tStatut("VALIDE")}</option>
                <option value="EXPIRE">{tStatut("EXPIRE")}</option>
                <option value="REVOQUE">{tStatut("REVOQUE")}</option>
              </select>
            </Field>
            <Field label={t("fields.fichier")}>
              <label className="inline-flex cursor-pointer items-center gap-2 rounded-[11px] border border-dashed border-line bg-bg-soft px-3.5 py-[11px] text-[12.5px] text-ink-2 hover:border-orange-300">
                {uploading ? (
                  <Loader2 className="h-3.5 w-3.5 animate-spin" />
                ) : fileId ? (
                  <CheckCircle2 className="h-3.5 w-3.5 text-success-500" />
                ) : (
                  <Upload className="h-3.5 w-3.5" />
                )}
                {fileId ? "✓ PDF" : t("fields.fichier")}
                <input
                  type="file"
                  accept="application/pdf,image/*"
                  className="hidden"
                  onChange={(e) => {
                    const f = e.target.files?.[0];
                    if (f) upload(f);
                  }}
                />
              </label>
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}
