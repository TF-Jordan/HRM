"use client";

import { useMutation } from "@tanstack/react-query";
import { Check, Copy, Eye, EyeOff, Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { Controller, useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Label } from "@/components/ui/input";
import { SectionTitle } from "@/components/ui/section-title";
import { Link, useRouter } from "@/i18n/navigation";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { cn } from "@/lib/utils";
import type {
  MobileOperator,
  PaymentChannel,
} from "@/server/ksm/modules/employees";

const DRAFT_KEY = "hrm:employee-draft";

type CreatedEmployeeResult = {
  employeeId: string;
  matricule: string;
  login?: {
    userId: string;
    username: string;
    email: string;
    temporaryPassword: string;
    rolesAssigned: number;
    welcomeMailSent: boolean;
    welcomeMailProvider: string;
  };
  warnings: string[];
};

const SELECT_CLS =
  "w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12";

/* ── CCNI Cameroun — Convention Collective Nationale Interprofessionnelle ─ */
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

/* ── Countries (ISO 3166-1 alpha-3) — Cameroon first ─────────────────────── */
const COUNTRIES: Array<{ code: string; name: string }> = [
  { code: "CMR", name: "Cameroun" },
  { code: "AGO", name: "Angola" },
  { code: "BEN", name: "Bénin" },
  { code: "BFA", name: "Burkina Faso" },
  { code: "BDI", name: "Burundi" },
  { code: "CPV", name: "Cap-Vert" },
  { code: "CAF", name: "Centrafrique" },
  { code: "COM", name: "Comores" },
  { code: "COD", name: "Congo (RDC)" },
  { code: "COG", name: "Congo (Brazzaville)" },
  { code: "CIV", name: "Côte d'Ivoire" },
  { code: "DJI", name: "Djibouti" },
  { code: "EGY", name: "Égypte" },
  { code: "ERI", name: "Érythrée" },
  { code: "ETH", name: "Éthiopie" },
  { code: "GAB", name: "Gabon" },
  { code: "GMB", name: "Gambie" },
  { code: "GHA", name: "Ghana" },
  { code: "GIN", name: "Guinée" },
  { code: "GNB", name: "Guinée-Bissau" },
  { code: "GNQ", name: "Guinée Équatoriale" },
  { code: "KEN", name: "Kenya" },
  { code: "LSO", name: "Lesotho" },
  { code: "LBR", name: "Libéria" },
  { code: "LBY", name: "Libye" },
  { code: "MDG", name: "Madagascar" },
  { code: "MWI", name: "Malawi" },
  { code: "MLI", name: "Mali" },
  { code: "MRT", name: "Mauritanie" },
  { code: "MUS", name: "Maurice" },
  { code: "MAR", name: "Maroc" },
  { code: "MOZ", name: "Mozambique" },
  { code: "NAM", name: "Namibie" },
  { code: "NER", name: "Niger" },
  { code: "NGA", name: "Nigéria" },
  { code: "RWA", name: "Rwanda" },
  { code: "STP", name: "Sao Tomé-et-Príncipe" },
  { code: "SEN", name: "Sénégal" },
  { code: "SYC", name: "Seychelles" },
  { code: "SLE", name: "Sierra Leone" },
  { code: "SOM", name: "Somalie" },
  { code: "ZAF", name: "Afrique du Sud" },
  { code: "SSD", name: "Soudan du Sud" },
  { code: "SDN", name: "Soudan" },
  { code: "SWZ", name: "Eswatini" },
  { code: "TZA", name: "Tanzanie" },
  { code: "TCD", name: "Tchad" },
  { code: "TGO", name: "Togo" },
  { code: "TUN", name: "Tunisie" },
  { code: "UGA", name: "Ouganda" },
  { code: "ZMB", name: "Zambie" },
  { code: "ZWE", name: "Zimbabwe" },
  { code: "DZA", name: "Algérie" },
  { code: "ALB", name: "Albanie" },
  { code: "DEU", name: "Allemagne" },
  { code: "AND", name: "Andorre" },
  { code: "AUT", name: "Autriche" },
  { code: "BLR", name: "Biélorussie" },
  { code: "BEL", name: "Belgique" },
  { code: "BIH", name: "Bosnie-Herzégovine" },
  { code: "BGR", name: "Bulgarie" },
  { code: "CYP", name: "Chypre" },
  { code: "HRV", name: "Croatie" },
  { code: "DNK", name: "Danemark" },
  { code: "ESP", name: "Espagne" },
  { code: "EST", name: "Estonie" },
  { code: "FIN", name: "Finlande" },
  { code: "FRA", name: "France" },
  { code: "GRC", name: "Grèce" },
  { code: "HUN", name: "Hongrie" },
  { code: "IRL", name: "Irlande" },
  { code: "ISL", name: "Islande" },
  { code: "ITA", name: "Italie" },
  { code: "LVA", name: "Lettonie" },
  { code: "LIE", name: "Liechtenstein" },
  { code: "LTU", name: "Lituanie" },
  { code: "LUX", name: "Luxembourg" },
  { code: "MKD", name: "Macédoine du Nord" },
  { code: "MLT", name: "Malte" },
  { code: "MDA", name: "Moldavie" },
  { code: "MCO", name: "Monaco" },
  { code: "MNE", name: "Monténégro" },
  { code: "NOR", name: "Norvège" },
  { code: "NLD", name: "Pays-Bas" },
  { code: "POL", name: "Pologne" },
  { code: "PRT", name: "Portugal" },
  { code: "CZE", name: "République tchèque" },
  { code: "ROU", name: "Roumanie" },
  { code: "GBR", name: "Royaume-Uni" },
  { code: "RUS", name: "Russie" },
  { code: "SMR", name: "Saint-Marin" },
  { code: "SRB", name: "Serbie" },
  { code: "SVK", name: "Slovaquie" },
  { code: "SVN", name: "Slovénie" },
  { code: "SWE", name: "Suède" },
  { code: "CHE", name: "Suisse" },
  { code: "UKR", name: "Ukraine" },
  { code: "VAT", name: "Vatican" },
  { code: "ARG", name: "Argentine" },
  { code: "BOL", name: "Bolivie" },
  { code: "BRA", name: "Brésil" },
  { code: "CHL", name: "Chili" },
  { code: "COL", name: "Colombie" },
  { code: "CRI", name: "Costa Rica" },
  { code: "CUB", name: "Cuba" },
  { code: "DOM", name: "République dominicaine" },
  { code: "ECU", name: "Équateur" },
  { code: "GTM", name: "Guatemala" },
  { code: "HTI", name: "Haïti" },
  { code: "HND", name: "Honduras" },
  { code: "JAM", name: "Jamaïque" },
  { code: "MEX", name: "Mexique" },
  { code: "NIC", name: "Nicaragua" },
  { code: "PAN", name: "Panama" },
  { code: "PRY", name: "Paraguay" },
  { code: "PER", name: "Pérou" },
  { code: "SLV", name: "Salvador" },
  { code: "TTO", name: "Trinité-et-Tobago" },
  { code: "URY", name: "Uruguay" },
  { code: "USA", name: "États-Unis" },
  { code: "VEN", name: "Venezuela" },
  { code: "CAN", name: "Canada" },
  { code: "AFG", name: "Afghanistan" },
  { code: "SAU", name: "Arabie saoudite" },
  { code: "ARM", name: "Arménie" },
  { code: "AZE", name: "Azerbaïdjan" },
  { code: "BHR", name: "Bahreïn" },
  { code: "BGD", name: "Bangladesh" },
  { code: "BTN", name: "Bhoutan" },
  { code: "CHN", name: "Chine" },
  { code: "GEO", name: "Géorgie" },
  { code: "IND", name: "Inde" },
  { code: "IDN", name: "Indonésie" },
  { code: "IRN", name: "Iran" },
  { code: "IRQ", name: "Irak" },
  { code: "ISR", name: "Israël" },
  { code: "JPN", name: "Japon" },
  { code: "JOR", name: "Jordanie" },
  { code: "KAZ", name: "Kazakhstan" },
  { code: "KWT", name: "Koweït" },
  { code: "KGZ", name: "Kirghizistan" },
  { code: "LAO", name: "Laos" },
  { code: "LBN", name: "Liban" },
  { code: "MYS", name: "Malaisie" },
  { code: "MDV", name: "Maldives" },
  { code: "MNG", name: "Mongolie" },
  { code: "MMR", name: "Myanmar" },
  { code: "NPL", name: "Népal" },
  { code: "PRK", name: "Corée du Nord" },
  { code: "OMN", name: "Oman" },
  { code: "UZB", name: "Ouzbékistan" },
  { code: "PAK", name: "Pakistan" },
  { code: "PSE", name: "Palestine" },
  { code: "PHL", name: "Philippines" },
  { code: "QAT", name: "Qatar" },
  { code: "KOR", name: "Corée du Sud" },
  { code: "SGP", name: "Singapour" },
  { code: "LKA", name: "Sri Lanka" },
  { code: "SYR", name: "Syrie" },
  { code: "TWN", name: "Taïwan" },
  { code: "TJK", name: "Tadjikistan" },
  { code: "THA", name: "Thaïlande" },
  { code: "TKM", name: "Turkménistan" },
  { code: "TUR", name: "Turquie" },
  { code: "ARE", name: "Émirats arabes unis" },
  { code: "VNM", name: "Viêt Nam" },
  { code: "YEM", name: "Yémen" },
  { code: "AUS", name: "Australie" },
  { code: "FJI", name: "Fidji" },
  { code: "NZL", name: "Nouvelle-Zélande" },
  { code: "PNG", name: "Papouasie-Nouvelle-Guinée" },
];

type FormValues = {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  gender: "" | "MALE" | "FEMALE" | "OTHER";
  nationality: string;
  birthDate: string;
  numCnps: string;
  dateEmbauche: string;
  categorie: string;
  echelon: string;
  departmentCode: string;
  modePaiement: "" | PaymentChannel;
  compteBancaire: string;
  numMobileMoney: string;
  operateurMm: "" | MobileOperator;
};

export function EmployeeCreateForm() {
  const t = useTranslations("employees");
  const tCreate = useTranslations("employees.create");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const router = useRouter();

  const [createdResult, setCreatedResult] = React.useState<CreatedEmployeeResult | null>(null);
  const [cnpsState, setCnpsState] = React.useState<"idle" | "checking" | "available" | "taken">("idle");

  const savedDraft = React.useMemo<Partial<FormValues> | null>(() => {
    if (typeof window === "undefined") return null;
    try {
      const raw = localStorage.getItem(DRAFT_KEY);
      return raw ? (JSON.parse(raw) as Partial<FormValues>) : null;
    } catch {
      return null;
    }
  }, []);

  const {
    register,
    handleSubmit,
    watch,
    getValues,
    control,
    setError,
    clearErrors,
    formState: { errors },
  } = useForm<FormValues>({
    defaultValues: {
      firstName: savedDraft?.firstName ?? "",
      lastName: savedDraft?.lastName ?? "",
      email: savedDraft?.email ?? "",
      phoneNumber: savedDraft?.phoneNumber ?? "",
      gender: savedDraft?.gender ?? "",
      nationality: savedDraft?.nationality ?? "CMR",
      birthDate: savedDraft?.birthDate ?? "",
      numCnps: savedDraft?.numCnps ?? "",
      dateEmbauche: savedDraft?.dateEmbauche ?? new Date().toISOString().slice(0, 10),
      categorie: savedDraft?.categorie ?? "11",
      echelon: savedDraft?.echelon ?? "A",
      departmentCode: savedDraft?.departmentCode ?? "",
      modePaiement: savedDraft?.modePaiement ?? "",
      compteBancaire: savedDraft?.compteBancaire ?? "",
      numMobileMoney: savedDraft?.numMobileMoney ?? "",
      operateurMm: savedDraft?.operateurMm ?? "",
    },
    mode: "onTouched",
  });

  const watched = watch();
  const isMobile =
    watched.modePaiement === "MTN_MOBILE_MONEY" || watched.modePaiement === "ORANGE_MONEY";
  const isBank = watched.modePaiement === "BANK_TRANSFER";

  const selectedCategory = CCNI_CATEGORIES.find((c) => String(c.value) === watched.categorie);
  const availableEchelons = selectedCategory?.echelons ?? ["A", "B", "C", "D"];

  async function checkCnps(value: string) {
    if (!value.trim()) {
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

  const mutation = useMutation({
    mutationFn: async (v: FormValues) => {
      const result = await apiFetch<CreatedEmployeeResult>(
        "/api/hrm/employees",
        {
          method: "POST",
          body: {
            firstName: v.firstName.trim(),
            lastName: v.lastName.trim(),
            email: v.email.trim().toLowerCase(),
            phoneNumber: v.phoneNumber.trim() || undefined,
            gender: v.gender || undefined,
            nationality: v.nationality.trim() || undefined,
            birthDate: v.birthDate || undefined,
            numCnps: v.numCnps.trim() || undefined,
            categorie: Number(v.categorie),
            echelon: v.echelon.trim() || undefined,
            dateEmbauche: v.dateEmbauche,
            departmentCode: v.departmentCode.trim() || undefined,
            modePaiement: v.modePaiement || undefined,
            compteBancaire: isBank && v.compteBancaire ? v.compteBancaire.trim() : undefined,
            numMobileMoney: isMobile ? v.numMobileMoney.trim() || undefined : undefined,
            operateurMm: isMobile ? v.operateurMm || undefined : undefined,
          },
        },
      );

      return result;
    },
    onSuccess: (result) => {
      try { localStorage.removeItem(DRAFT_KEY); } catch { /* ignore */ }
      toast.success(tCreate("success") + " · " + result.matricule);
      setCreatedResult(result);
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
      localStorage.setItem(DRAFT_KEY, JSON.stringify(getValues()));
      toast.success(tCreate("draftSaved"));
    } catch {
      toast.error(tErrors("unknown"));
    }
  }

  return (
    <form onSubmit={handleSubmit((v) => mutation.mutate(v))}>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[
          { label: "HR Core" },
          { label: t("list.title"), href: "/employees" },
          { label: tCreate("title") },
        ]}
        title={tCreate("title")}
        subtitle={tCreate("subtitle")}
      />

      <div className="space-y-8">

        {/* Section 1: Identification administrative */}
        <section>
          <SectionTitle>{tCreate("sections.admin")}</SectionTitle>
          <Card>
            <CardContent padding="lg">
              {/* Identity fields */}
              <div className="grid grid-cols-2 gap-4">
                <Field label={tCreate("fields.firstName")} error={errors.firstName && "—"}>
                  <Input {...register("firstName", { required: true, minLength: 2 })} />
                </Field>
                <Field label={tCreate("fields.lastName")} error={errors.lastName && "—"}>
                  <Input {...register("lastName", { required: true, minLength: 2 })} />
                </Field>
                <Field label={tCreate("fields.email")} error={errors.email && "—"}>
                  <Input
                    type="email"
                    placeholder="prenom.nom@example.com"
                    {...register("email", {
                      required: true,
                      pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                    })}
                  />
                </Field>
                <Field label={tCreate("fields.phoneNumber")}>
                  <Input {...register("phoneNumber")} placeholder="+237 6XX XX XX XX" />
                </Field>
                <Field label={tCreate("fields.gender")}>
                  <select {...register("gender")} className={SELECT_CLS}>
                    <option value="">—</option>
                    <option value="MALE">{tCreate("gender.MALE")}</option>
                    <option value="FEMALE">{tCreate("gender.FEMALE")}</option>
                    <option value="OTHER">{tCreate("gender.OTHER")}</option>
                  </select>
                </Field>
                <Field label={tCreate("fields.nationality")}>
                  <select {...register("nationality")} className={SELECT_CLS}>
                    {COUNTRIES.map((c) => (
                      <option key={c.code} value={c.code}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </Field>
                <Field label={tCreate("fields.birthDate")}>
                  <Input type="date" {...register("birthDate")} />
                </Field>
              </div>

              <div className="my-5 h-px bg-line-soft" />

              {/* HR admin fields */}
              <div className="grid grid-cols-3 gap-4">
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
                <Field
                  label={tCreate("fields.dateEmbauche")}
                  error={errors.dateEmbauche && "—"}
                >
                  <Input
                    type="date"
                    {...register("dateEmbauche", { required: true })}
                  />
                </Field>
                <div />
                {/* Catégorie CCNI */}
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
                <Field label={tCreate("fields.echelon")}>
                  <select {...register("echelon")} className={SELECT_CLS}>
                    {availableEchelons.map((e) => (
                      <option key={e} value={e}>
                        Échelon {e}
                      </option>
                    ))}
                  </select>
                </Field>
                <Field label={tCreate("fields.departmentCode")}>
                  <Input
                    {...register("departmentCode")}
                    placeholder="Ex. ENG, FIN, RH…"
                    maxLength={50}
                  />
                </Field>
              </div>
            </CardContent>
          </Card>
        </section>

        {/* Section 2: Mode de paiement */}
        <section>
          <SectionTitle>{tCreate("sections.paiement")}</SectionTitle>
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
                        {
                          value: "BANK_TRANSFER",
                          label: t("paymentChannel.BANK_TRANSFER"),
                        },
                        { value: "MTN_MOBILE_MONEY", label: "MTN Mobile Money" },
                        { value: "ORANGE_MONEY", label: "Orange Money" },
                        { value: "CASH", label: t("paymentChannel.CASH") },
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
                                { value: "MTN", label: "MTN MoMo" },
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

      {/* Credentials modal — shown after successful creation */}
      {createdResult && (
        <CredentialsModal
          result={createdResult}
          onContinue={() => router.push(`/employees/${createdResult.employeeId}`)}
        />
      )}

      {/* Footer */}
      <div className="mt-8 flex items-center gap-2 border-t border-line pt-6">
        <Link href="/employees">
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
          {tCreate("draft")}
        </Button>
        <Button type="submit" disabled={mutation.isPending || cnpsState === "taken"}>
          {mutation.isPending ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            tCreate("submit")
          )}
        </Button>
      </div>
    </form>
  );
}

/* ── CredentialsModal ───────────────────────────────────────────────── */

function CredentialsModal({
  result,
  onContinue,
}: {
  result: CreatedEmployeeResult;
  onContinue: () => void;
}) {
  const tCreate = useTranslations("employees.create");
  const { login, matricule, warnings } = result;
  const [showPassword, setShowPassword] = React.useState(false);
  const [copiedField, setCopiedField] = React.useState<"username" | "password" | null>(null);

  function copy(value: string, field: "username" | "password") {
    navigator.clipboard.writeText(value).then(() => {
      setCopiedField(field);
      setTimeout(() => setCopiedField(null), 2000);
    });
  }

  return (
    <Dialog
      open
      onClose={() => {/* non-dismissible — user must click the continue button */}}
      title={tCreate("credentials.title")}
      subtitle={tCreate("credentials.subtitle")}
      size="md"
      footer={
        <Button onClick={onContinue}>
          {tCreate("credentials.continue")}
        </Button>
      }
    >
      {/* Matricule */}
      <div className="mb-4 flex items-center justify-between rounded-[11px] bg-bg-dim px-4 py-3">
        <div>
          <p className="text-[11px] font-semibold uppercase tracking-wider text-ink-4">
            {tCreate("credentials.matricule")}
          </p>
          <p className="mt-0.5 font-mono-tabular text-[15px] font-bold text-ink">{matricule}</p>
        </div>
      </div>

      {login ? (
        <div className="space-y-3">
          {/* Username */}
          <CredentialRow
            label={tCreate("credentials.username")}
            value={login.username}
            copied={copiedField === "username"}
            onCopy={() => copy(login.username, "username")}
            tCreate={tCreate}
          />

          {/* Password */}
          <div className="rounded-[11px] border border-line bg-white px-4 py-3">
            <p className="text-[11px] font-semibold uppercase tracking-wider text-ink-4">
              {tCreate("credentials.password")}
            </p>
            <div className="mt-1.5 flex items-center gap-2">
              <span className="flex-1 font-mono-tabular text-[14px] font-semibold text-ink">
                {showPassword ? login.temporaryPassword : "•".repeat(login.temporaryPassword.length)}
              </span>
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                className="text-ink-3 hover:text-ink"
              >
                {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
              <button
                type="button"
                onClick={() => copy(login.temporaryPassword, "password")}
                className="text-ink-3 hover:text-orange-600"
              >
                {copiedField === "password" ? (
                  <Check className="h-4 w-4 text-success-600" />
                ) : (
                  <Copy className="h-4 w-4" />
                )}
              </button>
            </div>
            <p className="mt-1.5 text-[11.5px] text-ink-4">{tCreate("credentials.passwordHint")}</p>
          </div>

          {/* Email status */}
          <div
            className={cn(
              "flex items-center gap-2 rounded-[11px] px-4 py-3 text-[12.5px]",
              login.welcomeMailSent
                ? "bg-success-50 text-success-700"
                : "bg-warning-50 text-warning-700",
            )}
          >
            {login.welcomeMailSent ? (
              <Check className="h-3.5 w-3.5 shrink-0" />
            ) : (
              <span className="shrink-0 text-[14px]">⚠</span>
            )}
            {login.welcomeMailSent
              ? tCreate("credentials.emailSent")
              : tCreate("credentials.emailFailed")}
          </div>

          {/* Warnings */}
          {warnings.length > 0 && (
            <div className="rounded-[11px] border border-warning-200 bg-warning-50 px-4 py-3">
              <p className="mb-1.5 text-[11px] font-semibold uppercase tracking-wider text-warning-700">
                {tCreate("credentials.warnings")}
              </p>
              <ul className="space-y-1">
                {warnings.map((w, i) => (
                  <li key={i} className="text-[12px] text-warning-700">
                    · {w}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      ) : (
        <p className="text-[13px] text-ink-3">{tCreate("credentials.noLogin")}</p>
      )}
    </Dialog>
  );
}

function CredentialRow({
  label,
  value,
  copied,
  onCopy,
  tCreate,
}: {
  label: string;
  value: string;
  copied: boolean;
  onCopy: () => void;
  tCreate: ReturnType<typeof useTranslations<"employees.create">>;
}) {
  return (
    <div className="rounded-[11px] border border-line bg-white px-4 py-3">
      <p className="text-[11px] font-semibold uppercase tracking-wider text-ink-4">{label}</p>
      <div className="mt-1.5 flex items-center gap-2">
        <span className="flex-1 font-mono-tabular text-[14px] text-ink">{value}</span>
        <button type="button" onClick={onCopy} className="text-ink-3 hover:text-orange-600">
          {copied ? (
            <Check className="h-4 w-4 text-success-600" />
          ) : (
            <Copy className="h-4 w-4" />
          )}
        </button>
      </div>
    </div>
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
          {opt.sub && (
            <span className="text-[11.5px] text-ink-3">{opt.sub}</span>
          )}
        </button>
      ))}
    </div>
  );
}
