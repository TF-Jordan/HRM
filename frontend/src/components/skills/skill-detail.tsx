"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Award,
  ChevronLeft,
  Loader2,
  Plus,
  Sparkles,
  Star,
  Target,
  TrendingUp,
  Users,
} from "lucide-react";
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
import { AppLink as Link, useAppRouter as useRouter } from "@/components/ui/app-link";
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
  const td = useTranslations("skills.detail");
  const te = useTranslations("skills.experts");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();
  const router = useRouter();
  const canCreate = useCan("hrm:skill:create");
  const [showAssess, setShowAssess] = React.useState(false);

  const skillQuery = useQuery({
    queryKey: ["hrm", "skill", skillId],
    queryFn: () => apiFetch<SkillResponse>(`/api/hrm/skills/${skillId}`),
  });
  const mappingsQuery = useQuery({
    queryKey: ["hrm", "skill", skillId, "employee-skills"],
    queryFn: () => apiFetch<EmployeeSkillResponse[]>(`/api/hrm/skills/${skillId}/employee-skills`),
  });
  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees", "list"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
  });

  const employees = React.useMemo(() => employeesQuery.data ?? [], [employeesQuery.data]);
  const nameOf = React.useCallback(
    (employeeId: string) => {
      const e = employees.find((x) => x.id === employeeId);
      return e?.actorDisplayName ?? e?.matricule ?? `${employeeId.slice(0, 8)}…`;
    },
    [employees],
  );

  const mappings = React.useMemo(() => mappingsQuery.data ?? [], [mappingsQuery.data]);

  const summary = React.useMemo(() => {
    if (mappings.length === 0) {
      return { count: 0, avgActual: 0, avgTarget: 0, experts: 0, gap: 0, met: 0 };
    }
    const sumA = mappings.reduce((a, m) => a + m.niveauActuel, 0);
    const sumT = mappings.reduce((a, m) => a + m.niveauAttendu, 0);
    const avgActual = Math.round((sumA / mappings.length) * 10) / 10;
    const avgTarget = Math.round((sumT / mappings.length) * 10) / 10;
    return {
      count: mappings.length,
      avgActual,
      avgTarget,
      experts: mappings.filter((m) => m.niveauActuel >= 5).length,
      gap: Math.round((avgActual - avgTarget) * 10) / 10,
      met: mappings.filter((m) => m.niveauActuel >= m.niveauAttendu).length,
    };
  }, [mappings]);

  const distribution = React.useMemo(() => {
    const buckets = [0, 0, 0, 0, 0];
    for (const m of mappings) {
      const lvl = Math.min(5, Math.max(1, Math.round(m.niveauActuel)));
      buckets[lvl - 1] += 1;
    }
    const max = Math.max(1, ...buckets);
    return buckets.map((count, i) => ({ level: i + 1, count, ratio: count / max }));
  }, [mappings]);

  const experts = React.useMemo(
    () =>
      [...mappings]
        .filter((m) => m.niveauActuel >= 4)
        .sort((a, b) => b.niveauActuel - a.niveauActuel)
        .slice(0, 6)
        .map((m) => ({ employeeId: m.employeeId, name: nameOf(m.employeeId), level: m.niveauActuel })),
    [mappings, nameOf],
  );

  const create = useMutation({
    mutationFn: (body: CreateEmployeeSkillRequest) =>
      apiFetch<EmployeeSkillResponse>("/api/hrm/skills/employee-skills", { method: "POST", body }),
    onSuccess: () => {
      toast.success(td("addSuccess"));
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
  const masteryPct = (summary.avgActual / 5) * 100;
  const targetPct = (summary.avgTarget / 5) * 100;

  return (
    <>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: t("title"), href: "/skills" }, { label: skill.name }]}
        title={t("title")}
        subtitle={td("subtitlePage")}
        actions={
          <Link href="/skills">
            <Button type="button" variant="secondary" size="sm">
              <ChevronLeft className="h-4 w-4" />
              {td("back")}
            </Button>
          </Link>
        }
      />

      {/* Hero card */}
      <div
        className="relative mb-6 overflow-hidden rounded-[20px] border border-orange-200 p-[26px] shadow-sm-brand"
        style={{ background: "linear-gradient(135deg, #FFF8F0 0%, #FFFFFF 58%)" }}
      >
        <span
          aria-hidden="true"
          className="pointer-events-none absolute -right-20 -top-20 h-64 w-64 rounded-full bg-grad-orange opacity-[0.07] blur-2xl"
        />
        <div className="relative flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0 flex-1">
            <div className="mb-2 flex flex-wrap items-center gap-2">
              <span className="grid h-9 w-9 place-items-center rounded-[11px] bg-orange-50 text-orange-500">
                <Sparkles className="h-5 w-5" />
              </span>
              {skill.categorie && <Badge tone={categoryTone(skill.categorie)}>{skill.categorie}</Badge>}
            </div>
            <h1 className="font-display text-[26px] font-extrabold leading-tight tracking-tight text-ink">
              {skill.name}
            </h1>
            {skill.description && (
              <p className="mt-1.5 max-w-2xl text-[13px] text-ink-2 line-clamp-2">{skill.description}</p>
            )}
          </div>
          {canCreate && (
            <Button type="button" className="shrink-0" onClick={() => setShowAssess(true)}>
              <Plus className="h-4 w-4" />
              {td("addAssessment")}
            </Button>
          )}
        </div>

        {/* Hero tiles */}
        <div className="relative mt-6 grid grid-cols-2 gap-3.5 md:grid-cols-4">
          <HeroTile icon={Users} label={td("employees")} value={summary.count} />
          <HeroTile
            icon={TrendingUp}
            label={td("averageActual")}
            value={summary.count === 0 ? "—" : `${summary.avgActual}`}
            sub={summary.count === 0 ? undefined : "/ 5"}
          />
          <HeroTile
            icon={Target}
            label={td("averageTarget")}
            value={summary.count === 0 ? "—" : `${summary.avgTarget}`}
            sub={summary.count === 0 ? undefined : "/ 5"}
          />
          <HeroTile icon={Award} label={td("experts")} value={summary.experts} />
        </div>

        {/* Mastery vs target bar */}
        {summary.count > 0 && (
          <div className="relative mt-3.5 rounded-[13px] border border-line bg-white/80 p-3.5 backdrop-blur-sm">
            <div className="mb-2 flex items-center justify-between">
              <span className="text-[12px] font-semibold text-ink-2">{td("masteryTitle")}</span>
              <span
                className={cn(
                  "font-mono-tabular text-[11.5px] font-bold",
                  summary.gap < 0 ? "text-danger-600" : "text-success-600",
                )}
              >
                {td("gapLabel")}: {summary.gap > 0 ? `+${summary.gap}` : summary.gap}
              </span>
            </div>
            <div className="relative h-2.5 w-full overflow-hidden rounded-full bg-bg-dim">
              <div
                className="h-full rounded-full bg-grad-orange transition-all"
                style={{ width: `${masteryPct}%` }}
              />
              {/* target marker */}
              <span
                className="absolute top-1/2 h-4 w-[2px] -translate-y-1/2 rounded bg-ink"
                style={{ left: `calc(${targetPct}% - 1px)` }}
                aria-hidden="true"
              />
            </div>
            <div className="mt-1.5 flex items-center justify-between text-[10.5px] text-ink-4">
              <span>{td("mastered", { met: summary.met, total: summary.count })}</span>
              <span className="flex items-center gap-1">
                <span className="inline-block h-2 w-[2px] rounded bg-ink" />
                {td("targetMarker")}
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Body */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Left — 2/3 */}
        <div className="flex flex-col gap-6 lg:col-span-2">
          {/* Level distribution */}
          <Card>
            <CardContent padding="lg">
              <div className="mb-4">
                <div className="text-[14px] font-bold tracking-tight text-ink">{td("distributionTitle")}</div>
                <div className="text-[12px] text-ink-3">{td("distributionSub")}</div>
              </div>
              {summary.count === 0 ? (
                <p className="rounded-[12px] border border-dashed border-line bg-bg-soft px-4 py-6 text-center text-[13px] text-ink-3">
                  {td("evaluationsEmpty")}
                </p>
              ) : (
                <div className="flex flex-col gap-2.5">
                  {[...distribution].reverse().map((d) => (
                    <div key={d.level} className="flex items-center gap-3">
                      <div className="flex w-24 shrink-0 items-center gap-1">
                        {[1, 2, 3, 4, 5].map((i) => (
                          <Star
                            key={i}
                            className={cn(
                              "h-3 w-3",
                              i <= d.level ? "fill-orange-500 text-orange-500" : "fill-bg-soft text-bg-soft",
                            )}
                          />
                        ))}
                      </div>
                      <div className="h-3.5 flex-1 overflow-hidden rounded-full bg-bg-dim">
                        <div
                          className="h-full rounded-full bg-grad-orange transition-all"
                          style={{ width: `${Math.round(d.ratio * 100)}%` }}
                        />
                      </div>
                      <span className="w-8 shrink-0 text-right font-mono-tabular text-[12.5px] font-bold text-ink">
                        {d.count}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Evaluations table */}
          <Card>
            <div className="border-b border-line px-6 py-4 text-[14px] font-bold tracking-tight text-ink">
              {td("evaluationsTitle")}
            </div>
            {mappingsQuery.isLoading ? (
              <div className="grid place-items-center py-10">
                <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
              </div>
            ) : mappings.length === 0 ? (
              <CardContent padding="lg">
                <p className="text-center text-[13px] text-ink-3">{td("evaluationsEmpty")}</p>
              </CardContent>
            ) : (
              <table className="w-full border-collapse">
                <thead>
                  <tr className="bg-bg-dim">
                    <Th>{td("columns.employee")}</Th>
                    <Th>{td("columns.actual")}</Th>
                    <Th className="text-right">{td("columns.target")}</Th>
                    <Th>{td("columns.gap")}</Th>
                    <Th>{td("columns.date")}</Th>
                  </tr>
                </thead>
                <tbody>
                  {mappings.map((m) => {
                    const gap = m.niveauActuel - m.niveauAttendu;
                    return (
                      <tr
                        key={m.id}
                        className="cursor-pointer border-t border-line-soft hover:bg-bg-soft"
                        onClick={() => router.push(`/employees/${m.employeeId}`)}
                      >
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
                        <td className="px-3 py-3">
                          <Badge tone={gap < 0 ? "danger" : gap === 0 ? "gray" : "success"}>
                            {gap > 0 ? `+${gap}` : gap}
                          </Badge>
                        </td>
                        <td className="px-5 py-3 font-mono-tabular text-[12px] text-ink-2">
                          {m.dateEvaluation ? formatDate(m.dateEvaluation, { locale }) : "—"}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </Card>
        </div>

        {/* Right — 1/3 */}
        <div className="flex flex-col gap-4">
          {/* Experts */}
          <Card>
            <CardContent padding="lg">
              <div className="mb-4 flex items-center gap-2 text-[14px] font-bold tracking-tight text-ink">
                <Star className="h-4 w-4 fill-orange-500 text-orange-500" />
                {te("title")}
              </div>
              {experts.length === 0 ? (
                <p className="rounded-[12px] border border-dashed border-line bg-bg-soft px-4 py-6 text-center text-[12.5px] text-ink-3">
                  {te("empty")}
                </p>
              ) : (
                <div className="flex flex-col divide-y divide-line-soft">
                  {experts.map((e, idx) => (
                    <button
                      type="button"
                      key={`${e.employeeId}-${idx}`}
                      onClick={() => router.push(`/employees/${e.employeeId}`)}
                      className="flex items-center gap-3 py-2.5 text-left transition hover:opacity-80"
                    >
                      <Avatar name={e.name} size="sm" />
                      <div className="min-w-0 flex-1">
                        <div className="truncate text-[12.5px] font-semibold text-ink">{e.name}</div>
                      </div>
                      <div className="flex items-center gap-0.5">
                        {[1, 2, 3, 4, 5].map((i) => (
                          <Star
                            key={i}
                            className={cn(
                              "h-3 w-3",
                              i <= e.level ? "fill-orange-500 text-orange-500" : "fill-bg-soft text-bg-soft",
                            )}
                          />
                        ))}
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Description */}
          {skill.description && (
            <Card>
              <CardContent padding="lg">
                <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">
                  {td("description")}
                </Label>
                <p className="mt-1.5 whitespace-pre-line text-[13px] text-ink-2">{skill.description}</p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>

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

/* ── HeroTile ──────────────────────────────────────────────────────── */

function HeroTile({
  icon: Icon,
  label,
  value,
  sub,
}: {
  icon: typeof Users;
  label: string;
  value: React.ReactNode;
  sub?: React.ReactNode;
}) {
  return (
    <div className="rounded-[13px] border border-line bg-white/80 p-3.5 backdrop-blur-sm">
      <div className="mb-1.5 flex items-center gap-1.5 text-[11px] font-semibold uppercase tracking-[0.04em] text-ink-3">
        <Icon className="h-3.5 w-3.5" />
        {label}
      </div>
      <div className="font-display text-[22px] font-extrabold leading-tight tracking-tight text-ink tabular-nums">
        {value}
        {sub && <span className="ml-1 text-[12px] font-semibold text-ink-3">{sub}</span>}
      </div>
    </div>
  );
}

function Th({ children, className }: { children?: React.ReactNode; className?: string }) {
  return (
    <th
      className={cn(
        "px-3 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3 first:pl-5 last:pr-5",
        className,
      )}
    >
      {children}
    </th>
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
          <Input type="number" min={1} max={5} step={1} {...register("niveauActuel", { required: true, min: 1, max: 5 })} />
        </Field>
        <Field label={t("fields.target")}>
          <Input type="number" min={1} max={5} step={1} {...register("niveauAttendu", { required: true, min: 1, max: 5 })} />
        </Field>
        <Field label={t("fields.date")} className="col-span-2">
          <Input type="date" {...register("dateEvaluation")} />
        </Field>
      </div>
    </Dialog>
  );
}
