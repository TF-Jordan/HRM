"use client";

import { useMutation } from "@tanstack/react-query";
import { ChevronLeft, ChevronRight, Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm, type FieldErrors, type Path } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input, Label } from "@/components/ui/input";
import { SectionTitle } from "@/components/ui/section-title";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatMoney } from "@/lib/format";
import { cn } from "@/lib/utils";
import type {
  ContractType,
  MobileOperator,
  PaymentChannel,
} from "@/server/ksm/modules/employees";

type FormValues = {
  // Identity
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  gender: "" | "MALE" | "FEMALE" | "OTHER";
  nationality: string;
  birthDate: string;
  // HR
  categorie: string;
  echelon: string;
  departmentCode: string;
  dateEmbauche: string;
  numCnps: string;
  contractType: ContractType;
  contractDateDebut: string;
  contractDateFin: string;
  periodeEssai: string;
  // Compensation
  salaireBase: string;
  avantagesNature: string;
  modePaiement: "" | PaymentChannel;
  compteBancaire: string;
  numMobileMoney: string;
  operateurMm: "" | MobileOperator;
};

const STEPS = ["identity", "hr", "compensation", "summary"] as const;
type Step = (typeof STEPS)[number];

const STEP_FIELDS: Record<Step, Array<keyof FormValues>> = {
  identity: ["firstName", "lastName", "email", "phoneNumber", "gender", "nationality", "birthDate"],
  hr: [
    "categorie",
    "echelon",
    "departmentCode",
    "dateEmbauche",
    "numCnps",
    "contractType",
    "contractDateDebut",
    "contractDateFin",
    "periodeEssai",
  ],
  compensation: [
    "salaireBase",
    "avantagesNature",
    "modePaiement",
    "compteBancaire",
    "numMobileMoney",
    "operateurMm",
  ],
  summary: [],
};

