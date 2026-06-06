"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  AlertTriangle,
  CalendarClock,
  CheckCircle2,
  FileText,
  HeartPulse,
  Loader2,
  Plus,
  ShieldCheck,
  Stethoscope,
  Upload,
  X,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input } from "@/components/ui/input";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import {
  alertLevel,
  alertTone,
  aptitudeTone,
  type BadgeTone,
} from "@/lib/medical-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  MedicalCertificateResponse,
  MedicalVisitResponse,
  SubmitMyCertificateRequest,
} from "@/server/ksm/modules/medical";

type MinePayload = {
  employee: EmployeeResponse | null;
  visits: MedicalVisitResponse[];
  certificates: MedicalCertificateResponse[];
};

const CERT_TYPES = ["ARRET_MALADIE", "APTITUDE", "VACCINATION", "AUTRE"] as const;

const KNOWN_STATUTS = new Set(["VALIDE", "EXPIRE", "REVOQUE", "SOUMIS"]);

function statutTone(statut: string): BadgeTone {
  switch (statut.toUpperCase()) {
    case "VALIDE":
      return "success";
    case "SOUMIS":
      return "info";
    case "EXPIRE":
      return "danger";
    case "REVOQUE":
      return "gray";
    default:
      return "gray";
  }
}

export function MyMedical() {
  const t = useTranslations("medical");
  const locale = useLocale() as "fr" | "en";
  const [uploadOpen, setUploadOpen] = React.useState(false);

  const query = useQuery({
    queryKey: ["hrm", "medical", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/medical/mine"),
  });

  const visits = React.useMemo(
    () =>
      [...(query.data?.visits ?? [])].sort((a, b) =>
        b.dateVisite.localeCompare(a.dateVisite),
      ),
    [query.data?.visits],
  );
  const certificates = React.useMemo(
    () =>
      [...(query.data?.certificates ?? [])].sort((a, b) =>
        b.dateEmission.localeCompare(a.dateEmission),
      ),
    [query.data?.certificates],
  );

  const latestVisit = visits[0] ?? null;
  const stats = React.useMemo(() => {
    const overdueVisits = visits.filter(
      (v) => alertLevel(v.prochaineEcheance) === "OVERDUE",
    ).length;
    const overdueCerts = certificates.filter(
      (c) => alertLevel(c.dateExpiration) === "OVERDUE",
    ).length;
    const validCerts = certificates.filter(
      (c) => alertLevel(c.dateExpiration) !== "OVERDUE",
    ).length;
    return {
      nextDue: latestVisit?.prochaineEcheance ?? null,
      validCerts,
      alerts: overdueVisits + overdueCerts,
    };
  }, [visits, certificates, latestVisit]);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("mine.title") }]}
        title={t("mine.title")}
        subtitle={t("mine.subtitle")}
        actions={
          query.data?.employee ? (
            <Button onClick={() => setUploadOpen((v) => !v)}>
              {uploadOpen ? <X className="h-4 w-4" /> : <Upload className="h-4 w-4" />}
              {t("mine.upload.cta")}
            </Button>
          ) : undefined
        }
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !query.data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : !query.data.employee ? (
        <Card>
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("mine.noEmployee")}</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-6">
          <Hero visit={latestVisit} locale={locale} />

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Kpi
              icon={HeartPulse}
              label={t("mine.kpi.aptitude")}
              value={latestVisit ? t(`aptitude.${latestVisit.resultatAptitude}`) : "—"}
              tone={latestVisit ? aptitudeTone(latestVisit.resultatAptitude) : "gray"}
            />
            <Kpi
              icon={CalendarClock}
              label={t("mine.kpi.nextVisit")}
              value={stats.nextDue ? formatDate(stats.nextDue, { locale }) : "—"}
              tone={stats.nextDue ? alertTone(alertLevel(stats.nextDue)) : "gray"}
            />
            <Kpi
              icon={ShieldCheck}
              label={t("mine.kpi.validCerts")}
              value={String(stats.validCerts)}
              tone="success"
            />
            <Kpi
              icon={AlertTriangle}
              label={t("mine.kpi.alerts")}
              value={String(stats.alerts)}
              tone={stats.alerts > 0 ? "danger" : "gray"}
            />
          </div>

          {uploadOpen && (
            <UploadCertificate
              onDone={() => setUploadOpen(false)}
            />
          )}

          <VisitsSection visits={visits} locale={locale} />
          <CertificatesSection certificates={certificates} locale={locale} />

          <p className="flex items-start gap-1.5 text-[12px] text-ink-3">
            <Stethoscope className="mt-0.5 h-3.5 w-3.5 shrink-0 text-orange-500" />
            {t("mine.disclaimer")}
          </p>
        </div>
      )}
    </>
  );
}

