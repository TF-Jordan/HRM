"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  AlertTriangle,
  Building2,
  Check,
  ExternalLink,
  Globe,
  Image,
  Info,
  Loader2,
  Mail,
  Pencil,
  Scale,
  Shield,
  Tag,
  Upload,
  Users,
  X,
  XCircle,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Field, Input, Label, Textarea } from "@/components/ui/input";
import { IconTile } from "@/components/ui/icon-tile";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { cn } from "@/lib/utils";

/* ────────────────────────── Types ────────────────────────── */

type Tab = "identity" | "legal" | "contact" | "branding" | "governance";

type Organization = {
  id: string;
  tenantId: string;
  businessActorId: string;
  governanceStatus?: string | null;
  governedByUserId?: string | null;
  governedAt?: string | null;
  governanceReason?: string | null;
  code: string;
  service: string;
  organizationType?: string;
  isIndividualBusiness: boolean;
  email?: string | null;
  shortName: string;
  longName: string;
  displayName?: string;
  legalName?: string;
  description?: string | null;
  logoUri?: string | null;
  logoId?: string | null;
  websiteUrl?: string | null;
  socialNetwork?: string | null;
  businessRegistrationNumber?: string | null;
  taxNumber?: string | null;
  capitalShare?: number | string | null;
  ceoName?: string | null;
  yearFounded?: number | null;
  keywords?: string[] | null;
  numberOfEmployees?: number | null;
  legalForm?: string | null;
  isActive: boolean;
  status?: string | null;
};

type FormValues = {
  shortName: string;
  longName: string;
  code: string;
  service: string;
  legalForm: string;
  isIndividualBusiness: string; // "true" | "false" for select/radio
  yearFounded: string;
  numberOfEmployees: string;
  description: string;
  businessRegistrationNumber: string;
  taxNumber: string;
  capitalShare: string;
  ceoName: string;
  email: string;
  websiteUrl: string;
  socialNetwork: string;
};

/* ────────────────────────── Helpers ────────────────────────── */

const GOVERNANCE_STATUS_TONE: Record<string, "success" | "warning" | "danger" | "gray"> = {
  APPROVED: "success",
  PENDING_APPROVAL: "warning",
  SUSPENDED: "danger",
  CLOSED: "gray",
  REJECTED: "danger",
};

const SERVICE_OPTIONS = [
  { value: "TECH", label: "TECH — Technologies & Numérique" },
  { value: "TELECOM", label: "TELECOM — Télécommunications" },
  { value: "FINANCE", label: "FINANCE — Finance & Assurance" },
  { value: "RETAIL", label: "RETAIL — Commerce" },
  { value: "INDUSTRY", label: "INDUSTRY — Industrie" },
  { value: "SERVICES", label: "SERVICES — Services" },
  { value: "HEALTH", label: "HEALTH — Santé" },
  { value: "EDUCATION", label: "EDUCATION — Éducation" },
  { value: "AGRICULTURE", label: "AGRICULTURE" },
  { value: "OTHER", label: "OTHER — Autre" },
];

const LEGAL_FORM_OPTIONS = [
  { value: "SA", label: "SA — Société Anonyme" },
  { value: "SARL", label: "SARL — Société à Responsabilité Limitée" },
  { value: "SARLU", label: "SARLU — Unipersonnelle" },
  { value: "SAS", label: "SAS — Société par Actions Simplifiée" },
  { value: "SNC", label: "SNC — Société en Nom Collectif" },
  { value: "GIE", label: "GIE" },
  { value: "ASSOCIATION", label: "Association" },
  { value: "FONDATION", label: "Fondation" },
];

const SELECT_CLS =
  "w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12";

function govTone(status?: string | null): "success" | "warning" | "danger" | "gray" {
  return GOVERNANCE_STATUS_TONE[status ?? ""] ?? "gray";
}

function toFormValues(o: Organization): FormValues {
  return {
    shortName: o.shortName ?? "",
    longName: o.longName ?? "",
    code: o.code ?? "",
    service: o.service ?? o.organizationType ?? "",
    legalForm: o.legalForm ?? "",
    isIndividualBusiness: o.isIndividualBusiness ? "true" : "false",
    yearFounded: o.yearFounded == null ? "" : String(o.yearFounded),
    numberOfEmployees: o.numberOfEmployees == null ? "" : String(o.numberOfEmployees),
    description: o.description ?? "",
    businessRegistrationNumber: o.businessRegistrationNumber ?? "",
    taxNumber: o.taxNumber ?? "",
    capitalShare: o.capitalShare == null ? "" : String(o.capitalShare),
    ceoName: o.ceoName ?? "",
    email: o.email ?? "",
    websiteUrl: o.websiteUrl ?? "",
    socialNetwork: o.socialNetwork ?? "",
  };
}

