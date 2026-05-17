"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Send, Loader2, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import {
  useExpense,
  useExpenseLines,
  useAddExpenseLine,
  useSubmitExpense,
} from "@/hooks/modules/useExpenses";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import {
  addExpenseLineSchema,
  type AddExpenseLineFormValues,
} from "@/lib/validation/hrm/expense.schema";

const CATEGORIES = ["TRANSPORT", "MEAL", "HOTEL", "SUPPLIES", "MEDICAL", "OTHER"] as const;

export function ExpenseDetailClient({ id }: { id: string }) {
  const t = useTranslations("selfService.expenses");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const expense = useExpense(id);
  const lines = useExpenseLines(id);
  const addLine = useAddExpenseLine(id);
  const submit = useSubmitExpense(expense.data?.employeeId);
  const [open, setOpen] = React.useState(false);

  const form = useForm<AddExpenseLineFormValues>({
    resolver: zodResolver(addExpenseLineSchema),
    defaultValues: {
      categorie: "TRANSPORT",
      description: "",
      montant: 0,
    },
  });

  if (expense.isLoading) return <Skeleton className="h-40 w-full" />;
  if (!expense.data) {
    return (
      <Card>
        <CardContent className="flex items-start gap-3">
          <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
          <div className="text-sm">Failed to load expense report</div>
        </CardContent>
      </Card>
    );
  }

  const e = expense.data;
  const canEdit = e.status === "DRAFT";
  const canSubmit = e.status === "DRAFT";

  const onAddLine = (values: AddExpenseLineFormValues) => {
    addLine.mutate(
      {
        categorie: values.categorie,
        description: values.description ?? null,
        montant: Number(values.montant),
      } as never,
      {
        onSuccess: () => {
          toast.success("Ligne ajoutée");
          setOpen(false);
          form.reset({
            categorie: "TRANSPORT",
            description: "",
            montant: 0,
          });
        },
        onError: (err) => toast.error((err as Error).message),
      },
    );
  };

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-22"
        crumbs={[
          { label: tNav("items.myExpenses"), href: "/expenses/my" },
          { label: e.motif ?? "—" },
        ]}
        title={
          <div className="flex flex-wrap items-center gap-3">
            <span>{e.motif ?? "—"}</span>
            <StatusBadge kind="expense" status={e.status} />
          </div>
        }
        subtitle={`Total: ${fmt.money(e.totalMontant)}`}
        actions={
          <>
            {canEdit && (
              <Button variant="secondary" onClick={() => setOpen(true)}>
                <Plus className="size-4" />
                {t("detail.addLine")}
              </Button>
            )}
            {canSubmit && (
              <Button onClick={() => submit.mutate(id)} disabled={submit.isPending}>
                {submit.isPending ? <Loader2 className="size-4 animate-spin" /> : <Send className="size-4" />}
                {t("detail.submit")}
              </Button>
            )}
          </>
        }
      />

      {lines.isLoading && <Skeleton className="h-32 w-full" />}

      {!lines.isLoading && lines.data && lines.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("detail.linesEmpty")}</CardContent>
        </Card>
      )}

      {!lines.isLoading && lines.data && lines.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[t("detail.category"), t("detail.description"), t("detail.montant")].map((h, i) => (
                    <th key={i} className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {lines.data.map((l) => (
                  <tr key={l.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">
                      {t(`categories.${l.categorie}` as never)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">{l.description ?? "—"}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(l.montant)}</td>
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
            <DialogTitle>{t("detail.addLine")}</DialogTitle>
          </DialogHeader>
          <form onSubmit={form.handleSubmit(onAddLine)} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="categorie">{t("detail.category")}</Label>
                <Controller
                  control={form.control}
                  name="categorie"
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger id="categorie">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {CATEGORIES.map((c) => (
                          <SelectItem key={c} value={c}>
                            {t(`categories.${c}` as never)}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label htmlFor="description">{t("detail.description")}</Label>
                <Input id="description" {...form.register("description")} />
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label htmlFor="montant">{t("detail.montant")}</Label>
                <Input id="montant" type="number" min={1} step={100} {...form.register("montant")} />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
                Annuler
              </Button>
              <Button type="submit" disabled={addLine.isPending}>
                {addLine.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("detail.addLine")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