export function EmployeeCreateForm() {
  const t = useTranslations("employees");
  const tCreate = useTranslations("employees.create");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const router = useRouter();

  const [step, setStep] = React.useState<Step>("identity");
  const stepIndex = STEPS.indexOf(step);

  const {
    register,
    handleSubmit,
    trigger,
    watch,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      phoneNumber: "",
      gender: "",
      nationality: "CMR",
      birthDate: "",
      categorie: "5",
      echelon: "A",
      departmentCode: "",
      dateEmbauche: new Date().toISOString().slice(0, 10),
      numCnps: "",
      contractType: "CDI",
      contractDateDebut: new Date().toISOString().slice(0, 10),
      contractDateFin: "",
      periodeEssai: "",
      salaireBase: "",
      avantagesNature: "",
      modePaiement: "",
      compteBancaire: "",
      numMobileMoney: "",
      operateurMm: "",
    },
    mode: "onTouched",
  });

  const watchedAll = watch();
  const isCdd = watchedAll.contractType === "CDD" || watchedAll.contractType === "STAGE";
  const isMobile = watchedAll.modePaiement === "MTN_MOBILE_MONEY" || watchedAll.modePaiement === "ORANGE_MONEY";

  const mutation = useMutation({
    mutationFn: (v: FormValues) =>
      apiFetch<{ employeeId: string; matricule: string }>("/api/hrm/employees", {
        method: "POST",
        body: {
          firstName: v.firstName.trim(),
          lastName: v.lastName.trim(),
          email: v.email.trim().toLowerCase(),
          phoneNumber: v.phoneNumber.trim() || undefined,
          gender: v.gender || undefined,
          nationality: v.nationality.trim() || undefined,
          birthDate: v.birthDate || undefined,
          categorie: Number(v.categorie),
          echelon: v.echelon.trim() || undefined,
          departmentCode: v.departmentCode.trim() || undefined,
          dateEmbauche: v.dateEmbauche,
          numCnps: v.numCnps.trim() || undefined,
          contractType: v.contractType,
          contractDateDebut: v.contractDateDebut,
          contractDateFin: isCdd && v.contractDateFin ? v.contractDateFin : undefined,
          periodeEssai: v.periodeEssai ? Number(v.periodeEssai) : undefined,
          salaireBase: Number(v.salaireBase),
          avantagesNature: v.avantagesNature ? Number(v.avantagesNature) : undefined,
          modePaiement: v.modePaiement || undefined,
          compteBancaire: !isMobile && v.compteBancaire ? v.compteBancaire.trim() : undefined,
          numMobileMoney: isMobile ? v.numMobileMoney.trim() || undefined : undefined,
          operateurMm: isMobile ? v.operateurMm || undefined : undefined,
        },
      }),
    onSuccess: (result) => {
      toast.success(tCreate("success") + " · " + result.matricule);
      router.push(`/employees/${result.employeeId}`);
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) {
        toast.error(cause.message);
      } else {
        toast.error(tErrors("unknown"));
      }
    },
  });

  async function goNext() {
    const ok = await trigger(STEP_FIELDS[step] as Path<FormValues>[]);
    if (!ok) return;
    const next = STEPS[stepIndex + 1];
    if (next) setStep(next);
  }
  function goBack() {
    const prev = STEPS[stepIndex - 1];
    if (prev) setStep(prev);
  }

  return (
    <form onSubmit={handleSubmit((v) => mutation.mutate(v))}>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: tCreate("title").replace("Créer un ", "").replace("Create an ", "") + "s", href: "/employees" },
          { label: tCreate("title") },
        ]}
        title={tCreate("title")}
        subtitle={tCreate("subtitle")}
        actions={
          <Link href="/employees">
            <Button type="button" variant="secondary">
              {tCommon("actions.cancel")}
            </Button>
          </Link>
        }
      />

      {/* Stepper */}
      <div className="mb-7 flex flex-wrap items-center gap-2">
        {STEPS.map((s, idx) => {
          const done = idx < stepIndex;
          const active = idx === stepIndex;
          return (
            <React.Fragment key={s}>
              <button
                type="button"
                onClick={() => idx < stepIndex && setStep(s)}
                className={cn(
                  "rounded-full border px-3.5 py-1.5 text-[11.5px] font-semibold tracking-wider transition-all",
                  done && "border-transparent bg-success-50 text-success-600",
                  active && "border-transparent bg-grad-orange text-white shadow-orange-brand",
                  !done && !active && "border-line bg-white text-ink-3",
                )}
              >
                <span className="mr-1.5 font-mono-tabular text-[10px] opacity-70">{idx + 1}</span>
                {tCreate(`steps.${s}`)}
              </button>
              {idx < STEPS.length - 1 && (
                <ChevronRight className="h-3.5 w-3.5 shrink-0 text-ink-4" aria-hidden="true" />
              )}
            </React.Fragment>
          );
        })}
      </div>

      {step === "identity" && (
        <IdentityStep register={register} errors={errors} tCreate={tCreate} />
      )}
      {step === "hr" && (
        <HrStep register={register} errors={errors} tCreate={tCreate} t={t} isCdd={isCdd} />
      )}
      {step === "compensation" && (
        <CompensationStep
          register={register}
          errors={errors}
          tCreate={tCreate}
          t={t}
          isMobile={isMobile}
        />
      )}
      {step === "summary" && <Summary values={watchedAll} t={t} tCreate={tCreate} />}

      <div className="mt-6 flex items-center justify-end gap-2">
        {stepIndex > 0 && (
          <Button type="button" variant="secondary" onClick={goBack}>
            <ChevronLeft className="h-4 w-4" />
            {tCommon("actions.previous")}
          </Button>
        )}
        {stepIndex < STEPS.length - 1 ? (
          <Button type="button" onClick={goNext}>
            {tCommon("actions.next")}
            <ChevronRight className="h-4 w-4" />
          </Button>
        ) : (
          <Button type="submit" disabled={!isValid || mutation.isPending}>
            {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : tCreate("submit")}
          </Button>
        )}
      </div>
    </form>
  );
}

/* ============================== Steps ============================== */

type RegProps<K extends keyof FormValues> = {
  register: ReturnType<typeof useForm<FormValues>>["register"];
  errors: FieldErrors<FormValues>;
  tCreate: ReturnType<typeof useTranslations<"employees.create">>;
};