/* ----------------------------------------------------------------- Hero */
function Hero({
  visit,
  locale,
}: {
  visit: MedicalVisitResponse | null;
  locale: "fr" | "en";
}) {
  const t = useTranslations("medical");
  if (!visit) {
    return (
      <div className="rounded-[22px] border border-line bg-white p-7 text-center shadow-sm">
        <HeartPulse className="mx-auto h-8 w-8 text-ink-4" />
        <p className="mt-2 text-[14px] font-semibold text-ink">{t("mine.hero.noVisit")}</p>
        <p className="text-[12.5px] text-ink-3">{t("mine.hero.noVisitHint")}</p>
      </div>
    );
  }
  const level = alertLevel(visit.prochaineEcheance);
  return (
    <div className="overflow-hidden rounded-[22px] border border-line bg-gradient-to-br from-ink to-[#23314d] text-white shadow-sm">
      <div className="grid gap-6 p-6 md:grid-cols-[1.3fr_1fr] md:p-7">
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-[12px] font-medium uppercase tracking-wide text-white/60">
            <HeartPulse className="h-4 w-4" />
            {t("mine.hero.currentAptitude")}
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <span className="font-display text-[30px] font-extrabold leading-none">
              {t(`aptitude.${visit.resultatAptitude}`)}
            </span>
            <Badge tone={aptitudeTone(visit.resultatAptitude)}>
              {t(`aptitude.${visit.resultatAptitude}`)}
            </Badge>
          </div>
          {visit.restrictions && (
            <p className="max-w-md rounded-[12px] bg-white/10 px-3 py-2 text-[12.5px] text-white/85">
              <span className="font-semibold">{t("detail.restrictions")} : </span>
              {visit.restrictions}
            </p>
          )}
          <p className="text-[12px] text-white/60">
            {t("visits.columns.date")} · {formatDate(visit.dateVisite, { locale })} · {visit.medecin}
          </p>
        </div>

        <div className="flex flex-col justify-center rounded-[16px] bg-white/5 p-5">
          <div className="flex items-center gap-2 text-[11.5px] uppercase tracking-wide text-white/55">
            <CalendarClock className="h-3.5 w-3.5" />
            {t("mine.hero.nextDue")}
          </div>
          <p className="mt-1 font-mono-tabular text-[22px] font-bold">
            {formatDate(visit.prochaineEcheance, { locale })}
          </p>
          <span className="mt-2 w-fit">
            <Badge tone={alertTone(level)}>{t(`alert.${level}`)}</Badge>
          </span>
        </div>
      </div>
    </div>
  );
}

/* ----------------------------------------------------------------- KPI */
function Kpi({
  icon: Icon,
  label,
  value,
  tone,
}: {
  icon: typeof HeartPulse;
  label: string;
  value: string;
  tone: BadgeTone;
}) {
  const toneText: Record<BadgeTone, string> = {
    success: "text-success-600",
    warning: "text-warning-600",
    danger: "text-danger-600",
    info: "text-info-600",
    orange: "text-orange-600",
    violet: "text-violet-600",
    teal: "text-teal-600",
    gray: "text-ink-3",
  };
  return (
    <Card>
      <CardContent padding="md">
        <div className="flex items-center justify-between">
          <span className="text-[12.5px] font-medium text-ink-3">{label}</span>
          <Icon className={cn("h-4 w-4", toneText[tone])} />
        </div>
        <p className={cn("mt-2 text-[18px] font-bold", toneText[tone])}>{value}</p>
      </CardContent>
    </Card>
  );
}

