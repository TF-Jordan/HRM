"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft, Plus, Loader2, Check, X as CloseIcon } from "lucide-react";
import { toast } from "sonner";
import {
  useTraining,
  useTrainingEnrollments,
  useEnrollEmployee,
  useEnrollmentTransition,
} from "@/hooks/modules/useTrainings";
import { useEmployees } from "@/hooks/modules/useEmployees";
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Link } from "@/i18n/navigation";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import {
  enrollEmployeeSchema,
  completeEnrollmentSchema,
  type EnrollEmployeeFormValues,
  type CompleteEnrollmentFormValues,
} from "@/lib/validation/hrm/training.schema";

export function TrainingDetailClient({ id }: { id: string }) {
  const t = useTranslations("trainings");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const training = useTraining(id);
  const enrollments = useTrainingEnrollments(id);
  const employees = useEmployees();
  const enroll = useEnrollEmployee(id);
  const tx = useEnrollmentTransition(id);
  const [openEnroll, setOpenEnroll] = React.useState(false);
  const [completeId, setCompleteId] = React.useState<string | null>(null);

  const enrollForm = useForm<EnrollEmployeeFormValues>({
    resolver: zodResolver(enrollEmployeeSchema),
    defaultValues: { employeeId: "" },
  });

  const completeForm = useForm<CompleteEnrollmentFormValues>({
    resolver: zodResolver(completeEnrollmentSchema),
    defaultValues: { note: "", attestationId: "" },
  });

  if (training.isLoading) return <Skeleton className="h-64 w-full" />;
  if (!training.data) return null;
  const tr = training.data;

  const employeeName = (employeeId: string) => {
    const e = employees.data?.find((x) => x.id === employeeId);
    return e ? `${e.matricule} — ${e.actorDisplayName}` : employeeId;
  };

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-13"
        crumbs={[{ label: t("title"), href: "/trainings" }, { label: tr.intitule }]}
        title={tr.intitule}
        subtitle={`${fmt.date(tr.dateDebut)} → ${fmt.date(tr.dateFin)}`}
        actions={
          <Link href="/trainings" className="inline-flex items-center gap-1.5 text-[13px] text-ink-3 hover:text-ink">
            <ArrowLeft className="size-4" />
            {t("detail.back")}
          </Link>
        }
      />

      <Tabs defaultValue="overview">
        <TabsList>
          <TabsTrigger value="overview">{t("detail.tabs.overview")}</TabsTrigger>
          <TabsTrigger value="enrollments">{t("detail.tabs.enrollments")}</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <Card>
            <CardContent className="grid grid-cols-2 gap-4 p-5 text-[13.5px] md:grid-cols-4">
              <Field label={t("list.table.organisme")} value={tr.organisme ?? "—"} />
              <Field label={t("list.table.lieu")} value={tr.lieu ?? "—"} />
              <Field label={t("list.table.places")} value={String(tr.nbPlaces)} />
              <Field label={t("list.table.cout")} value={fmt.money(tr.cout)} />
              <div className="space-y-1.5">
                <div className="text-[11px] font-semibold uppercase tracking-wide text-ink-4">
                  {t("list.table.status")}
                </div>
                <StatusBadge kind="training" status={tr.status} />
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="enrollments">
          <div className="mb-3 flex justify-end">
            <Button onClick={() => setOpenEnroll(true)}>
              <Plus className="size-4" />
              {t("enrollments.newButton")}
            </Button>
          </div>

          {enrollments.isLoading && <Skeleton className="h-32 w-full" />}
          {!enrollments.isLoading && enrollments.data && enrollments.data.length === 0 && (
            <Card>
              <CardContent className="py-8 text-center text-sm text-ink-3">
                {t("enrollments.empty")}
              </CardContent>
            </Card>
          )}
          {!enrollments.isLoading && enrollments.data && enrollments.data.length > 0 && (
            <Card>
              <CardContent className="p-0">
                <table className="w-full border-separate border-spacing-0">
                  <thead>
                    <tr>
                      {[
                        t("enrollments.table.employee"),
                        t("enrollments.table.status"),
                        t("enrollments.table.note"),
                        t("enrollments.table.actions"),
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
                    {enrollments.data.map((e) => (
                      <tr key={e.id}>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink">
                          {employeeName(e.employeeId)}
                        </td>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
                          <StatusBadge kind="trainingEnrollment" status={e.status} />
                        </td>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                          {e.noteEvaluation ?? "—"}
                        </td>
                        <td className="border-b border-line-soft px-4 py-3 text-right">
                          {e.status === "ENROLLED" && (
                            <div className="flex justify-end gap-1.5">
                              <Button
                                size="sm"
                                onClick={() => {
                                  completeForm.reset({ note: "", attestationId: "" });
                                  setCompleteId(e.id);
                                }}
                              >
                                <Check className="size-4" />
                                {t("enrollments.actions.complete")}
                              </Button>
                              <Button
                                size="sm"
                                variant="secondary"
                                onClick={() =>
                                  tx.mutate(
                                    { id: e.id, action: "cancel" },
                                    {
                                      onSuccess: () =>
                                        toast.success(t("enrollments.actions.cancel")),
                                      onError: (err) => toast.error((err as Error).message),
                                    },
                                  )
                                }
                              >
                                <CloseIcon className="size-4" />
                                {t("enrollments.actions.cancel")}
                              </Button>
                            </div>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>

      <Dialog open={openEnroll} onOpenChange={setOpenEnroll}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("enrollments.newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={enrollForm.handleSubmit((values) =>
              enroll.mutate(
                { employeeId: values.employeeId },
                {
                  onSuccess: () => {
                    toast.success(t("enrollments.form.submit"));
                    setOpenEnroll(false);
                    enrollForm.reset({ employeeId: "" });
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label>{t("enrollments.form.employee")}</Label>
              <Select
                value={enrollForm.watch("employeeId") ?? ""}
                onValueChange={(v) => enrollForm.setValue("employeeId", v)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="—" />
                </SelectTrigger>
                <SelectContent>
                  {(employees.data ?? []).map((e) => (
                    <SelectItem key={e.id} value={e.id}>
                      {e.matricule} — {e.actorDisplayName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpenEnroll(false)}>
                {tCommon("actions.cancel")}
              </Button>
              <Button type="submit" disabled={enroll.isPending}>
                {enroll.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("enrollments.form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Dialog open={completeId !== null} onOpenChange={(o) => !o && setCompleteId(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("enrollments.completeDialog.title")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={completeForm.handleSubmit((values) => {
              if (!completeId) return;
              tx.mutate(
                {
                  id: completeId,
                  action: "complete",
                  body: { note: values.note ?? null, attestationId: values.attestationId ?? null },
                },
                {
                  onSuccess: () => {
                    toast.success(t("enrollments.actions.complete"));
                    setCompleteId(null);
                    completeForm.reset({ note: "", attestationId: "" });
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              );
            })}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="note">{t("enrollments.completeDialog.note")}</Label>
              <Input id="note" {...completeForm.register("note")} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="attestationId">{t("enrollments.completeDialog.attestation")}</Label>
              <Input id="attestationId" {...completeForm.register("attestationId")} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setCompleteId(null)}>
                {tCommon("actions.cancel")}
              </Button>
              <Button type="submit" disabled={tx.isPending}>
                {tx.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("enrollments.completeDialog.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div className="space-y-1.5">
      <div className="text-[11px] font-semibold uppercase tracking-wide text-ink-4">{label}</div>
      <div className="text-ink">{value}</div>
    </div>
  );
}