function IdentityStep({
  register,
  errors,
  tCreate,
}: RegProps<keyof FormValues>) {
  return (
    <>
      <SectionTitle>{tCreate("steps.identity")}</SectionTitle>
      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field
              label={tCreate("fields.firstName")}
              error={errors.firstName && (errors.firstName.message || "—")}
            >
              <Input {...register("firstName", { required: true, minLength: 2 })} />
            </Field>
            <Field
              label={tCreate("fields.lastName")}
              error={errors.lastName && (errors.lastName.message || "—")}
            >
              <Input {...register("lastName", { required: true, minLength: 2 })} />
            </Field>
            <Field
              label={tCreate("fields.email")}
              error={errors.email && (errors.email.message || "—")}
            >
              <Input
                type="email"
                {...register("email", { required: true, pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/ })}
                placeholder="prenom.nom@example.com"
              />
            </Field>
            <Field label={tCreate("fields.phoneNumber")}>
              <Input {...register("phoneNumber")} placeholder="+237 6XX XX XX XX" />
            </Field>
            <Field label={tCreate("fields.gender")}>
              <select
                {...register("gender")}
                className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
              >
                <option value="">—</option>
                <option value="MALE">{tCreate("gender.MALE")}</option>
                <option value="FEMALE">{tCreate("gender.FEMALE")}</option>
                <option value="OTHER">{tCreate("gender.OTHER")}</option>
              </select>
            </Field>
            <Field label={tCreate("fields.nationality")}>
              <Input {...register("nationality")} placeholder="CMR" />
            </Field>
            <Field label={tCreate("fields.birthDate")}>
              <Input type="date" {...register("birthDate")} />
            </Field>
          </div>
        </CardContent>
      </Card>
    </>
  );
}

function HrStep({
  register,
  errors,
  tCreate,
  t,
  isCdd,
}: RegProps<keyof FormValues> & {
  t: ReturnType<typeof useTranslations<"employees">>;
  isCdd: boolean;
}) {
  return (
    <>
      <SectionTitle>{tCreate("steps.hr")}</SectionTitle>
      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-3 gap-4">
            <Field
              label={tCreate("fields.categorie")}
              error={errors.categorie && (errors.categorie.message || "—")}
            >
              <Input
                type="number"
                min="1"
                max="12"
                {...register("categorie", { required: true, min: 1, max: 12 })}
              />
            </Field>
            <Field label={tCreate("fields.echelon")}>
              <Input {...register("echelon")} placeholder="A" />
            </Field>
            <Field label={tCreate("fields.departmentCode")}>
              <Input {...register("departmentCode")} placeholder="IT" />
            </Field>
            <Field
              label={tCreate("fields.dateEmbauche")}
              error={errors.dateEmbauche && (errors.dateEmbauche.message || "—")}
            >
              <Input type="date" {...register("dateEmbauche", { required: true })} />
            </Field>
            <Field label={tCreate("fields.numCnps")}>
              <Input {...register("numCnps")} placeholder="123456789" />
            </Field>
            <div />
          </div>

          <div className="my-5 h-px bg-line-soft" />

          <div className="grid grid-cols-3 gap-4">
            <Field
              label={tCreate("fields.contractType")}
              error={errors.contractType && (errors.contractType.message || "—")}
            >
              <select
                {...register("contractType", { required: true })}
                className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
              >
                <option value="CDI">{t("contractType.CDI")}</option>
                <option value="CDD">{t("contractType.CDD")}</option>
                <option value="STAGE">{t("contractType.STAGE")}</option>
                <option value="INTERIM">{t("contractType.INTERIM")}</option>
              </select>
            </Field>
            <Field
              label={tCreate("fields.contractDateDebut")}
              error={errors.contractDateDebut && (errors.contractDateDebut.message || "—")}
            >
              <Input type="date" {...register("contractDateDebut", { required: true })} />
            </Field>
            <Field
              label={tCreate("fields.contractDateFin")}
              hint={!isCdd ? "—" : undefined}
            >
              <Input
                type="date"
                disabled={!isCdd}
                {...register("contractDateFin", { required: isCdd })}
              />
            </Field>
            <Field label={tCreate("fields.periodeEssai")}>
              <Input type="number" min="0" max="365" {...register("periodeEssai")} />
            </Field>
          </div>
        </CardContent>
      </Card>
    </>
  );
}

