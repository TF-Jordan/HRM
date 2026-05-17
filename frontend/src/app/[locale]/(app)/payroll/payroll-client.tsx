"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { usePayrollRuns, useRunPayroll } from "@/hooks/modules/usePayroll";
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
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Link } from "@/i18n/navigation";
import {
  runPayrollSchema,
  type RunPayrollFormValues,
} from "@/lib/validation/hrm/payroll.schema";

export function PayrollClient() {
  const t = useTranslations("accounting.payroll");
  const tNav = useTranslations("navigation");
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
          <Button onClick={() => setOpen(true)}>
            <Plus className="size-4" />
            {t("newButton")}
          </Button>
        }
      />

      {runs.isLoading && <Skeleton className="h-32 w-full" />}

      {!runs.isLoading && runs.data && runs.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!runs.isLoading && runs.data && runs.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("table.periode"),
                    t("table.nbEmployes"),
                    t("table.totalBrut"),
                    t("table.totalNet"),
                    t("table.status"),
                    t("table.calculatedAt"),
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
                {runs.data.map((r) => (
                  <tr key={r.id} className="cursor-pointer hover:bg-brand-50/40">
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">
                      <Link href={`/payroll/runs/${r.id}` as never} className="hover:text-brand-700">
                        {r.periode}
                      </Link>
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-center">{r.nbEmployes}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(r.totalBrut)}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(r.totalNet)}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]"><StatusBadge kind="payrollRun" status={r.status} /></td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">{fmt.date(r.calculatedAt)}</td>
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
