"use client";

import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useSuspendEmployee } from "@/hooks/modules/useEmployees";
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
import { suspendEmployeeSchema } from "@/lib/validation/hrm/employee.schema";
import type { SuspendEmployeeInput } from "@/lib/types/hrm/employee";

export function SuspendDialog({
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
  const mutation = useSuspendEmployee(employeeId);
  const form = useForm<SuspendEmployeeInput>({
    resolver: zodResolver(suspendEmployeeSchema),
    defaultValues: { reason: "" },
  });

  const onSubmit = (values: SuspendEmployeeInput) => {
    mutation.mutate(values, {
      onSuccess: () => {
        toast.success("Employé suspendu");
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
          <DialogTitle>{t("transitions.suspendTitle")}</DialogTitle>
          <DialogDescription>{t("transitions.suspendDescription")}</DialogDescription>
        </DialogHeader>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1.5">
            <Label>{t("transitions.suspendReason")}</Label>
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
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending && <Loader2 className="size-4 animate-spin" />}
              {t("detail.actions.suspend")}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
