"use client";

import { useTranslations } from "next-intl";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useUpdateEmployee } from "@/hooks/modules/useEmployees";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  updateEmployeeSchema,
  type UpdateEmployeeFormValues,
} from "@/lib/validation/hrm/employee.schema";
import type { Employee } from "@/lib/types/hrm/employee";

const PAYMENT_MODES = ["BANK_TRANSFER", "MTN_MOBILE_MONEY", "ORANGE_MONEY", "CASH"] as const;
const OPERATORS = ["MTN", "ORANGE", "EU_MOBILE", "YOOMEE"] as const;

export function EditEmployeeDialog({
  employee,
  open,
  onOpenChange,
}: {
  employee: Employee;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const t = useTranslations("employees");
  const tCommon = useTranslations("common");
  const mutation = useUpdateEmployee(employee.id);

  const form = useForm<UpdateEmployeeFormValues>({
    resolver: zodResolver(updateEmployeeSchema),
    defaultValues: {
      numCnps: employee.numCnps ?? "",
      categorie: employee.categorie,
      echelon: employee.echelon ?? "",
      poste: employee.poste ?? "",
      departmentCode: employee.departmentCode ?? "",
      modePaiement: employee.modePaiement,
      compteBancaire: employee.compteBancaire ?? "",
      numMobileMoney: employee.numMobileMoney ?? "",
      operateurMm: employee.operateurMm,
    },
  });

  const onSubmit = (values: UpdateEmployeeFormValues) => {
    mutation.mutate(values as never, {
      onSuccess: () => {
        toast.success(t("detail.toast.updated"));
        onOpenChange(false);
      },
      onError: (err) => toast.error((err as Error).message),
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>{t("detail.actions.edit")}</DialogTitle>
        </DialogHeader>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field label={t("form.poste")}>
              <Input {...form.register("poste")} />
            </Field>
            <Field label={t("form.departmentCode")}>
              <Input {...form.register("departmentCode")} />
            </Field>
            <Field label={t("form.categorie")}>
              <Input type="number" min={1} max={20} {...form.register("categorie")} />
            </Field>
            <Field label={t("form.echelon")}>
              <Input {...form.register("echelon")} />
            </Field>
            <Field label={t("form.numCnps")}>
              <Input {...form.register("numCnps")} />
            </Field>
            <Field label={t("form.modePaiement")}>
              <Controller
                control={form.control}
                name="modePaiement"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {PAYMENT_MODES.map((m) => (
                        <SelectItem key={m} value={m}>
                          {t(`paymentModes.${m}` as never)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
            </Field>
            <Field label={t("form.compteBancaire")}>
              <Input {...form.register("compteBancaire")} />
            </Field>
            <Field label={t("form.numMobileMoney")}>
              <Input {...form.register("numMobileMoney")} />
            </Field>
            <Field label={t("form.operateurMm")}>
              <Controller
                control={form.control}
                name="operateurMm"
                render={({ field }) => (
                  <Select
                    value={field.value ?? ""}
                    onValueChange={(v) => field.onChange(v === "" ? null : v)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="—" />
                    </SelectTrigger>
                    <SelectContent>
                      {OPERATORS.map((o) => (
                        <SelectItem key={o} value={o}>
                          {t(`operators.${o}` as never)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
            </Field>
          </div>
          <DialogFooter>
            <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
              {tCommon("actions.cancel" as never)}
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending && <Loader2 className="size-4 animate-spin" />}
              {tCommon("actions.save" as never)}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1.5">
      <Label>{label}</Label>
      {children}
    </div>
  );
}
