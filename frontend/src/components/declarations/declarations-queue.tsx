"use client";

import { useQuery } from "@tanstack/react-query";
import { FileText, Loader2, Plus, ShieldCheck } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useCan } from "@/hooks/use-can";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { declarationStatusTone, declarationTypeTone } from "@/lib/declaration-status";
import { formatDate } from "@/lib/format";
import { cn } from "@/lib/utils";
import type {
  DeclarationStatus,
  SocialDeclarationResponse,
} from "@/server/ksm/modules/declarations";

type Filter = "ALL" | DeclarationStatus;

const FILTERS: { key: Filter; tKey: string }[] = [
  { key: "ALL", tKey: "filters.all" },
  { key: "DRAFT", tKey: "filters.draft" },
  { key: "GENERATED", tKey: "filters.generated" },
  { key: "SUBMITTED", tKey: "filters.submitted" },
  { key: "ACKNOWLEDGED", tKey: "filters.acknowledged" },
];

export function DeclarationsQueue() {
  const t = useTranslations("declarations");
  const locale = useLocale() as "fr" | "en";
  const router = useRouter();
  const canCreate = useCan("hrm:declaration:create");
  const [filter, setFilter] = React.useState<Filter>("ALL");

  const query = useQuery({
    queryKey: ["hrm", "declarations"],
    queryFn: () =>
      apiFetch<SocialDeclarationResponse[]>("/api/hrm/declarations"),
    refetchInterval: 60_000,
  });

  const all = React.useMemo(() => query.data ?? [], [query.data]);
  const visible = filter === "ALL" ? all : all.filter((d) => d.statut === filter);

  const counts = React.useMemo(() => {
    const c: Partial<Record<DeclarationStatus, number>> = {};
    for (const r of all) c[r.statut] = (c[r.statut] ?? 0) + 1;
    return {
      toGenerate: c.DRAFT ?? 0,
      toSubmit: c.GENERATED ?? 0,
      submitted: c.SUBMITTED ?? 0,
      acknowledged: c.ACKNOWLEDGED ?? 0,
    };
  }, [all]);

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          canCreate ? (
            <Link href="/declarations/new">
              <Button>
                <Plus className="h-4 w-4" />
                {t("new.title")}
              </Button>
            </Link>
          ) : undefined
        }
      />

      <div className="mb-5 grid grid-cols-4 gap-4">
        <Tile label={t("kpi.toGenerate")} value={counts.toGenerate} tone="bg-warning-500" />
        <Tile label={t("kpi.toSubmit")} value={counts.toSubmit} tone="bg-info-500" />
        <Tile label={t("kpi.submitted")} value={counts.submitted} tone="bg-orange-500" />
        <Tile label={t("kpi.acknowledged")} value={counts.acknowledged} tone="bg-success-500" />
      </div>

      <div className="mb-4 flex flex-wrap gap-2">
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

      <Card>
        {query.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
          </div>
        ) : query.error ? (
          <div className="px-5 py-10 text-center text-ink-3">
            {query.error instanceof BffApiError ? query.error.message : "—"}
          </div>
        ) : visible.length === 0 ? (
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("list.empty")}</p>
          </CardContent>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-bg-dim">
                <Th>{t("list.columns.reference")}</Th>
                <Th>{t("list.columns.type")}</Th>
                <Th>{t("list.columns.periode")}</Th>
                <Th>{t("list.columns.format")}</Th>
                <Th>{t("list.columns.status")}</Th>
                <Th>{t("list.columns.submitted")}</Th>
                <Th className="text-right">{t("list.columns.actions")}</Th>
              </tr>
            </thead>
            <tbody>
              {visible.map((d) => (
                <tr
                  key={d.id}
                  className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                  onClick={() => router.push(`/declarations/${d.id}`)}
                >
                  <td className="px-5 py-3 font-mono-tabular text-[11px] text-ink-3">
                    {shortRef(d.id)}
                  </td>
                  <td className="px-3 py-3">
                    <div className="flex items-center gap-2">
                      <ShieldCheck className="h-3.5 w-3.5 text-orange-500" />
                      <Badge tone={declarationTypeTone(d.type)}>{t(`type.${d.type}`)}</Badge>
                    </div>
                  </td>
                  <td className="px-3 py-3 font-mono-tabular text-[12.5px] text-ink-2">
                    {d.periode}
                  </td>
                  <td className="px-3 py-3 text-[12px] font-semibold text-ink-2">
                    <Badge tone="gray">{d.format}</Badge>
                  </td>
                  <td className="px-3 py-3">
                    <Badge tone={declarationStatusTone(d.statut)}>{t(`status.${d.statut}`)}</Badge>
                  </td>
                  <td className="px-3 py-3 font-mono-tabular text-[12px] text-ink-2">
                    {d.submittedAt ? formatDate(d.submittedAt, { locale }) : "—"}
                  </td>
                  <td className="px-5 py-3 text-right" onClick={(e) => e.stopPropagation()}>
                    {d.fichierId && (
                      <a
                        href={`/api/files/${d.fichierId}`}
                        target="_blank"
                        rel="noreferrer"
                        className="inline-flex"
                      >
                        <Badge tone="success">
                          <FileText className="h-3 w-3" />
                          PDF
                        </Badge>
                      </a>
                    )}
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
  return `DS-${uuid.slice(0, 4).toUpperCase()}-${uuid.slice(4, 8).toUpperCase()}`;
}
