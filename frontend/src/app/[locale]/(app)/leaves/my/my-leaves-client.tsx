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
import { useLeaveBalances } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { NotEmployeeCard } from "@/components/self-service/NotEmployeeCard";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import { RadioCardGroup, type RadioCardOption } from "@/components/ui-tokens/form-kit";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { cn } from "@/lib/utils";
import {
  submitLeaveSchema,
  type SubmitLeaveFormValues,
} from "@/lib/validation/hrm/leave.schema";
import type { LeaveRequest } from "@/lib/types/hrm/leave-request";
import type { LeaveBalance, LeaveType } from "@/lib/types/hrm/leave-balance";

export function MyLeavesClient() {
  const t = useTranslations("selfService.leaves");
  const tNav = useTranslations("navigation");
  const tStatuses = useTranslations("statuses.leaveType");
  const fmt = useFormat();
  const year = new Date().getFullYear();
  const me = useMyEmployee();
  const employeeId = me.data?.id;
  const leaves = useEmployeeLeaves(employeeId);
  const balances = useLeaveBalances(employeeId, year);
  const cancel = useCancelLeave(employeeId);
  const [open, setOpen] = React.useState(false);

  const annual = balances.data?.find((b) => b.type === "ANNUAL") ?? null;
  const annualRemaining = annual ? Number(annual.soldeRestant) : 0;

  if (me.isLoading) return <Skeleton className="h-40 w-full" />;
  if (me.isError) return <NotEmployeeCard />;

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
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

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[340px_1fr]">
        <BalancesCard balances={balances.data} isLoading={balances.isLoading} t={t} tStatuses={tStatuses} />

        <Card>
          <CardHeader>
            <CardTitle>{t("history")}</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            {leaves.isLoading ? (
              <div className="p-4">
                <Skeleton className="h-32 w-full" />
              </div>
            ) : !leaves.data || leaves.data.length === 0 ? (
              <div className="py-10 text-center text-sm text-ink-3">{t("empty")}</div>
            ) : (
              <table className="w-full border-collapse text-[13px]">
                <thead>
                  <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                    <th className="px-4 py-2.5 text-left font-semibold">{t("table.type")}</th>
                    <th className="px-3 py-2.5 text-left font-semibold">{t("table.dateDebut")}</th>
                    <th className="px-3 py-2.5 text-left font-semibold">{t("table.dateFin")}</th>
                    <th className="px-3 py-2.5 text-right font-semibold">{t("table.jours")}</th>
                    <th className="px-3 py-2.5 text-left font-semibold">{t("table.status")}</th>
                    <th className="w-10 px-3 py-2.5" />
                  </tr>
                </thead>
                <tbody>
                  {leaves.data.map((l) => (
                    <Row
                      key={l.id}
                      l={l}
                      onCancel={() => cancel.mutate(l.id)}
                      cancelLabel={t("form.cancel")}
                      typeLabel={tStatuses(l.type)}
                      fmt={fmt}
                    />
                  ))}
                </tbody>
              </table>
            )}
          </CardContent>
        </Card>
      </div>

      <SubmitLeaveDialog
        employeeId={employeeId}
        annualRemaining={annualRemaining}
        open={open}
        onOpenChange={setOpen}
      />
    </div>
  );
}

/* -------------------------------------------------------------------------- */