function toPayload(v: FormValues, org: Organization, keywords: string[], logoId?: string, logoUri?: string) {
  return {
    code: v.code || org.code,
    service: v.service,
    isIndividualBusiness: v.isIndividualBusiness === "true",
    email: v.email || undefined,
    shortName: v.shortName,
    longName: v.longName,
    description: v.description || undefined,
    websiteUrl: v.websiteUrl || undefined,
    socialNetwork: v.socialNetwork || undefined,
    legalForm: v.legalForm || undefined,
    businessRegistrationNumber: v.businessRegistrationNumber || undefined,
    taxNumber: v.taxNumber || undefined,
    capitalShare: v.capitalShare ? Number(v.capitalShare) : undefined,
    ceoName: v.ceoName || undefined,
    yearFounded: v.yearFounded ? Number(v.yearFounded) : undefined,
    numberOfEmployees: v.numberOfEmployees ? Number(v.numberOfEmployees) : undefined,
    keywords,
    logoId: logoId ?? org.logoId ?? undefined,
    logoUri: logoUri ?? org.logoUri ?? undefined,
    isActive: org.isActive,
    status: org.status ?? undefined,
  };
}

/* ────────────────────────── Main Component ────────────────── */

export function OrganizationView() {
  const t = useTranslations("admin");
  const tOrg = useTranslations("admin.organization");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const locale = useLocale() as "fr" | "en";
  const queryClient = useQueryClient();

  const [tab, setTab] = React.useState<Tab>("identity");
  const [keywords, setKeywords] = React.useState<string[]>([]);
  const [kwInput, setKwInput] = React.useState("");
  const [logoId, setLogoId] = React.useState<string | undefined>();
  const [logoUri, setLogoUri] = React.useState<string | undefined>();
  const [isUploadingLogo, setIsUploadingLogo] = React.useState(false);
  const logoInputRef = React.useRef<HTMLInputElement>(null);

  // Governance action modal
  const [govAction, setGovAction] = React.useState<"suspend" | "close" | "reopen" | null>(null);
  const [govReason, setGovReason] = React.useState("");

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "organization"],
    queryFn: () => apiFetch<Organization>("/api/admin/organization"),
  });

  const { register, handleSubmit, reset, watch, formState: { isDirty, isSubmitting } } = useForm<FormValues>({
    defaultValues: {
      shortName: "", longName: "", code: "", service: "", legalForm: "",
      isIndividualBusiness: "false", yearFounded: "", numberOfEmployees: "",
      description: "", businessRegistrationNumber: "", taxNumber: "",
      capitalShare: "", ceoName: "", email: "", websiteUrl: "", socialNetwork: "",
    },
  });

  const watched = watch();

  React.useEffect(() => {
    if (data) {
      reset(toFormValues(data));
      setKeywords(data.keywords ?? []);
      setLogoId(data.logoId ?? undefined);
      setLogoUri(data.logoUri ?? undefined);
    }
  }, [data, reset]);

  /* ── Save mutation ── */
  const saveMutation = useMutation({
    mutationFn: (values: FormValues) =>
      apiFetch<Organization>("/api/admin/organization", {
        method: "PATCH",
        body: toPayload(values, data!, keywords, logoId, logoUri),
      }),
    onSuccess: (saved) => {
      toast.success(tOrg("saved"));
      queryClient.setQueryData(["admin", "organization"], saved);
      reset(toFormValues(saved));
      setKeywords(saved.keywords ?? []);
    },
    onError: (cause) => {
      toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown"));
    },
  });

  /* ── Governance mutations ── */
  function makeGovMutation(endpoint: string, successKey: string) {
    return useMutation({
      mutationFn: (reason: string) =>
        apiFetch<Organization>(`/api/admin/organization/${endpoint}`, {
          method: "POST",
          body: { reason: reason || undefined },
        }),
      onSuccess: (saved) => {
        toast.success(tOrg(successKey as never));
        queryClient.setQueryData(["admin", "organization"], saved);
        setGovAction(null);
        setGovReason("");
      },
      onError: (cause) => {
        toast.error(cause instanceof BffApiError ? cause.message : tErrors("unknown"));
      },
    });
  }

  // eslint-disable-next-line react-hooks/rules-of-hooks
  const suspendMutation = makeGovMutation("suspend", "suspended");
  // eslint-disable-next-line react-hooks/rules-of-hooks
  const closeMutation = makeGovMutation("close", "closed");
  // eslint-disable-next-line react-hooks/rules-of-hooks
  const reopenMutation = makeGovMutation("reopen", "reopened");

  const govMutationMap = { suspend: suspendMutation, close: closeMutation, reopen: reopenMutation };

  /* ── Logo upload ── */
  async function handleLogoChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setIsUploadingLogo(true);
    try {
      const fd = new FormData();
      fd.append("file", file);
      const result = await apiFetch<{ id: string; fileName: string }>("/api/files/upload", {
        method: "POST",
        body: fd,
        json: false,
      });
      setLogoId(result.id);
      setLogoUri(`/api/files/${result.id}`);
    } catch {
      toast.error(tErrors("unknown"));
    } finally {
      setIsUploadingLogo(false);
      if (logoInputRef.current) logoInputRef.current.value = "";
    }
  }

  /* ── Keyword helpers ── */
  function addKeyword(value: string) {
    const kw = value.trim().toLowerCase();
    if (kw && !keywords.includes(kw)) setKeywords((prev) => [...prev, kw]);
    setKwInput("");
  }

  /* ── Loading / error states ── */
  if (isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }
  if (error || !data) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {error instanceof BffApiError ? error.message : tErrors("unknown")}
      </div>
    );
  }

  const displayLetter = (data.shortName || data.code || "O")[0].toUpperCase();
  const govStatus = data.governanceStatus;

  const TABS: { id: Tab; label: string; Icon: React.ElementType }[] = [
    { id: "identity", label: tOrg("tabs.identity"), Icon: Building2 },
    { id: "legal", label: tOrg("tabs.legal"), Icon: Scale },
    { id: "contact", label: tOrg("tabs.contact"), Icon: Mail },
    { id: "branding", label: tOrg("tabs.branding"), Icon: Image },
    { id: "governance", label: tOrg("tabs.governance"), Icon: Shield },
  ];

  return (
    <>
      <form onSubmit={handleSubmit((v) => saveMutation.mutate(v))}>
        <PageHeader
          ucBadge={t("ucBadge")}
          breadcrumb={[{ label: "HR Core" }, { label: "Administration" }, { label: tOrg("title") }]}
          title={data.longName || data.shortName || data.code}
          subtitle={tOrg("subtitle")}
          actions={
            tab !== "governance" ? (
              <Button type="submit" disabled={(!isDirty && keywords === (data.keywords ?? [])) || isSubmitting}>
                {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Check className="h-4 w-4" />}
                {tCommon("actions.save")}
              </Button>
            ) : null
          }
        />

        {/* ── Hero card ── */}
        <div
          className="relative mb-5 overflow-hidden rounded-[20px] border border-orange-200 p-[26px] shadow-sm-brand"
          style={{ background: "linear-gradient(135deg, #FFFAF2 0%, #FFFFFF 60%)" }}
        >
          <span
            aria-hidden="true"
            className="pointer-events-none absolute -right-16 -top-16 h-60 w-60 rounded-full bg-grad-orange opacity-[0.08] blur-2xl"
          />
          <div className="relative flex flex-wrap items-start gap-4">
            {/* Logo */}
            <div className="relative">
              {logoUri ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  src={logoUri}
                  alt={data.shortName}
                  className="h-[80px] w-[80px] rounded-[20px] object-contain border border-line"
                />
              ) : (
                <div
                  className="flex h-[80px] w-[80px] items-center justify-center rounded-[20px] font-display text-[38px] font-extrabold text-white shadow-sm-brand"
                  style={{ background: "linear-gradient(135deg, #F97316 0%, #EA580C 100%)" }}
                >
                  {displayLetter}
                </div>
              )}
              <button
                type="button"
                onClick={() => logoInputRef.current?.click()}
                className="absolute -bottom-1.5 -right-1.5 flex h-7 w-7 items-center justify-center rounded-full border-2 border-white bg-white shadow-sm hover:bg-orange-50"
              >
                <Pencil className="h-3 w-3 text-ink-3" />
              </button>
              <input ref={logoInputRef} type="file" accept="image/png,image/svg+xml,image/jpeg" className="sr-only" onChange={handleLogoChange} />
            </div>

            {/* Identity */}
            <div className="min-w-0 flex-1">
              <div className="mb-1 flex flex-wrap items-center gap-2">
                <Badge tone={govTone(govStatus)} showDot>{govStatus ?? "—"}</Badge>
                {data.isActive && <Badge tone="success" showDot={false}>isActive · true</Badge>}
                <span className="font-mono-tabular text-[11px] text-ink-3">code · {data.code}</span>
              </div>
              <h1 className="font-display text-[30px] font-extrabold leading-tight tracking-tight text-ink">
                {data.shortName}
              </h1>
              <p className="mt-1 text-[14px] text-ink-2">
                {data.longName}
                {data.service ? ` · ${data.service}` : ""}
                {data.numberOfEmployees ? ` · ${data.numberOfEmployees} employés` : ""}
                {data.yearFounded ? ` · fondée en ${data.yearFounded}` : ""}
              </p>
              <div className="mt-3.5 flex flex-wrap gap-4 text-[12.5px] text-ink-3">
                {data.businessRegistrationNumber && (
                  <span className="flex items-center gap-1.5">
                    <Scale className="h-3.5 w-3.5" />
                    RCCM · {data.businessRegistrationNumber}
                  </span>
                )}
                {data.taxNumber && (
                  <span className="flex items-center gap-1.5">
                    <ExternalLink className="h-3.5 w-3.5" />
                    NIU · {data.taxNumber}
                  </span>
                )}
                {data.websiteUrl && (
                  <span className="flex items-center gap-1.5">
                    <Globe className="h-3.5 w-3.5" />
                    {data.websiteUrl.replace(/^https?:\/\//, "")}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* ── Info banner ── */}
        <div className="mb-5 flex items-start gap-3 rounded-[16px] border border-orange-200 bg-orange-50 px-4 py-3.5">
          <IconTile icon={Info} tone="orange" size="sm" />
          <div>
            <p className="text-[13px] font-semibold text-ink">
              {tOrg("bannerTitle")} <code className="rounded bg-orange-100 px-1 text-[11px]">organization.organization</code>
            </p>
            <p className="mt-0.5 text-[12px] text-ink-2">
              {tOrg("bannerText")}
            </p>
          </div>
        </div>

        {/* ── Tabs ── */}
        <div className="mb-5 flex flex-wrap gap-0 border-b border-line-soft">
          {TABS.map(({ id, label, Icon }) => (
            <button
              key={id}
              type="button"
              onClick={() => setTab(id)}
              className={cn(
                "flex items-center gap-2 px-3.5 py-2.5 text-[13.5px] font-medium transition-colors",
                tab === id
                  ? "border-b-2 border-orange-500 text-orange-600"
                  : "text-ink-3 hover:text-ink",
              )}
            >
              <Icon className="h-3.5 w-3.5" />
              {label}
            </button>
          ))}
        </div>

        {/* ── Two-column layout ── */}
        <div className="grid gap-5 lg:grid-cols-[1fr_320px]">

          {/* ── Left: tab content ── */}
          <Card>
            <CardContent padding="lg">
              {tab === "identity" && <IdentityTab register={register} watched={watched} />}
              {tab === "legal" && <LegalTab register={register} />}
              {tab === "contact" && <ContactTab register={register} />}
              {tab === "branding" && (
                <BrandingTab
                  displayLetter={displayLetter}
                  logoUri={logoUri}
                  isUploadingLogo={isUploadingLogo}
                  onLogoClick={() => logoInputRef.current?.click()}
                  keywords={keywords}
                  kwInput={kwInput}
                  setKwInput={setKwInput}
                  onAddKeyword={addKeyword}
                  onRemoveKeyword={(k) => setKeywords((prev) => prev.filter((x) => x !== k))}
                />
              )}
              {tab === "governance" && <GovernanceTab org={data} locale={locale} onAction={setGovAction} />}

              {tab !== "governance" && (
                <div className="mt-8 flex justify-end gap-2 border-t border-line-soft pt-6">
                  <Button type="button" variant="ghost" onClick={() => reset(toFormValues(data))}>
                    {tCommon("actions.cancel")}
                  </Button>
                  <Button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Check className="h-4 w-4" />}
                    {tOrg("save")}
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>

          {/* ── Right: sidebar ── */}
          <div className="flex flex-col gap-4">
            {tab === "governance" ? (
              <LifecycleSidebar currentStatus={govStatus} />
            ) : (
              <DocPreviewSidebar org={data} watched={watched} />
            )}
          </div>
        </div>
      </form>

      {/* ── Governance action dialog ── */}
      {govAction && (
        <Dialog
          open
          onClose={() => { setGovAction(null); setGovReason(""); }}
          title={tOrg(`actions.${govAction}.title`)}
          subtitle={tOrg(`actions.${govAction}.subtitle`)}
          size="sm"
          footer={
            <div className="flex gap-2">
              <Button variant="ghost" onClick={() => { setGovAction(null); setGovReason(""); }}>
                {tCommon("actions.cancel")}
              </Button>
              <Button
                variant={govAction === "reopen" ? "primary" : "danger"}
                disabled={govMutationMap[govAction].isPending}
                onClick={() => govMutationMap[govAction].mutate(govReason)}
              >
                {govMutationMap[govAction].isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  tOrg(`actions.${govAction}.confirm`)
                )}
              </Button>
            </div>
          }
        >
          <Field label={tOrg("actions.reason")}>
            <Textarea
              value={govReason}
              onChange={(e) => setGovReason(e.target.value)}
              placeholder={tOrg("actions.reasonPlaceholder")}
              rows={3}
            />
          </Field>
        </Dialog>
      )}
    </>
  );
}

/* ══════════════════════════════════════════════
   TAB PANELS
══════════════════════════════════════════════ */

function OrgSection({ title, sub, cols = 2, children }: {
  title: string;
  sub?: string;
  cols?: 1 | 2 | 3;
  children: React.ReactNode;
}) {
  return (
    <div className="mb-6">
      <div className="mb-3.5 flex items-center gap-2.5">
        <span className="h-[2px] w-4 rounded-full bg-gradient-to-r from-orange-500 to-orange-300" />
        <span className="text-[11px] font-bold uppercase tracking-widest text-orange-700">{title}</span>
        {sub && <span className="text-[11px] font-normal text-ink-4">· {sub}</span>}
      </div>
      <div
        className="grid gap-4"
        style={{ gridTemplateColumns: cols === 1 ? "1fr" : `repeat(${cols}, minmax(0, 1fr))` }}
      >
        {children}
      </div>
    </div>
  );
}

function MappingBadge({ mapping }: { mapping: string }) {
  return (
    <code className="ml-auto rounded bg-bg-soft px-1.5 py-0.5 text-[10px] text-ink-4">{mapping}</code>
  );
}

function IdentityTab({
  register,
  watched,
}: {
  register: ReturnType<typeof useForm<FormValues>>["register"];
  watched: FormValues;
}) {
  const tOrg = useTranslations("admin.organization");
  return (
    <div>
      <OrgSection title={tOrg("sections.denomination")} sub="shortName & longName">
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.shortName")} <MappingBadge mapping="shortName" /></span>} hint={tOrg("hints.shortName")}>
          <Input {...register("shortName", { required: true })} />
        </Field>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.longName")} <MappingBadge mapping="longName" /></span>} hint={tOrg("hints.longName")}>
          <Input {...register("longName", { required: true })} />
        </Field>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.code")} <MappingBadge mapping="code" /></span>} hint={tOrg("hints.code")}>
          <Input {...register("code")} className="uppercase" />
        </Field>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.service")} <MappingBadge mapping="service" /></span>} hint={tOrg("hints.service")}>
          <select {...register("service")} className={SELECT_CLS}>
            {SERVICE_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>{o.label}</option>
            ))}
          </select>
        </Field>
      </OrgSection>

      <OrgSection title={tOrg("sections.characteristics")}>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.legalForm")} <MappingBadge mapping="legalForm" /></span>} hint={tOrg("hints.legalForm")}>
          <select {...register("legalForm")} className={SELECT_CLS}>
            <option value="">—</option>
            {LEGAL_FORM_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>{o.label}</option>
            ))}
          </select>
        </Field>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.isIndividualBusiness")} <MappingBadge mapping="isIndividualBusiness" /></span>} hint="boolean">
          <div className="flex gap-2">
            {["true", "false"].map((v) => (
              <label
                key={v}
                className={cn(
                  "flex cursor-pointer items-center gap-1.5 rounded-[10px] border px-3.5 py-2 text-[13px] font-semibold transition-all",
                  watched.isIndividualBusiness === v
                    ? "border-orange-400 bg-orange-50 text-orange-700"
                    : "border-line bg-white text-ink-3 hover:border-orange-200",
                )}
              >
                <input type="radio" value={v} {...register("isIndividualBusiness")} className="sr-only" />
                {v === "true" ? "Oui" : "Non"}
              </label>
            ))}
          </div>
        </Field>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.yearFounded")} <MappingBadge mapping="yearFounded" /></span>} hint="Integer">
          <Input type="number" min="1800" max="2100" {...register("yearFounded")} />
        </Field>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.numberOfEmployees")} <MappingBadge mapping="numberOfEmployees" /></span>} hint="Integer">
          <Input type="number" min="0" {...register("numberOfEmployees")} />
        </Field>
        <Field
          label={<span className="flex items-center gap-1">{tOrg("fields.description")} <MappingBadge mapping="description" /></span>}
          hint={tOrg("hints.description")}
        >
          <Textarea {...register("description")} rows={4} />
        </Field>
      </OrgSection>
    </div>
  );
}

