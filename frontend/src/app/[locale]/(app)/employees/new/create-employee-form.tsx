"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2, Send, AlertTriangle } from "lucide-react";
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
import { CredentialsModal } from "@/components/employees/CredentialsModal";
import type { EmployeeAccountInfo, MobileMoneyOperator } from "@/lib/types/hrm/employee";
import type { ContractType } from "@/lib/types/hrm/contract";

type PaymentChoice = "bank" | "mobile" | "cash";

export function CreateEmployeeForm() {
  const t = useTranslations("employees");
  const tNav = useTranslations("navigation");
  const tCommon = useTranslations("common");
  const router = useRouter();
  const mutation = useCreateEmployee();
  const [credModal, setCredModal] = React.useState<
    (EmployeeAccountInfo & { matricule: string; employeeId: string }) | null
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
      poste: "",
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
          setCredModal({ ...employee.account, matricule: employee.matricule, employeeId: employee.id });
        } else {
          router.push(`/employees/${employee.id}` as never);
        }
      },
      onError: (err) => toast.error((err as Error).message),
    });
  };

  const closeCredModal = () => {
    const employeeId = credModal?.employeeId;
    setCredModal(null);
    if (employeeId) router.push(`/employees/${employeeId}` as never);
  };

  const errors = form.formState.errors;
  const modePaiement = form.watch("modePaiement");
  const operateurMm = form.watch("operateurMm");
  const contractType = form.watch("contractType");
  const paymentChoice: PaymentChoice =
    modePaiement === "BANK_TRANSFER" ? "bank" : modePaiement === "CASH" ? "cash" : "mobile";

  function choosePayment(choice: PaymentChoice) {
    if (choice === "bank") {
      form.setValue("modePaiement", "BANK_TRANSFER");
      form.setValue("operateurMm", null);
    } else if (choice === "cash") {
      form.setValue("modePaiement", "CASH");
      form.setValue("operateurMm", null);
    } else {
      const op: MobileMoneyOperator = operateurMm === "ORANGE" ? "ORANGE" : "MTN";
      form.setValue("operateurMm", op);
      form.setValue("modePaiement", op === "ORANGE" ? "ORANGE_MONEY" : "MTN_MOBILE_MONEY");
    }
  }

  function chooseOperator(op: MobileMoneyOperator) {
    form.setValue("operateurMm", op);
    form.setValue("modePaiement", op === "ORANGE" ? "ORANGE_MONEY" : "MTN_MOBILE_MONEY");
  }

  const contractOptions: RadioCardOption<ContractType>[] = (
    ["CDI", "CDD", "STAGE", "INTERIM"] as const
  ).map((v) => ({
    value: v,
    label: v === "INTERIM" ? "Intérim" : v === "STAGE" ? "Stage" : v,
    sublabel: t(`form.contractTypes.${v}` as never),
    code: `enum: ${v}`,
  }));

  const paymentOptions: RadioCardOption<PaymentChoice>[] = [
    { value: "bank", label: t("form.paymentChoice.bank"), code: "enum" },
    { value: "mobile", label: t("form.paymentChoice.mobile"), code: "enum" },
    { value: "cash", label: t("form.paymentChoice.cash"), code: "enum" },
  ];

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        crumbs={[
          { label: tNav("items.employees"), href: "/employees" },
          { label: t("list.newButton") },
        ]}
        title={t("list.newButton")}
        subtitle={t("list.subtitle")}
      />

      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
        {mutation.isError && (
          <Card>
            <CardContent className="flex items-start gap-3">
              <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
              <div className="text-sm font-semibold text-status-red-600">
                {(mutation.error as Error).message}
              </div>
            </CardContent>
          </Card>
        )}

        <FormSection title={t("form.sections.identity")}>
          <FieldGrid>
            <Field id="firstName" label={t("form.firstName")} code="firstName" required error={errors.firstName?.message}>
              <Input id="firstName" {...form.register("firstName")} />
            </Field>
            <Field id="lastName" label={t("form.lastName")} code="lastName" required error={errors.lastName?.message}>
              <Input id="lastName" {...form.register("lastName")} />
            </Field>
            <Field id="email" label={t("form.email")} code="email" required error={errors.email?.message}>
              <Input id="email" type="email" {...form.register("email")} />
            </Field>
            <Field id="phoneNumber" label={t("form.phoneNumber")} code="phoneNumber">
              <Input id="phoneNumber" {...form.register("phoneNumber")} />
            </Field>
          </FieldGrid>
        </FormSection>

        <FormSection title={t("form.sections.administrative")} hint={t("form.sectionHints.administrative")}>
          <FieldGrid>
            <Field id="numCnps" label={t("form.numCnps")} code="numCnps" hint={t("form.hints.numCnps")}>
              <Input id="numCnps" {...form.register("numCnps")} />
            </Field>
            <Field id="dateEmbauche" label={t("form.dateEmbauche")} code="dateEmbauche" required error={errors.dateEmbauche?.message}>
              <Input id="dateEmbauche" type="date" {...form.register("dateEmbauche")} />
            </Field>
            <Field id="categorie" label={t("form.categorie")} code="categorie" required hint={t("form.hints.categorie")}>
              <Input id="categorie" type="number" min={1} max={20} {...form.register("categorie")} />
            </Field>
            <Field id="echelon" label={t("form.echelon")} code="echelon" hint={t("form.hints.echelon")}>
              <Input id="echelon" {...form.register("echelon")} />
            </Field>
            <Field id="poste" label={t("form.poste")} code="poste">
              <Input id="poste" {...form.register("poste")} />
            </Field>
            <Field id="departmentCode" label={t("form.departmentCode")} code="departmentCode">
              <Input id="departmentCode" {...form.register("departmentCode")} />
            </Field>
          </FieldGrid>
        </FormSection>

        <FormSection title={t("form.sections.firstContract")} hint={t("form.sectionHints.firstContract")}>
          <Field label={t("form.contractType")} code="contractType">
            <RadioCardGroup
              value={contractType ?? undefined}
              onChange={(v) => form.setValue("contractType", v)}
              options={contractOptions}
              cols={4}
            />
          </Field>
          <FieldGrid>
            <Field id="contractDateDebut" label={t("form.contractDateDebut")} code="contractDateDebut" error={errors.contractDateDebut?.message}>
              <Input id="contractDateDebut" type="date" {...form.register("contractDateDebut")} />
            </Field>
            <Field id="contractDateFin" label={t("form.contractDateFin")} code="contractDateFin" hint={t("form.hints.contractDateFin")} error={errors.contractDateFin?.message}>
              <Input id="contractDateFin" type="date" {...form.register("contractDateFin")} />
            </Field>
            <Field id="salaireBase" label={t("form.salaireBase")} code="salaireBase">
              <SuffixInput id="salaireBase" suffix="XAF" type="number" step="1" min={0} {...form.register("salaireBase")} />
            </Field>
            <Field id="avantagesNature" label={t("form.avantagesNature")} code="avantagesNature" hint={t("form.hints.avantagesNature")}>
              <SuffixInput id="avantagesNature" suffix="XAF" type="number" step="1" min={0} {...form.register("avantagesNature")} />
            </Field>
            <Field id="periodeEssai" label={t("form.periodeEssai")} code="periodeEssai" hint={t("form.hints.periodeEssai")}>
              <SuffixInput id="periodeEssai" suffix="jours" type="number" step="1" min={0} {...form.register("periodeEssai")} />
            </Field>
          </FieldGrid>
        </FormSection>

        <FormSection title={t("form.sections.payment")} hint={t("form.sectionHints.payment")}>
          <Field label={t("form.modePaiement")} code="modePaiement" required>
            <RadioCardGroup value={paymentChoice} onChange={choosePayment} options={paymentOptions} cols={3} />
          </Field>
          {paymentChoice === "bank" && (
            <Field id="compteBancaire" label={t("form.compteBancaire")} code="compteBancaire" required hint={t("form.hints.compteBancaire")} error={errors.compteBancaire?.message}>
              <Input id="compteBancaire" {...form.register("compteBancaire")} />
            </Field>
          )}
          {paymentChoice === "mobile" && (
            <FieldGrid>
              <Field id="numMobileMoney" label={t("form.numMobileMoney")} code="numMobileMoney" required hint={t("form.hints.numMobileMoney")} error={errors.numMobileMoney?.message}>
                <Input id="numMobileMoney" {...form.register("numMobileMoney")} />
              </Field>
              <Field label={t("form.operateurMm")} code="operateurMm">
                <RadioCardGroup
                  value={operateurMm === "ORANGE" ? "ORANGE" : "MTN"}
                  onChange={chooseOperator}
                  options={[
                    { value: "MTN", label: "MTN MoMo", code: "enum: MTN" },
                    { value: "ORANGE", label: "Orange Money", code: "enum: ORANGE" },
                  ]}
                  cols={2}
                />
              </Field>
            </FieldGrid>
          )}
        </FormSection>

        <FormFooter>
          <Button type="button" variant="ghost" onClick={() => router.push("/employees" as never)}>
            {tCommon("actions.cancel" as never)}
          </Button>
          <Button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? <Loader2 className="size-4 animate-spin" /> : <Send className="size-4" />}
            {mutation.isPending ? t("form.submitting") : t("form.submit")}
          </Button>
        </FormFooter>
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
