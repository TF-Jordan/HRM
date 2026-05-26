"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import { useMyEmployee } from "@/hooks/modules/useMe";
import { useEmployeeLoans, useRequestLoan } from "@/hooks/modules/useLoans";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { NotEmployeeCard } from "@/components/self-service/NotEmployeeCard";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { StatCard } from "@/components/ui-tokens/StatCard";
import {
  requestLoanSchema,
  type RequestLoanFormValues,
} from "@/lib/validation/hrm/loan.schema";

export function MyLoansClient() {
  const t = useTranslations("selfService.loans");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const me = useMyEmployee();
  const employeeId = me.data?.id;
  const loans = useEmployeeLoans(employeeId);
  const [open, setOpen] = React.useState(false);

  const repaying = (loans.data ?? []).filter((l) => l.status === "IN_REPAYMENT");
  const outstanding = repaying.reduce((s, l) => s + Number(l.soldeRestant), 0);
  const monthly = repaying.reduce((s, l) => s + Number(l.mensualite), 0);

  if (me.isLoading) return <Skeleton className="h-40 w-full" />;
  if (me.isError) return <NotEmployeeCard />;

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-11"
        crumbs={[{ label: tNav("items.myLoans") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus className="size-4" />
            {t("newButton")}
          </Button>
        }
      />

      {!loans.isLoading && loans.data && loans.data.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <StatCard tone="orange" label={t("summary.active")} value={repaying.length} footer={fmt.moneyShort(outstanding)} />
          <StatCard tone="blue" label={t("summary.outstanding")} value={fmt.moneyShort(outstanding)} footer="" />
          <StatCard tone="green" label={t("summary.monthly")} value={fmt.moneyShort(monthly)} footer="" />
        </div>
      )}

      {loans.isLoading && <Skeleton className="h-32 w-full" />}

      {!loans.isLoading && loans.data && loans.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!loans.isLoading && loans.data && loans.data.length > 0 && (
        <Card className="overflow-hidden p-0">
          <table className="w-full border-collapse text-[13px]">
            <thead>
              <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                <th className="px-4 py-2.5 text-right font-semibold">{t("table.montant")}</th>
                <th className="px-3 py-2.5 text-center font-semibold">{t("table.nbMois")}</th>
                <th className="px-3 py-2.5 text-left font-semibold">{t("table.motif")}</th>
                <th className="px-3 py-2.5 text-right font-semibold">{t("table.soldeRestant")}</th>
                <th className="px-3 py-2.5 text-left font-semibold">{t("progression")}</th>
                <th className="px-3 py-2.5 text-left font-semibold">{t("table.status")}</th>
              </tr>
            </thead>
            <tbody>
              {loans.data.map((l) => {
                const progress =
                  Number(l.montant) > 0
                    ? Math.round(((Number(l.montant) - Number(l.soldeRestant)) / Number(l.montant)) * 100)
                    : 0;
                return (
                  <tr key={l.id} className="border-b border-line-soft/70 last:border-0">
                    <td className="px-4 py-2.5 text-right font-semibold text-ink tabular">{fmt.money(l.montant)}</td>
                    <td className="px-3 py-2.5 text-center text-ink-3 tabular">{l.nbEcheances}</td>
                    <td className="px-3 py-2.5 text-ink-2">{l.motif ?? "—"}</td>
                    <td className="px-3 py-2.5 text-right text-ink-2 tabular">{fmt.money(l.soldeRestant)}</td>
                    <td className="px-3 py-2.5">
                      <div className="flex items-center gap-2">
                        <div className="h-1.5 w-20 overflow-hidden rounded-full bg-cream-2">
                          <div className="h-full rounded-full bg-grad-orange" style={{ width: `${progress}%` }} />
                        </div>
                        <span className="text-[11px] tabular text-ink-4">{progress}%</span>
                      </div>
                    </td>
                    <td className="px-3 py-2.5">
                      <StatusBadge kind="loan" status={l.status} />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </Card>
      )}

      <RequestLoanDialog employeeId={employeeId} open={open} onOpenChange={setOpen} />
    </div>
  );
}

function RequestLoanDialog({
  employeeId,
  open,
  onOpenChange,
}: {
  employeeId: string | undefined;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const t = useTranslations("selfService.loans");
  const tCommon = useTranslations("common");
  const mutation = useRequestLoan(employeeId);
  const fmt = useFormat();
  const form = useForm<RequestLoanFormValues>({
    resolver: zodResolver(requestLoanSchema),
    defaultValues: { montant: 0, nbEcheances: 6, motif: "" },
  });
  const montant = Number(form.watch("montant")) || 0;
  const nbEcheances = Number(form.watch("nbEcheances")) || 1;
  const estimated = nbEcheances > 0 ? montant / nbEcheances : 0;

  const onSubmit = (values: RequestLoanFormValues) => {
    if (!employeeId) return;
    mutation.mutate(
      {
        employeeId,
        montant: Number(values.montant),
        nbEcheances: Number(values.nbEcheances),
        motif: values.motif ?? null,
      } as never,
      {
        onSuccess: () => {
          toast.success("Demande soumise");
          onOpenChange(false);
          form.reset();
        },
        onError: (err) => toast.error((err as Error).message),
      },
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("newButton")}</DialogTitle>
          <DialogDescription>{t("subtitle")}</DialogDescription>
        </DialogHeader>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="montant">{t("form.montant")}</Label>
            <Input id="montant" type="number" min={1} step={1000} {...form.register("montant")} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="nbEcheances">{t("form.nbMois")}</Label>
            <Input id="nbEcheances" type="number" min={1} max={60} {...form.register("nbEcheances")} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="motif">{t("form.motif")}</Label>
            <Input id="motif" {...form.register("motif")} />
          </div>
          <div className="flex items-center justify-between rounded-[12px] bg-cream-soft/50 px-3 py-2.5">
            <span className="text-[12px] text-ink-3">{t("estimated")}</span>
            <span className="font-display text-[16px] font-extrabold text-ink tabular">{fmt.money(estimated)}</span>
          </div>
          {mutation.isError && (
            <div className="flex items-start gap-2 rounded-xl bg-status-red-50 px-3 py-2 text-status-red-600">
              <AlertTriangle className="size-4 shrink-0" />
              <span className="text-[12.5px]">{(mutation.error as Error).message}</span>
            </div>
          )}
          <DialogFooter>
            <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
              {tCommon("actions.cancel" as never)}
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending && <Loader2 className="size-4 animate-spin" />}
              {t("form.submit")}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