function LegalTab({ register }: { register: ReturnType<typeof useForm<FormValues>>["register"] }) {
  const tOrg = useTranslations("admin.organization");
  return (
    <div>
      <OrgSection title={tOrg("sections.registration")} sub="businessRegistrationNumber & taxNumber">
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.businessRegistrationNumber")} <MappingBadge mapping="businessRegistrationNumber" /></span>} hint={tOrg("hints.rccm")}>
          <Input {...register("businessRegistrationNumber")} placeholder="RC/YAO/2018/B/12345" />
        </Field>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.taxNumber")} <MappingBadge mapping="taxNumber" /></span>} hint={tOrg("hints.niu")}>
          <Input {...register("taxNumber")} placeholder="M122001234567P" />
        </Field>
      </OrgSection>

      <OrgSection title={tOrg("sections.capital")}>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.capitalShare")} <MappingBadge mapping="capitalShare" /></span>} hint="BigDecimal · FCFA">
          <Input type="number" min="0" placeholder="50 000 000" {...register("capitalShare")} />
        </Field>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.ceoName")} <MappingBadge mapping="ceoName" /></span>} hint={tOrg("hints.ceo")}>
          <Input {...register("ceoName")} placeholder="Prénom Nom" />
        </Field>
      </OrgSection>

      <div className="rounded-[14px] border border-line bg-bg-dim px-4 py-3.5">
        <div className="flex items-start gap-2.5 text-[12.5px] text-ink-2">
          <Scale className="mt-0.5 h-4 w-4 shrink-0 text-orange-600" />
          <span>
            <strong>businessRegistrationNumber</strong> (RCCM), <strong>taxNumber</strong> (NIU)
            {" "}{tOrg("legalBannerText")}
          </span>
        </div>
      </div>
    </div>
  );
}

