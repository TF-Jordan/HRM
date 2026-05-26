"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2, Send, Upload, FileText, X } from "lucide-react";
import { toast } from "sonner";
import { z } from "zod";
import { addContractSchema } from "@/lib/validation/hrm/contract.schema";
import { uuidLike } from "@/lib/validation/uuid";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useCreateContractWithDocument } from "@/hooks/modules/useContracts";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Avatar } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  FormSection,
  FieldGrid,
  Field,
  RadioCardGroup,
  SuffixInput,
  FormFooter,
  type RadioCardOption,
} from "@/components/ui-tokens/form-kit";
import { useRouter } from "@/i18n/navigation";
import { queryKeys } from "@/lib/api-client";
import { useQueryClient } from "@tanstack/react-query";
import type { ContractType } from "@/lib/types/hrm/contract";

const formSchema = z.object({ employeeId: uuidLike }).and(addContractSchema);
type FormValues = z.input<typeof formSchema>;

export function NewContractClient() {
  const t = useTranslations("employees.contractsPage");
  const tForm = useTranslations("employees.form");
  const tc = useTranslations("employees.contracts");
  const tNav = useTranslations("navigation");
  const tCommon = useTranslations("common");
  const router = useRouter();
  const fmt = useFormat();
  const employees = useEmployees();
  const qc = useQueryClient();
  const [pdfFile, setPdfFile] = React.useState<File | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      employeeId: "",
      type: "CDI",
      dateDebut: new Date().toISOString().slice(0, 10),
      dateFin: null,
      salaireBase: 0,
      avantagesNature: null,
      periodeEssai: null,
    },
  });

  const errors = form.formState.errors;
  const selectedEmployeeId = form.watch("employeeId");
  const selectedType = form.watch("type");
  const createContract = useCreateContractWithDocument(selectedEmployeeId);
  const selectedEmployee = employees.data?.find((e) => e.id === selectedEmployeeId);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.type !== "application/pdf") {
      toast.error("Seuls les fichiers PDF sont acceptés");
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      toast.error("Le fichier ne doit pas dépasser 10 Mo");
      return;
    }
    setPdfFile(file);
  };

  const onSubmit = (values: FormValues) => {
    createContract.mutate(
      {
        contract: {
          type: values.type,
          dateDebut: values.dateDebut,
          dateFin: values.dateFin ?? null,
          salaireBase: Number(values.salaireBase),
          avantagesNature: values.avantagesNature ? Number(values.avantagesNature) : null,
          periodeEssai: values.periodeEssai ? Number(values.periodeEssai) : null,
        },
        file: pdfFile ?? undefined,
      },
      {
        onSuccess: () => {
          toast.success(t("new.submit"));
          qc.invalidateQueries({ queryKey: queryKeys.hrm.allContracts() });
          router.push("/contracts" as never);
        },
        onError: (err) => toast.error((err as Error).message),
      },
    );
  };

  const typeOptions: RadioCardOption<ContractType>[] = (
    ["CDI", "CDD", "STAGE", "INTERIM"] as const
  ).map((v) => ({
    value: v,
    label: v === "INTERIM" ? "Intérim" : v === "STAGE" ? "Stage" : v,
    sublabel: tForm(`contractTypes.${v}` as never),
    code: "enum",
  }));

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        crumbs={[{ label: tNav("items.contracts"), href: "/contracts" }, { label: t("new.title") }]}
        title={t("new.title")}
        subtitle={t("new.subtitle")}
      />

      <form onSubmit={form.handleSubmit(onSubmit)} className="grid grid-cols-1 gap-5 lg:grid-cols-3">
        <div className="space-y-5 lg:col-span-2">
          <FormSection title={t("new.sections.characteristics")}>
            <Field label={t("new.sections.type")} code="type" required>
              <RadioCardGroup
                value={selectedType}
                onChange={(v) => form.setValue("type", v)}
                options={typeOptions}
                cols={4}
              />
            </Field>
            <FieldGrid>
              <Field id="dateDebut" label={tc("dateDebut")} code="dateDebut" required error={errors.dateDebut?.message}>
                <Input id="dateDebut" type="date" {...form.register("dateDebut")} />
              </Field>
              <Field
                id="dateFin"
                label={tc("dateFin")}
                code="dateFin"
                hint={selectedType === "CDI" ? t("new.hints.dateFin") : undefined}
                error={errors.dateFin?.message}
              >
                <Input id="dateFin" type="date" disabled={selectedType === "CDI"} {...form.register("dateFin")} />
              </Field>
            </FieldGrid>
          </FormSection>

          <FormSection title={t("new.sections.remuneration")} hint={t("new.sections.remunerationHint")}>
            <FieldGrid>
              <Field id="salaireBase" label={t("new.fields.salaireBase")} code="salaireBase" required error={errors.salaireBase?.message}>
                <SuffixInput id="salaireBase" suffix="XAF" type="number" min={0} step="1" {...form.register("salaireBase")} />
              </Field>
              <Field id="avantagesNature" label={t("new.fields.avantagesNature")} code="avantagesNature">
                <SuffixInput id="avantagesNature" suffix="XAF" type="number" min={0} step="1" {...form.register("avantagesNature")} />
              </Field>
              <Field id="periodeEssai" label={t("new.fields.periodeEssai")} code="periodeEssai" hint={t("new.hints.periodeEssai")}>
                <SuffixInput id="periodeEssai" suffix="jours" type="number" min={0} {...form.register("periodeEssai")} />
              </Field>
            </FieldGrid>
          </FormSection>

          <FormSection title={t("new.document.title")}>
            <input ref={fileInputRef} type="file" accept="application/pdf" className="hidden" onChange={handleFileChange} />
            {pdfFile ? (
              <div className="flex items-center gap-3 rounded-[12px] border border-line bg-cream-dim/50 px-4 py-3">
                <FileText className="size-5 shrink-0 text-brand-500" />
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium text-ink">{pdfFile.name}</p>
                  <p className="text-[12px] text-ink-3">{(pdfFile.size / 1024 / 1024).toFixed(2)} Mo</p>
                </div>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  onClick={() => {
                    setPdfFile(null);
                    if (fileInputRef.current) fileInputRef.current.value = "";
                  }}
                >
                  <X className="size-4" />
                </Button>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="flex w-full cursor-pointer flex-col items-center justify-center rounded-[12px] border-2 border-dashed border-line bg-cream-dim/50 py-9 transition-colors hover:border-brand-300 hover:bg-brand-50/30"
              >
                <Upload className="mb-2 size-6 text-ink-3" />
                <p className="text-sm font-medium text-ink-2">{t("new.document.cta")}</p>
                <p className="text-[12px] text-ink-3">{t("new.document.hint")}</p>
              </button>
            )}
          </FormSection>

          <FormFooter>
            <Button type="button" variant="ghost" onClick={() => router.push("/contracts" as never)}>
              {tCommon("actions.cancel" as never)}
            </Button>
            <Button type="submit" disabled={createContract.isPending || !selectedEmployeeId}>
              {createContract.isPending ? <Loader2 className="size-4 animate-spin" /> : <Send className="size-4" />}
              {createContract.isPending ? t("new.submitting") : t("new.submit")}
            </Button>
          </FormFooter>
        </div>

        {/* Sidebar: employee picker + current contract */}
        <div className="lg:col-span-1">
          <Card className="lg:sticky lg:top-4">
            <CardContent className="space-y-4 p-5">
              <div className="flex items-center gap-2">
                <span aria-hidden className="h-0.5 w-6 rounded-full bg-brand-500" />
                <span className="text-[11px] font-bold uppercase tracking-[0.12em] text-ink">
                  {t("new.sidebar.title")}
                </span>
              </div>
              <Controller
                control={form.control}
                name="employeeId"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger id="employeeId">
                      <SelectValue placeholder={t("new.selectEmployee")} />
                    </SelectTrigger>
                    <SelectContent>
                      {employees.data?.map((e) => (
                        <SelectItem key={e.id} value={e.id}>
                          {e.actorDisplayName} ({e.matricule})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.employeeId && (
                <p className="text-[11.5px] text-status-red-600">{errors.employeeId.message}</p>
              )}

              {selectedEmployee && (
                <div className="rounded-[14px] border border-line bg-cream-soft/40 p-3">
                  <div className="flex items-center gap-2.5">
                    <Avatar name={selectedEmployee.actorDisplayName} tone="orange" shape="square" size="lg" />
                    <div className="min-w-0">
                      <div className="truncate text-[13.5px] font-bold text-ink">
                        {selectedEmployee.actorDisplayName}
                      </div>
                      <div className="truncate text-[11.5px] text-ink-3">
                        {selectedEmployee.matricule}
                        {selectedEmployee.poste ? ` · ${selectedEmployee.poste}` : ""}
                      </div>
                    </div>
                  </div>
                  <dl className="mt-3 space-y-1.5 text-[12px]">
                    <div className="flex items-center justify-between">
                      <dt className="text-ink-3">{t("new.sidebar.currentContract")}</dt>
                      <dd>
                        {selectedEmployee.contractType ? (
                          <Badge tone="green">{selectedEmployee.contractType}</Badge>
                        ) : (
                          <span className="text-ink-4">{t("new.sidebar.noActive")}</span>
                        )}
                      </dd>
                    </div>
                    {selectedEmployee.contractSalaireBase != null && (
                      <div className="flex items-center justify-between">
                        <dt className="text-ink-3">{t("new.sidebar.currentSalary")}</dt>
                        <dd className="font-semibold text-ink tabular">
                          {fmt.money(selectedEmployee.contractSalaireBase)}
                        </dd>
                      </div>
                    )}
                  </dl>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </form>
    </div>
  );
}
