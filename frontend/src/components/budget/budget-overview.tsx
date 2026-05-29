"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ChartPie, Loader2, Minus, Plus } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input } from "@/components/ui/input";
import { useCan } from "@/hooks/use-can";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatNumber } from "@/lib/format";
import { cn } from "@/lib/utils";
import type { TrainingBudgetResponse } from "@/server/ksm/modules/training-budgets";

type TxKind = "engage" | "realise";

export function BudgetOverview() {
  const t = useTranslations("budget");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const canManage = useCan("hrm:budget:manage");
  const canCreate = useCan("hrm:budget:create");
  const [annee, setAnnee] = React.useState<number>(new Date().getFullYear());
  const [tx, setTx] = React.useState<{ id: string; kind: TxKind } | null>(null);

  const query = useQuery({
    queryKey: ["hrm", "training-budgets", annee],
    queryFn: () =>
      apiFetch<TrainingBudgetResponse[]>(`/api/hrm/training-budgets?annee=${annee}`),
    refetchInterval: 60_000,
  });

  const all = React.useMemo(() => query.data ?? [], [query.data]);
  const totals = React.useMemo(() => {
    let allocated = 0;
    let engaged = 0;
    let realised = 0;
    for (const b of all) {
      allocated += Number(b.montantAlloue ?? 0);
      engaged += Number(b.montantEngage ?? 0);
      realised += Number(b.montantRealise ?? 0);
    }
    const available = allocated - engaged - realised;
    const usedPct = allocated > 0 ? Math.round(((engaged + realised) / allocated) * 100) : 0;
    const realisedPct = allocated > 0 ? Math.round((realised / allocated) * 100) : 0;
    const engagedPct = allocated > 0 ? Math.round((engaged / allocated) * 100) : 0;
    return { allocated, engaged, realised, available, usedPct, realisedPct, engagedPct };
  }, [all]);

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["hrm", "training-budgets"] });
  function handleError(cause: unknown) {
    if (cause instanceof BffApiError) toast.error(cause.message);
    else toast.error(tErrors("unknown"));
  }
  const submitM = useMutation({
    mutationFn: ({ id, kind, montant }: { id: string; kind: TxKind; montant: number }) =>
      apiFetch(`/api/hrm/training-budgets/${id}/${kind === "engage" ? "engage" : "realiser"}`, {
        method: "POST",
        body: { montant },
      }),
    onSuccess: (_, vars) => {
      toast.success(vars.kind === "engage" ? t("detail.engageSuccess") : t("detail.realiseSuccess"));
      setTx(null);
      invalidate();
    },
    onError: handleError,
  });

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <>
            <div className="flex items-center gap-2 rounded-[11px] border border-line bg-white px-3 py-1.5 shadow-xs-brand">
              <ChartPie className="h-4 w-4 text-orange-500" />
              <span className="text-[11px] uppercase tracking-wider text-ink-3">{t("year")}</span>
              <input
                type="number"
                value={annee}
                onChange={(e) => setAnnee(Number(e.target.value) || annee)}
                className="w-20 border-none bg-transparent text-right font-mono-tabular text-[13px] font-semibold text-ink outline-none"
              />
            </div>
            {canCreate && (
              <Link href="/training-budgets/new">
                <Button>
                  <Plus className="h-4 w-4" />
                  {t("new.title")}
                </Button>
              </Link>
            )}
          </>
        }
      />

      <Card className="mb-6 border-orange-200 bg-[linear-gradient(135deg,var(--color-orange-50)_0%,#fff_100%)]">
        <CardContent padding="lg">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-[2fr_1fr]">
            <div>
              <div className="text-[11px] font-bold uppercase tracking-[0.06em] text-ink-3">
                {t("hero.annual", { annee })}
              </div>
              <div className="mt-2 font-display font-mono-tabular text-[44px] font-extrabold leading-none tracking-tight text-orange-700">
                {formatNumber(totals.allocated, locale)}{" "}
                <span className="text-[20px] text-ink-3">XAF</span>
              </div>
              <div className="mt-3 text-[13.5px] text-ink-2">
                <b className="font-mono-tabular">{formatNumber(totals.realised, locale)}</b> {t("hero.consumed")} ·{" "}
                <b className="font-mono-tabular">{formatNumber(totals.engaged, locale)}</b> {t("hero.engaged")} ·{" "}
                <b className="font-mono-tabular">{formatNumber(totals.available, locale)}</b> {t("hero.available")}
              </div>
              <div className="mt-4 flex h-7 w-full max-w-xl overflow-hidden rounded-full bg-bg-soft text-[10.5px] font-bold text-white">
                <div
                  className="grid place-items-center bg-grad-orange"
                  style={{ width: `${totals.realisedPct}%` }}
                >
                  {totals.realisedPct > 8 ? `${totals.realisedPct}%` : ""}
                </div>
                <div
                  className="grid place-items-center bg-orange-300 text-ink-2"
                  style={{ width: `${totals.engagedPct}%` }}
                >
                  {totals.engagedPct > 8 ? `${totals.engagedPct}%` : ""}
                </div>
              </div>
              <div className="mt-2 text-[11px] text-ink-3">{totals.usedPct}% {t("hero.used")}</div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <Mini label={t("kpi.allocated")} value={formatNumber(totals.allocated, locale)} tone="bg-orange-500" />
              <Mini label={t("kpi.engaged")} value={formatNumber(totals.engaged, locale)} tone="bg-warning-500" />
              <Mini label={t("kpi.realized")} value={formatNumber(totals.realised, locale)} tone="bg-info-500" />
              <Mini label={t("kpi.available")} value={formatNumber(totals.available, locale)} tone="bg-success-500" />
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="mb-3 text-[13px] font-bold tracking-tight text-ink">{t("queue.title")}</div>
      <Card>
        {query.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-7 w-7 animate-spin text-orange-500" />
          </div>
        ) : all.length === 0 ? (
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("queue.empty")}</p>
          </CardContent>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-bg-dim">
                <Th>{t("queue.columns.departement")}</Th>
                <Th className="text-right">{t("queue.columns.allocated")}</Th>
                <Th className="text-right">{t("queue.columns.engaged")}</Th>
                <Th className="text-right">{t("queue.columns.realized")}</Th>
                <Th className="text-right">{t("queue.columns.available")}</Th>
                <Th className="text-right" />
              </tr>
            </thead>
            <tbody>
              {all.map((b) => {
                const allocated = Number(b.montantAlloue ?? 0);
                const engaged = Number(b.montantEngage ?? 0);
                const realised = Number(b.montantRealise ?? 0);
                const available = allocated - engaged - realised;
                const pct = allocated > 0 ? Math.round(((engaged + realised) / allocated) * 100) : 0;
                return (
                  <tr key={b.id} className="border-t border-line-soft">
                    <td className="px-5 py-3">
                      <div className="text-[13px] font-semibold text-ink">
                        {b.agencyId ? `Agence · ${b.agencyId.slice(0, 8)}…` : "Organisation"}
                      </div>
                      <div className="mt-1.5 h-1.5 w-40 overflow-hidden rounded-full bg-bg-soft">
                        <div className="h-full bg-grad-orange" style={{ width: `${pct}%` }} />
                      </div>
                    </td>
                    <td className="px-3 py-3 text-right font-mono-tabular text-[13px] font-bold text-ink">
                      {formatNumber(allocated, locale)}
                    </td>
                    <td className="px-3 py-3 text-right font-mono-tabular text-[13px] text-warning-700">
                      {formatNumber(engaged, locale)}
                    </td>
                    <td className="px-3 py-3 text-right font-mono-tabular text-[13px] text-info-700">
                      {formatNumber(realised, locale)}
                    </td>
                    <td className="px-3 py-3 text-right font-mono-tabular text-[13px] font-semibold text-success-700">
                      {formatNumber(available, locale)}
                    </td>
                    <td className="px-5 py-3 text-right">
                      {canManage && (
                        <div className="flex justify-end gap-2">
                          <Button
                            type="button"
                            variant="ghost"
                            className="!h-7 !px-2.5 !text-[11px]"
                            onClick={() => setTx({ id: b.id, kind: "engage" })}
                          >
                            <Plus className="h-3 w-3" />
                            {t("detail.engageConfirm")}
                          </Button>
                          <Button
                            type="button"
                            variant="secondary"
                            className="!h-7 !px-2.5 !text-[11px]"
                            onClick={() => setTx({ id: b.id, kind: "realise" })}
                          >
                            <Minus className="h-3 w-3" />
                            {t("detail.realiseConfirm")}
                          </Button>
                        </div>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </Card>

      <TxDialog
        open={!!tx}
        kind={tx?.kind ?? "engage"}
        loading={submitM.isPending}
        onClose={() => setTx(null)}
        onConfirm={(montant) => tx && submitM.mutate({ ...tx, montant })}
      />
    </>
  );
}

function Mini({ label, value, tone }: { label: string; value: React.ReactNode; tone: string }) {
  return (
    <div className="rounded-[12px] border border-line bg-white px-3.5 py-3 shadow-xs-brand">
      <div className="flex items-center justify-between">
        <span className="text-[10.5px] font-semibold uppercase tracking-wider text-ink-3">{label}</span>
        <span className={cn("inline-block h-2 w-2 rounded-full", tone)} />
      </div>
      <div className="mt-1 font-display font-mono-tabular text-[18px] font-extrabold text-ink">
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

function TxDialog({
  open,
  kind,
  loading,
  onClose,
  onConfirm,
}: {
  open: boolean;
  kind: TxKind;
  loading: boolean;
  onClose: () => void;
  onConfirm: (montant: number) => void;
}) {
  const t = useTranslations("budget.detail");
  const tCommon = useTranslations("common");
  const [montant, setMontant] = React.useState<string>("");
  React.useEffect(() => {
    if (!open) setMontant("");
  }, [open]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={kind === "engage" ? t("engageTitle") : t("realiseTitle")}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!Number(montant) || loading}
            onClick={() => onConfirm(Number(montant))}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
            {kind === "engage" ? t("engageConfirm") : t("realiseConfirm")}
          </Button>
        </>
      }
    >
      <Field label={t("montantLabel")}>
        <Input
          type="number"
          min={0}
          step={1000}
          value={montant}
          onChange={(e) => setMontant(e.target.value)}
        />
      </Field>
    </Dialog>
  );
}
