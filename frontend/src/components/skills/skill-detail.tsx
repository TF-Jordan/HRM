"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ChevronLeft, Loader2, Plus, Sparkles, Star } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Label } from "@/components/ui/input";
import { useCan } from "@/hooks/use-can";
import { AppLink as Link } from "@/components/ui/app-link";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { categoryTone } from "@/lib/skill-aggregates";
import { cn } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";
import type {
  CreateEmployeeSkillRequest,
  EmployeeSkillResponse,
  SkillResponse,
} from "@/server/ksm/modules/skills";

export function SkillDetail({ skillId }: { skillId: string }) {
  const t = useTranslations("skills");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const canCreate = useCan("hrm:skill:create");
  const [showAssess, setShowAssess] = React.useState(false);

  const skillQuery = useQuery({
    queryKey: ["hrm", "skill", skillId],
    queryFn: () => apiFetch<SkillResponse>(`/api/hrm/skills/${skillId}`),
  });
  const mappingsQuery = useQuery({
    queryKey: ["hrm", "skill", skillId, "employee-skills"],
    queryFn: () =>
      apiFetch<EmployeeSkillResponse[]>(`/api/hrm/skills/${skillId}/employee-skills`),
  });
  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const employees = employeesQuery.data ?? [];
  const nameOf = React.useCallback(
    (employeeId: string) => {
      const e = employees.find((x) => x.id === employeeId);
      return e?.actorDisplayName ?? e?.matricule ?? `${employeeId.slice(0, 8)}…`;
    },
    [employees],
  );

  const mappings = React.useMemo(
    () => mappingsQuery.data ?? [],
    [mappingsQuery.data],
  );
  const summary = React.useMemo(() => {
    if (mappings.length === 0) {
      return { count: 0, avgActual: 0, avgTarget: 0, experts: 0 };
    }
    const sumA = mappings.reduce((a, m) => a + m.niveauActuel, 0);
    const sumT = mappings.reduce((a, m) => a + m.niveauAttendu, 0);
    return {
      count: mappings.length,
      avgActual: Math.round((sumA / mappings.length) * 10) / 10,
      avgTarget: Math.round((sumT / mappings.length) * 10) / 10,
      experts: mappings.filter((m) => m.niveauActuel >= 5).length,
    };
  }, [mappings]);

  const create = useMutation({
    mutationFn: (body: CreateEmployeeSkillRequest) =>
      apiFetch<EmployeeSkillResponse>("/api/hrm/skills/employee-skills", {
        method: "POST",
        body,
      }),
    onSuccess: () => {
      toast.success(t("detail.addSuccess"));
      setShowAssess(false);
      queryClient.invalidateQueries({ queryKey: ["hrm", "skill", skillId, "employee-skills"] });
      queryClient.invalidateQueries({ queryKey: ["hrm", "skills", "employee-skills"] });
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  if (skillQuery.isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (skillQuery.error || !skillQuery.data) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {skillQuery.error instanceof BffApiError ? skillQuery.error.message : "—"}
      </div>
    );
  }

  const skill = skillQuery.data;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("title"), href: "/skills" },
          { label: skill.name },
        ]}
        title={
          <span className="flex items-center gap-3">
            <Sparkles className="h-6 w-6 text-orange-500" />
            {skill.name}
          </span>
        }
        subtitle={
          <span className="flex flex-wrap items-center gap-3">
            {skill.categorie && (
              <Badge tone={categoryTone(skill.categorie)}>{skill.categorie}</Badge>
            )}
            <span className="font-mono-tabular text-[11px] text-ink-3">{skill.id.slice(0, 8)}…</span>
          </span>
        }
        actions={
          <>
            <Link href="/skills">
              <Button type="button" variant="secondary">
                <ChevronLeft className="h-4 w-4" />
                {t("detail.back")}
              </Button>
            </Link>
            {canCreate && (
              <Button type="button" onClick={() => setShowAssess(true)}>
                <Plus className="h-4 w-4" />
                {t("detail.addAssessment")}
              </Button>
            )}
          </>
        }
      />

      <Card className="mb-6">
        <CardContent padding="lg">
          <dl className="grid grid-cols-2 gap-x-8 gap-y-4 md:grid-cols-4">
            <Detail label={t("detail.employees")} value={summary.count} mono />
            <Detail
              label={t("detail.averageActual")}
              value={summary.count === 0 ? "—" : `${summary.avgActual} / 5`}
              mono
            />
            <Detail
              label={t("detail.averageTarget")}
              value={summary.count === 0 ? "—" : `${summary.avgTarget} / 5`}
              mono
            />
            <Detail label={t("detail.experts")} value={summary.experts} mono />
          </dl>
          {skill.description && (
            <div className="mt-6">
              <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                {t("detail.description")}
              </Label>
              <p className="mt-1 whitespace-pre-line text-[13.5px] text-ink-2">{skill.description}</p>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="mb-3 text-[13px] font-bold tracking-tight text-ink">
        {t("detail.evaluationsTitle")}
      </div>
      <Card>
        {mappingsQuery.isLoading ? (
          <div className="grid place-items-center py-10">
            <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
          </div>
        ) : mappings.length === 0 ? (
          <CardContent padding="lg">
            <p className="text-center text-[13px] text-ink-3">{t("detail.evaluationsEmpty")}</p>
          </CardContent>
        ) : (
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-bg-dim">
                <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("detail.columns.employee")}
                </th>
                <th className="px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("detail.columns.actual")}
                </th>
                <th className="px-3 py-3 text-right text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("detail.columns.target")}
                </th>
                <th className="px-5 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3">
                  {t("detail.columns.date")}
                </th>
              </tr>
            </thead>
            <tbody>
              {mappings.map((m) => (
                <tr key={m.id} className="border-t border-line-soft">
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-2.5">
                      <Avatar name={nameOf(m.employeeId)} size="sm" />
                      <span className="text-[13px] font-semibold text-ink">{nameOf(m.employeeId)}</span>
                    </div>
                  </td>
                  <td className="px-3 py-3">
                    <div className="flex items-center gap-2">
                      <span className="font-mono-tabular text-[13px] font-bold text-ink">
                        {m.niveauActuel}
                      </span>
                      <div className="flex items-center gap-0.5">
                        {[1, 2, 3, 4, 5].map((i) => (
                          <Star
                            key={i}
                            className={cn(
                              "h-3 w-3",
                              i <= m.niveauActuel
                                ? "fill-orange-500 text-orange-500"
                                : "fill-bg-soft text-bg-soft",
                            )}
                          />
                        ))}
                      </div>
                    </div>
                  </td>
                  <td className="px-3 py-3 text-right font-mono-tabular text-[12.5px] text-ink-2">
                    {m.niveauAttendu}
                  </td>
                  <td className="px-5 py-3 font-mono-tabular text-[12px] text-ink-2">
                    {m.dateEvaluation ? formatDate(m.dateEvaluation, { locale }) : "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <AssessDialog
        open={showAssess}
        skill={skill}
        employees={employees}
        loading={create.isPending}
        onClose={() => setShowAssess(false)}
        onSubmit={(body) => create.mutate(body)}
        cancelLabel={tCommon("actions.cancel")}
      />
    </>
  );
}

function Detail({ label, value, mono }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div>
      <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">{label}</Label>
      <p
        className={
          mono
            ? "mt-0.5 font-mono-tabular text-[13.5px] font-bold text-ink"
            : "mt-0.5 text-[14px] font-medium text-ink"
        }
      >
        {value}
      </p>
    </div>
  );
}

function AssessDialog({
  open,
  skill,
  employees,
  loading,
  onClose,
  onSubmit,
  cancelLabel,
}: {
  open: boolean;
  skill: SkillResponse;
  employees: EmployeeResponse[];
  loading: boolean;
  onClose: () => void;
  onSubmit: (body: CreateEmployeeSkillRequest) => void;
  cancelLabel: string;
}) {
  const t = useTranslations("skills.assessment");
  const today = new Date().toISOString().slice(0, 10);
  const {
    register,
    handleSubmit,
    reset,
    formState: { isValid },
  } = useForm<{
    employeeId: string;
    niveauActuel: string;
    niveauAttendu: string;
    dateEvaluation: string;
  }>({
    mode: "onChange",
    defaultValues: { employeeId: "", niveauActuel: "3", niveauAttendu: "4", dateEvaluation: today },
  });
  React.useEffect(() => {
    if (!open) reset({ employeeId: "", niveauActuel: "3", niveauAttendu: "4", dateEvaluation: today });
  }, [open, reset, today]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={t("title")}
      subtitle={skill.name}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            {cancelLabel}
          </Button>
          <Button
            type="button"
            disabled={!isValid || loading}
            onClick={handleSubmit((v) =>
              onSubmit({
                employeeId: v.employeeId,
                skillId: skill.id,
                niveauActuel: Number(v.niveauActuel),
                niveauAttendu: Number(v.niveauAttendu),
                dateEvaluation: v.dateEvaluation || null,
              }),
            )}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
            OK
          </Button>
        </>
      }
    >
      <div className="grid grid-cols-2 gap-3">
        <Field label={t("fields.employee")} className="col-span-2">
          <select
            {...register("employeeId", { required: true })}
            className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
          >
            <option value="">—</option>
            {employees.map((e) => (
              <option key={e.id} value={e.id}>
                {e.matricule} · {e.actorDisplayName ?? e.actorId.slice(0, 8)}
              </option>
            ))}
          </select>
        </Field>
        <Field label={t("fields.actual")}>
          <Input
            type="number"
            min={1}
            max={5}
            step={1}
            {...register("niveauActuel", { required: true, min: 1, max: 5 })}
          />
        </Field>
        <Field label={t("fields.target")}>
          <Input
            type="number"
            min={1}
            max={5}
            step={1}
            {...register("niveauAttendu", { required: true, min: 1, max: 5 })}
          />
        </Field>
        <Field label={t("fields.date")} className="col-span-2">
          <Input type="date" {...register("dateEvaluation")} />
        </Field>
      </div>
    </Dialog>
  );
}
