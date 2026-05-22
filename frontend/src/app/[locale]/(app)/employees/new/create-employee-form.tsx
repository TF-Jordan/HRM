"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2, Save, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import {
  createEmployeeSchema,
  type CreateEmployeeFormValues,
} from "@/lib/validation/hrm/employee.schema";
import { useCreateEmployee } from "@/hooks/modules/useEmployees";
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
import { CredentialsModal } from "@/components/employees/CredentialsModal";
import type { EmployeeAccountInfo } from "@/lib/types/hrm/employee";

export function CreateEmployeeForm() {
  const t = useTranslations("employees");
  const tNav = useTranslations("navigation");
  const tCommon = useTranslations("common");
  const router = useRouter();
  const mutation = useCreateEmployee();
  const [credModal, setCredModal] = React.useState<
    | (EmployeeAccountInfo & { matricule: string; employeeId: string })
    | null
  >(null);

  const form = useForm<CreateEmployeeFormValues>({
    resolver: zodResolver(createEmployeeSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      phoneNumber: "",
      numCnps: "",
      categorie: 1,
      echelon: "",
      dateEmbauche: new Date().toISOString().slice(0, 10),
      departmentCode: "",
      modePaiement: "BANK_TRANSFER",
      compteBancaire: "",
      numMobileMoney: "",
      operateurMm: null,
      contractType: null,
      contractDateDebut: null,
      contractDateFin: null,
      salaireBase: null,
      avantagesNature: null,
      periodeEssai: null,
    },
  });

  const onSubmit = (values: CreateEmployeeFormValues) => {
    mutation.mutate(values as never, {
      onSuccess: (employee) => {
        toast.success(`${employee.actorDisplayName} — ${employee.matricule}`);
        if (employee.account) {
          setCredModal({
            ...employee.account,
            matricule: employee.matricule,
            employeeId: employee.id,
          });
        } else {
          router.push(`/employees/${employee.id}` as never);
        }
      },
      onError: (err) => {
        toast.error((err as Error).message);
      },
    });
  };

  const closeCredModal = () => {
    const employeeId = credModal?.employeeId;
    setCredModal(null);
    if (employeeId) router.push(`/employees/${employeeId}` as never);
  };

  const modePaiement = form.watch("modePaiement");

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-01"
        crumbs={[
          { label: tNav("items.employees"), href: "/employees" },
          { label: t("list.newButton") },
        ]}
        title={t("list.newButton")}
        subtitle={t("list.subtitle")}
      />

      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        {mutation.isError && (
          <Card>
            <CardContent className="flex items-start gap-3">
              <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
              <div className="text-sm">
                <div className="font-semibold text-status-red-600">
                  {(mutation.error as Error).message}
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        <Section title={t("form.sections.identity")}>
          <FieldGrid>
            <Field id="firstName" label={t("form.firstName")} required error={form.formState.errors.firstName?.message}>
              <Input id="firstName" {...form.register("firstName")} />
            </Field>
            <Field id="lastName" label={t("form.lastName")} required error={form.formState.errors.lastName?.message}>
              <Input id="lastName" {...form.register("lastName")} />
            </Field>
            <Field id="email" label={t("form.email")} required error={form.formState.errors.email?.message}>
              <Input id="email" type="email" {...form.register("email")} />
            </Field>
            <Field id="phoneNumber" label={t("form.phoneNumber")}>
              <Input id="phoneNumber" {...form.register("phoneNumber")} />
            </Field>
          </FieldGrid>
        </Section>

        <Section title={t("form.sections.employment")}>
          <FieldGrid>
            <Field id="numCnps" label={t("form.numCnps")}>
              <Input id="numCnps" {...form.register("numCnps")} />
            </Field>
            <Field id="categorie" label={t("form.categorie")} required>
              <Input id="categorie" type="number" min={1} max={20} {...form.register("categorie")} />
            </Field>
            <Field id="echelon" label={t("form.echelon")}>
              <Input id="echelon" {...form.register("echelon")} />
            </Field>
            <Field id="dateEmbauche" label={t("form.dateEmbauche")} required error={form.formState.errors.dateEmbauche?.message}>
              <Input id="dateEmbauche" type="date" {...form.register("dateEmbauche")} />
            </Field>
            <Field id="departmentCode" label={t("form.departmentCode")}>
              <Input id="departmentCode" {...form.register("departmentCode")} />
            </Field>
          </FieldGrid>
        </Section>

        <Section title={t("form.sections.payment")}>
          <FieldGrid>
            <Field id="modePaiement" label={t("form.modePaiement")} required>
              <Controller
                control={form.control}
                name="modePaiement"
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger id="modePaiement">
                      <SelectValue placeholder="—" />
                    </SelectTrigger>
                    <SelectContent>
                      {(["BANK_TRANSFER", "MTN_MOBILE_MONEY", "ORANGE_MONEY", "CASH"] as const).map((v) => (
                        <SelectItem key={v} value={v}>
                          {t(`paymentModes.${v}` as never)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
            </Field>
            {modePaiement === "BANK_TRANSFER" && (
              <Field
                id="compteBancaire"
                label={t("form.compteBancaire")}
                required
                error={form.formState.errors.compteBancaire?.message}
              >
                <Input id="compteBancaire" {...form.register("compteBancaire")} />
              </Field>
            )}
            {(modePaiement === "MTN_MOBILE_MONEY" || modePaiement === "ORANGE_MONEY") && (
              <Field
                id="numMobileMoney"
                label={t("form.numMobileMoney")}
                required
                error={form.formState.errors.numMobileMoney?.message}
              >
                <Input id="numMobileMoney" {...form.register("numMobileMoney")} />
              </Field>
            )}
          </FieldGrid>
        </Section>

        <Section title={t("form.sections.firstContract")}>
          <FieldGrid>
            <Field id="contractType" label={t("form.contractType")}>
              <Controller
                control={form.control}
                name="contractType"
                render={({ field }) => (
                  <Select value={field.value ?? ""} onValueChange={(v) => field.onChange(v || null)}>
                    <SelectTrigger id="contractType">
                      <SelectValue placeholder={tCommon("none")} />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="CDI">CDI</SelectItem>
                      <SelectItem value="CDD">CDD</SelectItem>
                      <SelectItem value="STAGE">Stage</SelectItem>
                      <SelectItem value="INTERIM">Intérim</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
            </Field>
            <Field id="contractDateDebut" label={t("form.contractDateDebut")}>
              <Input id="contractDateDebut" type="date" {...form.register("contractDateDebut")} />
            </Field>
            <Field id="contractDateFin" label={t("form.contractDateFin")}>
              <Input id="contractDateFin" type="date" {...form.register("contractDateFin")} />
            </Field>
            <Field id="salaireBase" label={t("form.salaireBase")}>
              <Input id="salaireBase" type="number" step="1" min={0} {...form.register("salaireBase")} />
            </Field>
            <Field id="avantagesNature" label={t("form.avantagesNature")}>
              <Input id="avantagesNature" type="number" step="1" min={0} {...form.register("avantagesNature")} />
            </Field>
            <Field id="periodeEssai" label={t("form.periodeEssai")}>
              <Input id="periodeEssai" type="number" step="1" min={0} {...form.register("periodeEssai")} />
            </Field>
          </FieldGrid>
        </Section>

        <div className="flex items-center justify-end gap-3">
          <Button type="button" variant="secondary" onClick={() => router.push("/employees" as never)}>
            {tCommon("actions.cancel" as never)}
          </Button>
          <Button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? <Loader2 className="size-4 animate-spin" /> : <Save className="size-4" />}
            {mutation.isPending ? t("form.submitting") : t("form.submit")}
          </Button>
        </div>
      </form>

      {credModal && (
        <CredentialsModal
          open={true}
          onClose={closeCredModal}
          matricule={credModal.matricule}
          email={credModal.email}
          temporaryPassword={credModal.temporaryPassword}
          emailSent={credModal.emailSent}
          roleAssigned={credModal.roleAssigned}
          membershipCreated={credModal.membershipCreated}
        />
      )}
    </div>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <Card>
      <CardContent className="space-y-5">
        <div className="border-b border-line-soft pb-3 font-display text-base font-bold text-ink">
          {title}
        </div>
        {children}
      </CardContent>
    </Card>
  );
}

function FieldGrid({ children }: { children: React.ReactNode }) {
  return <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">{children}</div>;
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
