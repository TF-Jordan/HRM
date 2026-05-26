"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useDependents, useAddDependent } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field } from "@/components/ui-tokens/form-kit";
import {
  Dialog,
  DialogContent,
  DialogDescription,
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
import { Skeleton } from "@/components/ui/skeleton";
import {
  addDependentSchema,
  type AddDependentFormValues,
} from "@/lib/validation/hrm/dependent.schema";

export function DependentsTab({ employeeId }: { employeeId: string }) {
  const t = useTranslations("employees");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const { data, isLoading } = useDependents(employeeId);
  const [open, setOpen] = React.useState(false);

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button onClick={() => setOpen(true)}>
          <Plus className="size-4" />
          {t("detail.actions.addDependent")}
        </Button>
      </div>

      {isLoading && <Skeleton className="h-24 w-full" />}

      {!isLoading && data && data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">
            {t("dependents.empty")}
          </CardContent>
        </Card>
      )}

      {!isLoading && data && data.length > 0 && (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          {data.map((d) => (
            <Card key={d.id}>
              <CardContent>
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="font-display text-base font-bold text-ink">
                      {d.prenom} {d.nom}
                    </div>
                    <div className="mt-1 text-[12px] text-ink-3">
                      {t(`dependents.relationships.${d.lienParente}` as never)} •{" "}
                      {fmt.date(d.dateNaissance)}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <AddDependentDialog
        employeeId={employeeId}
        open={open}
        onOpenChange={setOpen}
        labels={{ submit: t("dependents.submit"), cancel: tCommon("actions.cancel" as never) }}
      />
    </div>
  );
}

function AddDependentDialog({
  employeeId,
  open,
  onOpenChange,
  labels,
}: {
  employeeId: string;
  open: boolean;
  onOpenChange: (v: boolean) => void;
  labels: { submit: string; cancel: string };
}) {
  const t = useTranslations("employees");
  const mutation = useAddDependent(employeeId);
  const form = useForm<AddDependentFormValues>({
    resolver: zodResolver(addDependentSchema),
    defaultValues: { nom: "", prenom: "", dateNaissance: "", lienParente: "CHILD" },
  });

  const onSubmit = (values: AddDependentFormValues) => {
    mutation.mutate(values as never, {
      onSuccess: () => {
        toast.success("Ajouté");
        onOpenChange(false);
        form.reset();
      },
      onError: (err) => toast.error((err as Error).message),
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-xl">
        <DialogHeader>
          <DialogTitle>{t("dependents.newTitle")}</DialogTitle>
          <DialogDescription>{t("dependents.newSubtitle")}</DialogDescription>
        </DialogHeader>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field label={t("dependents.nom")} code="nom" required>
              <Input {...form.register("nom")} />
            </Field>
            <Field label={t("dependents.prenom")} code="prenom" required>
              <Input {...form.register("prenom")} />
            </Field>
            <Field label={t("dependents.dateNaissance")} code="dateNaissance" required>
              <Input type="date" max={new Date().toISOString().slice(0, 10)} {...form.register("dateNaissance")} />
            </Field>
            <Field label={t("dependents.lienParente")} code="lienParente" required>
              <Controller
                control={form.control}
                name="lienParente"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {(["SPOUSE", "CHILD", "PARENT", "SIBLING", "OTHER"] as const).map((v) => (
                        <SelectItem key={v} value={v}>
                          {t(`dependents.relationships.${v}` as never)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
            </Field>
          </div>
          <div className="rounded-[12px] border border-status-amber-500/30 bg-status-amber-50 p-3 text-[12px] text-ink-2">
            <div className="font-semibold text-status-amber-600">{t("dependents.fiscalImpactTitle")}</div>
            <p className="mt-1 text-ink-3">{t("dependents.fiscalImpactText")}</p>
          </div>
          <DialogFooter>
            <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
              {labels.cancel}
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending && <Loader2 className="size-4 animate-spin" />}
              {labels.submit}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
