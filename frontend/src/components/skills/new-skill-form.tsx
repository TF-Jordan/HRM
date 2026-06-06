"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { AlertTriangle, Lightbulb, Loader2, Plus, Sparkles } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm, useWatch } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input, Textarea } from "@/components/ui/input";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { categoryTone } from "@/lib/skill-aggregates";
import { cn } from "@/lib/utils";
import type { CreateSkillRequest, SkillResponse } from "@/server/ksm/modules/skills";

type FormValues = { name: string; categorie: string; description: string };

const DESC_MAX = 400;
const NAME_MIN = 2;

const normalize = (s: string) => s.trim().toLowerCase();

export function NewSkillForm() {
  const t = useTranslations("skills.new");
  const tSkills = useTranslations("skills");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const tVal = useTranslations("validation");
  const router = useRouter();

  const skillsQuery = useQuery({
    queryKey: ["hrm", "skills"],
    queryFn: () => apiFetch<SkillResponse[]>("/api/hrm/skills"),
  });
  const skills = React.useMemo(() => skillsQuery.data ?? [], [skillsQuery.data]);

  const existingNames = React.useMemo(
    () => new Set(skills.map((s) => normalize(s.name))),
    [skills],
  );
  const existingCategories = React.useMemo(() => {
    const set = new Set<string>();
    for (const s of skills) if (s.categorie?.trim()) set.add(s.categorie.trim());
    return [...set].sort((a, b) => a.localeCompare(b));
  }, [skills]);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: { name: "", categorie: "", description: "" },
  });

  const nameValue = useWatch({ control, name: "name" }) ?? "";
  const categoryValue = useWatch({ control, name: "categorie" }) ?? "";
  const descriptionValue = useWatch({ control, name: "description" }) ?? "";

  const trimmedName = (nameValue ?? "").trim();
  const isDuplicate = trimmedName.length >= NAME_MIN && existingNames.has(normalize(trimmedName));
  const canSubmit = trimmedName.length >= NAME_MIN && !isDuplicate;

  const mutation = useMutation({
    mutationFn: (v: FormValues) => {
      const body: CreateSkillRequest = {
        name: v.name.trim(),
        categorie: v.categorie.trim() || null,
        description: v.description.trim() || null,
      };
      return apiFetch<SkillResponse>("/api/hrm/skills", { method: "POST", body });
    },
    onSuccess: (skill) => {
      toast.success(t("success"));
      router.push(`/skills/${skill.id}`);
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  const onSubmit = (v: FormValues) => {
    if (!canSubmit) return;
    mutation.mutate(v);
  };

  const nameError = errors.name
    ? errors.name.type === "minLength"
      ? t("minLength", { min: NAME_MIN })
      : tVal("required")
    : isDuplicate
      ? t("duplicate")
      : undefined;

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <PageHeader
        ucBadge={tSkills("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: tSkills("title"), href: "/skills" },
          { label: t("title") },
        ]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <>
            <Link href="/skills">
              <Button type="button" variant="secondary">
                {tCommon("actions.cancel")}
              </Button>
            </Link>
            <Button type="submit" disabled={!canSubmit || mutation.isPending}>
              {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
              {t("submit")}
            </Button>
          </>
        }
      />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Main form — 2/3 */}
        <div className="lg:col-span-2">
          <Card>
            <div className="border-b border-line px-6 py-4">
              <div className="text-[14px] font-bold tracking-tight text-ink">{t("sectionTitle")}</div>
              <div className="text-[12px] text-ink-3">{t("sectionSubtitle")}</div>
            </div>
            <CardContent padding="lg">
              <div className="grid grid-cols-2 gap-x-4 gap-y-5">
                <Field
                  label={t("fields.name")}
                  hint={t("fields.nameHint")}
                  error={nameError}
                  className="col-span-2"
                >
                  <Input
                    placeholder={t("fields.namePlaceholder")}
                    autoComplete="off"
                    aria-invalid={Boolean(nameError)}
                    className={isDuplicate ? "border-danger-400 focus:border-danger-400 focus:ring-danger-500/12" : undefined}
                    {...register("name", { required: true, minLength: NAME_MIN })}
                  />
                </Field>

                <Field
                  label={t("fields.categorie")}
                  hint={t("fields.categoryHint")}
                  className="col-span-2 sm:col-span-1"
                >
                  <Input
                    list="skill-category-suggestions"
                    placeholder={t("fields.categoriePlaceholder")}
                    autoComplete="off"
                    {...register("categorie")}
                  />
                  <datalist id="skill-category-suggestions">
                    {existingCategories.map((c) => (
                      <option key={c} value={c} />
                    ))}
                  </datalist>
                </Field>

                <Field label={t("fields.description")} className="col-span-2">
                  <Textarea
                    rows={4}
                    maxLength={DESC_MAX}
                    placeholder={t("fields.descriptionPlaceholder")}
                    {...register("description")}
                  />
                  <div className="mt-0.5 text-right text-[11px] text-ink-4 tabular-nums">
                    {descriptionValue.length} / {DESC_MAX}
                  </div>
                </Field>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Sidebar — 1/3 */}
        <div className="flex flex-col gap-4">
          {/* Live preview */}
          <Card>
            <CardContent padding="lg">
              <div className="mb-3 text-[11.5px] font-semibold uppercase tracking-wider text-ink-4">
                {t("preview.title")}
              </div>
              <div className="rounded-[14px] border border-line bg-bg-soft p-4">
                <div className="flex items-start gap-3">
                  <span className="grid h-9 w-9 shrink-0 place-items-center rounded-[11px] bg-orange-50 text-orange-500">
                    <Sparkles className="h-5 w-5" />
                  </span>
                  <div className="min-w-0 flex-1">
                    {trimmedName ? (
                      <div className="truncate text-[15px] font-bold text-ink">{trimmedName}</div>
                    ) : (
                      <div className="text-[13px] italic text-ink-4">{t("preview.empty")}</div>
                    )}
                    <div className="mt-1.5">
                      {categoryValue?.trim() ? (
                        <Badge tone={categoryTone(categoryValue)}>{categoryValue.trim()}</Badge>
                      ) : (
                        <Badge tone="gray">{t("preview.uncategorized")}</Badge>
                      )}
                    </div>
                  </div>
                </div>
                {descriptionValue.trim() && (
                  <p className="mt-3 line-clamp-3 text-[12.5px] text-ink-2">{descriptionValue.trim()}</p>
                )}
              </div>
              {isDuplicate && (
                <div className="mt-3 flex items-start gap-2 rounded-[11px] border border-danger-200 bg-danger-50 px-3 py-2.5 text-[12px] text-danger-700">
                  <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
                  <span>{t("duplicate")}</span>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Mastery scale */}
          <Card>
            <CardContent padding="lg">
              <div className="mb-3 text-[13px] font-bold tracking-tight text-ink">{t("scale.title")}</div>
              <p className="mb-3 text-[11.5px] text-ink-3">{t("scale.subtitle")}</p>
              <div className="flex flex-col gap-2">
                {[1, 2, 3, 4, 5].map((lvl) => (
                  <div key={lvl} className="flex items-center gap-2.5">
                    <span className="grid h-5 w-5 shrink-0 place-items-center rounded-full bg-grad-orange text-[11px] font-bold text-white">
                      {lvl}
                    </span>
                    <span className="text-[12px] text-ink-2">{t(`scale.l${lvl}` as "scale.l1")}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Tips */}
          <Card>
            <CardContent padding="lg">
              <div className="mb-3 flex items-center gap-2 text-[13px] font-bold tracking-tight text-ink">
                <Lightbulb className="h-4 w-4 text-orange-500" />
                {t("tips.title")}
              </div>
              <ul className="flex flex-col gap-2">
                {["t1", "t2", "t3"].map((k) => (
                  <li key={k} className="flex items-start gap-2 text-[12px] text-ink-2">
                    <span className={cn("mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-orange-400")} />
                    {t(`tips.${k}` as "tips.t1")}
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>
        </div>
      </div>
    </form>
  );
}