/* ----------------------------------------------------------------- Visits */
function VisitsSection({
  visits,
  locale,
}: {
  visits: MedicalVisitResponse[];
  locale: "fr" | "en";
}) {
  const t = useTranslations("medical");
  return (
    <section>
      <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold tracking-tight text-ink">
        <Stethoscope className="h-4 w-4 text-orange-500" />
        {t("mine.visitsTitle")}
      </h2>
      {visits.length === 0 ? (
        <Card>
          <CardContent padding="md">
            <p className="text-center text-[13px] text-ink-3">{t("mine.visitsEmpty")}</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {visits.map((v) => {
            const level = alertLevel(v.prochaineEcheance);
            return (
              <Card key={v.id}>
                <CardContent padding="md">
                  <div className="flex flex-wrap items-center gap-3">
                    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-orange-50 text-orange-500">
                      <Stethoscope className="h-4 w-4" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="text-[13.5px] font-semibold text-ink">{v.medecin}</span>
                        <Badge tone={aptitudeTone(v.resultatAptitude)}>
                          {t(`aptitude.${v.resultatAptitude}`)}
                        </Badge>
                      </div>
                      <div className="text-[11.5px] text-ink-3">
                        {formatDate(v.dateVisite, { locale })}
                      </div>
                      {v.restrictions && (
                        <p className="mt-1 text-[12px] text-ink-2">{v.restrictions}</p>
                      )}
                    </div>
                    <div className="flex flex-col items-end gap-1">
                      <div className="flex items-center gap-1.5 text-[11.5px] text-ink-3">
                        <CalendarClock className="h-3 w-3" />
                        {formatDate(v.prochaineEcheance, { locale })}
                      </div>
                      <Badge tone={alertTone(level)}>{t(`alert.${level}`)}</Badge>
                    </div>
                    {v.certificatFileId && (
                      <a
                        href={`/api/files/${v.certificatFileId}`}
                        target="_blank"
                        rel="noreferrer"
                        className="inline-flex"
                      >
                        <Badge tone="success">
                          <FileText className="h-3 w-3" />
                          {t("certificates.viewFile")}
                        </Badge>
                      </a>
                    )}
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </section>
  );
}

/* ----------------------------------------------------------------- Certificates */
function CertificatesSection({
  certificates,
  locale,
}: {
  certificates: MedicalCertificateResponse[];
  locale: "fr" | "en";
}) {
  const t = useTranslations("medical");
  return (
    <section>
      <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold tracking-tight text-ink">
        <FileText className="h-4 w-4 text-orange-500" />
        {t("mine.certificatesTitle")}
      </h2>
      {certificates.length === 0 ? (
        <Card>
          <CardContent padding="md">
            <p className="text-center text-[13px] text-ink-3">{t("mine.certificatesEmpty")}</p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-3 sm:grid-cols-2">
          {certificates.map((c) => {
            const level = alertLevel(c.dateExpiration);
            return (
              <Card key={c.id}>
                <CardContent padding="md">
                  <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0">
                      <div className="truncate text-[13.5px] font-semibold text-ink">
                        {c.typeCertificat}
                      </div>
                      <div className="mt-0.5 flex flex-wrap items-center gap-1.5">
                        <Badge tone={statutTone(c.statut)}>
                          {KNOWN_STATUTS.has(c.statut.toUpperCase())
                            ? t(`certificates.new.statutValues.${c.statut.toUpperCase()}` as never)
                            : c.statut}
                        </Badge>
                        <Badge tone={alertTone(level)}>{t(`alert.${level}`)}</Badge>
                      </div>
                    </div>
                    {c.fichierId ? (
                      <a
                        href={`/api/files/${c.fichierId}`}
                        target="_blank"
                        rel="noreferrer"
                        className="inline-flex shrink-0"
                      >
                        <Badge tone="success">
                          <FileText className="h-3 w-3" />
                          {t("certificates.viewFile")}
                        </Badge>
                      </a>
                    ) : (
                      <Badge tone="gray">{t("certificates.noFile")}</Badge>
                    )}
                  </div>
                  <div className="mt-3 flex items-center justify-between text-[11.5px] text-ink-3">
                    <span>
                      {t("certificates.columns.emission")} · {formatDate(c.dateEmission, { locale })}
                    </span>
                    <span>
                      {t("certificates.columns.expiration")} · {formatDate(c.dateExpiration, { locale })}
                    </span>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </section>
  );
}

/* ----------------------------------------------------------------- Upload form */
type StoredFile = { id: string };

function UploadCertificate({ onDone }: { onDone: () => void }) {
  const t = useTranslations("medical");
  const tErrors = useTranslations("errors");
  const tVal = useTranslations("validation");
  const queryClient = useQueryClient();
  const today = new Date().toISOString().slice(0, 10);
  const nextYear = new Date(new Date().setFullYear(new Date().getFullYear() + 1))
    .toISOString()
    .slice(0, 10);

  const [type, setType] = React.useState<(typeof CERT_TYPES)[number]>("ARRET_MALADIE");
  const [customType, setCustomType] = React.useState("");
  const [dateEmission, setDateEmission] = React.useState(today);
  const [dateExpiration, setDateExpiration] = React.useState(nextYear);
  const [fileId, setFileId] = React.useState("");
  const [fileName, setFileName] = React.useState("");
  const [uploading, setUploading] = React.useState(false);

  async function upload(file: File) {
    setUploading(true);
    try {
      const fd = new FormData();
      fd.append("file", file, file.name);
      const res = await fetch("/api/files/upload", { method: "POST", body: fd });
      const json = (await res.json()) as { ok: boolean; data?: StoredFile; message?: string };
      if (!res.ok || !json.ok || !json.data) throw new Error(json.message ?? "upload failed");
      setFileId(json.data.id);
      setFileName(file.name);
    } catch (cause) {
      toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown"));
    } finally {
      setUploading(false);
    }
  }

  const resolvedType = type === "AUTRE" ? customType.trim() : t(`mine.upload.types.${type}`);

  const mutation = useMutation({
    mutationFn: () => {
      const body: SubmitMyCertificateRequest = {
        typeCertificat: resolvedType,
        dateEmission,
        dateExpiration,
        fichierId: fileId || null,
      };
      return apiFetch<MedicalCertificateResponse>("/api/hrm/medical/mine", {
        method: "POST",
        body,
      });
    },
    onSuccess: () => {
      toast.success(t("mine.upload.success"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "medical", "mine"] });
      onDone();
    },
    onError: (cause) => {
      toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown"));
    },
  });

  const canSubmit =
    !uploading &&
    !mutation.isPending &&
    !!resolvedType &&
    !!dateEmission &&
    !!dateExpiration &&
    dateExpiration >= dateEmission;

  return (
    <Card>
      <CardContent padding="lg">
        <h3 className="mb-1 flex items-center gap-2 text-[13.5px] font-bold tracking-tight text-ink">
          <Upload className="h-4 w-4 text-orange-500" />
          {t("mine.upload.title")}
        </h3>
        <p className="mb-4 text-[12px] text-ink-3">{t("mine.upload.subtitle")}</p>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field label={t("certificates.new.fields.type")} className={type === "AUTRE" ? "" : "sm:col-span-2"}>
            <select
              value={type}
              onChange={(e) => setType(e.target.value as (typeof CERT_TYPES)[number])}
              className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
            >
              {CERT_TYPES.map((ct) => (
                <option key={ct} value={ct}>
                  {t(`mine.upload.types.${ct}`)}
                </option>
              ))}
            </select>
          </Field>
          {type === "AUTRE" && (
            <Field
              label={t("certificates.new.fields.type")}
              error={!customType.trim() ? tVal("required") : undefined}
            >
              <Input
                placeholder={t("certificates.new.fields.typePlaceholder")}
                value={customType}
                onChange={(e) => setCustomType(e.target.value)}
              />
            </Field>
          )}
          <Field label={t("certificates.new.fields.dateEmission")}>
            <Input
              type="date"
              value={dateEmission}
              onChange={(e) => setDateEmission(e.target.value)}
            />
          </Field>
          <Field label={t("certificates.new.fields.dateExpiration")}>
            <Input
              type="date"
              value={dateExpiration}
              onChange={(e) => setDateExpiration(e.target.value)}
            />
          </Field>
          <Field label={t("certificates.new.fields.fichier")} className="sm:col-span-2">
            <label className="inline-flex cursor-pointer items-center gap-2 rounded-[11px] border border-dashed border-line bg-bg-soft px-3.5 py-3 text-[13px] text-ink-2 hover:border-orange-300">
              {uploading ? (
                <Loader2 className="h-3.5 w-3.5 animate-spin" />
              ) : fileId ? (
                <CheckCircle2 className="h-3.5 w-3.5 text-success-500" />
              ) : (
                <Upload className="h-3.5 w-3.5" />
              )}
              {fileId ? fileName || "✓ PDF" : t("certificates.new.fields.fichier")}
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
        <div className="mt-4 flex items-center justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onDone}>
            {t("mine.upload.cancel")}
          </Button>
          <Button type="button" disabled={!canSubmit} onClick={() => mutation.mutate()}>
            {mutation.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Plus className="h-4 w-4" />
            )}
            {t("mine.upload.submit")}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
