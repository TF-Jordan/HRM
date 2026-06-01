"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Building2,
  Calendar,
  Check,
  CheckCircle2,
  Loader2,
  Mail,
  Pencil,
  Phone,
  Plus,
  Printer,
  Trash2,
  Users,
} from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { useSession } from "@/components/providers/session-provider";
import { PageHeader } from "@/components/shell/page-header";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input, Label } from "@/components/ui/input";
import { apiFetch, BffApiError } from "@/lib/api-client";
import { formatDate } from "@/lib/format";
import { cn } from "@/lib/utils";

import type {
  EmergencyContactResponse,
  EmployeeProfileResponse,
  PersonalInfoResponse,
  TimelineEventResponse,
} from "@/server/ksm/modules/employee-profile";
import type { DependentResponse, EmployeeResponse } from "@/server/ksm/modules/employees";
import type { OrganizationResponse } from "@/server/ksm/modules/organization";

// ─── Types ───────────────────────────────────────────────────────────────────

type Tab = "overview" | "contact" | "banking" | "family" | "emergency";

type ProfilePayload = {
  employee: EmployeeResponse | null;
  profile: EmployeeProfileResponse | null;
  personalInfo: PersonalInfoResponse | null;
  dependents: DependentResponse[];
  emergencyContacts: EmergencyContactResponse[];
  timeline: TimelineEventResponse[];
};

// ─── Helpers ─────────────────────────────────────────────────────────────────

function Field2({ label, value, verified }: { label: string; value?: string | null; verified?: boolean }) {
  return (
    <div>
      <div className="flex items-center gap-1.5">
        <span className="text-[11px] font-semibold uppercase tracking-wide text-ink-3">{label}</span>
        {verified && (
          <span className="inline-flex items-center gap-0.5 rounded-full bg-success-50 px-1.5 py-0.5 text-[9px] font-semibold text-success-600">
            <CheckCircle2 size={9} />
            Vérifié
          </span>
        )}
      </div>
      <div className="mt-1 text-[13.5px] text-ink-2">{value ?? <span className="text-ink-4">—</span>}</div>
    </div>
  );
}

function SectionRow({ children }: { children: React.ReactNode }) {
  return <div className="grid grid-cols-2 gap-x-8 gap-y-5 px-6 py-5">{children}</div>;
}

// ─── Completeness ─────────────────────────────────────────────────────────────

function useCompleteness(data: ProfilePayload | null | undefined) {
  if (!data) return { pct: 0, items: [] };
  const { profile, personalInfo, dependents, emergencyContacts } = data;

  const items: { key: string; done: boolean }[] = [
    { key: "birthDate", done: !!profile?.actorBirthDate },
    { key: "birthPlace", done: !!personalInfo?.lieuNaissance },
    { key: "idDocument", done: !!(personalInfo?.typePiece && personalInfo?.numeroPiece) },
    { key: "bankAccount", done: !!(data.employee?.compteBancaire || data.employee?.numMobileMoney) },
    { key: "dependents", done: dependents.length > 0 },
    { key: "emergencyContact", done: emergencyContacts.length > 0 },
    { key: "address", done: !!personalInfo?.adresseDomicile },
    { key: "languages", done: !!personalInfo?.languesParlees },
  ];
  const done = items.filter((i) => i.done).length;
  return { pct: Math.round((done / items.length) * 100), items };
}

// ─── Donut SVG ────────────────────────────────────────────────────────────────

