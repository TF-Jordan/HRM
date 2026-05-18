"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import {
  useSkills,
  useCreateSkill,
  useEmployeeSkills,
  useCreateEmployeeSkill,
} from "@/hooks/modules/useSkills";
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
import {
  createSkillSchema,
  createEmployeeSkillSchema,
  type CreateSkillFormValues,
  type CreateEmployeeSkillFormValues,
} from "@/lib/validation/hrm/skill.schema";

export function SkillsClient() {
  const t = useTranslations("skills");
  const tCommon = useTranslations("common");
  const tNav = useTranslations("navigation");
  const skills = useSkills();
  const createSkill = useCreateSkill();
  const [openSkill, setOpenSkill] = React.useState(false);

  const skillForm = useForm<CreateSkillFormValues>({
    resolver: zodResolver(createSkillSchema),
    defaultValues: { name: "", categorie: "", description: "" },
  });

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-24"
        crumbs={[{ label: tNav("items.skills") }]}
        title={t("title")}
        subtitle={t("subtitle")}
      />

      <Tabs defaultValue="catalogue">
        <TabsList>
          <TabsTrigger value="catalogue">{t("tabs.catalogue")}</TabsTrigger>
          <TabsTrigger value="evaluations">{t("tabs.evaluations")}</TabsTrigger>
        </TabsList>

        <TabsContent value="catalogue">
          <div className="mb-3 flex justify-end">
            <Button onClick={() => setOpenSkill(true)}>
              <Plus className="size-4" />
              {t("list.newButton")}
            </Button>
          </div>

          {skills.isLoading && <Skeleton className="h-32 w-full" />}
          {!skills.isLoading && skills.data && skills.data.length === 0 && (
            <Card>
              <CardContent className="py-8 text-center text-sm text-ink-3">
                {t("list.empty")}
              </CardContent>
            </Card>
          )}
          {!skills.isLoading && skills.data && skills.data.length > 0 && (
            <Card>
              <CardContent className="p-0">
                <table className="w-full border-separate border-spacing-0">
                  <thead>
                    <tr>
                      {[
                        t("list.table.name"),
                        t("list.table.categorie"),
                        t("list.table.description"),
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
                    {skills.data.map((s) => (
                      <tr key={s.id}>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">
                          {s.name}
                        </td>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">
                          {s.categorie ?? "—"}
                        </td>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">
                          {s.description ?? "—"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="evaluations">
          <EmployeeSkillsPanel />
        </TabsContent>
      </Tabs>

      <Dialog open={openSkill} onOpenChange={setOpenSkill}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("list.newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={skillForm.handleSubmit((values) =>
              createSkill.mutate(
                {
                  name: values.name,
                  categorie: values.categorie ?? null,
                  description: values.description ?? null,
                },
                {
                  onSuccess: () => {
                    toast.success(t("form.submit"));
                    setOpenSkill(false);
                    skillForm.reset();
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="name">{t("form.name")}</Label>
              <Input id="name" {...skillForm.register("name")} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="categorie">{t("form.categorie")}</Label>
              <Input id="categorie" {...skillForm.register("categorie")} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="description">{t("form.description")}</Label>
              <Input id="description" {...skillForm.register("description")} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpenSkill(false)}>
                {tCommon("actions.cancel")}
              </Button>
              <Button type="submit" disabled={createSkill.isPending}>
                {createSkill.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function EmployeeSkillsPanel() {
  const t = useTranslations("skills");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const employees = useEmployees();
  const skills = useSkills();
  const [employeeId, setEmployeeId] = React.useState<string>("");
  const empSkills = useEmployeeSkills(employeeId || undefined);
  const create = useCreateEmployeeSkill(employeeId || undefined);
  const [openEval, setOpenEval] = React.useState(false);

  const form = useForm<CreateEmployeeSkillFormValues>({
    resolver: zodResolver(createEmployeeSkillSchema),
    defaultValues: {
      employeeId: "",
      skillId: "",
      niveauActuel: 0,
      niveauAttendu: 0,
      dateEvaluation: new Date().toISOString().slice(0, 10),
    },
  });

  React.useEffect(() => {
    form.setValue("employeeId", employeeId);
  }, [employeeId, form]);

  const skillName = (skillId: string) => skills.data?.find((s) => s.id === skillId)?.name ?? skillId;

  return (
    <div className="space-y-3">
      <Card>
        <CardContent className="flex items-center gap-3 p-4">
          <Label className="shrink-0">{t("evaluations.selectEmployee")}</Label>
          <Select value={employeeId} onValueChange={setEmployeeId}>
            <SelectTrigger className="max-w-md">
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
          <div className="grow" />
          {employeeId && (
            <Button onClick={() => setOpenEval(true)}>
              <Plus className="size-4" />
              {t("evaluations.newButton")}
            </Button>
          )}
        </CardContent>
      </Card>

      {employeeId && (
        <>
          {empSkills.isLoading && <Skeleton className="h-32 w-full" />}
          {!empSkills.isLoading && empSkills.data && empSkills.data.length === 0 && (
            <Card>
              <CardContent className="py-8 text-center text-sm text-ink-3">
                {t("evaluations.empty")}
              </CardContent>
            </Card>
          )}
          {!empSkills.isLoading && empSkills.data && empSkills.data.length > 0 && (
            <Card>
              <CardContent className="p-0">
                <table className="w-full border-separate border-spacing-0">
                  <thead>
                    <tr>
                      {[
                        t("evaluations.table.skill"),
                        t("evaluations.table.actuel"),
                        t("evaluations.table.attendu"),
                        t("evaluations.table.ecart"),
                        t("evaluations.table.date"),
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
                    {empSkills.data.map((es) => {
                      const ecart = es.niveauAttendu - es.niveauActuel;
                      return (
                        <tr key={es.id}>
                          <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink">
                            {skillName(es.skillId)}
                          </td>
                          <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                            {es.niveauActuel}/5
                          </td>
                          <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                            {es.niveauAttendu}/5
                          </td>
                          <td className="border-b border-line-soft px-4 py-3 text-[13.5px] tabular">
                            <span className={ecart > 0 ? "text-status-amber-600" : "text-status-green-600"}>
                              {ecart > 0 ? `+${ecart}` : ecart}
                            </span>
                          </td>
                          <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                            {fmt.date(es.dateEvaluation)}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </CardContent>
            </Card>
          )}
        </>
      )}

      <Dialog open={openEval} onOpenChange={setOpenEval}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("evaluations.newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={form.handleSubmit((values) =>
              create.mutate(
                {
                  employeeId: values.employeeId,
                  skillId: values.skillId,
                  niveauActuel: Number(values.niveauActuel),
                  niveauAttendu: Number(values.niveauAttendu),
                  dateEvaluation: values.dateEvaluation,
                },
                {
                  onSuccess: () => {
                    toast.success(t("evaluations.form.submit"));
                    setOpenEval(false);
                    form.reset({
                      employeeId,
                      skillId: "",
                      niveauActuel: 0,
                      niveauAttendu: 0,
                      dateEvaluation: new Date().toISOString().slice(0, 10),
                    });
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label>{t("evaluations.form.skill")}</Label>
              <Select
                value={form.watch("skillId") ?? ""}
                onValueChange={(v) => form.setValue("skillId", v)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="—" />
                </SelectTrigger>
                <SelectContent>
                  {(skills.data ?? []).map((s) => (
                    <SelectItem key={s.id} value={s.id}>
                      {s.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="niveauActuel">{t("evaluations.form.niveauActuel")}</Label>
                <Input
                  id="niveauActuel"
                  type="number"
                  min="0"
                  max="5"
                  {...form.register("niveauActuel")}
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="niveauAttendu">{t("evaluations.form.niveauAttendu")}</Label>
                <Input
                  id="niveauAttendu"
                  type="number"
                  min="0"
                  max="5"
                  {...form.register("niveauAttendu")}
                />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="dateEvaluation">{t("evaluations.form.dateEvaluation")}</Label>
              <Input id="dateEvaluation" type="date" {...form.register("dateEvaluation")} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpenEval(false)}>
                {tCommon("actions.cancel")}
              </Button>
              <Button type="submit" disabled={create.isPending}>
                {create.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("evaluations.form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
