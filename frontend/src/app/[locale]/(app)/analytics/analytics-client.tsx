"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2, GitCompare, Download, FileText } from "lucide-react";
import { toast } from "sonner";
import { useKpiSnapshots, useCreateKpiSnapshot } from "@/hooks/modules/useKpi";
import { useFormat } from "@/hooks/useFormat";
import { exportPdf } from "@/lib/pdf";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { KpiCard } from "@/components/ui-tokens/KpiCard";
import { exportCsv } from "@/lib/csv";
import type { RhKpiSnapshot } from "@/lib/types/hrm/kpi";
import {
  createKpiSnapshotSchema,
  type CreateKpiSnapshotFormValues,
} from "@/lib/validation/hrm/kpi.schema";

export function AnalyticsClient() {
  const t = useTranslations("analytics");
  const tCommon = useTranslations("common");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const list = useKpiSnapshots();
  const create = useCreateKpiSnapshot();
  const [open, setOpen] = React.useState(false);

  const form = useForm<CreateKpiSnapshotFormValues>({
    resolver: zodResolver(createKpiSnapshotSchema),
    defaultValues: {
      periode: new Date().toISOString().slice(0, 7),
      effectifTotal: 0,
      effectifActif: 0,
      tauxTurnover: "0",
      tauxAbsenteisme: "0",
      masseSalariale: "0",
      couvertureCompetences: "0",
    },
  });

  const sorted = React.useMemo(
    () => [...(list.data ?? [])].sort((a, b) => b.periode.localeCompare(a.periode)),
    [list.data],
  );
  const last = sorted[0];

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-27"
        crumbs={[{ label: tNav("items.analytics") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <>
            <Button
              variant="secondary"
              onClick={() =>
                exportCsv(
                  `analytics-kpi-${new Date().toISOString().slice(0, 10)}`,
                  sorted,
                  [
                    { header: t("list.table.periode"), value: (s) => s.periode },
                    { header: t("list.table.effectifTotal"), value: (s) => s.effectifTotal },
                    { header: t("list.table.effectifActif"), value: (s) => s.effectifActif },
                    { header: t("list.table.tauxTurnover"), value: (s) => s.tauxTurnover },
                    { header: t("list.table.tauxAbsenteisme"), value: (s) => s.tauxAbsenteisme },
                    { header: t("list.table.masseSalariale"), value: (s) => s.masseSalariale },
                    {
                      header: t("list.table.couvertureCompetences"),
                      value: (s) => s.couvertureCompetences,
                    },
                  ],
                )
              }
              disabled={sorted.length === 0}
            >
              <Download className="size-4" />
              {tCommon("actions.exportCsv")}
            </Button>
            <Button
              variant="secondary"
              disabled={sorted.length === 0}
              onClick={() =>
                exportPdf(
                  `analytics-kpi-${new Date().toISOString().slice(0, 10)}`,
                  sorted,
                  [
                    { header: t("list.table.periode"), value: (s) => s.periode },
                    { header: t("list.table.effectifTotal"), value: (s) => s.effectifTotal },
                    { header: t("list.table.effectifActif"), value: (s) => s.effectifActif },
                    {
                      header: t("list.table.tauxTurnover"),
                      value: (s) => `${(Number(s.tauxTurnover) * 100).toFixed(2)}%`,
                    },
                    {
                      header: t("list.table.tauxAbsenteisme"),
                      value: (s) => `${(Number(s.tauxAbsenteisme) * 100).toFixed(2)}%`,
                    },
                    { header: t("list.table.masseSalariale"), value: (s) => s.masseSalariale },
                  ],
                  { title: t("title"), subtitle: t("subtitle") },
                )
              }
            >
              <FileText className="size-4" />
              {tCommon("actions.exportPdf")}
            </Button>
            <Button onClick={() => setOpen(true)}>
              <Plus className="size-4" />
              {t("list.newButton")}
            </Button>
          </>
        }
      />

      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        <KpiCard
          label={t("kpis.snapshots")}
          value={list.data ? String(list.data.length) : "—"}
        />
        <KpiCard
          label={t("kpis.lastTurnover")}
          value={last ? `${(Number(last.tauxTurnover) * 100).toFixed(1)}%` : "—"}
        />
        <KpiCard
          label={t("kpis.lastAbsenteisme")}
          value={last ? `${(Number(last.tauxAbsenteisme) * 100).toFixed(1)}%` : "—"}
        />
        <KpiCard
          label={t("kpis.lastMasseSalariale")}
          value={last ? fmt.moneyShort(last.masseSalariale) : "—"}
        />
      </div>

      {sorted.length >= 2 && <ComparisonPanel snapshots={sorted} />}

      {list.isLoading && <Skeleton className="h-32 w-full" />}

      {!list.isLoading && sorted.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">
            {t("list.empty")}
          </CardContent>
        </Card>
      )}

      {sorted.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("list.table.periode"),
                    t("list.table.effectifTotal"),
                    t("list.table.effectifActif"),
                    t("list.table.tauxTurnover"),
                    t("list.table.tauxAbsenteisme"),
                    t("list.table.masseSalariale"),
                    t("list.table.couvertureCompetences"),
                  ].map((h, i) => (
                    <th
                      key={i}
                      className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {sorted.map((s) => (
                  <tr key={s.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink tabular">
                      {s.periode}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.integer(s.effectifTotal)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.integer(s.effectifActif)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {(Number(s.tauxTurnover) * 100).toFixed(2)}%
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {(Number(s.tauxAbsenteisme) * 100).toFixed(2)}%
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.money(s.masseSalariale)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {(Number(s.couvertureCompetences) * 100).toFixed(1)}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("list.newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={form.handleSubmit((values) =>
              create.mutate(
                {
                  periode: values.periode,
                  effectifTotal: Number(values.effectifTotal),
                  effectifActif: Number(values.effectifActif),
                  tauxTurnover: values.tauxTurnover,
                  tauxAbsenteisme: values.tauxAbsenteisme,
                  masseSalariale: values.masseSalariale,
                  couvertureCompetences: values.couvertureCompetences,
                },
                {
                  onSuccess: () => {
                    toast.success(t("form.submit"));
                    setOpen(false);
                    form.reset();
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5 col-span-2">
                <Label htmlFor="periode">{t("form.periode")}</Label>
                <Input id="periode" placeholder="2026-05" {...form.register("periode")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="effectifTotal">{t("form.effectifTotal")}</Label>
                <Input id="effectifTotal" type="number" min="0" {...form.register("effectifTotal")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="effectifActif">{t("form.effectifActif")}</Label>
                <Input id="effectifActif" type="number" min="0" {...form.register("effectifActif")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="tauxTurnover">{t("form.tauxTurnover")}</Label>
                <Input id="tauxTurnover" placeholder="0.05" {...form.register("tauxTurnover")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="tauxAbsenteisme">{t("form.tauxAbsenteisme")}</Label>
                <Input id="tauxAbsenteisme" placeholder="0.03" {...form.register("tauxAbsenteisme")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="masseSalariale">{t("form.masseSalariale")}</Label>
                <Input id="masseSalariale" {...form.register("masseSalariale")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="couvertureCompetences">{t("form.couvertureCompetences")}</Label>
                <Input id="couvertureCompetences" placeholder="0.75" {...form.register("couvertureCompetences")} />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
                {tCommon("actions.cancel")}
              </Button>
              <Button type="submit" disabled={create.isPending}>
                {create.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function delta(a: string | number, b: string | number): number | null {
  const na = Number(a);
  const nb = Number(b);
  if (!Number.isFinite(na) || !Number.isFinite(nb) || nb === 0) return null;
  return ((na - nb) / Math.abs(nb)) * 100;
}

function DeltaCard({
  label,
  refValue,
  curValue,
  formatter,
  invert,
}: {
  label: string;
  refValue: string | number;
  curValue: string | number;
  formatter: (v: string | number) => string;
  invert?: boolean;
}) {
  const d = delta(curValue, refValue);
  const positive = d != null && (invert ? d < 0 : d > 0);
  const negative = d != null && (invert ? d > 0 : d < 0);
  return (
    <Card>
      <CardContent className="space-y-1 p-4">
        <div className="text-[11px] font-semibold uppercase tracking-wide text-ink-4">
          {label}
        </div>
        <div className="flex items-baseline gap-2">
          <span className="font-display text-[22px] font-bold tabular text-ink">
            {formatter(curValue)}
          </span>
          <span className="text-[11.5px] text-ink-3 tabular">
            ({formatter(refValue)})
          </span>
        </div>
        {d != null && (
          <div
            className={
              "text-[12px] font-semibold tabular " +
              (positive
                ? "text-status-green-600"
                : negative
                  ? "text-status-red-600"
                  : "text-ink-3")
            }
          >
            {d > 0 ? "+" : ""}
            {d.toFixed(2)}%
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function ComparisonPanel({ snapshots }: { snapshots: RhKpiSnapshot[] }) {
  const t = useTranslations("analytics");
  const fmt = useFormat();
  const [refId, setRefId] = React.useState<string>(snapshots[1]?.id ?? "");
  const [curId, setCurId] = React.useState<string>(snapshots[0]?.id ?? "");

  const ref = snapshots.find((s) => s.id === refId);
  const cur = snapshots.find((s) => s.id === curId);

  return (
    <Card>
      <CardContent className="space-y-4 p-5">
        <div className="flex items-center gap-2 text-[13px] font-semibold text-ink">
          <GitCompare className="size-4 text-brand-700" />
          {t("comparison.title")}
        </div>
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
          <div className="space-y-1.5">
            <Label>{t("comparison.reference")}</Label>
            <Select value={refId} onValueChange={setRefId}>
              <SelectTrigger>
                <SelectValue placeholder="—" />
              </SelectTrigger>
              <SelectContent>
                {snapshots.map((s) => (
                  <SelectItem key={s.id} value={s.id}>
                    {s.periode}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label>{t("comparison.current")}</Label>
            <Select value={curId} onValueChange={setCurId}>
              <SelectTrigger>
                <SelectValue placeholder="—" />
              </SelectTrigger>
              <SelectContent>
                {snapshots.map((s) => (
                  <SelectItem key={s.id} value={s.id}>
                    {s.periode}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
        {ref && cur && (
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-6">
            <DeltaCard
              label={t("list.table.effectifTotal")}
              refValue={ref.effectifTotal}
              curValue={cur.effectifTotal}
              formatter={(v) => fmt.integer(Number(v))}
            />
            <DeltaCard
              label={t("list.table.effectifActif")}
              refValue={ref.effectifActif}
              curValue={cur.effectifActif}
              formatter={(v) => fmt.integer(Number(v))}
            />
            <DeltaCard
              label={t("list.table.tauxTurnover")}
              refValue={ref.tauxTurnover}
              curValue={cur.tauxTurnover}
              formatter={(v) => `${(Number(v) * 100).toFixed(2)}%`}
              invert
            />
            <DeltaCard
              label={t("list.table.tauxAbsenteisme")}
              refValue={ref.tauxAbsenteisme}
              curValue={cur.tauxAbsenteisme}
              formatter={(v) => `${(Number(v) * 100).toFixed(2)}%`}
              invert
            />
            <DeltaCard
              label={t("list.table.masseSalariale")}
              refValue={ref.masseSalariale}
              curValue={cur.masseSalariale}
              formatter={(v) => fmt.moneyShort(v)}
            />
            <DeltaCard
              label={t("list.table.couvertureCompetences")}
              refValue={ref.couvertureCompetences}
              curValue={cur.couvertureCompetences}
              formatter={(v) => `${(Number(v) * 100).toFixed(1)}%`}
            />
          </div>
        )}
      </CardContent>
    </Card>
  );
}
