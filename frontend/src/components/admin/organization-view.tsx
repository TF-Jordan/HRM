"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Loader2 } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { PageHeader } from "@/components/shell/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, Input, Textarea } from "@/components/ui/input";
import { SectionTitle } from "@/components/ui/section-title";
import { apiFetch, BffApiError } from "@/lib/api-client";

type Organization = {
  id: string;
  code: string;
  shortName: string;
  longName: string;
  service: string;
  organizationType: string;
  email?: string | null;
  websiteUrl?: string | null;
  description?: string | null;
  legalForm?: string | null;
  businessRegistrationNumber?: string | null;
  taxNumber?: string | null;
  capitalShare?: number | string | null;
  ceoName?: string | null;
  yearFounded?: number | null;
  numberOfEmployees?: number | null;
  isActive: boolean;
  status?: string;
};

type FormValues = {
  shortName: string;
  longName: string;
  service: string;
  email: string;
  websiteUrl: string;
  description: string;
  legalForm: string;
  businessRegistrationNumber: string;
  taxNumber: string;
  capitalShare: string;
  ceoName: string;
  yearFounded: string;
  numberOfEmployees: string;
};

export function OrganizationView() {
  const t = useTranslations("admin");
  const tOrg = useTranslations("admin.organization");
  const tCommon = useTranslations("common");
  const tErrors = useTranslations("errors");
  const queryClient = useQueryClient();

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "organization"],
    queryFn: () => apiFetch<Organization>("/api/admin/organization"),
  });

  const { register, handleSubmit, reset, formState: { isSubmitting, isDirty } } = useForm<FormValues>({
    defaultValues: emptyValues(),
  });

  React.useEffect(() => {
    if (data) reset(toFormValues(data));
  }, [data, reset]);

  const mutation = useMutation({
    mutationFn: (values: FormValues) =>
      apiFetch<Organization>("/api/admin/organization", {
        method: "PATCH",
        body: toPayload(values, data),
      }),
    onSuccess: (saved) => {
      toast.success(tOrg("saved"));
      queryClient.setQueryData(["admin", "organization"], saved);
      reset(toFormValues(saved));
    },
    onError: (cause) => {
      if (cause instanceof BffApiError) {
        toast.error(cause.message);
      } else {
        toast.error(tErrors("unknown"));
      }
    },
  });

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

  return (
    <form onSubmit={handleSubmit((v) => mutation.mutate(v))}>
      <PageHeader
        ucBadge={t("ucBadge")}
        breadcrumb={[{ label: "HR Core" }, { label: "Administration" }, { label: tOrg("title") }]}
        title={data.longName ?? data.shortName ?? data.code}
        subtitle={tOrg("subtitle")}
        actions={
          <Button type="submit" disabled={!isDirty || isSubmitting}>
            {isSubmitting ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" /> {tCommon("actions.loading")}
              </>
            ) : (
              tCommon("actions.save")
            )}
          </Button>
        }
      />

      <SectionTitle>{tOrg("tabs.general")}</SectionTitle>
      <Card className="mb-6">
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field label={tOrg("fields.shortName")}>
              <Input {...register("shortName")} />
            </Field>
            <Field label={tOrg("fields.longName")}>
              <Input {...register("longName")} />
            </Field>
            <Field label={tOrg("fields.service")}>
              <Input {...register("service")} />
            </Field>
            <Field label={tOrg("fields.legalForm")}>
              <Input {...register("legalForm")} placeholder="SA, SARL, SAS…" />
            </Field>
            <Field label={tOrg("fields.email")}>
              <Input type="email" {...register("email")} />
            </Field>
            <Field label={tOrg("fields.websiteUrl")}>
              <Input type="url" {...register("websiteUrl")} placeholder="https://…" />
            </Field>
          </div>
          <div className="mt-4">
            <Field label={tOrg("fields.description")}>
              <Textarea {...register("description")} />
            </Field>
          </div>
        </CardContent>
      </Card>

      <SectionTitle>{tOrg("tabs.legal")}</SectionTitle>
      <Card className="mb-6">
        <CardContent padding="lg">
          <div className="grid grid-cols-2 gap-4">
            <Field label={tOrg("fields.businessRegistrationNumber")}>
              <Input {...register("businessRegistrationNumber")} placeholder="RC/YDE/2024/B/123" />
            </Field>
            <Field label={tOrg("fields.taxNumber")}>
              <Input {...register("taxNumber")} placeholder="P123456789" />
            </Field>
            <Field label={tOrg("fields.capitalShare")}>
              <Input type="number" {...register("capitalShare")} placeholder="100 000 000" />
            </Field>
            <Field label={tOrg("fields.ceoName")}>
              <Input {...register("ceoName")} />
            </Field>
            <Field label={tOrg("fields.yearFounded")}>
              <Input type="number" {...register("yearFounded")} />
            </Field>
            <Field label={tOrg("fields.numberOfEmployees")}>
              <Input type="number" {...register("numberOfEmployees")} />
            </Field>
          </div>
        </CardContent>
      </Card>
    </form>
  );
}

function emptyValues(): FormValues {
  return {
    shortName: "",
    longName: "",
    service: "",
    email: "",
    websiteUrl: "",
    description: "",
    legalForm: "",
    businessRegistrationNumber: "",
    taxNumber: "",
    capitalShare: "",
    ceoName: "",
    yearFounded: "",
    numberOfEmployees: "",
  };
}

function toFormValues(o: Organization): FormValues {
  return {
    shortName: o.shortName ?? "",
    longName: o.longName ?? "",
    service: o.service ?? o.organizationType ?? "",
    email: o.email ?? "",
    websiteUrl: o.websiteUrl ?? "",
    description: o.description ?? "",
    legalForm: o.legalForm ?? "",
    businessRegistrationNumber: o.businessRegistrationNumber ?? "",
    taxNumber: o.taxNumber ?? "",
    capitalShare: o.capitalShare == null ? "" : String(o.capitalShare),
    ceoName: o.ceoName ?? "",
    yearFounded: o.yearFounded == null ? "" : String(o.yearFounded),
    numberOfEmployees: o.numberOfEmployees == null ? "" : String(o.numberOfEmployees),
  };
}

function toPayload(v: FormValues, original?: Organization | null) {
  return {
    code: original?.code ?? "",
    service: v.service,
    isIndividualBusiness: false,
    email: v.email || undefined,
    shortName: v.shortName,
    longName: v.longName,
    description: v.description || undefined,
    websiteUrl: v.websiteUrl || undefined,
    legalForm: v.legalForm || undefined,
    businessRegistrationNumber: v.businessRegistrationNumber || undefined,
    taxNumber: v.taxNumber || undefined,
    capitalShare: v.capitalShare ? Number(v.capitalShare) : undefined,
    ceoName: v.ceoName || undefined,
    yearFounded: v.yearFounded ? Number(v.yearFounded) : undefined,
    numberOfEmployees: v.numberOfEmployees ? Number(v.numberOfEmployees) : undefined,
    isActive: original?.isActive ?? true,
    status: original?.status,
  };
}
