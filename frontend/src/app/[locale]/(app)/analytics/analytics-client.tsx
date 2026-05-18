"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useKpiSnapshots, useCreateKpiSnapshot } from "@/hooks/modules/useKpi";
import { useFormat } from "@/hooks/useFormat";
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
import { KpiCard } from "@/components/ui-tokens/KpiCard";
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
          <Button onClick={() => setOpen(true)}>
            <Plus className="size-4" />
            {t("list.newButton")}
          </Button>
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
