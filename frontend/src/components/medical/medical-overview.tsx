"use client";

import { useQuery } from "@tanstack/react-query";
import { Calendar, FileText, Loader2, Plus, Stethoscope } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useCan } from "@/hooks/use-can";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { alertLevel, alertTone, aptitudeTone } from "@/lib/medical-status";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  MedicalCertificateResponse,
  MedicalVisitResponse,
} from "@/server/ksm/modules/medical";

type Tab = "visits" | "certificates";
type Filter = "ALL" | "OVERDUE" | "DUE_SOON" | "RESTRICTIONS";

export function MedicalOverview() {
  const t = useTranslations("medical");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const canCreate = useCan("hrm:medical:create");
  const [tab, setTab] = React.useState<Tab>("visits");
  const [filter, setFilter] = React.useState<Filter>("ALL");

  const visitsQuery = useQuery({
    queryKey: ["hrm", "medical", "visits"],
    queryFn: () => apiFetch<MedicalVisitResponse[]>("/api/hrm/medical/visits"),
    refetchInterval: 60_000,
  });
  const certificatesQuery = useQuery({
    queryKey: ["hrm", "medical", "certificates"],
    queryFn: () =>
      apiFetch<MedicalCertificateResponse[]>("/api/hrm/medical/certificates"),
    refetchInterval: 60_000,
  });
  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });
  const nameOf = React.useCallback(
    (employeeId: string) => {
      const e = employeesQuery.data?.find((x) => x.id === employeeId);
      return e?.actorDisplayName ?? e?.matricule ?? `${employeeId.slice(0, 8)}…`;
    },
    [employeesQuery.data],
  );

  const visits = React.useMemo(() => visitsQuery.data ?? [], [visitsQuery.data]);
  const certificates = React.useMemo(
    () => certificatesQuery.data ?? [],
    [certificatesQuery.data],
  );

  const counts = React.useMemo(() => {
    const yearStart = new Date(new Date().getFullYear(), 0, 1);
    const visitsThisYear = visits.filter((v) => new Date(v.dateVisite) >= yearStart).length;
    const overdue =
      visits.filter((v) => alertLevel(v.prochaineEcheance) === "OVERDUE").length +
      certificates.filter((c) => alertLevel(c.dateExpiration) === "OVERDUE").length;
    const dueSoon =
      visits.filter((v) => alertLevel(v.prochaineEcheance) === "DUE_SOON").length +
      certificates.filter((c) => alertLevel(c.dateExpiration) === "DUE_SOON").length;
    const restrictions = visits.filter(
      (v) => v.resultatAptitude === "APTE_AVEC_RESTRICTIONS" || v.resultatAptitude === "INAPTE_TEMPORAIRE",
    ).length;
    return { visitsThisYear, overdue, dueSoon, restrictions };
  }, [visits, certificates]);

  const filteredVisits = React.useMemo(() => {
    if (filter === "ALL") return visits;
    if (filter === "OVERDUE")
      return visits.filter((v) => alertLevel(v.prochaineEcheance) === "OVERDUE");
    if (filter === "DUE_SOON")
      return visits.filter((v) => alertLevel(v.prochaineEcheance) === "DUE_SOON");
    return visits.filter(
      (v) => v.resultatAptitude !== "APTE",
    );
  }, [visits, filter]);

  const filteredCerts = React.useMemo(() => {
    if (filter === "ALL") return certificates;
    if (filter === "OVERDUE")
      return certificates.filter((c) => alertLevel(c.dateExpiration) === "OVERDUE");
    if (filter === "DUE_SOON")
      return certificates.filter((c) => alertLevel(c.dateExpiration) === "DUE_SOON");
    return certificates;
  }, [certificates, filter]);

  const filters: { key: Filter; tKey: string }[] = [
    { key: "ALL", tKey: "filters.all" },
    { key: "OVERDUE", tKey: "filters.due" },
    { key: "DUE_SOON", tKey: "filters.dueSoon" },
    { key: "RESTRICTIONS", tKey: "filters.restrictions" },
  ];

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          canCreate ? (
            <>
              <Link href="/medical/certificates/new">
                <Button variant="secondary">
                  <FileText className="h-4 w-4" />
                  {t("certificates.addCertificate")}
                </Button>
              </Link>
              <Link href="/medical/visits/new">
                <Button>
                  <Plus className="h-4 w-4" />
                  {t("visits.addVisit")}
                </Button>
              </Link>
            </>
          ) : undefined
        }
      />

      <div className="mb-5 grid grid-cols-4 gap-4">
        <Tile label={t("kpi.totalVisits")} value={counts.visitsThisYear} tone="bg-orange-500" />
        <Tile label={t("kpi.overdueChecks")} value={counts.overdue} tone="bg-danger-500" />
        <Tile label={t("kpi.dueSoonChecks")} value={counts.dueSoon} tone="bg-warning-500" />
        <Tile label={t("kpi.restrictionsCount")} value={counts.restrictions} tone="bg-violet-500" />
      </div>

      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2 rounded-full border border-line bg-white p-1 shadow-xs-brand">
          <button
            type="button"
            onClick={() => setTab("visits")}
            className={cn(
              "flex items-center gap-1.5 rounded-full px-3.5 py-1.5 text-[12.5px] font-semibold transition-colors",
              tab === "visits" ? "bg-grad-orange text-white shadow-orange-brand" : "text-ink-2 hover:bg-bg-soft",
            )}
          >
            <Stethoscope className="h-3.5 w-3.5" />
            {t("visits.title")}
          </button>
          <button
            type="button"
            onClick={() => setTab("certificates")}
            className={cn(
              "flex items-center gap-1.5 rounded-full px-3.5 py-1.5 text-[12.5px] font-semibold transition-colors",
              tab === "certificates"
                ? "bg-grad-orange text-white shadow-orange-brand"
                : "text-ink-2 hover:bg-bg-soft",
            )}
          >
            <FileText className="h-3.5 w-3.5" />
            {t("certificates.title")}
          </button>
        </div>
        <div className="flex flex-wrap gap-2">
          {filters.map((f) => (
            <button
              key={f.key}
              type="button"
              onClick={() => setFilter(f.key)}
              className={cn(
                "rounded-full px-3 py-1.5 text-[12px] font-semibold transition-colors",
                filter === f.key
                  ? "bg-grad-orange text-white shadow-orange-brand"
                  : "border border-line bg-white text-ink-2 hover:bg-bg-soft",
              )}
            >
              {t(f.tKey)}
            </button>
          ))}
        </div>
      </div>

      {tab === "visits" ? (
        <Card>
          {visitsQuery.isLoading ? (
            <div className="grid place-items-center py-16">
              <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
            </div>
          ) : visitsQuery.error ? (
            <div className="px-5 py-10 text-center text-ink-3">
              {visitsQuery.error instanceof BffApiError ? visitsQuery.error.message : "—"}
            </div>
          ) : filteredVisits.length === 0 ? (
            <CardContent padding="lg">
              <p className="text-center text-[13px] text-ink-3">{t("visits.empty")}</p>
            </CardContent>
          ) : (
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-bg-dim">
                  <Th>{t("visits.columns.reference")}</Th>
                  <Th>{t("visits.columns.employee")}</Th>
                  <Th>{t("visits.columns.date")}</Th>
                  <Th>{t("visits.columns.medecin")}</Th>
                  <Th>{t("visits.columns.aptitude")}</Th>
                  <Th>{t("visits.columns.nextDue")}</Th>
                  <Th>{t("visits.columns.status")}</Th>
                </tr>
              </thead>
              <tbody>
                {filteredVisits.map((v) => {
                  const lvl = alertLevel(v.prochaineEcheance);
                  return (
                    <tr
                      key={v.id}
                      className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                      onClick={() => router.push(`/medical/visits/${v.id}`)}
                    >
                      <td className="px-5 py-3 font-mono-tabular text-[11px] text-ink-3">
                        {shortRef("MV", v.id)}
                      </td>
                      <td className="px-3 py-3">
                        <div className="flex items-center gap-2.5">
                          <Avatar name={nameOf(v.employeeId)} size="sm" />
                          <span className="text-[13px] font-semibold text-ink">{nameOf(v.employeeId)}</span>
                        </div>
                      </td>
                      <td className="px-3 py-3 font-mono-tabular text-[12.5px] text-ink-2">
                        {formatDate(v.dateVisite, { locale })}
                      </td>
                      <td className="px-3 py-3 text-[12.5px] text-ink-2">{v.medecin}</td>
                      <td className="px-3 py-3">
                        <Badge tone={aptitudeTone(v.resultatAptitude)}>
                          {t(`aptitude.${v.resultatAptitude}`)}
                        </Badge>
                      </td>
                      <td className="px-3 py-3">
                        <div className="flex items-center gap-1.5 font-mono-tabular text-[12.5px] text-ink-2">
                          <Calendar className="h-3 w-3 text-ink-3" />
                          {formatDate(v.prochaineEcheance, { locale })}
                        </div>
                      </td>
                      <td className="px-5 py-3">
                        <Badge tone={alertTone(lvl)}>{t(`alert.${lvl}`)}</Badge>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </Card>
      ) : (
        <Card>
          {certificatesQuery.isLoading ? (
            <div className="grid place-items-center py-16">
              <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
            </div>
          ) : certificatesQuery.error ? (
            <div className="px-5 py-10 text-center text-ink-3">
              {certificatesQuery.error instanceof BffApiError
                ? certificatesQuery.error.message
                : "—"}
            </div>
          ) : filteredCerts.length === 0 ? (
            <CardContent padding="lg">
              <p className="text-center text-[13px] text-ink-3">{t("certificates.empty")}</p>
            </CardContent>
          ) : (
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-bg-dim">
                  <Th>{t("certificates.columns.reference")}</Th>
                  <Th>{t("certificates.columns.employee")}</Th>
                  <Th>{t("certificates.columns.type")}</Th>
                  <Th>{t("certificates.columns.emission")}</Th>
                  <Th>{t("certificates.columns.expiration")}</Th>
                  <Th>{t("certificates.columns.statut")}</Th>
                  <Th>{t("certificates.columns.file")}</Th>
                </tr>
              </thead>
              <tbody>
                {filteredCerts.map((c) => {
                  const lvl = alertLevel(c.dateExpiration);
                  return (
                    <tr
                      key={c.id}
                      className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                      onClick={() => router.push(`/medical/certificates/${c.id}`)}
                    >
                      <td className="px-5 py-3 font-mono-tabular text-[11px] text-ink-3">
                        {shortRef("CR", c.id)}
                      </td>
                      <td className="px-3 py-3">
                        <div className="flex items-center gap-2.5">
                          <Avatar name={nameOf(c.employeeId)} size="sm" />
                          <span className="text-[13px] font-semibold text-ink">{nameOf(c.employeeId)}</span>
                        </div>
                      </td>
                      <td className="px-3 py-3 text-[13px] font-semibold text-ink">{c.typeCertificat}</td>
                      <td className="px-3 py-3 font-mono-tabular text-[12.5px] text-ink-2">
                        {formatDate(c.dateEmission, { locale })}
                      </td>
                      <td className="px-3 py-3">
                        <div className="flex items-center gap-1.5">
                          <span className="font-mono-tabular text-[12.5px] text-ink-2">
                            {formatDate(c.dateExpiration, { locale })}
                          </span>
                          <Badge tone={alertTone(lvl)}>{t(`alert.${lvl}`)}</Badge>
                        </div>
                      </td>
                      <td className="px-3 py-3 text-[12px] text-ink-2">{c.statut}</td>
                      <td className="px-5 py-3" onClick={(e) => e.stopPropagation()}>
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
                  );
                })}
              </tbody>
            </table>
          )}
        </Card>
      )}
    </>
  );
}

function Tile({ label, value, tone }: { label: string; value: React.ReactNode; tone: string }) {
  return (
    <div className="relative flex flex-col gap-1 overflow-hidden rounded-[16px] border border-line bg-white px-[18px] py-4 shadow-xs-brand">
      <div className="flex items-center justify-between">
        <span className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">{label}</span>
        <span className={cn("inline-block h-2 w-2 rounded-full", tone)} />
      </div>
      <div className="font-display font-mono-tabular text-[24px] font-extrabold tracking-tight text-ink">
        {value}
      </div>
    </div>
  );
}

function Th({ children }: { children: React.ReactNode }) {
  return (
    <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3 first:pl-5 last:pr-5">
      {children}
    </th>
  );
}

function shortRef(prefix: string, uuid: string): string {
  return `${prefix}-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}
