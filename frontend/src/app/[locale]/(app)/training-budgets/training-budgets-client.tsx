"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2, Wallet, Receipt } from "lucide-react";
import { toast } from "sonner";
import {
  useTrainingBudgets,
  useCreateTrainingBudget,
  useBudgetTransition,
} from "@/hooks/modules/useTrainingBudgets";
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
  createTrainingBudgetSchema,
  montantSchema,
  type CreateTrainingBudgetFormValues,
  type MontantFormValues,
} from "@/lib/validation/hrm/training.schema";

export function TrainingBudgetsClient() {
  const t = useTranslations("trainingBudgets");
  const tCommon = useTranslations("common");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const [annee, setAnnee] = React.useState(new Date().getFullYear());
  const list = useTrainingBudgets(annee);
  const create = useCreateTrainingBudget(annee);
  const tx = useBudgetTransition(annee);
  const [openCreate, setOpenCreate] = React.useState(false);
  const [actionState, setActionState] = React.useState<{
    id: string;
    action: "engage" | "realiser";
  } | null>(null);

  const createForm = useForm<CreateTrainingBudgetFormValues>({
    resolver: zodResolver(createTrainingBudgetSchema),
    defaultValues: { annee, montantAlloue: "0" },
  });

  const txForm = useForm<MontantFormValues>({
    resolver: zodResolver(montantSchema),
    defaultValues: { montant: "0" },
  });

  React.useEffect(() => {
    createForm.setValue("annee", annee);
  }, [annee, createForm]);

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-14"
        crumbs={[{ label: tNav("items.trainingBudgets") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button onClick={() => setOpenCreate(true)}>
            <Plus className="size-4" />
            {t("list.newButton")}
          </Button>
        }
      />

      <Card>
        <CardContent className="flex items-center gap-3 p-4">
          <Label className="shrink-0">{t("list.filterYear")}</Label>
          <Input
            type="number"
            min="2000"
            max="2100"
            value={annee}
            onChange={(e) => setAnnee(Number(e.target.value) || annee)}
            className="max-w-[150px] tabular"
          />
        </CardContent>
      </Card>

      {!list.isLoading && list.data && list.data.length > 0 && (() => {
        const alloue = list.data.reduce((s, b) => s + Number(b.montantAlloue), 0);
        const engage = list.data.reduce((s, b) => s + Number(b.montantEngage), 0);
        const realise = list.data.reduce((s, b) => s + Number(b.montantRealise), 0);
        const consomme = engage + realise;
        const dispo = alloue - consomme;
        const pct = alloue > 0 ? Math.round((consomme / alloue) * 100) : 0;
        return (
          <Card>
            <CardContent className="flex flex-col items-center gap-6 p-5 sm:flex-row">
              <div className="flex-1">
                <div className="text-[11px] font-semibold uppercase tracking-[0.12em] text-ink-4">
                  {t("hero.annual", { year: annee })}
                </div>
                <div className="mt-1 font-display text-[34px] font-extrabold leading-none text-ink tabular">
                  {fmt.moneyShort(alloue)}
                </div>
                <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-[12.5px] text-ink-3">
                  <span>{fmt.moneyShort(consomme)} {t("hero.consumed")}</span>
                  <span>·</span>
                  <span>{fmt.moneyShort(dispo)} {t("hero.available")}</span>
                  <span>·</span>
                  <span className="font-semibold text-brand-600">{pct}% {t("hero.used")}</span>
                </div>
                <div className="mt-3 h-2.5 overflow-hidden rounded-full bg-cream-2">
                  <div className="h-full rounded-full bg-grad-orange" style={{ width: `${Math.min(100, pct)}%` }} />
                </div>
              </div>
              <div
                className="relative grid size-28 shrink-0 place-items-center rounded-full"
                style={{ background: `conic-gradient(var(--color-brand-500) ${pct}%, var(--color-cream-2) 0)` }}
              >
                <div className="grid size-20 place-items-center rounded-full bg-white">
                  <span className="font-display text-[20px] font-extrabold text-ink tabular">{pct}%</span>
                </div>
              </div>
            </CardContent>
          </Card>
        );
      })()}

      {list.isLoading && <Skeleton className="h-32 w-full" />}

      {!list.isLoading && list.data && list.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">
            {t("list.empty")}
          </CardContent>
        </Card>
      )}

      {!list.isLoading && list.data && list.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("list.table.annee"),
                    t("list.table.alloue"),
                    t("list.table.engage"),
                    t("list.table.realise"),
                    t("list.table.disponible"),
                    t("list.table.actions"),
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
                {list.data.map((b) => {
                  const dispo = Number(b.montantAlloue) - Number(b.montantEngage);
                  return (
                    <tr key={b.id}>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink tabular">
                        {b.annee}
                      </td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink tabular">
                        {fmt.money(b.montantAlloue)}
                      </td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                        {fmt.money(b.montantEngage)}
                      </td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                        {fmt.money(b.montantRealise)}
                      </td>
                      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] tabular">
                        <span className={dispo > 0 ? "text-status-green-600" : "text-status-red-600"}>
                          {fmt.money(dispo)}
                        </span>
                      </td>
                      <td className="border-b border-line-soft px-4 py-3 text-right">
                        <div className="flex justify-end gap-1.5">
                          <Button
                            size="sm"
                            onClick={() => {
                              txForm.reset({ montant: "0" });
                              setActionState({ id: b.id, action: "engage" });
                            }}
                          >
                            <Wallet className="size-4" />
                            {t("actions.engage")}
                          </Button>
                          <Button
                            size="sm"
                            variant="secondary"
                            onClick={() => {
                              txForm.reset({ montant: "0" });
                              setActionState({ id: b.id, action: "realiser" });
                            }}
                          >
                            <Receipt className="size-4" />
                            {t("actions.realiser")}
                          </Button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      <Dialog open={openCreate} onOpenChange={setOpenCreate}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("list.newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={createForm.handleSubmit((values) =>
              create.mutate(
                { annee: Number(values.annee), montantAlloue: values.montantAlloue },
                {
                  onSuccess: () => {
                    toast.success(t("form.submit"));
                    setOpenCreate(false);
                    createForm.reset({ annee, montantAlloue: "0" });
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="annee">{t("form.annee")}</Label>
              <Input id="annee" type="number" min="2000" max="2100" {...createForm.register("annee")} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="montantAlloue">{t("form.montantAlloue")}</Label>
              <Input id="montantAlloue" {...createForm.register("montantAlloue")} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpenCreate(false)}>
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

      <Dialog open={actionState !== null} onOpenChange={(o) => !o && setActionState(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {actionState?.action === "engage" ? t("dialog.engageTitle") : t("dialog.realiserTitle")}
            </DialogTitle>
          </DialogHeader>
          <form
            onSubmit={txForm.handleSubmit((values) => {
              if (!actionState) return;
              tx.mutate(
                {
                  id: actionState.id,
                  action: actionState.action,
                  body: { montant: values.montant },
                },
                {
                  onSuccess: () => {
                    toast.success(t("dialog.submit"));
                    setActionState(null);
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              );
            })}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="montant">{t("dialog.montant")}</Label>
              <Input id="montant" {...txForm.register("montant")} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setActionState(null)}>
                {tCommon("actions.cancel")}
              </Button>
              <Button type="submit" disabled={tx.isPending}>
                {tx.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("dialog.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
