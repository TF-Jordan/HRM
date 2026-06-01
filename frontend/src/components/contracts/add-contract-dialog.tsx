"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  AlertCircle,
  AlertTriangle,
  CheckCircle2,
  ChevronRight,
  ExternalLink,
  Info,
  Loader2,
  Paperclip,
  X,
} from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { Avatar } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Label } from "@/components/ui/input";
import { Link } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate, formatMoney } from "@/lib/format";
import { cn, initials } from "@/lib/utils";
import type {
  ContractResponse,
  ContractType,
  EmployeeResponse,
} from "@/server/ksm/modules/employees";

/* ── Types ──────────────────────────────────────────────────────────── */

export interface AddContractDialogProps {
  open: boolean;
  onClose: () => void;
  /** When provided, skips the employee-selection step */
  employeeId?: string;
  employeeName?: string;
  /** Called after the contract is created successfully */
  onSuccess: () => void;
}

type Step = "select-employee" | "contract-form";

type FormValues = {
  type: ContractType;
  dateDebut: string;
  dateFin: string;
  salaireBase: string;
  avantagesNature: string;
  periodeEssai: string;
};

type StoredFile = { id: string; fileName: string };

/* ── Constants ───────────────────────────────────────────────────────── */

const AVATAR_TONES = ["orange", "blue", "green", "violet", "amber", "teal"] as const;
function avatarTone(id: string) {
  return AVATAR_TONES[id.charCodeAt(0) % AVATAR_TONES.length];
}

/* ── RadioCard sub-component ─────────────────────────────────────────── */

type ContractTypeOption = {
  value: ContractType;
  label: string;
  sub: string;
  requiresEnd: boolean;
};

function TypeRadioCards({
  value,
  onChange,
  options,
}: {
  value: ContractType;
  onChange: (v: ContractType) => void;
  options: ContractTypeOption[];
}) {
  return (
    <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-4">
      {options.map((opt) => (
        <button
          key={opt.value}
          type="button"
          onClick={() => onChange(opt.value)}
          className={cn(
            "flex flex-col gap-0.5 rounded-[12px] border px-3.5 py-3 text-left transition-all",
            value === opt.value
              ? "border-orange-400 bg-orange-50 shadow-sm ring-2 ring-orange-400/20"
              : "border-line bg-white hover:border-orange-300 hover:bg-orange-50/40",
          )}
        >
          <span
            className={cn(
              "text-[13.5px] font-semibold",
              value === opt.value ? "text-orange-700" : "text-ink",
            )}
          >
            {opt.label}
          </span>
          <span className="text-[11px] text-ink-3">{opt.sub}</span>
        </button>
      ))}
    </div>
  );
}

/* ── FileUploadField ─────────────────────────────────────────────────── */

function FileUploadField({
  file,
  uploading,
  uploaded,
  onPick,
  onClear,
}: {
  file: File | null;
  uploading: boolean;
  uploaded: StoredFile | null;
  onPick: (f: File) => void;
  onClear: () => void;
}) {
  const inputRef = React.useRef<HTMLInputElement>(null);
  return (
    <div>
      <Label className="mb-1.5 block">Contrat signé (PDF / image)</Label>
      {!file ? (
        <button
          type="button"
          onClick={() => inputRef.current?.click()}
          className="flex w-full items-center gap-2.5 rounded-[11px] border border-dashed border-line bg-bg-soft px-4 py-3 text-[13px] text-ink-3 transition hover:border-orange-300 hover:bg-orange-50"
        >
          <Paperclip className="h-4 w-4 shrink-0" />
          <span>Joindre le contrat signé et scanné…</span>
        </button>
      ) : (
        <div className="flex items-center gap-3 rounded-[11px] border border-line bg-bg-soft px-4 py-2.5">
          <Paperclip className="h-4 w-4 shrink-0 text-orange-500" />
          <div className="min-w-0 flex-1">
            <p className="truncate text-[13px] font-medium text-ink">{file.name}</p>
            {uploading && (
              <p className="text-[11.5px] text-ink-3 flex items-center gap-1">
                <Loader2 className="h-3 w-3 animate-spin" /> Envoi en cours…
              </p>
            )}
            {uploaded && !uploading && (
              <p className="text-[11.5px] text-success-600 flex items-center gap-1">
                <CheckCircle2 className="h-3 w-3" /> Fichier prêt
              </p>
            )}
          </div>
          <button
            type="button"
            onClick={onClear}
            className="shrink-0 rounded p-0.5 text-ink-4 hover:text-ink-2"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      )}
      <input
        ref={inputRef}
        type="file"
        accept="application/pdf,image/*"
        className="hidden"
        onChange={(e) => {
          const f = e.target.files?.[0];
          if (f) onPick(f);
          e.target.value = "";
        }}
      />
    </div>
  );
}

