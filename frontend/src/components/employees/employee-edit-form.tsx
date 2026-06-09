"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AlertTriangle, Check, Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input, Label } from "@/components/ui/input";
import { SectionTitle } from "@/components/ui/section-title";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { cn, initials } from "@/lib/utils";
import type {
  EmployeeResponse,
  MobileOperator,
  PaymentChannel,
} from "@/server/ksm/modules/employees";
import type {
  PersonalInfoResponse,
  UpsertPersonalInfoRequest,
} from "@/server/ksm/modules/employee-profile";

const MARITAL_STATUSES = ["SINGLE", "MARRIED", "DIVORCED", "WIDOWED"] as const;
type MaritalStatus = (typeof MARITAL_STATUSES)[number];

function normalizeMaritalStatus(value?: string | null): "" | MaritalStatus {
  const v = (value ?? "").toUpperCase();
  return (MARITAL_STATUSES as readonly string[]).includes(v) ? (v as MaritalStatus) : "";
}

/* ── CCNI grid — same as create form ─────────────────────────────────────── */
const CCNI_CATEGORIES = [
  { value: 1,  label: "Catégorie 1",  description: "Manœuvre ordinaire",                echelons: ["A", "B"] },
  { value: 2,  label: "Catégorie 2",  description: "Manœuvre spécialisé",               echelons: ["A", "B", "C"] },
  { value: 3,  label: "Catégorie 3",  description: "Ouvrier spécialisé",                echelons: ["A", "B", "C"] },
  { value: 4,  label: "Catégorie 4",  description: "Ouvrier qualifié",                  echelons: ["A", "B", "C", "D"] },
  { value: 5,  label: "Catégorie 5",  description: "Ouvrier très qualifié",             echelons: ["A", "B", "C", "D"] },
  { value: 6,  label: "Catégorie 6",  description: "Ouvrier hautement qualifié",        echelons: ["A", "B", "C", "D"] },
  { value: 7,  label: "Catégorie 7",  description: "Technicien / Agent de maîtrise",    echelons: ["A", "B", "C", "D"] },
  { value: 8,  label: "Catégorie 8",  description: "Technicien supérieur / Maîtrise",   echelons: ["A", "B", "C", "D"] },
  { value: 9,  label: "Catégorie 9",  description: "Agent de maîtrise supérieur",       echelons: ["A", "B", "C", "D"] },
  { value: 10, label: "Catégorie 10", description: "Cadre junior",                      echelons: ["A", "B", "C"] },
  { value: 11, label: "Catégorie 11", description: "Cadre confirmé",                    echelons: ["A", "B", "C"] },
  { value: 12, label: "Catégorie 12", description: "Cadre supérieur / Dirigeant",       echelons: ["A", "B"] },
];

const SELECT_CLS =
  "w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12";

type FormValues = {
  numCnps: string;
  categorie: string;
  echelon: string;
  departmentCode: string;
  situationMatrimoniale: "" | MaritalStatus;
  modePaiement: "" | PaymentChannel;
  compteBancaire: string;
  numMobileMoney: string;
  operateurMm: "" | MobileOperator;
};

function draftKey(employeeId: string) {
  return `hrm:employee-edit-draft:${employeeId}`;
}

