"use client";

import { useQuery } from "@tanstack/react-query";
import { Calendar, ChevronLeft, FileText, Loader2, Stethoscope } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/input";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { alertLevel, alertTone, aptitudeTone } from "@/lib/medical-status";
import type { MedicalVisitResponse } from "@/server/ksm/modules/medical";

export function VisitDetail({ visitId }: { visitId: string }) {
  const t = useTranslations("medical");
  const locale = useLocale() as "fr" | "en";

  const query = useQuery({
    queryKey: ["hrm", "medical", "visit", visitId],
    queryFn: () => apiFetch<MedicalVisitResponse>(`/api/hrm/medical/visits/${visitId}`),
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

  const v = query.data;
  const lvl = alertLevel(v.prochaineEcheance);
  const reference = `MV-${v.id.slice(0, 4).toUpperCase()}-${v.id.slice(4, 8).toUpperCase()}`;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/medical" },
          { label: reference },
        ]}
        title={
          <span className="flex items-center gap-3">
            <Stethoscope className="h-6 w-6 text-orange-500" />
            {v.medecin}
          </span>
        }
        subtitle={
          <span className="flex flex-wrap items-center gap-3">
            <Badge tone={aptitudeTone(v.resultatAptitude)}>
              {t(`aptitude.${v.resultatAptitude}`)}
            </Badge>
            <Badge tone={alertTone(lvl)}>{t(`alert.${lvl}`)}</Badge>
            <span className="font-mono-tabular text-[12px] text-ink-3">{reference}</span>
          </span>
        }
        actions={
          <Link href="/medical">
            <Button type="button" variant="secondary">
              <ChevronLeft className="h-4 w-4" />
              {t("detail.back")}
            </Button>
          </Link>
        }
      />

      <Card>
        <CardContent padding="lg">
          <dl className="grid grid-cols-2 gap-x-8 gap-y-4 md:grid-cols-3">
            <Detail label={t("detail.date")} value={formatDate(v.dateVisite, { locale })} mono />
            <Detail label={t("detail.medecin")} value={v.medecin} />
            <Detail
              label={t("detail.nextDue")}
              value={
                <span className="flex items-center gap-1.5">
                  <Calendar className="h-3 w-3 text-ink-3" />
                  {formatDate(v.prochaineEcheance, { locale })}
                </span>
              }
              mono
            />
            <Detail label={t("detail.aptitude")} value={t(`aptitude.${v.resultatAptitude}`)} />
            <Detail
              label={t("detail.file")}
              value={
                v.certificatFileId ? (
                  <a
                    href={`/api/files/${v.certificatFileId}`}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-1.5 text-orange-600 hover:text-orange-700"
                  >
                    <FileText className="h-3 w-3" />
                    {t("detail.viewFile")}
                  </a>
                ) : (
                  "—"
                )
              }
            />
          </dl>
          {v.restrictions && (
            <div className="mt-6">
              <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                {t("detail.restrictions")}
              </Label>
              <p className="mt-1 whitespace-pre-line text-[13.5px] text-ink-2">{v.restrictions}</p>
            </div>
          )}
          <p className="mt-6 text-[12px] text-ink-4">
            ID · <span className="font-mono-tabular">{v.id}</span>
          </p>
        </CardContent>
      </Card>
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
