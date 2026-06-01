"use client";

import { useQuery } from "@tanstack/react-query";
import { Calendar, FileText, Loader2, Stethoscope } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { alertLevel, alertTone, aptitudeTone } from "@/lib/medical-status";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  MedicalCertificateResponse,
  MedicalVisitResponse,
} from "@/server/ksm/modules/medical";

type MinePayload = {
  employee: EmployeeResponse | null;
  visits: MedicalVisitResponse[];
  certificates: MedicalCertificateResponse[];
};

export function MyMedical() {
  const t = useTranslations("medical");
  const locale = useLocale() as "fr" | "en";

  const query = useQuery({
    queryKey: ["hrm", "medical", "mine"],
    queryFn: () => apiFetch<MinePayload>("/api/hrm/medical/mine"),
  });

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("mine.title") }]}
        title={t("mine.title")}
        subtitle={t("mine.subtitle")}
      />

      {query.isLoading ? (
        <div className="grid place-items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
        </div>
      ) : query.error || !query.data ? (
        <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
          {query.error instanceof BffApiError ? query.error.message : "—"}
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6">
          <section>
            <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold tracking-tight text-ink">
              <Stethoscope className="h-4 w-4 text-orange-500" />
              {t("mine.visitsTitle")}
            </h2>
            {query.data.visits.length === 0 ? (
              <Card>
                <CardContent padding="md">
                  <p className="text-center text-[13px] text-ink-3">{t("mine.visitsEmpty")}</p>
                </CardContent>
              </Card>
            ) : (
              <div className="overflow-hidden rounded-[20px] border border-line bg-white shadow-sm-brand">
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="bg-bg-dim">
                      <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                        {t("visits.columns.date")}
                      </th>
                      <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                        {t("visits.columns.medecin")}
                      </th>
                      <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                        {t("visits.columns.aptitude")}
                      </th>
                      <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                        {t("visits.columns.nextDue")}
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {query.data.visits.map((v) => (
                      <tr key={v.id} className="border-t border-line-soft">
                        <td className="px-5 py-3 font-mono-tabular text-[12.5px] text-ink-2">
                          {formatDate(v.dateVisite, { locale })}
                        </td>
                        <td className="px-3 py-3 text-[13px] text-ink">{v.medecin}</td>
                        <td className="px-3 py-3">
                          <Badge tone={aptitudeTone(v.resultatAptitude)}>
                            {t(`aptitude.${v.resultatAptitude}`)}
                          </Badge>
                        </td>
                        <td className="px-5 py-3">
                          <div className="flex items-center gap-1.5">
                            <Calendar className="h-3 w-3 text-ink-3" />
                            <span className="font-mono-tabular text-[12.5px] text-ink-2">
                              {formatDate(v.prochaineEcheance, { locale })}
                            </span>
                            <Badge tone={alertTone(alertLevel(v.prochaineEcheance))}>
                              {t(`alert.${alertLevel(v.prochaineEcheance)}`)}
                            </Badge>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section>
            <h2 className="mb-3 flex items-center gap-2 text-[13px] font-bold tracking-tight text-ink">
              <FileText className="h-4 w-4 text-orange-500" />
              {t("mine.certificatesTitle")}
            </h2>
            {query.data.certificates.length === 0 ? (
              <Card>
                <CardContent padding="md">
                  <p className="text-center text-[13px] text-ink-3">{t("mine.certificatesEmpty")}</p>
                </CardContent>
              </Card>
            ) : (
              <div className="overflow-hidden rounded-[20px] border border-line bg-white shadow-sm-brand">
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="bg-bg-dim">
                      <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                        {t("certificates.columns.type")}
                      </th>
                      <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                        {t("certificates.columns.emission")}
                      </th>
                      <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                        {t("certificates.columns.expiration")}
                      </th>
                      <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                        {t("certificates.columns.file")}
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {query.data.certificates.map((c) => (
                      <tr key={c.id} className="border-t border-line-soft">
                        <td className="px-5 py-3 text-[13px] font-semibold text-ink">{c.typeCertificat}</td>
                        <td className="px-3 py-3 font-mono-tabular text-[12.5px] text-ink-2">
                          {formatDate(c.dateEmission, { locale })}
                        </td>
                        <td className="px-3 py-3">
                          <div className="flex items-center gap-1.5">
                            <span className="font-mono-tabular text-[12.5px] text-ink-2">
                              {formatDate(c.dateExpiration, { locale })}
                            </span>
                            <Badge tone={alertTone(alertLevel(c.dateExpiration))}>
                              {t(`alert.${alertLevel(c.dateExpiration)}`)}
                            </Badge>
                          </div>
                        </td>
                        <td className="px-5 py-3">
                          {c.fichierId ? (
                            <a
                              href={`/api/files/${c.fichierId}`}
                              target="_blank"
                              rel="noreferrer"
                              className="inline-flex"
                            >
                              <Badge tone="success">
                                <FileText className="h-3 w-3" />
                                {t("certificates.viewFile")}
                              </Badge>
                            </a>
                          ) : (
                            <Badge tone="gray">{t("certificates.noFile")}</Badge>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </div>
      )}
    </>
  );
}