function CompensationStep({
  register,
  errors,
  tCreate,
  t,
  isMobile,
}: RegProps<keyof FormValues> & {
  t: ReturnType<typeof useTranslations<"employees">>;
  isMobile: boolean;
}) {
  return (
    <>
      <SectionTitle>{tCreate("steps.compensation")}</SectionTitle>
      <Card>
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field
              label={tCreate("fields.salaireBase")}
              error={errors.salaireBase && (errors.salaireBase.message || "—")}
            >
              <Input
                type="number"
                min="1"
                {...register("salaireBase", { required: true, min: 1 })}
                placeholder="350 000"
              />
            </Field>
            <Field label={tCreate("fields.avantagesNature")}>
              <Input type="number" min="0" {...register("avantagesNature")} placeholder="0" />
            </Field>
          </div>

          <div className="my-5 h-px bg-line-soft" />

          <div className="grid grid-cols-2 gap-4">
            <Field label={tCreate("fields.modePaiement")}>
              <select
                {...register("modePaiement")}
                className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
              >
                <option value="">—</option>
                <option value="BANK_TRANSFER">{t("paymentChannel.BANK_TRANSFER")}</option>
                <option value="MTN_MOBILE_MONEY">{t("paymentChannel.MTN_MOBILE_MONEY")}</option>
                <option value="ORANGE_MONEY">{t("paymentChannel.ORANGE_MONEY")}</option>
                <option value="CASH">{t("paymentChannel.CASH")}</option>
              </select>
            </Field>
            {isMobile ? (
              <>
                <Field label={tCreate("fields.numMobileMoney")}>
                  <Input {...register("numMobileMoney")} placeholder="6XXXXXXXX" />
                </Field>
                <Field label={tCreate("fields.operateurMm")}>
                  <select
                    {...register("operateurMm")}
                    className="w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12"
                  >
                    <option value="">—</option>
                    <option value="MTN">{tCreate("operator.MTN")}</option>
                    <option value="ORANGE">{tCreate("operator.ORANGE")}</option>
                  </select>
                </Field>
              </>
            ) : (
              <Field label={tCreate("fields.compteBancaire")}>
                <Input {...register("compteBancaire")} placeholder="100012345678" />
              </Field>
            )}
          </div>
        </CardContent>
      </Card>
    </>
  );
}

function Summary({
  values,
  t,
  tCreate,
}: {
  values: FormValues;
  t: ReturnType<typeof useTranslations<"employees">>;
  tCreate: ReturnType<typeof useTranslations<"employees.create">>;
}) {
  const fullName = `${values.firstName} ${values.lastName}`.trim();
  const items = [
    [tCreate("fields.firstName") + " / " + tCreate("fields.lastName"), fullName],
    [tCreate("summaryEmail"), values.email],
    [tCreate("fields.phoneNumber"), values.phoneNumber || "—"],
    [tCreate("fields.dateEmbauche"), values.dateEmbauche],
    [tCreate("fields.contractType"), t(`contractType.${values.contractType}`)],
    [tCreate("fields.categorie") + " / " + tCreate("fields.echelon"), `${values.categorie} / ${values.echelon || "—"}`],
    [
      tCreate("fields.salaireBase"),
      values.salaireBase ? formatMoney(Number(values.salaireBase), { locale: "fr" }) : "—",
    ],
    [
      tCreate("fields.modePaiement"),
      values.modePaiement ? t(`paymentChannel.${values.modePaiement}`) : "—",
    ],
  ] as const;

  return (
    <>
      <SectionTitle>{tCreate("steps.summary")}</SectionTitle>
      <Card>
        <CardContent padding="lg">
          <dl className="grid grid-cols-2 gap-x-8 gap-y-3.5">
            {items.map(([k, v]) => (
              <div key={k} className="flex flex-col">
                <Label className="text-[11.5px] uppercase tracking-wider text-ink-4">{k}</Label>
                <span className="mt-0.5 text-[14px] font-medium text-ink">{v}</span>
              </div>
            ))}
          </dl>
        </CardContent>
      </Card>
    </>
  );
}
