"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2, Download, FileText } from "lucide-react";
import { toast } from "sonner";
import { usePayrollRuns, useRunPayroll } from "@/hooks/modules/usePayroll";
import { useFormat } from "@/hooks/useFormat";
import { exportCsv } from "@/lib/csv";
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
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Link } from "@/i18n/navigation";
import {
  runPayrollSchema,
  type RunPayrollFormValues,
} from "@/lib/validation/hrm/payroll.schema";

export function PayrollClient() {
  const t = useTranslations("accounting.payroll");
  const tNav = useTranslations("navigation");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const runs = usePayrollRuns();
  const create = useRunPayroll();
  const [open, setOpen] = React.useState(false);

  const form = useForm<RunPayrollFormValues>({
    resolver: zodResolver(runPayrollSchema),
    defaultValues: { periode: new Date().toISOString().slice(0, 7) },
  });

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-06"
        crumbs={[{ label: tNav("items.payroll") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <>
            <Button
              variant="secondary"
              disabled={!runs.data || runs.data.length === 0}
              onClick={() =>
                exportCsv(
                  `payroll-runs-${new Date().toISOString().slice(0, 10)}`,
                  runs.data ?? [],
                  [
                    { header: t("table.periode"), value: (r) => r.periode },
                    { header: t("table.nbEmployes"), value: (r) => r.nbEmployes },
                    { header: t("table.totalBrut"), value: (r) => r.totalBrut },
                    { header: t("table.totalNet"), value: (r) => r.totalNet },
                    { header: t("table.status"), value: (r) => r.status },
                    {
                      header: t("table.calculatedAt"),
                      value: (r) => r.calculatedAt ?? "",
                    },
                  ],
                )
              }
            >
              <Download className="size-4" />
              {tCommon("actions.exportCsv")}
            </Button>
            <Button
              variant="secondary"
              disabled={!runs.data || runs.data.length === 0}
              onClick={() =>
                exportPdf(
                  `payroll-runs-${new Date().toISOString().slice(0, 10)}`,
                  runs.data ?? [],
                  [
                    { header: t("table.periode"), value: (r) => r.periode },
                    { header: t("table.nbEmployes"), value: (r) => r.nbEmployes },
                    { header: t("table.totalBrut"), value: (r) => r.totalBrut },
                    { header: t("table.totalNet"), value: (r) => r.totalNet },
                    { header: t("table.status"), value: (r) => r.status },
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
              {t("newButton")}
            </Button>
          </>
        }
      />

      {runs.isLoading && <Skeleton className="h-40 w-full rounded-[20px]" />}

      {!runs.isLoading && runs.data && runs.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!runs.isLoading && runs.data && runs.data.length > 0 && (
        <>
          {(() => {
            const latest = [...runs.data].sort((a, b) => b.periode.localeCompare(a.periode))[0]!;
            const cotis = Number(latest.totalBrut) - Number(latest.totalNet);
            return (
              <div className="overflow-hidden rounded-[20px] bg-grad-dark p-5 text-white shadow-elev-lg">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <div className="text-[11px] font-semibold uppercase tracking-[0.14em] text-white/55">
                      {t("currentCycle")}
                    </div>
                    <div className="font-display text-[22px] font-extrabold tracking-tight">
                      {t("title")} · {latest.periode}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <StatusBadge kind="payrollRun" status={latest.status} />
                    <Button asChild variant="secondary">
                      <Link href={`/payroll/runs/${latest.id}` as never}>{t("openCycle")}</Link>
                    </Button>
                  </div>
                </div>
                <div className="mt-4 grid grid-cols-2 gap-4 sm:grid-cols-4">
                  <HeroMetric label={t("table.nbEmployes")} value={String(latest.nbEmployes)} />
                  <HeroMetric label={t("table.totalBrut")} value={fmt.moneyShort(latest.totalBrut)} />
                  <HeroMetric label={t("cotisations")} value={fmt.moneyShort(cotis)} />
                  <HeroMetric label={t("table.totalNet")} value={fmt.moneyShort(latest.totalNet)} />
                </div>
              </div>
            );
          })()}

          <Card className="overflow-hidden p-0">
            <div className="border-b border-line-soft p-3">
              <h3 className="px-1 font-display text-[15px] font-bold text-ink">{t("cycles")}</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full border-collapse text-[13px]">
                <thead>
                  <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                    <th className="px-4 py-2.5 text-left font-semibold">{t("table.periode")}</th>
                    <th className="px-3 py-2.5 text-center font-semibold">{t("table.nbEmployes")}</th>
                    <th className="px-3 py-2.5 text-right font-semibold">{t("table.totalBrut")}</th>
                    <th className="px-3 py-2.5 text-right font-semibold">{t("table.totalNet")}</th>
                    <th className="px-3 py-2.5 text-left font-semibold">{t("table.status")}</th>
                    <th className="px-3 py-2.5 text-left font-semibold">{t("table.calculatedAt")}</th>
                  </tr>
                </thead>
                <tbody>
                  {[...runs.data]
                    .sort((a, b) => b.periode.localeCompare(a.periode))
                    .map((r) => (
                      <tr key={r.id} className="border-b border-line-soft/70 last:border-0 hover:bg-brand-50/40">
                        <td className="px-4 py-2.5 font-semibold text-ink">
                          <Link href={`/payroll/runs/${r.id}` as never} className="hover:text-brand-700">
                            {r.periode}
                          </Link>
                        </td>
                        <td className="px-3 py-2.5 text-center tabular text-ink-2">{r.nbEmployes}</td>
                        <td className="px-3 py-2.5 text-right tabular text-ink-2">{fmt.money(r.totalBrut)}</td>
                        <td className="px-3 py-2.5 text-right tabular text-ink-2">{fmt.money(r.totalNet)}</td>
                        <td className="px-3 py-2.5">
                          <StatusBadge kind="payrollRun" status={r.status} />
                        </td>
                        <td className="px-3 py-2.5 tabular text-ink-2">{fmt.date(r.calculatedAt)}</td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          </Card>
        </>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={form.handleSubmit((values) =>
              create.mutate(values, {
                onSuccess: () => {
                  toast.success(t("form.submit"));
                  setOpen(false);
                  form.reset();
                },
                onError: (err) => toast.error((err as Error).message),
              }),
            )}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="periode">{t("form.periode")}</Label>
              <Input
                id="periode"
                placeholder="2025-08"
                {...form.register("periode")}
              />
              {form.formState.errors.periode && (
                <p className="text-[12px] text-status-red-600">
                  {form.formState.errors.periode.message}
                </p>
              )}
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
                Annuler
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

function HeroMetric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-[14px] bg-white/5 p-3 ring-1 ring-white/10">
      <div className="text-[10.5px] font-semibold uppercase tracking-[0.1em] text-white/50">{label}</div>
      <div className="mt-1 font-display text-[20px] font-extrabold tabular">{value}</div>
    </div>
  );
}