/* ── Main component ──────────────────────────────────────────────────── */

export function AddContractDialog({
  open,
  onClose,
  employeeId: propEmployeeId,
  employeeName: propEmployeeName,
  onSuccess,
}: AddContractDialogProps) {
  const t = useTranslations("contracts");
  const tErrors = useTranslations("errors");
  const tCommon = useTranslations("common");
  const queryClient = useQueryClient();

  const today = new Date().toISOString().slice(0, 10);

  /* ── Step management ─────────────────────────────────────────────── */
  const [step, setStep] = React.useState<Step>(
    propEmployeeId ? "contract-form" : "select-employee",
  );
  const [selectedEmployee, setSelectedEmployee] = React.useState<EmployeeResponse | null>(null);
  const [searchValue, setSearchValue] = React.useState("");

  /* ── File upload state ───────────────────────────────────────────── */
  const [pickedFile, setPickedFile] = React.useState<File | null>(null);
  const [uploadedFile, setUploadedFile] = React.useState<StoredFile | null>(null);
  const [isUploading, setIsUploading] = React.useState(false);

  const resolvedEmployeeId = propEmployeeId ?? selectedEmployee?.id;
  const resolvedEmployeeName =
    propEmployeeName ??
    selectedEmployee?.actorDisplayName ??
    selectedEmployee?.matricule ??
    "—";

  /* ── Fetch employees (only when step 1 is needed) ────────────────── */
  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
    enabled: open && !propEmployeeId,
    staleTime: 120_000,
  });

  /* ── Fetch contracts of selected employee (active contract check) ── */
  const selectedContracts = useQuery({
    queryKey: ["hrm", "contracts", selectedEmployee?.id],
    queryFn: () =>
      apiFetch<ContractResponse[]>(
        `/api/hrm/employees/${selectedEmployee!.id}/contracts`,
      ),
    enabled: !!selectedEmployee?.id && !propEmployeeId,
    staleTime: 60_000,
  });

  const activeContract = (selectedContracts.data ?? []).find(
    (c) => c.status === "ACTIVE" || c.status === "TRIAL",
  );

  const filteredEmployees = React.useMemo(() => {
    const all = employeesQuery.data ?? [];
    const q = searchValue.toLowerCase();
    if (!q) return all;
    return all.filter(
      (e) =>
        (e.actorDisplayName ?? "").toLowerCase().includes(q) ||
        e.matricule.toLowerCase().includes(q),
    );
  }, [employeesQuery.data, searchValue]);

  /* ── Form ────────────────────────────────────────────────────────── */
  const {
    register,
    handleSubmit,
    watch,
    control,
    reset,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    mode: "onChange",
    defaultValues: {
      type: "CDI",
      dateDebut: today,
      dateFin: "",
      salaireBase: "",
      avantagesNature: "",
      periodeEssai: "",
    },
  });

  const watchedType = watch("type");
  const watchedSalaireBase = watch("salaireBase");
  const watchedDateDebut = watch("dateDebut");
  const needsEndDate = watchedType === "CDD" || watchedType === "STAGE" || watchedType === "INTERIM";

  /* ── Handle file pick: upload immediately ────────────────────────── */
  async function handleFilePick(f: File) {
    setPickedFile(f);
    setUploadedFile(null);
    setIsUploading(true);
    try {
      const fd = new FormData();
      fd.append("file", f);
      const stored = await apiFetch<StoredFile>("/api/files/upload", {
        method: "POST",
        body: fd,
        json: false,
      });
      setUploadedFile(stored);
    } catch {
      toast.error("Échec de l'envoi du fichier");
      setPickedFile(null);
    } finally {
      setIsUploading(false);
    }
  }

  /* Reset when dialog closes */
  React.useEffect(() => {
    if (!open) {
      reset({
        type: "CDI",
        dateDebut: today,
        dateFin: "",
        salaireBase: "",
        avantagesNature: "",
        periodeEssai: "",
      });
      setStep(propEmployeeId ? "contract-form" : "select-employee");
      setSelectedEmployee(null);
      setSearchValue("");
      setPickedFile(null);
      setUploadedFile(null);
      setIsUploading(false);
    }
  }, [open, propEmployeeId, reset, today]);

  /* ── Mutation ────────────────────────────────────────────────────── */
  const mutation = useMutation({
    mutationFn: (v: FormValues) => {
      if (!resolvedEmployeeId) throw new Error("No employee selected");
      return apiFetch<ContractResponse>(
        `/api/hrm/employees/${resolvedEmployeeId}/contracts`,
        {
          method: "POST",
          body: {
            type: v.type,
            dateDebut: v.dateDebut,
            dateFin: needsEndDate && v.dateFin ? v.dateFin : undefined,
            salaireBase: Number(v.salaireBase),
            avantagesNature: v.avantagesNature ? Number(v.avantagesNature) : undefined,
            periodeEssai: v.periodeEssai ? Number(v.periodeEssai) : undefined,
            documentFileId: uploadedFile?.id ?? undefined,
          },
        },
      );
    },
    onSuccess: () => {
      toast.success(t("form.success"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "contracts", resolvedEmployeeId] });
      queryClient.invalidateQueries({ queryKey: ["hrm", "contracts"] });
      onSuccess();
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) toast.error(cause.message);
      else toast.error(tErrors("unknown"));
    },
  });

  /* ── Contract type options ────────────────────────────────────────── */
  const typeOptions: ContractTypeOption[] = [
    { value: "CDI", label: t("contractType.CDI"), sub: "Sans fin fixée", requiresEnd: false },
    { value: "CDD", label: t("contractType.CDD"), sub: "Durée déterminée", requiresEnd: true },
    { value: "STAGE", label: t("contractType.STAGE"), sub: "Convention stage", requiresEnd: true },
    { value: "INTERIM", label: t("contractType.INTERIM"), sub: "Mission temporaire", requiresEnd: true },
  ];

  /* ── Salary preview ──────────────────────────────────────────────── */
  const salaryPreview = watchedSalaireBase
    ? formatMoney(Number(watchedSalaireBase), { withCurrency: true })
    : null;

  /* ── Render: step 1 — employee selection ─────────────────────────── */
  if (step === "select-employee") {
    const hasActiveContract = !!activeContract;
    const contractsLoading = selectedContracts.isLoading && !!selectedEmployee;

    return (
      <Dialog
        open={open}
        onClose={onClose}
        title={t("form.title")}
        subtitle={t("form.stepEmployeeHint")}
        size="md"
        footer={
          <>
            <Button type="button" variant="ghost" onClick={onClose}>
              {tCommon("actions.cancel")}
            </Button>
            <Button
              type="button"
              disabled={!selectedEmployee || contractsLoading || hasActiveContract}
              onClick={() => setStep("contract-form")}
            >
              {contractsLoading ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <>
                  {t("form.next")}
                  <ChevronRight className="h-4 w-4" />
                </>
              )}
            </Button>
          </>
        }
      >
        <div className="flex flex-col gap-4">
          {/* Search input */}
          <Input
            type="search"
            placeholder={t("form.searchEmployee")}
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            autoFocus
          />

          {/* Employee list */}
          {employeesQuery.isLoading ? (
            <div className="flex justify-center py-6">
              <Loader2 className="h-6 w-6 animate-spin text-orange-500" />
            </div>
          ) : filteredEmployees.length === 0 ? (
            <p className="py-4 text-center text-[13px] text-ink-3">
              {t("form.noEmployees")}
            </p>
          ) : (
            <div className="max-h-56 overflow-y-auto rounded-[12px] border border-line">
              {filteredEmployees.map((emp, idx) => (
                <button
                  key={emp.id}
                  type="button"
                  onClick={() => setSelectedEmployee(emp)}
                  className={cn(
                    "flex w-full items-center gap-3 px-4 py-3 text-left transition-colors",
                    "hover:bg-orange-50",
                    idx > 0 && "border-t border-line-soft",
                    selectedEmployee?.id === emp.id && "bg-orange-50",
                  )}
                >
                  <Avatar
                    name={emp.actorDisplayName ?? emp.matricule}
                    initials={initials(emp.actorDisplayName ?? emp.matricule, 2)}
                    tone={avatarTone(emp.id)}
                    size="sm"
                  />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-[13px] font-semibold text-ink">
                      {emp.actorDisplayName ?? "—"}
                    </p>
                    <p className="font-mono-tabular text-[11px] text-ink-3">{emp.matricule}</p>
                  </div>
                  {selectedEmployee?.id === emp.id && (
                    <CheckCircle2 className="h-4 w-4 shrink-0 text-orange-500" />
                  )}
                </button>
              ))}
            </div>
          )}

          {/* Active contract warning */}
          {selectedEmployee && !contractsLoading && hasActiveContract && activeContract && (
            <div className="rounded-[12px] border border-warning-300 bg-warning-50 p-4">
              <div className="flex items-start gap-3">
                <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-warning-600" />
                <div className="flex-1 min-w-0">
                  <p className="text-[13px] font-semibold text-warning-700">
                    Contrat actif existant
                  </p>
                  <p className="mt-0.5 text-[12px] text-warning-600">
                    {selectedEmployee.actorDisplayName ?? selectedEmployee.matricule} possède déjà
                    un contrat{" "}
                    <strong>{t(`contractType.${activeContract.type as ContractType}`)}</strong> actif
                    depuis le{" "}
                    <strong>{formatDate(activeContract.dateDebut, { locale: "fr" })}</strong>.
                    Pour créer un nouveau contrat, résiliez d'abord le contrat existant.
                  </p>
                </div>
              </div>
              <Link
                href={`/contracts/${activeContract.id}?employeeId=${selectedEmployee.id}&from=employee`}
                className="mt-3 flex w-full items-center justify-center gap-1.5 rounded-[10px] border border-warning-300 bg-white py-2 text-[12.5px] font-semibold text-warning-700 transition hover:bg-warning-50"
              >
                Modifier le contrat existant
                <ExternalLink className="h-3.5 w-3.5" />
              </Link>
            </div>
          )}
        </div>
      </Dialog>
    );
  }

  /* ── Render: step 2 — contract form ──────────────────────────────── */
  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={t("form.title")}
      size="lg"
      footer={
        <>
          {!propEmployeeId && (
            <Button
              type="button"
              variant="ghost"
              onClick={() => setStep("select-employee")}
              className="mr-auto"
            >
              ← {t("form.stepEmployee")}
            </Button>
          )}
          <Button type="button" variant="ghost" onClick={onClose}>
            {tCommon("actions.cancel")}
          </Button>
          <Button
            type="button"
            disabled={!isValid || !resolvedEmployeeId || mutation.isPending || isUploading}
            onClick={handleSubmit((v) => mutation.mutate(v))}
          >
            {mutation.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              t("form.submit")
            )}
          </Button>
        </>
      }
    >
      <form className="flex flex-col gap-6">

        {/* Employee summary card (shown when from global page) */}
        {!propEmployeeId && selectedEmployee && (
          <div className="flex items-center gap-3 rounded-[12px] border border-orange-200 bg-orange-50 px-4 py-3">
            <Avatar
              name={resolvedEmployeeName}
              initials={initials(resolvedEmployeeName, 2)}
              tone={avatarTone(selectedEmployee.id)}
              size="sm"
            />
            <div className="min-w-0">
              <p className="truncate text-[13px] font-semibold text-ink">{resolvedEmployeeName}</p>
              <p className="font-mono-tabular text-[11px] text-ink-3">
                {selectedEmployee.matricule}
              </p>
            </div>
          </div>
        )}

        {/* ── Section 1: Type & durée ─────────────────────────────── */}
        <section>
          <Label className="mb-3 block text-[11.5px] font-semibold uppercase tracking-wider text-ink-3">
            {t("form.contractSection")}
          </Label>

          {/* RadioCards */}
          <div className="mb-4">
            <Controller
              name="type"
              control={control}
              rules={{ required: true }}
              render={({ field }) => (
                <TypeRadioCards
                  value={field.value}
                  onChange={field.onChange}
                  options={typeOptions}
                />
              )}
            />
          </div>

          {/* Type hint */}
          <div
            className={cn(
              "mb-4 flex items-start gap-2 rounded-[10px] px-3.5 py-2.5 text-[12.5px]",
              needsEndDate
                ? "bg-warning-50 text-warning-700"
                : "bg-success-50 text-success-700",
            )}
          >
            <Info className="mt-0.5 h-3.5 w-3.5 shrink-0" />
            <span>
              {needsEndDate ? t("form.fixedTermHint") : t("form.permanentHint")}
            </span>
          </div>

          {/* Dates */}
          <div className="grid grid-cols-2 gap-4">
            <Field
              label={t("form.fields.dateDebut")}
              error={errors.dateDebut?.message}
            >
              <Input
                type="date"
                {...register("dateDebut", { required: true })}
              />
            </Field>

            <Field
              label={t("form.fields.dateFin")}
              error={errors.dateFin?.message}
            >
              <Input
                type="date"
                disabled={!needsEndDate}
                {...register("dateFin", {
                  required: needsEndDate
                    ? t("form.fields.dateFinRequired")
                    : false,
                  validate: (v) => {
                    if (!needsEndDate || !v) return true;
                    return v > watchedDateDebut || t("form.fields.dateFinFuture");
                  },
                })}
                className={!needsEndDate ? "opacity-40" : ""}
              />
            </Field>

            <Field
              label={t("form.fields.periodeEssai")}
              hint={t("form.trialHint")}
            >
              <Input
                type="number"
                min="0"
                max="365"
                placeholder="0"
                {...register("periodeEssai", {
                  min: { value: 0, message: "≥ 0" },
                  max: { value: 365, message: "≤ 365" },
                })}
              />
            </Field>
          </div>
        </section>

        {/* ── Section 2: Rémunération ──────────────────────────────── */}
        <section>
          <Label className="mb-3 block text-[11.5px] font-semibold uppercase tracking-wider text-ink-3">
            {t("form.compensationSection")}
          </Label>

          <div className="grid grid-cols-2 gap-4">
            <Field
              label={t("form.fields.salaireBase")}
              error={errors.salaireBase?.message}
            >
              <div className="relative">
                <Input
                  type="number"
                  min="1"
                  placeholder="500 000"
                  {...register("salaireBase", {
                    required: "Requis",
                    min: { value: 1, message: "Salaire requis" },
                  })}
                  className="pr-14"
                />
                <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[11.5px] font-semibold text-ink-3">
                  XAF
                </span>
              </div>
              {salaryPreview && (
                <p className="mt-1 text-[11.5px] text-ink-3">{salaryPreview}</p>
              )}
            </Field>

            <Field
              label={t("form.fields.avantagesNature")}
              hint="Optionnel"
            >
              <div className="relative">
                <Input
                  type="number"
                  min="0"
                  placeholder="0"
                  {...register("avantagesNature", { min: 0 })}
                  className="pr-14"
                />
                <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-[11.5px] font-semibold text-ink-3">
                  XAF
                </span>
              </div>
            </Field>
          </div>
        </section>

        {/* ── Section 3: Document ─────────────────────────────────── */}
        <section>
          <Label className="mb-3 block text-[11.5px] font-semibold uppercase tracking-wider text-ink-3">
            Document
          </Label>
          <FileUploadField
            file={pickedFile}
            uploading={isUploading}
            uploaded={uploadedFile}
            onPick={handleFilePick}
            onClear={() => {
              setPickedFile(null);
              setUploadedFile(null);
            }}
          />
        </section>

        {/* ── Summary preview ──────────────────────────────────────── */}
        {watchedSalaireBase && resolvedEmployeeId && (
          <div className="rounded-[12px] border border-line bg-bg-soft px-4 py-3">
            <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-wider text-ink-3">
              Récapitulatif
            </p>
            <div className="flex flex-wrap gap-x-6 gap-y-1 text-[12.5px]">
              <span className="text-ink-2">
                Type :{" "}
                <strong className="text-ink">
                  {typeOptions.find((o) => o.value === watchedType)?.label}
                </strong>
              </span>
              <span className="text-ink-2">
                Salaire :{" "}
                <strong className="text-ink">{salaryPreview ?? "—"} / mois</strong>
              </span>
              {watch("periodeEssai") && (
                <span className="text-ink-2">
                  Essai :{" "}
                  <strong className="text-ink">{watch("periodeEssai")} jours</strong>
                </span>
              )}
              {uploadedFile && (
                <span className="text-ink-2">
                  Document :{" "}
                  <strong className="text-success-600">{uploadedFile.fileName}</strong>
                </span>
              )}
            </div>
          </div>
        )}

        {/* API error */}
        {mutation.isError && (
          <div className="flex items-center gap-2 rounded-[10px] bg-danger-50 px-3.5 py-2.5 text-[12.5px] text-danger-700">
            <AlertCircle className="h-4 w-4 shrink-0" />
            {mutation.error instanceof BffApiError
              ? mutation.error.message
              : tErrors("unknown")}
          </div>
        )}
      </form>
    </Dialog>
  );
}