function ContactTab({ register }: { register: ReturnType<typeof useForm<FormValues>>["register"] }) {
  const tOrg = useTranslations("admin.organization");
  return (
    <div>
      <OrgSection title={tOrg("sections.contact")} sub="email normalisé en minuscules">
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.email")} <MappingBadge mapping="email" /></span>} hint={tOrg("hints.email")}>
          <Input type="email" {...register("email")} placeholder="contact@organisation.cm" />
        </Field>
        <Field label={<span className="flex items-center gap-1">{tOrg("fields.websiteUrl")} <MappingBadge mapping="websiteUrl" /></span>} hint="URL complète">
          <Input type="url" {...register("websiteUrl")} placeholder="https://www.organisation.cm" />
        </Field>
        <div style={{ gridColumn: "span 2" }}>
          <Field label={<span className="flex items-center gap-1">{tOrg("fields.socialNetwork")} <MappingBadge mapping="socialNetwork" /></span>} hint={tOrg("hints.social")}>
            <Input {...register("socialNetwork")} placeholder="https://linkedin.com/company/…" />
          </Field>
        </div>
      </OrgSection>

      <div className="rounded-[14px] border border-line bg-bg-dim px-4 py-3.5">
        <div className="flex items-start gap-2.5 text-[12.5px] text-ink-2">
          <Info className="mt-0.5 h-4 w-4 shrink-0 text-orange-600" />
          <span>{tOrg("contactBannerText")}</span>
        </div>
      </div>
    </div>
  );
}

