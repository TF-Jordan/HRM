"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useTerminateEmployee } from "@/hooks/modules/useEmployees";
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
import { terminateEmployeeSchema } from "@/lib/validation/hrm/employee.schema";
import type { TerminateEmployeeInput } from "@/lib/types/hrm/employee";

export function TerminateDialog({
  employeeId,
  open,
  onOpenChange,
}: {
  employeeId: string;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const t = useTranslations("employees");
  const tCommon = useTranslations("common");
  const mutation = useTerminateEmployee(employeeId);
  const form = useForm<TerminateEmployeeInput>({
    resolver: zodResolver(terminateEmployeeSchema),
    defaultValues: { terminationDate: new Date().toISOString().slice(0, 10), reason: "" },
  });

  const onSubmit = (values: TerminateEmployeeInput) => {
    mutation.mutate(values, {
      onSuccess: () => {
        toast.success("Employé résilié");
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
          <DialogTitle>{t("transitions.terminateTitle")}</DialogTitle>
          <DialogDescription>{t("transitions.terminateDescription")}</DialogDescription>
        </DialogHeader>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1.5">
            <Label>{t("transitions.terminationDate")}</Label>
            <Input type="date" {...form.register("terminationDate")} />
          </div>
          <div className="space-y-1.5">
            <Label>{t("transitions.terminationReason")}</Label>
            <Input {...form.register("reason")} />
            {form.formState.errors.reason && (
              <p className="text-[12px] text-status-red-600">
                {form.formState.errors.reason.message}
              </p>
            )}
          </div>
          <DialogFooter>
            <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
              {tCommon("actions.cancel" as never)}
            </Button>
            <Button type="submit" variant="destructive" disabled={mutation.isPending}>
              {mutation.isPending && <Loader2 className="size-4 animate-spin" />}
              {t("detail.actions.terminate")}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
