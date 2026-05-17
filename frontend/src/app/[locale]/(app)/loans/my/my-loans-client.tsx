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

      {loans.isLoading && <Skeleton className="h-32 w-full" />}

      {!loans.isLoading && loans.data && loans.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!loans.isLoading && loans.data && loans.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[t("table.montant"), t("table.nbMois"), t("table.motif"), t("table.soldeRestant"), t("table.status")].map((h, i) => (
                    <th key={i} className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {loans.data.map((l) => (
                  <tr key={l.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink tabular text-right">{fmt.money(l.montant)}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-center">{l.nbEcheances}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">{l.motif ?? "—"}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(l.soldeRestant)}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]"><StatusBadge kind="loan" status={l.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
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
  const form = useForm<RequestLoanFormValues>({
    resolver: zodResolver(requestLoanSchema),
    defaultValues: { montant: 0, nbEcheances: 6, motif: "" },
  });

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