function BrandingTab({
  displayLetter,
  logoUri,
  isUploadingLogo,
  onLogoClick,
  keywords,
  kwInput,
  setKwInput,
  onAddKeyword,
  onRemoveKeyword,
}: {
  displayLetter: string;
  logoUri?: string;
  isUploadingLogo: boolean;
  onLogoClick: () => void;
  keywords: string[];
  kwInput: string;
  setKwInput: (v: string) => void;
  onAddKeyword: (v: string) => void;
  onRemoveKeyword: (k: string) => void;
}) {
  const tOrg = useTranslations("admin.organization");
  return (
    <div>
      <OrgSection title={tOrg("sections.logo")} sub="logoUri + logoId (UUID FilePort)" cols={1}>
        <div className="flex items-start gap-4">
          <div
            className="flex h-[100px] w-[100px] shrink-0 items-center justify-center rounded-[22px] font-display text-[46px] font-extrabold text-white shadow-sm-brand"
            style={{ background: logoUri ? undefined : "linear-gradient(135deg, #F97316 0%, #EA580C 100%)" }}
          >
            {logoUri ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img src={logoUri} alt="" className="h-full w-full rounded-[22px] object-contain" />
            ) : displayLetter}
          </div>
          <button
            type="button"
            onClick={onLogoClick}
            disabled={isUploadingLogo}
            className="flex-1 rounded-[14px] border-2 border-dashed border-line py-7 text-center transition-colors hover:border-orange-300 hover:bg-orange-50"
          >
            {isUploadingLogo ? (
              <Loader2 className="mx-auto h-6 w-6 animate-spin text-orange-500" />
            ) : (
              <>
                <Upload className="mx-auto mb-2 h-6 w-6 text-ink-4" />
                <p className="text-[13px] font-semibold text-ink">{tOrg("uploadLogoLabel")}</p>
                <p className="mt-1 text-[11px] text-ink-3">PNG ou SVG transparent · min 512×512 px</p>
                <code className="mt-2 block text-[10px] text-ink-4">logoId · UUID · logoUri généré au upload</code>
              </>
            )}
          </button>
        </div>
      </OrgSection>

      <OrgSection title={tOrg("sections.keywords")} sub="keywords · Set<String>" cols={1}>
        <Field
          label={<span className="flex items-center gap-1">{tOrg("fields.keywords")} <MappingBadge mapping="keywords" /></span>}
          hint={tOrg("hints.keywords")}
        >
          <div>
            <div className="mb-2.5 flex flex-wrap gap-1.5">
              {keywords.map((k) => (
                <span
                  key={k}
                  className="flex items-center gap-1 rounded-full border border-line bg-bg-dim px-2.5 py-1 text-[12px] font-medium text-ink-2"
                >
                  {k}
                  <button
                    type="button"
                    onClick={() => onRemoveKeyword(k)}
                    className="ml-0.5 text-ink-4 hover:text-danger-600"
                  >
                    <X className="h-3 w-3" />
                  </button>
                </span>
              ))}
            </div>
            <Input
              value={kwInput}
              onChange={(e) => setKwInput(e.target.value)}
              placeholder={tOrg("keywordPlaceholder")}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  onAddKeyword(kwInput);
                }
              }}
            />
          </div>
        </Field>
      </OrgSection>
    </div>
  );
}

