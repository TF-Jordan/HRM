"use client";

import { useTranslations } from "next-intl";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useReactivateEmployee } from "@/hooks/modules/useEmployees";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

export function ReactivateDialog({
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
  const mutation = useReactivateEmployee(employeeId);

  const submit = () => {
    mutation.mutate(undefined, {
      onSuccess: () => {
        toast.success("Employé réactivé");
        onOpenChange(false);
      },
      onError: (err) => toast.error((err as Error).message),
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("transitions.reactivateTitle")}</DialogTitle>
          <DialogDescription>{t("transitions.reactivateDescription")}</DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
            {tCommon("actions.cancel" as never)}
          </Button>
          <Button onClick={submit} disabled={mutation.isPending}>
            {mutation.isPending && <Loader2 className="size-4 animate-spin" />}
            {t("detail.actions.reactivate")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
