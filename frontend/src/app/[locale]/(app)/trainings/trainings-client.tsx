"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2, Play, Check, X as CloseIcon } from "lucide-react";
import { toast } from "sonner";
import {
  useTrainings,
  usePlanTraining,
  useTrainingTransition,
} from "@/hooks/modules/useTrainings";
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
import { Link } from "@/i18n/navigation";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import {
  planTrainingSchema,
  type PlanTrainingFormValues,
} from "@/lib/validation/hrm/training.schema";

export function TrainingsClient() {
  const t = useTranslations("trainings");
  const tNav = useTranslations("navigation");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const list = useTrainings();
  const plan = usePlanTraining();
  const tx = useTrainingTransition();
  const [open, setOpen] = React.useState(false);

  const form = useForm<PlanTrainingFormValues>({
    resolver: zodResolver(planTrainingSchema),
    defaultValues: {
      intitule: "",
      organisme: "",
      dateDebut: "",
      dateFin: "",
      cout: "0",
      nbPlaces: 1,
      lieu: "",
    },
  });

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-13"
        crumbs={[{ label: tNav("items.trainings") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus className="size-4" />
            {t("list.newButton")}
          </Button>
        }
      />

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
                    t("list.table.intitule"),
                    t("list.table.organisme"),
                    t("list.table.dates"),
                    t("list.table.lieu"),
                    t("list.table.places"),
                    t("list.table.cout"),
                    t("list.table.status"),
                    "",
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
                {list.data.map((tr) => (
                  <tr key={tr.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">
                      <Link
                        href={`/trainings/${tr.id}` as never}
                        className="hover:text-brand-700 hover:underline"
                      >
                        {tr.intitule}
                      </Link>
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">
                      {tr.organisme ?? "—"}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.date(tr.dateDebut)} → {fmt.date(tr.dateFin)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">
                      {tr.lieu ?? "—"}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {tr.nbPlaces}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.money(tr.cout)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
                      <StatusBadge kind="training" status={tr.status} />
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-right">
                      <div className="flex justify-end gap-1.5">
                        {tr.status === "PLANNED" && (
                          <Button
                            size="sm"
                            onClick={() =>
                              tx.mutate(
                                { id: tr.id, action: "start" },
                                {
                                  onSuccess: () => toast.success(t("actions.start")),
                                  onError: (err) => toast.error((err as Error).message),
                                },
                              )
                            }
                          >
                            <Play className="size-4" />
                            {t("actions.start")}
                          </Button>
                        )}
                        {tr.status === "IN_PROGRESS" && (
                          <Button
                            size="sm"
                            onClick={() =>
                              tx.mutate(
                                { id: tr.id, action: "complete" },
                                {
                                  onSuccess: () => toast.success(t("actions.complete")),
                                  onError: (err) => toast.error((err as Error).message),
                                },
                              )
                            }
                          >
                            <Check className="size-4" />
                            {t("actions.complete")}
                          </Button>
                        )}
                        {(tr.status === "PLANNED" || tr.status === "IN_PROGRESS") && (
                          <Button
                            size="sm"
                            variant="secondary"
                            onClick={() =>
                              tx.mutate(
                                { id: tr.id, action: "cancel" },
                                {
                                  onSuccess: () => toast.success(t("actions.cancel")),
                                  onError: (err) => toast.error((err as Error).message),
                                },
                              )
                            }
                          >
                            <CloseIcon className="size-4" />
                            {t("actions.cancel")}
                          </Button>
                        )}
                      </div>
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
              plan.mutate(
                {
                  intitule: values.intitule,
                  organisme: values.organisme ?? null,
                  dateDebut: values.dateDebut,
                  dateFin: values.dateFin,
                  cout: values.cout,
                  nbPlaces: Number(values.nbPlaces),
                  lieu: values.lieu ?? null,
                } as never,
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
                <Label htmlFor="intitule">{t("form.intitule")}</Label>
                <Input id="intitule" {...form.register("intitule")} />
              </div>
              <div className="space-y-1.5 col-span-2">
                <Label htmlFor="organisme">{t("form.organisme")}</Label>
                <Input id="organisme" {...form.register("organisme")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="dateDebut">{t("form.dateDebut")}</Label>
                <Input id="dateDebut" type="date" {...form.register("dateDebut")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="dateFin">{t("form.dateFin")}</Label>
                <Input id="dateFin" type="date" {...form.register("dateFin")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="cout">{t("form.cout")}</Label>
                <Input id="cout" {...form.register("cout")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="nbPlaces">{t("form.nbPlaces")}</Label>
                <Input id="nbPlaces" type="number" min="1" {...form.register("nbPlaces")} />
              </div>
              <div className="space-y-1.5 col-span-2">
                <Label htmlFor="lieu">{t("form.lieu")}</Label>
                <Input id="lieu" {...form.register("lieu")} />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
                {tCommon("actions.cancel")}
              </Button>
              <Button type="submit" disabled={plan.isPending}>
                {plan.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