function GovernanceTab({
  org,
  locale,
  onAction,
}: {
  org: Organization;
  locale: "fr" | "en";
  onAction: (action: "suspend" | "close" | "reopen") => void;
}) {
  const tOrg = useTranslations("admin.organization");

  const GOV_STATES = [
    { key: "PENDING_APPROVAL", label: tOrg("govStatus.PENDING_APPROVAL") },
    { key: "APPROVED", label: tOrg("govStatus.APPROVED") },
    { key: "SUSPENDED", label: tOrg("govStatus.SUSPENDED") },
    { key: "CLOSED", label: tOrg("govStatus.CLOSED") },
    { key: "REJECTED", label: tOrg("govStatus.REJECTED") },
  ];

  const GOV_ACTIONS: { key: "suspend" | "close" | "reopen"; icon: React.ElementType; tone: string }[] = [
    { key: "suspend", icon: AlertTriangle, tone: "text-danger-600" },
    { key: "close", icon: XCircle, tone: "text-ink-3" },
    { key: "reopen", icon: Check, tone: "text-success-600" },
  ];

  return (
    <div>
      {/* Status cards */}
      <OrgSection title={tOrg("sections.govStatus")} sub="OrganizationGovernanceStatus" cols={1}>
        <div className="flex flex-wrap gap-2">
          {GOV_STATES.map((s) => {
            const current = org.governanceStatus === s.key;
            return (
              <div
                key={s.key}
                className={cn(
                  "flex min-w-[160px] flex-col gap-1 rounded-[12px] border p-3",
                  current ? "border-orange-400 bg-orange-50" : "border-line bg-white",
                )}
              >
                <div className="flex items-center gap-2">
                  <span className={cn("h-2 w-2 rounded-full", {
                    "bg-success-500": s.key === "APPROVED",
                    "bg-warning-500": s.key === "PENDING_APPROVAL",
                    "bg-danger-500": s.key === "SUSPENDED" || s.key === "REJECTED",
                    "bg-ink-4": s.key === "CLOSED",
                  })} />
                  <code className="text-[11px] font-bold text-ink">{s.key}</code>
                </div>
                <span className="text-[11.5px] text-ink-3">{s.label}</span>
                {current && (
                  <span className="mt-1 self-start rounded-full bg-orange-100 px-2 py-0.5 text-[9px] font-bold uppercase tracking-wider text-orange-700">
                    {tOrg("currentStatus")}
                  </span>
                )}
              </div>
            );
          })}
        </div>
      </OrgSection>

      {/* Metadata */}
      <OrgSection title={tOrg("sections.govMeta")}>
        <Field label={tOrg("fields.governedByUserId")} hint="UUID admin">
          <Input value={org.governedByUserId ?? "—"} disabled />
        </Field>
        <Field label={tOrg("fields.governedAt")} hint="Instant">
          <Input
            value={org.governedAt ? formatDate(org.governedAt, { locale }) : "—"}
            disabled
          />
        </Field>
        <div style={{ gridColumn: "span 2" }}>
          <Field label={tOrg("fields.governanceReason")} hint="Justification du dernier changement">
            <Input value={org.governanceReason ?? "—"} disabled />
          </Field>
        </div>
        <Field label={tOrg("fields.businessActorId")} hint="UUID · propriétaire">
          <Input value={org.businessActorId ?? "—"} disabled />
        </Field>
        <Field label={tOrg("fields.tenantId")} hint="UUID · isolation multi-tenant">
          <Input value={org.tenantId ?? "—"} disabled />
        </Field>
      </OrgSection>

      {/* Actions */}
      <OrgSection title={tOrg("sections.govActions")} sub="méthodes du modèle Organization" cols={1}>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
          {GOV_ACTIONS.map(({ key, icon: Icon, tone }) => (
            <button
              key={key}
              type="button"
              onClick={() => onAction(key)}
              className="flex items-center gap-3 rounded-[12px] border border-line bg-white p-3 text-left transition hover:border-orange-300 hover:bg-orange-50"
            >
              <Icon className={cn("h-5 w-5 shrink-0", tone)} />
              <div>
                <p className="text-[13px] font-semibold text-ink">{tOrg(`actions.${key}.title`)}</p>
                <code className="text-[10px] text-ink-4">{key}(adminId, reason)</code>
              </div>
            </button>
          ))}
        </div>
      </OrgSection>
    </div>
  );
}