export function EmployeeEditForm({ employeeId }: { employeeId: string }) {
  const t = useTranslations("employees");
  const tEdit = useTranslations("employees.edit");
  const tCreate = useTranslations("employees.create");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const router = useRouter();
  const queryClient = useQueryClient();

  const [cnpsState, setCnpsState] = React.useState<"idle" | "checking" | "available" | "taken">("idle");

  /* ── Load current employee data ──────────────────────────────────── */
  const employeeQuery = useQuery({
    queryKey: ["hrm", "employee", employeeId],
    queryFn: () => apiFetch<EmployeeResponse>(`/api/hrm/employees/${employeeId}`),
    staleTime: 0,
  });
  const e = employeeQuery.data;

  /* ── Load current personal info (marital status) ─────────────────── */
  const personalInfoQuery = useQuery({
    queryKey: ["hrm", "personal-info", employeeId],
    queryFn: () =>
      apiFetch<PersonalInfoResponse | null>(
        `/api/hrm/employees/${employeeId}/personal-info`,
      ),
    staleTime: 0,
  });

  /* ── Draft restore (optional) ────────────────────────────────────── */
  const savedDraft = React.useMemo<Partial<FormValues> | null>(() => {
    if (typeof window === "undefined") return null;
    try {
      const raw = localStorage.getItem(draftKey(employeeId));
      return raw ? (JSON.parse(raw) as Partial<FormValues>) : null;
    } catch {
      return null;
    }
  }, [employeeId]);

  /* ── Form ─────────────────────────────────────────────────────────── */
  const {
    register,
    handleSubmit,
    watch,
    getValues,
    reset,
    setValue,
    control,
    setError,
    clearErrors,
    formState: { errors },
  } = useForm<FormValues>({
    defaultValues: {
      numCnps: savedDraft?.numCnps ?? "",
      categorie: savedDraft?.categorie ?? "11",
      echelon: savedDraft?.echelon ?? "A",
      departmentCode: savedDraft?.departmentCode ?? "",
      situationMatrimoniale: savedDraft?.situationMatrimoniale ?? "",
      modePaiement: savedDraft?.modePaiement ?? "",
      compteBancaire: savedDraft?.compteBancaire ?? "",
      numMobileMoney: savedDraft?.numMobileMoney ?? "",
      operateurMm: savedDraft?.operateurMm ?? "",
    },
    mode: "onTouched",
  });

  /* Populate form once employee data arrives (only if no draft) */
  const populated = React.useRef(false);
  React.useEffect(() => {
    if (e && !personalInfoQuery.isLoading && !populated.current && !savedDraft) {
      populated.current = true;
      reset({
        numCnps: e.numCnps ?? "",
        categorie: String(e.categorie),
        echelon: e.echelon ?? "A",
        departmentCode: e.departmentCode ?? "",
        situationMatrimoniale: normalizeMaritalStatus(personalInfoQuery.data?.situationMatrimoniale),
        modePaiement: (e.modePaiement as PaymentChannel) ?? "",
        compteBancaire: e.compteBancaire ?? "",
        numMobileMoney: e.numMobileMoney ?? "",
        operateurMm: (e.operateurMm as MobileOperator) ?? "",
      });
    } else if (e && !populated.current && savedDraft) {
      populated.current = true; // draft takes precedence, just mark as populated
      setValue(
        "situationMatrimoniale",
        normalizeMaritalStatus(personalInfoQuery.data?.situationMatrimoniale),
      );
    }
  }, [e, personalInfoQuery.isLoading, personalInfoQuery.data, reset, setValue, savedDraft]);

  const watched = watch();
  const isMobile =
    watched.modePaiement === "MTN_MOBILE_MONEY" || watched.modePaiement === "ORANGE_MONEY";
  const isBank = watched.modePaiement === "BANK_TRANSFER";
  const selectedCategory = CCNI_CATEGORIES.find((c) => String(c.value) === watched.categorie);
  const availableEchelons = selectedCategory?.echelons ?? ["A", "B", "C", "D"];

  /* ── CNPS inline uniqueness check ────────────────────────────────── */
  async function checkCnps(value: string) {
    // Skip check if value is unchanged from original
    if (!value.trim() || value.trim() === (e?.numCnps ?? "")) {
      setCnpsState("idle");
      clearErrors("numCnps");
      return;
    }
    setCnpsState("checking");
    try {
      const available = await apiFetch<boolean>(
        `/api/hrm/employees/check-cnps?value=${encodeURIComponent(value)}`,
      );
      if (available) {
        setCnpsState("available");
        clearErrors("numCnps");
      } else {
        setCnpsState("taken");
        setError("numCnps", { type: "manual", message: tCreate("fields.cnpsTaken") });
      }
    } catch {
      setCnpsState("idle");
    }
  }

  /* ── Submit ──────────────────────────────────────────────────────── */
  const mutation = useMutation({
    mutationFn: async (v: FormValues) => {
      const updated = await apiFetch<EmployeeResponse>(`/api/hrm/employees/${employeeId}`, {
        method: "PUT",
        body: {
          numCnps: v.numCnps.trim() || undefined,
          categorie: Number(v.categorie),
          echelon: v.echelon.trim() || undefined,
          departmentCode: v.departmentCode.trim() || undefined,
          modePaiement: v.modePaiement || undefined,
          compteBancaire: isBank && v.compteBancaire ? v.compteBancaire.trim() : undefined,
          numMobileMoney: isMobile ? v.numMobileMoney.trim() || undefined : undefined,
          operateurMm: isMobile ? v.operateurMm || undefined : undefined,
          // preserve existing managerId — backend would nullify it otherwise
          managerId: e?.managerId ?? undefined,
        },
      });

      // Persist marital status on the personal-info row when it changed.
      // We merge the existing record so other personal fields are preserved
      // (the backend upsert overwrites the whole row).
      const current: PersonalInfoResponse | null = personalInfoQuery.data ?? null;
      const nextStatus = v.situationMatrimoniale || undefined;
      const previousStatus = normalizeMaritalStatus(current?.situationMatrimoniale) || undefined;
      if (nextStatus !== previousStatus) {
        const rest: UpsertPersonalInfoRequest = {};
        if (current) {
          for (const [key, value] of Object.entries(current)) {
            if (key === "id" || key === "employeeId") continue;
            (rest as Record<string, unknown>)[key] = value;
          }
        }
        const body: UpsertPersonalInfoRequest = {
          ...rest,
          situationMatrimoniale: nextStatus,
        };
        await apiFetch<PersonalInfoResponse>(
          `/api/hrm/employees/${employeeId}/personal-info`,
          { method: "PUT", body },
        );
      }

      return updated;
    },
    onSuccess: () => {
      try { localStorage.removeItem(draftKey(employeeId)); } catch { /* ignore */ }
      toast.success(tEdit("success"));
      queryClient.invalidateQueries({ queryKey: ["hrm", "employee", employeeId] });
      queryClient.invalidateQueries({ queryKey: ["hrm", "personal-info", employeeId] });
      queryClient.invalidateQueries({ queryKey: ["hrm", "employees"] });
      router.push(`/employees/${employeeId}`);
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) {
        toast.error(cause.message);
      } else {
        toast.error(tErrors("unknown"));
      }
    },
  });

  function saveDraft() {
    try {
      localStorage.setItem(draftKey(employeeId), JSON.stringify(getValues()));
      toast.success(tEdit("draftSaved"));
    } catch {
      toast.error(tErrors("unknown"));
    }
  }

  /* ── Loading / error state ────────────────────────────────────────── */
  if (employeeQuery.isLoading) {
    return (
      <div className="grid place-items-center py-24">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (employeeQuery.error || !e) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {employeeQuery.error instanceof BffApiError
          ? employeeQuery.error.message
          : "Employee not found"}
      </div>
    );
  }

  const avatarTone = (["orange", "blue", "green", "violet", "amber", "teal"] as const)[
    e.id.charCodeAt(0) % 6
  ];

  return (
    <form onSubmit={handleSubmit((v) => mutation.mutate(v))}>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("list.title"), href: "/employees" },
          { label: e.actorDisplayName ?? e.matricule, href: `/employees/${employeeId}` },
          { label: tEdit("title") },
        ]}
        title={tEdit("title")}
        subtitle={tEdit("subtitle")}
      />

      {/* Two-column layout: form + sidebar */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_280px]">

        {/* ── Main form ───────────────────────────────────────────── */}
        <div className="space-y-8">

          {/* Section 1 — Identification administrative */}
          <section>
            <SectionTitle>{tEdit("sections.admin")}</SectionTitle>
            <Card>
              <CardContent padding="lg">
                <div className="grid grid-cols-2 gap-4">
                  {/* N° CNPS */}
                  <Field
                    label={tCreate("fields.numCnps")}
                    hint="Ex. 110428937H"
                    error={errors.numCnps?.message}
                  >
                    <div className="relative">
                      <Input
                        {...register("numCnps")}
                        placeholder="110428937H"
                        maxLength={13}
                        onBlur={(e) => checkCnps(e.target.value)}
                      />
                      {cnpsState === "checking" && (
                        <span className="absolute right-3 top-1/2 -translate-y-1/2">
                          <Loader2 className="h-3.5 w-3.5 animate-spin text-ink-4" />
                        </span>
                      )}
                      {cnpsState === "available" && (
                        <span className="absolute right-3 top-1/2 -translate-y-1/2">
                          <Check className="h-3.5 w-3.5 text-success-600" />
                        </span>
                      )}
                    </div>
                  </Field>

                  {/* Empty column for grid alignment */}
                  <div />

                  {/* Catégorie */}
                  <Field label={tCreate("fields.categorie")}>
                    <select
                      {...register("categorie", { required: true })}
                      className={SELECT_CLS}
                    >
                      {CCNI_CATEGORIES.map((c) => (
                        <option key={c.value} value={String(c.value)}>
                          {c.label} — {c.description}
                        </option>
                      ))}
                    </select>
                    {selectedCategory && (
                      <p className="mt-1 text-[11.5px] text-ink-4">
                        Échelons disponibles : {selectedCategory.echelons.join(", ")}
                      </p>
                    )}
                  </Field>

                  {/* Échelon */}
                  <Field label={tCreate("fields.echelon")}>
                    <select {...register("echelon")} className={SELECT_CLS}>
                      {availableEchelons.map((ech) => (
                        <option key={ech} value={ech}>
                          Échelon {ech}
                        </option>
                      ))}
                    </select>
                  </Field>

                  {/* Code département */}
                  <Field label={tCreate("fields.departmentCode")}>
                    <Input
                      {...register("departmentCode")}
                      placeholder="Ex. ENG, FIN, RH…"
                      maxLength={50}
                    />
                  </Field>

                  {/* Situation matrimoniale */}
                  <Field
                    label={tCreate("fields.situationMatrimoniale")}
                    hint={tCreate("fields.situationMatrimonialeHint")}
                  >
                    <select {...register("situationMatrimoniale")} className={SELECT_CLS}>
                      <option value="">—</option>
                      <option value="SINGLE">{tCreate("maritalStatus.SINGLE")}</option>
                      <option value="MARRIED">{tCreate("maritalStatus.MARRIED")}</option>
                      <option value="DIVORCED">{tCreate("maritalStatus.DIVORCED")}</option>
                      <option value="WIDOWED">{tCreate("maritalStatus.WIDOWED")}</option>
                    </select>
                  </Field>
                </div>
              </CardContent>
            </Card>
          </section>

          {/* Section 2 — Mode de paiement */}
          <section>
            <SectionTitle>{tEdit("sections.paiement")}</SectionTitle>
            <Card>
              <CardContent padding="lg">
                <div className="mb-5">
                  <Label className="mb-2.5 block text-[11.5px] font-semibold uppercase tracking-wider text-ink-3">
                    {tCreate("fields.modePaiement")}
                    <span className="ml-1 text-danger-600">*</span>
                  </Label>
                  <Controller
                    name="modePaiement"
                    control={control}
                    rules={{ required: true, validate: (v) => v !== "" }}
                    render={({ field }) => (
                      <RadioCards
                        value={field.value}
                        onChange={field.onChange}
                        options={[
                          { value: "BANK_TRANSFER",    label: t("paymentChannel.BANK_TRANSFER") },
                          { value: "MTN_MOBILE_MONEY", label: "MTN Mobile Money" },
                          { value: "ORANGE_MONEY",     label: "Orange Money" },
                          { value: "CASH",             label: t("paymentChannel.CASH") },
                        ]}
                      />
                    )}
                  />
                  {errors.modePaiement && (
                    <p className="mt-1.5 text-[11.5px] text-danger-600">
                      {tErrors("fieldRequired")}
                    </p>
                  )}
                </div>

                {(isBank || isMobile) && (
                  <div className="grid grid-cols-2 gap-4">
                    {isBank && (
                      <Field label={tCreate("fields.compteBancaire")} hint="24 chiffres">
                        <Input
                          {...register("compteBancaire")}
                          placeholder="10006 00012 00000123456 78"
                          maxLength={29}
                        />
                      </Field>
                    )}
                    {isMobile && (
                      <>
                        <Field label={tCreate("fields.numMobileMoney")} hint="+237">
                          <Input
                            {...register("numMobileMoney")}
                            placeholder="6 78 12 34 56"
                            maxLength={11}
                          />
                        </Field>
                        <div>
                          <Label className="mb-2.5 block text-[11.5px] font-semibold uppercase tracking-wider text-ink-3">
                            {tCreate("fields.operateurMm")}
                          </Label>
                          <Controller
                            name="operateurMm"
                            control={control}
                            render={({ field }) => (
                              <RadioCards
                                value={field.value}
                                onChange={field.onChange}
                                options={[
                                  { value: "MTN",    label: "MTN MoMo" },
                                  { value: "ORANGE", label: "Orange Money" },
                                ]}
                              />
                            )}
                          />
                        </div>
                      </>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>
          </section>
        </div>

        {/* ── Sidebar ─────────────────────────────────────────────── */}
        <aside className="space-y-4 lg:sticky lg:top-6 lg:self-start">
          <Card>
            <CardContent padding="lg">
              <p className="mb-3 text-[13px] font-bold text-ink">
                {tEdit("sidebar.title")}
              </p>
              {/* Employee card */}
              <div className="flex items-center gap-3 rounded-[11px] border border-line bg-white px-3.5 py-3">
                <Avatar
                  name={e.actorDisplayName ?? e.matricule}
                  initials={initials(e.actorDisplayName ?? e.matricule, 2)}
                  tone={avatarTone}
                  size="md"
                />
                <div className="min-w-0">
                  <p className="truncate text-[13px] font-semibold text-ink">
                    {e.actorDisplayName ?? "—"}
                  </p>
                  <p className="font-mono-tabular text-[11px] text-ink-3">{e.matricule}</p>
                </div>
              </div>

              {/* Salary warning */}
              <div className="mt-3 flex items-start gap-2 rounded-[10px] bg-warning-50 px-3.5 py-3">
                <AlertTriangle className="mt-0.5 h-3.5 w-3.5 shrink-0 text-warning-600" />
                <p className="text-[12px] leading-relaxed text-warning-700">
                  {tEdit("sidebar.salaryWarning")}
                </p>
              </div>
            </CardContent>
          </Card>
        </aside>
      </div>

      {/* Footer */}
      <div className="mt-8 flex items-center gap-2 border-t border-line pt-6">
        <Link href={`/employees/${employeeId}`}>
          <Button type="button" variant="ghost">
            {tCommon("actions.cancel")}
          </Button>
        </Link>
        <div className="flex-1" />
        <Button
          type="button"
          variant="secondary"
          onClick={saveDraft}
          disabled={mutation.isPending}
        >
          {tEdit("draft")}
        </Button>
        <Button
          type="submit"
          disabled={mutation.isPending || cnpsState === "taken"}
        >
          {mutation.isPending ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            tEdit("submit")
          )}
        </Button>
      </div>
    </form>
  );
}

/* ── RadioCards ─────────────────────────────────────────────────────── */

function RadioCards({
  value,
  onChange,
  options,
}: {
  value: string;
  onChange: (v: string) => void;
  options: Array<{ value: string; label: string; sub?: string }>;
}) {
  return (
    <div className="flex flex-wrap gap-2">
      {options.map((opt) => (
        <button
          key={opt.value}
          type="button"
          onClick={() => onChange(opt.value)}
          className={cn(
            "flex flex-col rounded-[11px] border px-4 py-2.5 text-left transition-all",
            value === opt.value
              ? "border-orange-400 bg-orange-50 shadow-sm"
              : "border-line bg-white hover:border-orange-300",
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
          {opt.sub && <span className="text-[11.5px] text-ink-3">{opt.sub}</span>}
        </button>
      ))}
    </div>
  );
}
