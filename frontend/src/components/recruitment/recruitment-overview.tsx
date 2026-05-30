"use client";

import { useQuery } from "@tanstack/react-query";
import { Briefcase, Loader2, MapPin, Plus } from "lucide-react";
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
import {
  applicationStatusTone,
  jobOfferStatusTone,
  KANBAN_COLUMNS,
} from "@/lib/recruitment-status";
import { cn } from "@/lib/utils";
import type {
  ApplicationResponse,
  ApplicationStatus,
  JobOfferResponse,
  JobOfferStatus,
} from "@/server/ksm/modules/recruitment";

type Filter = "ALL" | JobOfferStatus;
const FILTERS: { key: Filter; tKey: string }[] = [
  { key: "ALL", tKey: "filters.all" },
  { key: "PUBLISHED", tKey: "filters.open" },
  { key: "DRAFT", tKey: "filters.draft" },
  { key: "CLOSED", tKey: "filters.closed" },
];

type OfferWithApps = JobOfferResponse & {
  applications: ApplicationResponse[];
};

export function RecruitmentOverview() {
  const t = useTranslations("recruitment");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const canCreate = useCan("hrm:recruitment:create");
  const [filter, setFilter] = React.useState<Filter>("ALL");

  const offersQuery = useQuery({
    queryKey: ["hrm", "job-offers"],
    queryFn: () => apiFetch<JobOfferResponse[]>("/api/hrm/job-offers"),
    refetchInterval: 60_000,
  });

  // For each offer, fetch its applications in parallel so we can colour KPIs,
  // build the kanban and the per-offer counts from real data.
  const offers = React.useMemo(() => offersQuery.data ?? [], [offersQuery.data]);
  const appsQueries = useQuery({
    queryKey: ["hrm", "job-offers", "applications", offers.map((o) => o.id)],
    queryFn: async () => {
      const all = await Promise.all(
        offers.map((o) =>
          apiFetch<ApplicationResponse[]>(`/api/hrm/job-offers/${o.id}/applications`).catch(
            () => [],
          ),
        ),
      );
      return offers.map<OfferWithApps>((o, i) => ({ ...o, applications: all[i] ?? [] }));
    },
    enabled: offers.length > 0,
  });
  const offersWithApps = appsQueries.data ?? [];

  const allApps = React.useMemo(
    () => offersWithApps.flatMap((o) => o.applications),
    [offersWithApps],
  );
  const counts = React.useMemo(() => {
    const open = offers.filter((o) => o.status === "PUBLISHED").length;
    const active = allApps.filter(
      (a) => a.status !== "REJECTED" && a.status !== "HIRED",
    ).length;
    const hires = allApps.filter((a) => a.status === "HIRED").length;
    const interviews = allApps.filter((a) => a.status === "INTERVIEWING").length;
    return { open, active, hires, interviews };
  }, [offers, allApps]);

  const visibleOffers = filter === "ALL" ? offersWithApps : offersWithApps.filter((o) => o.status === filter);

  const kanban = React.useMemo(() => {
    const map: Record<ApplicationStatus, OfferWithApps["applications"]> = {
      NEW: [],
      SHORTLISTED: [],
      INTERVIEWING: [],
      OFFERED: [],
      REJECTED: [],
      HIRED: [],
    };
    for (const a of allApps) {
      map[a.status]?.push(a);
    }
    return map;
  }, [allApps]);

  const offerById = React.useMemo(() => {
    const m = new Map<string, JobOfferResponse>();
    for (const o of offers) m.set(o.id, o);
    return m;
  }, [offers]);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          canCreate ? (
            <Link href="/recruitment/new">
              <Button>
                <Plus className="h-4 w-4" />
                {t("new.title")}
              </Button>
            </Link>
          ) : undefined
        }
      />

      <div className="mb-5 grid grid-cols-4 gap-4">
        <Tile label={t("kpi.openOffers")} value={counts.open} tone="bg-orange-500" />
        <Tile label={t("kpi.activeApplications")} value={counts.active} tone="bg-info-500" />
        <Tile label={t("kpi.scheduledInterviews")} value={counts.interviews} tone="bg-violet-500" />
        <Tile label={t("kpi.hires")} value={counts.hires} tone="bg-success-500" />
      </div>

      <Card className="mb-6">
        <CardContent padding="lg">
          <div className="mb-4">
            <div className="text-[14px] font-bold tracking-tight text-ink">{t("pipeline.title")}</div>
            <div className="text-[12px] text-ink-3">{t("pipeline.subtitle")}</div>
          </div>
          {offersQuery.isLoading || appsQueries.isLoading ? (
            <div className="grid place-items-center py-16">
              <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
            </div>
          ) : (
            <div className="grid grid-cols-[repeat(5,minmax(200px,1fr))] gap-3 overflow-x-auto">
              {KANBAN_COLUMNS.map((col) => {
                const cards = kanban[col] ?? [];
                return (
                  <div
                    key={col}
                    className="flex min-w-[200px] flex-col gap-2 rounded-[14px] bg-bg-dim p-3"
                  >
                    <div className="mb-1 flex items-center justify-between">
                      <Badge tone={applicationStatusTone(col)}>{t(`applicationStatus.${col}`)}</Badge>
                      <span className="font-mono-tabular text-[11px] font-bold text-ink-3">{cards.length}</span>
                    </div>
                    {cards.length === 0 ? (
                      <p className="rounded-[10px] border border-dashed border-line bg-white/40 px-3 py-3 text-center text-[11px] text-ink-4">
                        {t("pipeline.empty")}
                      </p>
                    ) : (
                      cards.map((c) => (
                        <button
                          key={c.id}
                          type="button"
                          onClick={() => router.push(`/recruitment/candidates/${c.id}`)}
                          className="rounded-[10px] border border-line bg-white p-3 text-left transition-colors hover:border-orange-300"
                        >
                          <div className="flex items-center gap-2.5">
                            <Avatar name={`${c.candidatPrenom} ${c.candidatNom}`} size="sm" />
                            <div className="min-w-0">
                              <div className="truncate text-[12.5px] font-semibold text-ink">
                                {c.candidatPrenom} {c.candidatNom}
                              </div>
                              <div className="truncate text-[10.5px] text-ink-3">
                                {offerById.get(c.jobOfferId)?.poste ?? c.jobOfferId.slice(0, 8)}
                              </div>
                            </div>
                          </div>
                          {c.candidatEmail && (
                            <div className="mt-2 truncate text-[10.5px] text-ink-4 font-mono-tabular">
                              {c.candidatEmail}
                            </div>
                          )}
                        </button>
                      ))
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      <div className="mb-3 flex items-center justify-between gap-3">
        <span className="text-[13px] font-bold tracking-tight text-ink">{t("offers.title")}</span>
        <div className="flex flex-wrap gap-2">
          {FILTERS.map((f) => (
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

      <Card>
        {offersQuery.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
          </div>
        ) : offersQuery.error ? (
          <div className="px-5 py-10 text-center text-ink-3">
            {offersQuery.error instanceof BffApiError ? offersQuery.error.message : "—"}
          </div>
        ) : visibleOffers.length === 0 ? (
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("offers.empty")}</p>
          </CardContent>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-bg-dim">
                <Th>{t("offers.columns.reference")}</Th>
                <Th>{t("offers.columns.poste")}</Th>
                <Th>{t("offers.columns.departement")}</Th>
                <Th>{t("offers.columns.site")}</Th>
                <Th>{t("offers.columns.package")}</Th>
                <Th className="text-right">{t("offers.columns.applications")}</Th>
                <Th>{t("offers.columns.deadline")}</Th>
                <Th>{t("offers.columns.status")}</Th>
              </tr>
            </thead>
            <tbody>
              {visibleOffers.map((o) => (
                <tr
                  key={o.id}
                  className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                  onClick={() => router.push(`/recruitment/offers/${o.id}`)}
                >
                  <td className="px-5 py-3 font-mono-tabular text-[11px] text-ink-3">{shortRef(o.id)}</td>
                  <td className="px-3 py-3">
                    <div className="flex items-center gap-2">
                      <Briefcase className="h-3.5 w-3.5 text-orange-500" />
                      <span className="text-[13px] font-semibold text-ink">{o.poste}</span>
                    </div>
                  </td>
                  <td className="px-3 py-3 text-[12.5px] text-ink-2">{o.departement ?? "—"}</td>
                  <td className="px-3 py-3 text-[12.5px] text-ink-2">
                    {o.localisation ? (
                      <span className="flex items-center gap-1">
                        <MapPin className="h-3 w-3 text-ink-3" />
                        {o.localisation}
                      </span>
                    ) : (
                      "—"
                    )}
                  </td>
                  <td className="px-3 py-3 font-mono-tabular text-[12px] text-ink-2">{o.packageSalarial ?? "—"}</td>
                  <td className="px-3 py-3 text-right font-mono-tabular text-[13px] font-bold text-ink">
                    {o.applications.length}
                  </td>
                  <td className="px-3 py-3 font-mono-tabular text-[12px] text-ink-2">
                    {o.dateLimite ? formatDate(o.dateLimite, { locale }) : "—"}
                  </td>
                  <td className="px-5 py-3">
                    <Badge tone={jobOfferStatusTone(o.status)}>{t(`jobOfferStatus.${o.status}`)}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
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

function Th({ children, className }: { children?: React.ReactNode; className?: string }) {
  return (
    <th
      className={cn(
        "px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3 first:pl-5 last:pr-5",
        className,
      )}
    >
      {children}
    </th>
  );
}

function shortRef(uuid: string): string {
  return `JO-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}