/* ══════════════════════════════════════════════
   SIDEBARS
══════════════════════════════════════════════ */

function DocPreviewSidebar({ org, watched }: { org: Organization; watched: FormValues }) {
  const tOrg = useTranslations("admin.organization");

  const displayName = watched.longName || org.longName || org.shortName;
  const rccm = watched.businessRegistrationNumber || org.businessRegistrationNumber;
  const niu = watched.taxNumber || org.taxNumber;
  const capital = watched.capitalShare || org.capitalShare;
  const ceo = watched.ceoName || org.ceoName;
  const email = watched.email || org.email;
  const web = watched.websiteUrl || org.websiteUrl;

  const IMPACTED = [
    { label: tOrg("impacted.payslips"), Icon: Users },
    { label: tOrg("impacted.contracts"), Icon: Scale },
    { label: tOrg("impacted.attestations"), Icon: Shield },
    { label: tOrg("impacted.declarations"), Icon: ExternalLink },
    { label: tOrg("impacted.missions"), Icon: Globe },
  ];

  return (
    <>
      {/* Live preview */}
      <div className="sticky top-[88px]">
        <Card>
          <div className="flex items-center justify-between border-b border-line px-4 py-3">
            <p className="text-[13px] font-semibold text-ink">{tOrg("docPreviewTitle")}</p>
            <Badge tone="warning" showDot={false}>Live</Badge>
          </div>
          <CardContent padding="md">
            <div className="rounded-[10px] border border-line bg-white p-4">
              {/* Header */}
              <div className="mb-3 flex items-start gap-2 border-b-2 border-ink pb-3">
                <div
                  className="flex h-8 w-8 shrink-0 items-center justify-center rounded-[8px] font-display text-[15px] font-extrabold text-white"
                  style={{ background: "linear-gradient(135deg, #F97316 0%, #EA580C 100%)" }}
                >
                  {(watched.shortName || org.shortName || "O")[0].toUpperCase()}
                </div>
                <div className="min-w-0">
                  <p className="truncate font-display text-[13px] font-extrabold leading-tight">{displayName}</p>
                  <p className="mt-0.5 text-[8px] leading-tight text-ink-3">
                    {rccm && `RCCM ${rccm} · `}{niu && `NIU ${niu}`}
                    <br />
                    {capital && `Capital ${Number(capital).toLocaleString()} XAF`}
                    {ceo && ` · CEO ${ceo}`}
                  </p>
                </div>
              </div>
              <p className="text-center font-display text-[11px] font-bold uppercase tracking-wider text-ink">
                BULLETIN DE PAIE
              </p>
              <p className="mt-0.5 text-center text-[8.5px] text-ink-3">Période · {new Date().toLocaleDateString("fr-FR", { month: "long", year: "numeric" })}</p>
              {(email || web) && (
                <p className="mt-2.5 border-t border-dashed border-line pt-2 text-center text-[8px] text-ink-4">
                  {email}{web && ` · ${web.replace(/^https?:\/\//, "")}`}
                </p>
              )}
            </div>
            <p className="mt-3 text-center text-[11px] text-ink-3">
              <code className="text-[9px]">longName, businessRegistrationNumber, taxNumber, capitalShare, ceoName, email, websiteUrl</code>{" "}
              repris automatiquement.
            </p>
          </CardContent>
        </Card>

        {/* Impacted docs */}
        <Card className="mt-4">
          <CardContent padding="md">
            <p className="mb-3 text-[13px] font-semibold text-ink">{tOrg("impactedTitle")}</p>
            {IMPACTED.map(({ label, Icon }, i) => (
              <div
                key={label}
                className={cn(
                  "flex items-center gap-2.5 py-2",
                  i < IMPACTED.length - 1 && "border-b border-line-soft",
                )}
              >
                <Icon className="h-3.5 w-3.5 shrink-0 text-ink-3" />
                <span className="flex-1 text-[12.5px] text-ink-2">{label}</span>
                <Check className="h-3.5 w-3.5 text-success-500" />
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </>
  );
}

function LifecycleSidebar({ currentStatus }: { currentStatus?: string | null }) {
  const tOrg = useTranslations("admin.organization");

  return (
    <div className="sticky top-[88px]">
      <Card>
        <CardContent padding="md">
          <p className="mb-4 text-[13px] font-semibold text-ink">{tOrg("lifecycle.title")}</p>
          <div className="flex flex-col gap-2">
            {[
              { key: "PENDING_APPROVAL", label: "PENDING_APPROVAL", arrow: "↓ approve()" },
              { key: "APPROVED", label: "APPROVED · active", arrow: "↓ suspend() / close()" },
              { key: "SUSPENDED", label: "SUSPENDED / CLOSED", arrow: null },
            ].map((step) => (
              <React.Fragment key={step.key}>
                <div
                  className={cn(
                    "rounded-[10px] border px-3 py-2 text-[12px] font-semibold",
                    currentStatus === step.key
                      ? "border-orange-400 bg-orange-50 text-orange-700"
                      : "border-line bg-white text-ink-3",
                  )}
                >
                  {step.label}
                </div>
                {step.arrow && (
                  <p className="pl-2 text-[11px] text-ink-4">{step.arrow}</p>
                )}
              </React.Fragment>
            ))}
          </div>

          <div className="mt-4 border-t border-line-soft pt-4 text-[11.5px] text-ink-3">
            {tOrg("lifecycle.note")}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
