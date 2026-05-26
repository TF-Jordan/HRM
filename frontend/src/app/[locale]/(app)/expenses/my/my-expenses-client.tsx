"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Link, useRouter } from "@/i18n/navigation";
import { useMyEmployee } from "@/hooks/modules/useMe";
import { useEmployeeExpenses, useCreateExpense } from "@/hooks/modules/useExpenses";
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
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { StatCard } from "@/components/ui-tokens/StatCard";
import {
  createExpenseSchema,
  type CreateExpenseFormValues,
} from "@/lib/validation/hrm/expense.schema";

export function MyExpensesClient() {
  const t = useTranslations("selfService.expenses");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const router = useRouter();
  const me = useMyEmployee();
  const employeeId = me.data?.id;
  const expenses = useEmployeeExpenses(employeeId);
  const create = useCreateExpense(employeeId);
  const [open, setOpen] = React.useState(false);

  const form = useForm<CreateExpenseFormValues>({
    resolver: zodResolver(createExpenseSchema),
    defaultValues: { periode: new Date().toISOString().slice(0, 10), motif: "" },
  });

  if (me.isLoading) return <Skeleton className="h-40 w-full" />;
  if (me.isError) return <NotEmployeeCard />;

  const onSubmit = (values: CreateExpenseFormValues) => {
    if (!employeeId) return;
    create.mutate(
      { employeeId, periode: values.periode, motif: values.motif },
      {
        onSuccess: (e) => {
          toast.success(t("form.submit"));
          setOpen(false);
          form.reset();
          router.push(`/expenses/${e.id}` as never);
        },
        onError: (err) => toast.error((err as Error).message),
      },
    );
  };

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-22"
        crumbs={[{ label: tNav("items.myExpenses") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus className="size-4" />
            {t("newButton")}
          </Button>
        }
      />

      {expenses.isLoading && <Skeleton className="h-32 w-full" />}

      {!expenses.isLoading && expenses.data && expenses.data.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <StatCard
            tone="amber"
            label={t("summary.pending")}
            value={expenses.data.filter((e) => e.status === "SUBMITTED").length}
            footer=""
          />
          <StatCard
            tone="blue"
            label={t("summary.total")}
            value={fmt.moneyShort(expenses.data.reduce((s, e) => s + Number(e.totalMontant), 0))}
            footer=""
          />
          <StatCard
            tone="green"
            label={t("summary.reimbursed")}
            value={fmt.moneyShort(
              expenses.data.filter((e) => e.status === "REIMBURSED").reduce((s, e) => s + Number(e.totalMontant), 0),
            )}
            footer=""
          />
        </div>
      )}

      {!expenses.isLoading && expenses.data && expenses.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!expenses.isLoading && expenses.data && expenses.data.length > 0 && (
        <Card className="overflow-hidden p-0">
          <table className="w-full border-collapse text-[13px]">
            <thead>
              <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                <th className="px-4 py-2.5 text-left font-semibold">{t("table.titre")}</th>
                <th className="px-3 py-2.5 text-right font-semibold">{t("table.total")}</th>
                <th className="px-3 py-2.5 text-left font-semibold">{t("table.dateSoumission")}</th>
                <th className="px-3 py-2.5 text-left font-semibold">{t("table.status")}</th>
              </tr>
            </thead>
            <tbody>
              {expenses.data.map((e) => (
                <tr
                  key={e.id}
                  className="cursor-pointer border-b border-line-soft/70 last:border-0 hover:bg-brand-50/40"
                  onClick={() => router.push(`/expenses/${e.id}` as never)}
                >
                  <td className="px-4 py-2.5 font-medium text-ink">
                    <Link href={`/expenses/${e.id}` as never} className="hover:text-brand-700">
                      {e.motif ?? "—"}
                    </Link>
                  </td>
                  <td className="px-3 py-2.5 text-right tabular text-ink-2">{fmt.money(e.totalMontant)}</td>
                  <td className="px-3 py-2.5 text-ink-2 tabular">{fmt.date(e.periode)}</td>
                  <td className="px-3 py-2.5">
                    <StatusBadge kind="expense" status={e.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("newButton")}</DialogTitle>
          </DialogHeader>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="periode">Période</Label>
              <Input id="periode" type="date" {...form.register("periode")} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="motif">{t("form.titre")}</Label>
              <Input id="motif" {...form.register("motif")} />
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
