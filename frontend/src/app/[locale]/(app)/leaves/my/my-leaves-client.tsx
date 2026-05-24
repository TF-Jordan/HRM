"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2, X, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import { useMyEmployee } from "@/hooks/modules/useMe";
import {
  useEmployeeLeaves,
  useSubmitLeave,
  useCancelLeave,
} from "@/hooks/modules/useLeaves";
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
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import {
  submitLeaveSchema,
  type SubmitLeaveFormValues,
} from "@/lib/validation/hrm/leave.schema";
import type { LeaveRequest } from "@/lib/types/hrm/leave-request";

export function MyLeavesClient() {
  const t = useTranslations("selfService.leaves");
  const tNav = useTranslations("navigation");
  const tStatuses = useTranslations("statuses.leaveType");
  const fmt = useFormat();
  const me = useMyEmployee();
  const employeeId = me.data?.id;
  const leaves = useEmployeeLeaves(employeeId);
  const cancel = useCancelLeave(employeeId);
  const [open, setOpen] = React.useState(false);

  if (me.isLoading) return <Skeleton className="h-40 w-full" />;
  if (me.isError) return <NotEmployeeCard />;

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-09"
        crumbs={[{ label: tNav("items.myLeaves") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus className="size-4" />
            {t("newButton")}
          </Button>
        }
      />

      {leaves.isLoading && <Skeleton className="h-32 w-full" />}

      {!leaves.isLoading && leaves.data && leaves.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!leaves.isLoading && leaves.data && leaves.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("table.type"),
                    t("table.dateDebut"),
                    t("table.dateFin"),
                    t("table.jours"),
                    t("table.status"),
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
                {leaves.data.map((l) => (
                  <Row key={l.id} l={l} onCancel={() => cancel.mutate(l.id)} t={t} tStatuses={tStatuses} fmt={fmt} />
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      <SubmitLeaveDialog
        employeeId={employeeId}
        open={open}
        onOpenChange={setOpen}
      />
    </div>
  );
}

function Row({
  l,
  onCancel,
  t,
  tStatuses,
  fmt,
}: {
  l: LeaveRequest;
  onCancel: () => void;
  t: (key: string) => string;
  tStatuses: (key: string) => string;
  fmt: { date: (v: string) => string; number: (v: number) => string };
}) {
  const canCancel = l.status === "PENDING" || l.status === "APPROVED";
  return (
    <tr>
      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink last:border-b-0">
        {tStatuses(l.type)}
      </td>
      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">{fmt.date(l.dateDebut)}</td>
      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">{fmt.date(l.dateFin)}</td>
      <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">
        {fmt.number(Number(l.nbJours))}
      </td>
      <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
        <StatusBadge kind="leave" status={l.status} />
      </td>
      <td className="border-b border-line-soft px-4 py-3 text-right last:border-b-0">
        {canCancel && (
          <Button variant="ghost" size="sm" onClick={onCancel} aria-label={t("form.cancel")}>
            <X className="size-4" />
          </Button>
        )}
      </td>
    </tr>
  );
}

function SubmitLeaveDialog({
  employeeId,
  open,
  onOpenChange,
}: {
  employeeId: string | undefined;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const t = useTranslations("selfService.leaves");
  const tCommon = useTranslations("common");
  const tStatuses = useTranslations("statuses.leaveType");
  const mutation = useSubmitLeave(employeeId);
  const form = useForm<SubmitLeaveFormValues>({
    resolver: zodResolver(submitLeaveSchema),
    defaultValues: {
      type: "ANNUAL",
      dateDebut: new Date().toISOString().slice(0, 10),
      dateFin: new Date().toISOString().slice(0, 10),
      motif: "",
    },
  });

  const onSubmit = (values: SubmitLeaveFormValues) => {
    if (!employeeId) return;
    mutation.mutate(
      {
        employeeId,
        type: values.type,
        dateDebut: values.dateDebut,
        dateFin: values.dateFin,
        motif: values.motif ?? null,
      },
      {
        onSuccess: () => {
          toast.success("Demande soumise");
          onOpenChange(false);
          form.reset();
        },
        onError: (err) => toast.error((err as Error).message),
      },
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("newButton")}</DialogTitle>
          <DialogDescription>{t("subtitle")}</DialogDescription>
        </DialogHeader>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="leaveType">{t("form.type")}</Label>
            <Controller
              control={form.control}
              name="type"
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger id="leaveType">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {(["ANNUAL", "SICK", "MATERNITY", "PATERNITY", "UNPAID", "SPECIAL"] as const).map((v) => (
                      <SelectItem key={v} value={v}>
                        {tStatuses(v)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="dateDebut">{t("form.dateDebut")}</Label>
              <Input id="dateDebut" type="date" min={new Date().toISOString().slice(0, 10)} {...form.register("dateDebut")} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="dateFin">{t("form.dateFin")}</Label>
              <Input id="dateFin" type="date" {...form.register("dateFin")} />
              {form.formState.errors.dateFin && (
                <p className="text-[12px] text-status-red-600">
                  {form.formState.errors.dateFin.message}
                </p>
              )}
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="motif">{t("form.motif")}</Label>
            <Input id="motif" {...form.register("motif")} />
          </div>
          {mutation.isError && (
            <div className="flex items-start gap-2 rounded-xl bg-status-red-50 px-3 py-2 text-status-red-600">
              <AlertTriangle className="size-4 shrink-0" />
              <span className="text-[12.5px]">{(mutation.error as Error).message}</span>
            </div>
          )}
          <DialogFooter>
            <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
              {tCommon("actions.cancel" as never)}
            </Button>
            <Button type="submit" disabled={mutation.isPending}>
              {mutation.isPending && <Loader2 className="size-4 animate-spin" />}
              {t("form.submit")}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
