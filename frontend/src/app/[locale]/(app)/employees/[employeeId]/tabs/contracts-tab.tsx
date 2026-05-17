"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useContracts, useAddContract } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import {
  addContractSchema,
  type AddContractFormValues,
} from "@/lib/validation/hrm/contract.schema";

export function ContractsTab({ employeeId }: { employeeId: string }) {
  const t = useTranslations("employees");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const { data, isLoading } = useContracts(employeeId);
  const [open, setOpen] = React.useState(false);

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button onClick={() => setOpen(true)}>
          <Plus className="size-4" />
          {t("detail.actions.addContract")}
        </Button>
      </div>

      {isLoading && <Skeleton className="h-24 w-full" />}

      {!isLoading && data && data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">
            {t("contracts.empty")}
          </CardContent>
        </Card>
      )}

      {!isLoading && data && data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("contracts.type"),
                    t("contracts.dateDebut"),
                    t("contracts.dateFin"),
                    t("contracts.salaireBase"),
                    t("contracts.status"),
                  ].map((h) => (
                    <th
                      key={h}
                      className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {data.map((c) => (
                  <tr key={c.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink last:border-b-0">
                      {c.type}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.date(c.dateDebut)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {c.dateFin ? fmt.date(c.dateFin) : "—"}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">
                      {fmt.money(c.salaireBase)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
                      <StatusBadge kind="contract" status={c.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      <AddContractDialog
        employeeId={employeeId}
        open={open}
        onOpenChange={setOpen}
        labels={{ submit: t("contracts.submit"), cancel: tCommon("actions.cancel" as never) }}
      />
    </div>
  );
}

function AddContractDialog({
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
  const mutation = useAddContract(employeeId);
  const form = useForm<AddContractFormValues>({
    resolver: zodResolver(addContractSchema),
    defaultValues: {
      type: "CDI",
      dateDebut: new Date().toISOString().slice(0, 10),
      dateFin: null,
      salaireBase: 0,
      avantagesNature: null,
      periodeEssai: null,
    },
  });

  const onSubmit = (values: AddContractFormValues) => {
    mutation.mutate(values as never, {
      onSuccess: () => {
        toast.success("Contrat créé");
        onOpenChange(false);
        form.reset();
      },
      onError: (err) => toast.error((err as Error).message),
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("contracts.newTitle")}</DialogTitle>
          <DialogDescription>{t("detail.tabs.contracts")}</DialogDescription>
        </DialogHeader>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <Label>{t("form.contractType")}</Label>
              <Controller
                control={form.control}
                name="type"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="CDI">CDI</SelectItem>
                      <SelectItem value="CDD">CDD</SelectItem>
                      <SelectItem value="STAGE">Stage</SelectItem>
                      <SelectItem value="INTERIM">Intérim</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </div>
            <div className="space-y-1.5">
              <Label>{t("form.dateEmbauche")}</Label>
              <Input type="date" {...form.register("dateDebut")} />
            </div>
            <div className="space-y-1.5">
              <Label>{t("form.contractDateFin")}</Label>
              <Input type="date" {...form.register("dateFin")} />
              {form.formState.errors.dateFin && (
                <p className="text-[12px] text-status-red-600">
                  {form.formState.errors.dateFin.message}
                </p>
              )}
            </div>
            <div className="space-y-1.5">
              <Label>{t("form.salaireBase")}</Label>
              <Input type="number" min={0} {...form.register("salaireBase")} />
            </div>
            <div className="space-y-1.5">
              <Label>{t("form.avantagesNature")}</Label>
              <Input type="number" min={0} {...form.register("avantagesNature")} />
            </div>
            <div className="space-y-1.5">
              <Label>{t("form.periodeEssai")}</Label>
              <Input type="number" min={0} {...form.register("periodeEssai")} />
            </div>
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
