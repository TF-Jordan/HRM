"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2, Save, Upload, FileText, X } from "lucide-react";
import { toast } from "sonner";
import { z } from "zod";
import { addContractSchema } from "@/lib/validation/hrm/contract.schema";
import { uuidLike } from "@/lib/validation/uuid";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useCreateContractWithDocument } from "@/hooks/modules/useContracts";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useRouter } from "@/i18n/navigation";
import { queryKeys } from "@/lib/api-client";
import { useQueryClient } from "@tanstack/react-query";
import type { ContractType } from "@/lib/types/hrm/contract";

const formSchema = z.object({ employeeId: uuidLike }).and(addContractSchema);
type FormValues = z.input<typeof formSchema>;

const CONTRACT_TYPES: ContractType[] = ["CDI", "CDD", "STAGE", "INTERIM"];

export function NewContractClient() {
  const t = useTranslations("employees.contractsPage");
  const tNav = useTranslations("navigation");
  const tCommon = useTranslations("common");
  const router = useRouter();
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

  const selectedEmployeeId = form.watch("employeeId");
  const selectedType = form.watch("type");
  const createContract = useCreateContractWithDocument(selectedEmployeeId);

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

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-04"
        crumbs={[
          { label: tNav("items.contracts"), href: "/contracts" },
          { label: t("new.title") },
        ]}
        title={t("new.title")}
        subtitle={t("new.subtitle")}
      />

      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        {/* Employee selection */}
        <Section title={t("new.sections.employee")} hint={t("new.sections.employeeHint")}>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field id="employeeId" label={t("employee")} required error={form.formState.errors.employeeId?.message}>
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
            </Field>
          </div>
        </Section>

        {/* Contract type */}
        <Section title={t("new.sections.type")} hint={t("new.sections.typeHint")}>
          <Controller
            control={form.control}
            name="type"
            render={({ field }) => (
              <div className="flex gap-3">
                {CONTRACT_TYPES.map((tp) => (
                  <button
                    key={tp}
                    type="button"
                    onClick={() => field.onChange(tp)}
                    className={`rounded-lg border-2 px-6 py-2.5 text-sm font-semibold transition-colors ${
                      field.value === tp
                        ? "border-brand-500 bg-brand-50 text-brand-700"
                        : "border-line bg-white text-ink-2 hover:border-brand-300"
                    }`}
                  >
                    {tp}
                  </button>
                ))}
              </div>
            )}
          />
          <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field id="dateDebut" label="Date de début" required error={form.formState.errors.dateDebut?.message}>
              <Input id="dateDebut" type="date" {...form.register("dateDebut")} />
            </Field>
            <Field id="dateFin" label="Date de fin" error={form.formState.errors.dateFin?.message}>
              <Input
                id="dateFin"
                type="date"
                disabled={selectedType === "CDI"}
                {...form.register("dateFin")}
              />
              {selectedType === "CDI" && (
                <p className="text-[11px] text-ink-3 mt-0.5">CDI — pas de date de fin</p>
              )}
            </Field>
            <Field id="periodeEssai" label="Période d'essai (jours)">
              <Input id="periodeEssai" type="number" min={0} {...form.register("periodeEssai")} />
            </Field>
          </div>
        </Section>

        {/* Remuneration */}
        <Section title={t("new.sections.remuneration")} hint={t("new.sections.remunerationHint")}>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field id="salaireBase" label="Salaire de base (XAF)" required error={form.formState.errors.salaireBase?.message}>
              <Input id="salaireBase" type="number" min={0} step="1" {...form.register("salaireBase")} />
            </Field>
            <Field id="avantagesNature" label="Avantages en nature (XAF)">
              <Input id="avantagesNature" type="number" min={0} step="1" {...form.register("avantagesNature")} />
            </Field>
          </div>
        </Section>

        {/* Document de contrat */}
        <Section title="Document de contrat" hint="Téléverser le contrat signé (obligatoire)">
          <input
            ref={fileInputRef}
            type="file"
            accept="application/pdf"
            className="hidden"
            onChange={handleFileChange}
          />
          {pdfFile ? (
            <div className="flex items-center gap-3 rounded-lg border border-line bg-cream-dim/50 px-4 py-3">
              <FileText className="size-5 text-brand-500 shrink-0" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-ink truncate">{pdfFile.name}</p>
                <p className="text-[12px] text-ink-3">
                  {(pdfFile.size / 1024 / 1024).toFixed(2)} Mo
                </p>
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
              className="flex w-full flex-col items-center justify-center rounded-lg border-2 border-dashed border-line bg-cream-dim/50 py-10 transition-colors hover:border-brand-300 hover:bg-brand-50/30 cursor-pointer"
            >
              <Upload className="mb-2 size-6 text-ink-3" />
              <p className="text-sm font-medium text-ink-2">Téléverser le contrat signé</p>
              <p className="text-[12px] text-ink-3">PDF — max 10 Mo</p>
            </button>
          )}
        </Section>

        <div className="flex items-center justify-end gap-3">
          <Button type="button" variant="secondary" onClick={() => router.push("/contracts" as never)}>
            {tCommon("actions.cancel" as never)}
          </Button>
          <Button type="submit" disabled={createContract.isPending || !selectedEmployeeId || !pdfFile}>
            {createContract.isPending ? (
              <Loader2 className="size-4 animate-spin" />
            ) : (
              <Save className="size-4" />
            )}
            {createContract.isPending ? t("new.submitting") : t("new.submit")}
          </Button>
        </div>
      </form>
    </div>
  );
}

function Section({
  title,
  hint,
  children,
}: {
  title: string;
  hint?: string;
  children: React.ReactNode;
}) {
  return (
    <Card>
      <CardContent className="space-y-5">
        <div className="border-b border-line-soft pb-3">
          <div className="font-display text-base font-bold text-ink">{title}</div>
          {hint && <p className="mt-0.5 text-[13px] text-ink-3">{hint}</p>}
        </div>
        {children}
      </CardContent>
    </Card>
  );
}

function Field({
  id,
  label,
  required,
  error,
  children,
}: {
  id: string;
  label: string;
  required?: boolean;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="space-y-1.5">
      <Label htmlFor={id} className="flex items-center gap-1">
        <span>{label}</span>
        {required && <span className="text-brand-500">*</span>}
      </Label>
      {children}
      {error && <p className="text-[12px] text-status-red-600">{error}</p>}
    </div>
  );
}