function BalancesCard({
  balances,
  isLoading,
  t,
  tStatuses,
}: {
  balances: LeaveBalance[] | undefined;
  isLoading: boolean;
  t: (k: string) => string;
  tStatuses: (k: string) => string;
}) {
  const annual = balances?.find((b) => b.type === "ANNUAL");
  const acquired = annual ? Number(annual.acquis) : 0;
  const remaining = annual ? Number(annual.soldeRestant) : 0;
  const pct = acquired > 0 ? Math.round((remaining / acquired) * 100) : 0;

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("balances.title")}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-5">
        {isLoading ? (
          <Skeleton className="mx-auto size-32 rounded-full" />
        ) : (
          <>
            <div className="flex justify-center">
              <div
                className="relative grid size-36 place-items-center rounded-full"
                style={{
                  background: `conic-gradient(var(--color-brand-500) ${pct}%, var(--color-cream-2) 0)`,
                }}
              >
                <div className="grid size-28 place-items-center rounded-full bg-white text-center">
                  <div>
                    <div className="font-display text-[28px] font-extrabold leading-none text-ink tabular">
                      {remaining.toFixed(1)}
                    </div>
                    <div className="mt-1 px-2 text-[10.5px] leading-tight text-ink-4">
                      {t("balances.available")}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="space-y-1.5">
              {(balances ?? [])
                .slice()
                .sort((a, b) => a.type.localeCompare(b.type))
                .map((b) => (
                  <div
                    key={b.id}
                    className="flex items-center justify-between rounded-[10px] border border-line-soft px-3 py-2"
                  >
                    <span className="flex items-center gap-2 text-[12.5px] text-ink-2">
                      <span className="size-2 rounded-full bg-brand-400" />
                      {tStatuses(b.type)}
                    </span>
                    <span className="font-semibold text-ink tabular">
                      {Number(b.soldeRestant).toFixed(1)} j
                      <span className="ml-1 text-[11px] font-normal text-ink-4">
                        / {Number(b.acquis).toFixed(0)}
                      </span>
                    </span>
                  </div>
                ))}
              {(!balances || balances.length === 0) && (
                <p className="py-2 text-center text-[12.5px] text-ink-4">—</p>
              )}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}

function Row({
  l,
  onCancel,
  cancelLabel,
  typeLabel,
  fmt,
}: {
  l: LeaveRequest;
  onCancel: () => void;
  cancelLabel: string;
  typeLabel: string;
  fmt: { date: (v: string) => string; number: (v: number) => string };
}) {
  const canCancel = l.status === "PENDING" || l.status === "APPROVED";
  return (
    <tr className="border-b border-line-soft/70 last:border-0">
      <td className="px-4 py-2.5 font-medium text-ink">{typeLabel}</td>
      <td className="px-3 py-2.5 text-ink-2 tabular">{fmt.date(l.dateDebut)}</td>
      <td className="px-3 py-2.5 text-ink-2 tabular">{fmt.date(l.dateFin)}</td>
      <td className="px-3 py-2.5 text-right text-ink-2 tabular">{fmt.number(Number(l.nbJours))}</td>
      <td className="px-3 py-2.5">
        <StatusBadge kind="leave" status={l.status} />
      </td>
      <td className="px-3 py-2.5 text-right">
        {canCancel && (
          <Button variant="ghost" size="sm" onClick={onCancel} aria-label={cancelLabel}>
            <X className="size-4" />
          </Button>
        )}
      </td>
    </tr>
  );
}

function diffDaysInclusive(start: string, end: string): number {
  const s = new Date(start).getTime();
  const e = new Date(end).getTime();
  if (Number.isNaN(s) || Number.isNaN(e) || e < s) return 0;
  return Math.floor((e - s) / 86_400_000) + 1;
}

function SubmitLeaveDialog({
  employeeId,
  annualRemaining,
  open,
  onOpenChange,
}: {
  employeeId: string | undefined;
  annualRemaining: number;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const t = useTranslations("selfService.leaves");
  const tCommon = useTranslations("common");
  const tStatuses = useTranslations("statuses.leaveType");
  const mutation = useSubmitLeave(employeeId);
  const today = new Date().toISOString().slice(0, 10);
  const form = useForm<SubmitLeaveFormValues>({
    resolver: zodResolver(submitLeaveSchema),
    defaultValues: { type: "ANNUAL", dateDebut: today, dateFin: today, motif: "" },
  });

  const type = form.watch("type");
  const dateDebut = form.watch("dateDebut");
  const dateFin = form.watch("dateFin");
  const requested = diffDaysInclusive(dateDebut, dateFin);
  const balanceAfter = annualRemaining - requested;

  const typeOptions: RadioCardOption<LeaveType>[] = (
    ["ANNUAL", "SICK", "MATERNITY", "PATERNITY", "UNPAID", "SPECIAL"] as const
  ).map((v) => ({ value: v, label: tStatuses(v) }));

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
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>{t("newButton")}</DialogTitle>
          <DialogDescription>{t("subtitle")}</DialogDescription>
        </DialogHeader>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1.5">
            <Label>{t("form.type")}</Label>
            <Controller
              control={form.control}
              name="type"
              render={({ field }) => (
                <RadioCardGroup
                  value={field.value}
                  onChange={field.onChange}
                  options={typeOptions}
                  cols={3}
                />
              )}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="dateDebut">{t("form.dateDebut")}</Label>
              <Input id="dateDebut" type="date" min={today} {...form.register("dateDebut")} />
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

          {/* Live recap */}
          <div className="grid grid-cols-3 gap-2 rounded-[12px] bg-cream-soft/50 p-3 text-center">
            <Recap label={t("recap.requested")} value={`${requested} ${t("recap.days")}`} />
            <Recap label={tStatuses(type)} value="" muted />
            <Recap
              label={t("recap.balanceAfter")}
              value={`${balanceAfter.toFixed(1)} j`}
              danger={balanceAfter < 0}
            />
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

function Recap({
  label,
  value,
  danger,
  muted,
}: {
  label: string;
  value: string;
  danger?: boolean;
  muted?: boolean;
}) {
  return (
    <div>
      <div className={cn("text-[10.5px] uppercase tracking-wider text-ink-4", muted && "normal-case")}>
        {label}
      </div>
      {value && (
        <div
          className={cn(
            "mt-0.5 font-display text-[16px] font-extrabold tabular",
            danger ? "text-status-red-600" : "text-ink",
          )}
        >
          {value}
        </div>
      )}
    </div>
  );
}