function DonutChart({ pct }: { pct: number }) {
  const size = 130;
  const stroke = 16;
  const r = (size - stroke) / 2;
  const circ = 2 * Math.PI * r;
  const filled = (pct / 100) * circ;
  const color = pct >= 80 ? "#10B981" : pct >= 50 ? "#F97316" : "#EF4444";

  return (
    <div className="relative flex items-center justify-center" style={{ width: size, height: size }}>
      <svg width={size} height={size} style={{ transform: "rotate(-90deg)" }}>
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="var(--bg-soft)" strokeWidth={stroke} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke={color}
          strokeWidth={stroke}
          strokeDasharray={`${filled} ${circ - filled}`}
          strokeLinecap="round"
          style={{ transition: "stroke-dasharray 0.6s ease" }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
        <span className="font-display text-[26px] font-extrabold leading-none" style={{ color }}>{pct}%</span>
        <span className="mt-0.5 text-[10px] text-ink-3">complété</span>
      </div>
    </div>
  );
}

// ─── Personal info form ────────────────────────────────────────────────────────

type PersonalInfoFormValues = {
  lieuNaissance: string;
  situationMatrimoniale: string;
  typePiece: string;
  numeroPiece: string;
  dateEmissionPiece: string;
  niuFiscal: string;
  permisConduire: string;
  languesParlees: string;
};

function OverviewTab({
  profile,
  personalInfo,
  onSaved,
}: {
  profile: EmployeeProfileResponse | null;
  personalInfo: PersonalInfoResponse | null;
  onSaved: () => void;
}) {
  const t = useTranslations("profile");
  const [editing, setEditing] = React.useState(false);
  const { register, handleSubmit, reset } = useForm<PersonalInfoFormValues>({
    defaultValues: {
      lieuNaissance: personalInfo?.lieuNaissance ?? "",
      situationMatrimoniale: personalInfo?.situationMatrimoniale ?? "",
      typePiece: personalInfo?.typePiece ?? "",
      numeroPiece: personalInfo?.numeroPiece ?? "",
      dateEmissionPiece: personalInfo?.dateEmissionPiece ?? "",
      niuFiscal: personalInfo?.niuFiscal ?? "",
      permisConduire: personalInfo?.permisConduire ?? "",
      languesParlees: personalInfo?.languesParlees ?? "",
    },
  });

  const mutation = useMutation({
    mutationFn: (body: PersonalInfoFormValues) =>
      apiFetch("/api/hrm/profile/me/personal-info", { method: "PUT", body }),
    onSuccess: () => {
      toast.success(t("form.saved"));
      setEditing(false);
      onSaved();
    },
    onError: (err) => {
      toast.error(err instanceof BffApiError ? err.message : "Erreur");
    },
  });

  const idDisplay =
    personalInfo?.typePiece && personalInfo?.numeroPiece
      ? `${personalInfo.typePiece} · ${personalInfo.numeroPiece}`
      : null;

  if (!editing) {
    return (
      <>
        <div className="flex items-center justify-between border-b border-line px-6 py-3">
          <span className="text-[13px] font-semibold text-ink">{t("overview.title")}</span>
          <Button variant="ghost" size="sm" onClick={() => setEditing(true)}>
            <Pencil size={13} /> {t("overview.edit")}
          </Button>
        </div>
        <SectionRow>
          <Field2 label={t("overview.fullName")} value={profile?.actorDisplayName} />
          <Field2
            label={t("overview.birthDate")}
            value={profile?.actorBirthDate ? formatDate(profile.actorBirthDate) : null}
          />
          <Field2 label={t("overview.birthPlace")} value={personalInfo?.lieuNaissance} />
          <Field2 label={t("overview.nationality")} value={profile?.actorNationality} />
          <Field2 label={t("overview.gender")} value={profile?.actorGender} />
          <Field2 label={t("overview.maritalStatus")} value={personalInfo?.situationMatrimoniale} />
          <Field2 label={t("overview.idDocument")} value={idDisplay} />
          <Field2
            label={t("overview.idIssuedDate")}
            value={personalInfo?.dateEmissionPiece ? formatDate(personalInfo.dateEmissionPiece) : null}
          />
          <Field2 label={t("overview.cnps")} value={profile?.numCnps} />
          <Field2 label={t("overview.niu")} value={personalInfo?.niuFiscal} />
          <Field2 label={t("overview.drivingLicense")} value={personalInfo?.permisConduire} />
          <Field2 label={t("overview.languages")} value={personalInfo?.languesParlees} />
        </SectionRow>
      </>
    );
  }

  return (
    <form
      onSubmit={handleSubmit((v) => mutation.mutate(v))}
      onReset={() => { reset(); setEditing(false); }}
    >
      <div className="flex items-center justify-between border-b border-line px-6 py-3">
        <span className="text-[13px] font-semibold text-ink">{t("overview.title")}</span>
        <div className="flex gap-2">
          <Button type="reset" variant="ghost" size="sm" disabled={mutation.isPending}>{t("form.cancel")}</Button>
          <Button type="submit" size="sm" disabled={mutation.isPending}>
            {mutation.isPending ? <Loader2 size={13} className="animate-spin" /> : <Check size={13} />}
            {mutation.isPending ? t("form.saving") : t("form.save")}
          </Button>
        </div>
      </div>
      <div className="grid grid-cols-2 gap-x-8 gap-y-4 px-6 py-5">
        <Field>
          <Label>{t("overview.birthPlace")}</Label>
          <Input {...register("lieuNaissance")} placeholder="Ex: Douala, Cameroun" />
        </Field>
        <Field>
          <Label>{t("overview.maritalStatus")}</Label>
          <Input {...register("situationMatrimoniale")} placeholder="Ex: Marié(e), Célibataire…" />
        </Field>
        <Field>
          <Label>{t("overview.idDocument")} — Type</Label>
          <Input {...register("typePiece")} placeholder="CNI, Passeport…" />
        </Field>
        <Field>
          <Label>{t("overview.idDocument")} — N°</Label>
          <Input {...register("numeroPiece")} placeholder="Numéro de pièce" />
        </Field>
        <Field>
          <Label>{t("overview.idIssuedDate")}</Label>
          <Input type="date" {...register("dateEmissionPiece")} />
        </Field>
        <Field>
          <Label>{t("overview.niu")}</Label>
          <Input {...register("niuFiscal")} placeholder="Ex: M0123456789F" />
        </Field>
        <Field>
          <Label>{t("overview.drivingLicense")}</Label>
          <Input {...register("permisConduire")} placeholder="Ex: Catégorie B · 2010" />
        </Field>
        <Field>
          <Label>{t("overview.languages")}</Label>
          <Input {...register("languesParlees")} placeholder="Ex: Français, Anglais…" />
        </Field>
      </div>
    </form>
  );
}

// ─── Contact tab ──────────────────────────────────────────────────────────────

type ContactFormValues = {
  emailPersonnel: string;
  telephoneDomicile: string;
  whatsapp: string;
  adressePostale: string;
  adresseDomicile: string;
  ville: string;
  region: string;
  codePostal: string;
};

function ContactTab({
  profile,
  personalInfo,
  onSaved,
}: {
  profile: EmployeeProfileResponse | null;
  personalInfo: PersonalInfoResponse | null;
  onSaved: () => void;
}) {
  const t = useTranslations("profile");
  const [editing, setEditing] = React.useState(false);
  const { register, handleSubmit, reset } = useForm<ContactFormValues>({
    defaultValues: {
      emailPersonnel: personalInfo?.emailPersonnel ?? "",
      telephoneDomicile: personalInfo?.telephoneDomicile ?? "",
      whatsapp: personalInfo?.whatsapp ?? "",
      adressePostale: personalInfo?.adressePostale ?? "",
      adresseDomicile: personalInfo?.adresseDomicile ?? "",
      ville: personalInfo?.ville ?? "",
      region: personalInfo?.region ?? "",
      codePostal: personalInfo?.codePostal ?? "",
    },
  });

  const mutation = useMutation({
    mutationFn: (body: ContactFormValues) =>
      apiFetch("/api/hrm/profile/me/personal-info", { method: "PUT", body }),
    onSuccess: () => {
      toast.success(t("form.saved"));
      setEditing(false);
      onSaved();
    },
    onError: (err) => {
      toast.error(err instanceof BffApiError ? err.message : "Erreur");
    },
  });

  if (!editing) {
    return (
      <>
        <div className="flex items-center justify-between border-b border-line px-6 py-3">
          <span className="text-[13px] font-semibold text-ink">{t("contact.title")}</span>
          <Button variant="ghost" size="sm" onClick={() => setEditing(true)}>
            <Pencil size={13} /> {t("overview.edit")}
          </Button>
        </div>
        <SectionRow>
          <Field2 label={t("contact.professionalEmail")} value={profile?.actorEmail} verified />
          <Field2 label={t("contact.personalEmail")} value={personalInfo?.emailPersonnel} />
          <Field2 label={t("contact.mobilePhone")} value={profile?.actorPhoneNumber} verified />
          <Field2 label={t("contact.homePhone")} value={personalInfo?.telephoneDomicile} />
          <Field2 label={t("contact.whatsapp")} value={personalInfo?.whatsapp} />
          <Field2 label={t("contact.postalAddress")} value={personalInfo?.adressePostale} />
          <Field2 label={t("contact.homeAddress")} value={personalInfo?.adresseDomicile} />
          <Field2 label={t("contact.city")} value={personalInfo?.ville} />
          <Field2 label={t("contact.region")} value={personalInfo?.region} />
          <Field2 label={t("contact.postalCode")} value={personalInfo?.codePostal} />
        </SectionRow>
      </>
    );
  }

  return (
    <form
      onSubmit={handleSubmit((v) => mutation.mutate(v))}
      onReset={() => { reset(); setEditing(false); }}
    >
      <div className="flex items-center justify-between border-b border-line px-6 py-3">
        <span className="text-[13px] font-semibold text-ink">{t("contact.title")}</span>
        <div className="flex gap-2">
          <Button type="reset" variant="ghost" size="sm" disabled={mutation.isPending}>{t("form.cancel")}</Button>
          <Button type="submit" size="sm" disabled={mutation.isPending}>
            {mutation.isPending ? <Loader2 size={13} className="animate-spin" /> : <Check size={13} />}
            {mutation.isPending ? t("form.saving") : t("form.save")}
          </Button>
        </div>
      </div>
      <div className="grid grid-cols-2 gap-x-8 gap-y-4 px-6 py-5">
        <Field>
          <Label>{t("contact.personalEmail")}</Label>
          <Input type="email" {...register("emailPersonnel")} placeholder="email@example.com" />
        </Field>
        <Field>
          <Label>{t("contact.homePhone")}</Label>
          <Input {...register("telephoneDomicile")} placeholder="+237 2 22…" />
        </Field>
        <Field>
          <Label>{t("contact.whatsapp")}</Label>
          <Input {...register("whatsapp")} placeholder="+237 6…" />
        </Field>
        <Field>
          <Label>{t("contact.postalAddress")}</Label>
          <Input {...register("adressePostale")} placeholder="BP 1234, Yaoundé" />
        </Field>
        <Field className="col-span-2">
          <Label>{t("contact.homeAddress")}</Label>
          <Input {...register("adresseDomicile")} placeholder="Quartier, Rue…" />
        </Field>
        <Field>
          <Label>{t("contact.city")}</Label>
          <Input {...register("ville")} />
        </Field>
        <Field>
          <Label>{t("contact.region")}</Label>
          <Input {...register("region")} />
        </Field>
        <Field>
          <Label>{t("contact.postalCode")}</Label>
          <Input {...register("codePostal")} />
        </Field>
      </div>
    </form>
  );
}

// ─── Banking tab (read-only from employee record) ────────────────────────────

function BankingTab({ employee }: { employee: EmployeeResponse | null }) {
  const t = useTranslations("profile");
  const paymentModeLabel: Record<string, string> = {
    BANK_TRANSFER: "Virement bancaire",
    MTN_MOBILE_MONEY: "MTN Mobile Money",
    ORANGE_MONEY: "Orange Money",
    CASH: "Espèces",
  };
  const operatorLabel: Record<string, string> = { MTN: "MTN Mobile Money", ORANGE: "Orange Money" };

  return (
    <>
      <div className="flex items-center justify-between border-b border-line px-6 py-3">
        <span className="text-[13px] font-semibold text-ink">{t("banking.title")}</span>
      </div>
      <div className="px-6 py-5 space-y-5">
        {employee?.compteBancaire ? (
          <div
            className="relative overflow-hidden rounded-[14px] p-5"
            style={{ background: "linear-gradient(135deg, #1A150E 0%, #2D2520 100%)", color: "#fff" }}
          >
            <div
              className="pointer-events-none absolute -right-10 -top-10 h-48 w-48 rounded-full"
              style={{ background: "radial-gradient(circle, rgba(249,115,22,0.3) 0%, transparent 70%)" }}
            />
            <div className="relative">
              <div className="text-[11px] uppercase tracking-wider opacity-60">{t("banking.mainAccount")}</div>
              <div className="mt-1.5 font-display text-[20px] font-bold">{t("banking.bankName")}</div>
              <div className="mt-2 font-mono text-[13px] tracking-widest opacity-80">
                {employee.compteBancaire.replace(/(.{4})/g, "$1 ").trim()}
              </div>
              <div className="mt-3">
                <span className="inline-flex items-center gap-1 rounded-full bg-success-600/20 px-2.5 py-0.5 text-[11px] font-semibold text-emerald-300">
                  <Check size={10} /> {t("banking.active")}
                </span>
              </div>
            </div>
          </div>
        ) : null}

        {(employee?.numMobileMoney || employee?.operateurMm) && (
          <div className="rounded-[12px] border border-line bg-white p-4">
            <div className="flex items-center gap-3">
              <div className="flex h-8 w-8 items-center justify-center rounded-[10px] bg-orange-100 text-[13px] font-bold text-orange-700">
                {employee.operateurMm === "MTN" ? "M" : "O"}
              </div>
              <div>
                <div className="text-[13px] font-semibold">
                  {operatorLabel[employee.operateurMm ?? ""] ?? employee.operateurMm}
                </div>
                <div className="font-mono text-[11px] text-ink-3">{employee.numMobileMoney}</div>
              </div>
            </div>
          </div>
        )}

        {!employee?.compteBancaire && !employee?.numMobileMoney && (
          <p className="text-[13px] text-ink-3">{t("banking.noData")}</p>
        )}

        {employee?.modePaiement && (
          <div>
            <div className="mb-2 text-[11px] font-semibold uppercase tracking-wide text-ink-3">
              {t("banking.paymentPreference")}
            </div>
            <span className="inline-flex rounded-full bg-orange-50 px-3 py-1 text-[12.5px] font-semibold text-orange-700">
              {paymentModeLabel[employee.modePaiement] ?? employee.modePaiement}
            </span>
          </div>
        )}
      </div>
    </>
  );
}

// ─── Family tab ────────────────────────────────────────────────────────────────

const DEPENDENT_TONES = ["orange", "blue", "green", "violet", "amber", "teal"] as const;

function FamilyTab({ dependents }: { dependents: DependentResponse[] }) {
  const t = useTranslations("profile");

  return (
    <>
      <div className="flex items-center justify-between border-b border-line px-6 py-3">
        <span className="text-[13px] font-semibold text-ink">{t("family.title")}</span>
      </div>
      <div className="px-6 py-5 space-y-3">
        <div className="flex items-center justify-between">
          <span className="text-[13px] font-semibold text-ink">
            {t("family.dependents")} ({dependents.length})
          </span>
        </div>

        {dependents.length === 0 ? (
          <p className="rounded-[12px] bg-bg-soft px-4 py-4 text-[13px] text-ink-3">{t("family.noData")}</p>
        ) : (
          <div className="grid grid-cols-2 gap-3">
            {dependents.map((dep, i) => (
              <div key={dep.id} className="rounded-[12px] border border-line bg-white p-4">
                <div className="flex items-center gap-3">
                  <Avatar
                    name={`${dep.prenom} ${dep.nom}`}
                    tone={DEPENDENT_TONES[i % DEPENDENT_TONES.length]}
                  />
                  <div>
                    <div className="text-[13.5px] font-bold text-ink">{dep.prenom} {dep.nom}</div>
                    <div className="text-[11px] text-ink-3">{dep.lienParente}</div>
                    {dep.dateNaissance && (
                      <div className="text-[11px] text-ink-3">{formatDate(dep.dateNaissance)}</div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {dependents.length > 0 && (
          <div className="flex items-center gap-2 rounded-[10px] bg-orange-50 px-3 py-2.5 text-[12.5px] text-orange-700">
            <Users size={14} />
            {t("family.irppNote", { count: dependents.length })}
          </div>
        )}
      </div>
    </>
  );
}

// ─── Emergency contacts tab ────────────────────────────────────────────────────

type EmergencyFormValues = {
  nom: string;
  prenom: string;
  relation: string;
  telephone: string;
  email: string;
  priorite: number;
};

function EmergencyTab({
  contacts,
  onSaved,
}: {
  contacts: EmergencyContactResponse[];
  onSaved: () => void;
}) {
  const t = useTranslations("profile");
  const qc = useQueryClient();
  const [showForm, setShowForm] = React.useState(false);
  const { register, handleSubmit, reset } = useForm<EmergencyFormValues>({
    defaultValues: { nom: "", prenom: "", relation: "", telephone: "", email: "", priorite: contacts.length + 1 },
  });

  const addMutation = useMutation({
    mutationFn: (body: EmergencyFormValues) =>
      apiFetch("/api/hrm/profile/me/emergency-contacts", { method: "POST", body }),
    onSuccess: () => {
      toast.success(t("form.saved"));
      setShowForm(false);
      reset();
      qc.invalidateQueries({ queryKey: ["hrm", "profile", "me"] });
      onSaved();
    },
    onError: (err) => toast.error(err instanceof BffApiError ? err.message : "Erreur"),
  });

  const deleteMutation = useMutation({
    mutationFn: (contactId: string) =>
      apiFetch(`/api/hrm/profile/me/emergency-contacts/${contactId}`, { method: "DELETE" }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["hrm", "profile", "me"] });
      onSaved();
    },
    onError: (err) => toast.error(err instanceof BffApiError ? err.message : "Erreur"),
  });

  const CONTACT_TONES = ["blue", "amber", "green", "orange", "violet", "teal"] as const;

  return (
    <>
      <div className="flex items-center justify-between border-b border-line px-6 py-3">
        <span className="text-[13px] font-semibold text-ink">{t("emergency.title")}</span>
      </div>
      <div className="px-6 py-5 space-y-3">
        {contacts.length === 0 && !showForm && (
          <p className="rounded-[12px] bg-bg-soft px-4 py-4 text-[13px] text-ink-3">{t("emergency.noData")}</p>
        )}

        {contacts.map((c, i) => (
          <div key={c.id} className="flex items-center gap-3 rounded-[12px] border border-line bg-white p-4">
            <Avatar name={`${c.prenom ?? ""} ${c.nom}`} tone={CONTACT_TONES[i % CONTACT_TONES.length]} size="lg" />
            <div className="flex-1 min-w-0">
              <div className="flex flex-wrap items-center gap-2">
                <span className="text-[14px] font-bold text-ink">{c.prenom} {c.nom}</span>
                {c.relation && (
                  <span className="rounded-full bg-bg-soft px-2 py-0.5 text-[11px] text-ink-3">{c.relation}</span>
                )}
                {c.priorite === 1 && (
                  <Badge tone="orange" showDot={false} className="text-[10px] px-1.5 py-0.5">
                    {t("emergency.priority", { n: 1 })}
                  </Badge>
                )}
              </div>
              <div className="mt-1.5 flex flex-wrap gap-4 text-[12px] text-ink-3">
                {c.telephone && (
                  <span className="flex items-center gap-1"><Phone size={11} />{c.telephone}</span>
                )}
                {c.email && (
                  <span className="flex items-center gap-1"><Mail size={11} />{c.email}</span>
                )}
              </div>
            </div>
            <button
              onClick={() => { if (confirm(t("emergency.confirmDelete"))) deleteMutation.mutate(c.id); }}
              className="shrink-0 rounded-[8px] p-1.5 text-ink-4 transition hover:bg-danger-50 hover:text-danger-600"
              disabled={deleteMutation.isPending}
            >
              <Trash2 size={13} />
            </button>
          </div>
        ))}

        {showForm && (
          <form
            onSubmit={handleSubmit((v) => addMutation.mutate(v))}
            className="rounded-[12px] border border-orange-200 bg-orange-50/40 p-4 space-y-3"
          >
            <div className="grid grid-cols-2 gap-3">
              <Field>
                <Label>Nom *</Label>
                <Input {...register("nom")} required placeholder="Nom" />
              </Field>
              <Field>
                <Label>Prénom</Label>
                <Input {...register("prenom")} placeholder="Prénom" />
              </Field>
              <Field>
                <Label>{t("emergency.relation")}</Label>
                <Input {...register("relation")} placeholder="Ex: Conjoint(e), Mère…" />
              </Field>
              <Field>
                <Label>{t("emergency.phone")}</Label>
                <Input {...register("telephone")} placeholder="+237 6…" />
              </Field>
              <Field>
                <Label>{t("emergency.email")}</Label>
                <Input type="email" {...register("email")} placeholder="email@example.com" />
              </Field>
              <Field>
                <Label>Priorité</Label>
                <Input type="number" {...register("priorite", { valueAsNumber: true })} min={1} />
              </Field>
            </div>
            <div className="flex justify-end gap-2">
              <Button type="button" variant="ghost" size="sm" onClick={() => { setShowForm(false); reset(); }}>
                {t("form.cancel")}
              </Button>
              <Button type="submit" size="sm" disabled={addMutation.isPending}>
                {addMutation.isPending ? <Loader2 size={13} className="animate-spin" /> : <Check size={13} />}
                {addMutation.isPending ? t("form.saving") : t("form.save")}
              </Button>
            </div>
          </form>
        )}

        {!showForm && (
          <Button variant="secondary" size="sm" onClick={() => setShowForm(true)}>
            <Plus size={13} /> {t("emergency.addContact")}
          </Button>
        )}
      </div>
    </>
  );
}

// ─── Main component ────────────────────────────────────────────────────────────

export function MyProfile() {
  const t = useTranslations("profile");
  const { session } = useSession();
  const qc = useQueryClient();
  const [tab, setTab] = React.useState<Tab>("overview");

  const query = useQuery({
    queryKey: ["hrm", "profile", "me"],
    queryFn: () => apiFetch<ProfilePayload>("/api/hrm/profile/me"),
  });

  const orgQuery = useQuery({
    queryKey: ["admin", "organization"],
    queryFn: () => apiFetch<OrganizationResponse>("/api/admin/organization"),
    staleTime: 5 * 60 * 1000,
  });

  const data = query.data;
  const { pct, items: completenessItems } = useCompleteness(data);

  const name = data?.profile?.actorDisplayName ?? data?.employee?.actorDisplayName ?? session?.user.fullName ?? "";
  const matricule = data?.employee?.matricule ?? "";
  const role = session?.user.roles?.[0] ?? "Employé";
  const dept = data?.employee?.departmentCode ?? "";
  const hireDate = data?.employee?.dateEmbauche ? formatDate(data.employee.dateEmbauche) : null;
  const manager = data?.profile?.managerDisplayName;
  const status = data?.employee?.status ?? "ACTIVE";
  const contractType = "CDI"; // derived from contracts if needed

  const TABS: { id: Tab; label: string }[] = [
    { id: "overview", label: t("tabs.overview") },
    { id: "contact", label: t("tabs.contact") },
    { id: "banking", label: t("tabs.banking") },
    { id: "family", label: t("tabs.family") },
    { id: "emergency", label: t("tabs.emergency") },
  ];

  const invalidate = () => qc.invalidateQueries({ queryKey: ["hrm", "profile", "me"] });

  if (query.isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="animate-spin text-ink-3" size={28} />
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <PageHeader title={t("title")} subtitle={t("subtitle")} />

      {/* Hero */}
      <div
        className="relative overflow-hidden rounded-[18px] border border-orange-200 p-6"
        style={{ background: "linear-gradient(135deg, #FFFAF2 0%, #FFFFFF 60%)" }}
      >
        <div
          className="pointer-events-none absolute -right-16 -top-16 h-60 w-60 rounded-full opacity-[0.07]"
          style={{ background: "radial-gradient(circle, #F97316 0%, transparent 70%)", filter: "blur(20px)" }}
        />
        <div className="relative flex flex-wrap items-start gap-5">
          {/* Avatar */}
          <div className="relative shrink-0">
            <Avatar name={name} size="xl" tone="orange" />
          </div>

          {/* Identity */}
          <div className="flex-1 min-w-0">
            <div className="mb-1.5 flex flex-wrap items-center gap-2">
              <span className="font-mono rounded-full bg-orange-50 px-2.5 py-0.5 text-[11px] text-orange-700">
                {matricule}
              </span>
              <Badge tone="success" showDot>
                {contractType} · {status === "ACTIVE" ? "Actif" : status}
              </Badge>
            </div>
            <div className="font-display text-[28px] font-extrabold leading-tight text-ink">{name}</div>
            <div className="mt-1 text-[13.5px] text-ink-2">
              {role}{dept ? ` · ${dept}` : ""}
            </div>
            <div className="mt-3 flex flex-wrap gap-5 text-[13px] text-ink-2">
              {hireDate && (
                <span className="flex items-center gap-1.5">
                  <Calendar size={13} className="text-ink-4" />
                  {t("hero.since", { company: orgQuery.data?.longName || orgQuery.data?.shortName || session?.workspace?.organizationName || "—", date: hireDate })}
                </span>
              )}
              {dept && (
                <span className="flex items-center gap-1.5">
                  <Building2 size={13} className="text-ink-4" /> {dept}
                </span>
              )}
              {manager && (
                <span className="flex items-center gap-1.5">
                  <Users size={13} className="text-ink-4" /> {manager}
                </span>
              )}
            </div>
          </div>

          {/* Actions */}
          <div className="flex shrink-0 flex-col gap-2">
            <Button size="sm">
              <Pencil size={13} /> {t("hero.editProfile")}
            </Button>
            <Button variant="secondary" size="sm">
              <Printer size={13} /> {t("hero.printSheet")}
            </Button>
          </div>
        </div>
      </div>

      {/* Tabs nav */}
      <div className="flex gap-1 border-b border-line-soft">
        {TABS.map((t_) => (
          <button
            key={t_.id}
            onClick={() => setTab(t_.id)}
            className={cn(
              "px-3.5 py-2.5 text-[13.5px] transition-colors",
              tab === t_.id
                ? "border-b-2 border-orange-500 font-bold text-ink"
                : "border-b-2 border-transparent font-medium text-ink-3 hover:text-ink-2",
            )}
          >
            {t_.label}
          </button>
        ))}
      </div>

      {/* Content */}
      <div className="grid grid-cols-[2fr_1fr] gap-4">
        {/* Left: tab card */}
        <Card>
          <CardContent className="p-0">
            {tab === "overview" && (
              <OverviewTab
                profile={data?.profile ?? null}
                personalInfo={data?.personalInfo ?? null}
                onSaved={invalidate}
              />
            )}
            {tab === "contact" && (
              <ContactTab
                profile={data?.profile ?? null}
                personalInfo={data?.personalInfo ?? null}
                onSaved={invalidate}
              />
            )}
            {tab === "banking" && <BankingTab employee={data?.employee ?? null} />}
            {tab === "family" && <FamilyTab dependents={data?.dependents ?? []} />}
            {tab === "emergency" && (
              <EmergencyTab contacts={data?.emergencyContacts ?? []} onSaved={invalidate} />
            )}
          </CardContent>
        </Card>

        {/* Right column */}
        <div className="flex flex-col gap-4">
          {/* Completeness */}
          <Card>
            <CardContent className="p-5">
              <div className="mb-3 text-[13.5px] font-semibold text-ink">
                {t("completeness.title")}
              </div>
              <div className="flex justify-center mb-4">
                <DonutChart pct={pct} />
              </div>
              <div className="mb-2 text-[11px] font-semibold uppercase tracking-wide text-ink-3">
                {t("completeness.toFill")}
              </div>
              <div className="space-y-1">
                {completenessItems.map((item) => {
                  const labelMap: Record<string, string> = {
                    birthDate: t("completeness.items.birthDate"),
                    birthPlace: t("completeness.items.birthPlace"),
                    idDocument: t("completeness.items.idDocument"),
                    bankAccount: t("completeness.items.bankAccount"),
                    dependents: t("completeness.items.dependents"),
                    emergencyContact: t("completeness.items.emergencyContact"),
                    address: t("completeness.items.address"),
                    languages: t("completeness.items.languages"),
                  };
                  return (
                    <div key={item.key} className="flex items-center gap-2 py-1 text-[12.5px]">
                      {item.done ? (
                        <Check size={13} className="shrink-0 text-success-500" />
                      ) : (
                        <div className="h-3.5 w-3.5 shrink-0 rounded-[3px] border-[1.5px] border-dashed border-ink-4" />
                      )}
                      <span className={item.done ? "text-ink-3 line-through" : "text-ink-2"}>
                        {labelMap[item.key] ?? item.key}
                      </span>
                    </div>
                  );
                })}
              </div>
            </CardContent>
          </Card>

          {/* Recent history */}
          <Card>
            <CardContent className="p-5">
              <div className="mb-3 text-[13.5px] font-semibold text-ink">{t("history.title")}</div>
              {(data?.timeline ?? []).length === 0 ? (
                <p className="text-[12.5px] text-ink-3">—</p>
              ) : (
                <div className="space-y-0">
                  {(data?.timeline ?? []).slice(0, 5).map((ev, i, arr) => (
                    <div
                      key={`${ev.type}-${ev.date}-${i}`}
                      className={cn(
                        "flex items-start gap-3 py-2",
                        i < arr.length - 1 && "border-b border-line-soft",
                      )}
                    >
                      <div className="mt-1 h-2 w-2 shrink-0 rounded-full bg-orange-400" />
                      <div className="flex-1 text-[12.5px] text-ink-2">{ev.title}</div>
                      <span className="shrink-0 text-[11px] text-ink-4">
                        {formatDate(ev.date)}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
