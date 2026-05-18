"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { useEmployees } from "@/hooks/modules/useEmployees";
import {
  useEmployeeVisits,
  useCreateMedicalVisit,
  useEmployeeCertificates,
  useCreateMedicalCertificate,
} from "@/hooks/modules/useMedical";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  createMedicalVisitSchema,
  createMedicalCertificateSchema,
  type CreateMedicalVisitFormValues,
  type CreateMedicalCertificateFormValues,
} from "@/lib/validation/hrm/medical.schema";
import type { AptitudeResult } from "@/lib/types/hrm/medical";

const APTITUDES: AptitudeResult[] = ["APTE", "APTE_AVEC_RESTRICTIONS", "INAPTE_TEMPORAIRE"];
const APTITUDE_TONE: Record<AptitudeResult, "green" | "amber" | "red"> = {
  APTE: "green",
  APTE_AVEC_RESTRICTIONS: "amber",
  INAPTE_TEMPORAIRE: "red",
};

export function MedicalClient() {
  const t = useTranslations("medical");
  const tNav = useTranslations("navigation");
  const employees = useEmployees();
  const [employeeId, setEmployeeId] = React.useState<string>("");

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-23"
        crumbs={[{ label: tNav("items.medical") }]}
        title={t("title")}
        subtitle={t("subtitle")}
      />

      <Card>
        <CardContent className="flex items-center gap-3 p-4">
          <Label className="shrink-0">{t("selectEmployee")}</Label>
          <Select value={employeeId} onValueChange={setEmployeeId}>
            <SelectTrigger className="max-w-md">
              <SelectValue placeholder="—" />
            </SelectTrigger>
            <SelectContent>
              {(employees.data ?? []).map((e) => (
                <SelectItem key={e.id} value={e.id}>
                  {e.matricule} — {e.actorDisplayName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </CardContent>
      </Card>

      {employeeId && (
        <Tabs defaultValue="visits">
          <TabsList>
            <TabsTrigger value="visits">{t("tabs.visits")}</TabsTrigger>
            <TabsTrigger value="certificates">{t("tabs.certificates")}</TabsTrigger>
          </TabsList>
          <TabsContent value="visits">
            <VisitsPanel employeeId={employeeId} />
          </TabsContent>
          <TabsContent value="certificates">
            <CertificatesPanel employeeId={employeeId} />
          </TabsContent>
        </Tabs>
      )}
    </div>
  );
}

function VisitsPanel({ employeeId }: { employeeId: string }) {
  const t = useTranslations("medical");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const list = useEmployeeVisits(employeeId);
  const create = useCreateMedicalVisit(employeeId);
  const [open, setOpen] = React.useState(false);

  const form = useForm<CreateMedicalVisitFormValues>({
    resolver: zodResolver(createMedicalVisitSchema),
    defaultValues: {
      employeeId,
      dateVisite: new Date().toISOString().slice(0, 10),
      medecin: "",
      resultatAptitude: "APTE",
      restrictions: "",
      prochaineEcheance: null,
      certificatFileId: "",
    },
  });

  React.useEffect(() => {
    form.setValue("employeeId", employeeId);
  }, [employeeId, form]);

  return (
    <div className="space-y-3">
      <div className="flex justify-end">
        <Button onClick={() => setOpen(true)}>
          <Plus className="size-4" />
          {t("visits.newButton")}
        </Button>
      </div>

      {list.isLoading && <Skeleton className="h-32 w-full" />}
      {!list.isLoading && list.data && list.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">
            {t("visits.empty")}
          </CardContent>
        </Card>
      )}
      {!list.isLoading && list.data && list.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("visits.table.dateVisite"),
                    t("visits.table.medecin"),
                    t("visits.table.resultat"),
                    t("visits.table.restrictions"),
                    t("visits.table.prochaineEcheance"),
                  ].map((h, i) => (
                    <th
                      key={i}
                      className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {list.data.map((v) => (
                  <tr key={v.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink tabular">
                      {fmt.date(v.dateVisite)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">
                      {v.medecin}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
                      <Badge tone={APTITUDE_TONE[v.resultatAptitude]}>
                        {t(`visits.aptitudes.${v.resultatAptitude}` as never)}
                      </Badge>
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">
                      {v.restrictions ?? "—"}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {v.prochaineEcheance ? fmt.date(v.prochaineEcheance) : "—"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("visits.newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={form.handleSubmit((values) =>
              create.mutate(
                {
                  employeeId: values.employeeId,
                  dateVisite: values.dateVisite,
                  medecin: values.medecin,
                  resultatAptitude: values.resultatAptitude,
                  restrictions: values.restrictions ?? null,
                  prochaineEcheance: values.prochaineEcheance ?? null,
                  certificatFileId: values.certificatFileId ?? null,
                },
                {
                  onSuccess: () => {
                    toast.success(t("visits.form.submit"));
                    setOpen(false);
                    form.reset({
                      employeeId,
                      dateVisite: new Date().toISOString().slice(0, 10),
                      medecin: "",
                      resultatAptitude: "APTE",
                      restrictions: "",
                      prochaineEcheance: null,
                      certificatFileId: "",
                    });
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="dateVisite">{t("visits.form.dateVisite")}</Label>
                <Input id="dateVisite" type="date" {...form.register("dateVisite")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="medecin">{t("visits.form.medecin")}</Label>
                <Input id="medecin" {...form.register("medecin")} />
              </div>
              <div className="space-y-1.5 col-span-2">
                <Label>{t("visits.form.resultatAptitude")}</Label>
                <Select
                  value={form.watch("resultatAptitude")}
                  onValueChange={(v) => form.setValue("resultatAptitude", v as AptitudeResult)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="—" />
                  </SelectTrigger>
                  <SelectContent>
                    {APTITUDES.map((a) => (
                      <SelectItem key={a} value={a}>
                        {t(`visits.aptitudes.${a}` as never)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5 col-span-2">
                <Label htmlFor="restrictions">{t("visits.form.restrictions")}</Label>
                <Input id="restrictions" {...form.register("restrictions")} />
              </div>
              <div className="space-y-1.5 col-span-2">
                <Label htmlFor="prochaineEcheance">{t("visits.form.prochaineEcheance")}</Label>
                <Input
                  id="prochaineEcheance"
                  type="date"
                  {...form.register("prochaineEcheance")}
                />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
                {tCommon("actions.cancel")}
              </Button>
              <Button type="submit" disabled={create.isPending}>
                {create.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("visits.form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function CertificatesPanel({ employeeId }: { employeeId: string }) {
  const t = useTranslations("medical");
  const tCommon = useTranslations("common");
  const fmt = useFormat();
  const list = useEmployeeCertificates(employeeId);
  const create = useCreateMedicalCertificate(employeeId);
  const [open, setOpen] = React.useState(false);

  const form = useForm<CreateMedicalCertificateFormValues>({
    resolver: zodResolver(createMedicalCertificateSchema),
    defaultValues: {
      employeeId,
      typeCertificat: "",
      dateEmission: new Date().toISOString().slice(0, 10),
      dateExpiration: null,
      statut: "ACTIF",
      fichierId: "",
    },
  });

  React.useEffect(() => {
    form.setValue("employeeId", employeeId);
  }, [employeeId, form]);

  return (
    <div className="space-y-3">
      <div className="flex justify-end">
        <Button onClick={() => setOpen(true)}>
          <Plus className="size-4" />
          {t("certificates.newButton")}
        </Button>
      </div>

      {list.isLoading && <Skeleton className="h-32 w-full" />}
      {!list.isLoading && list.data && list.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">
            {t("certificates.empty")}
          </CardContent>
        </Card>
      )}
      {!list.isLoading && list.data && list.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {[
                    t("certificates.table.typeCertificat"),
                    t("certificates.table.dateEmission"),
                    t("certificates.table.dateExpiration"),
                    t("certificates.table.statut"),
                  ].map((h, i) => (
                    <th
                      key={i}
                      className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {list.data.map((c) => (
                  <tr key={c.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink">
                      {c.typeCertificat}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.date(c.dateEmission)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {c.dateExpiration ? fmt.date(c.dateExpiration) : "—"}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
                      <Badge tone="blue">{c.statut}</Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("certificates.newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={form.handleSubmit((values) =>
              create.mutate(
                {
                  employeeId: values.employeeId,
                  typeCertificat: values.typeCertificat,
                  dateEmission: values.dateEmission,
                  dateExpiration: values.dateExpiration ?? null,
                  statut: values.statut,
                  fichierId: values.fichierId ?? null,
                },
                {
                  onSuccess: () => {
                    toast.success(t("certificates.form.submit"));
                    setOpen(false);
                    form.reset({
                      employeeId,
                      typeCertificat: "",
                      dateEmission: new Date().toISOString().slice(0, 10),
                      dateExpiration: null,
                      statut: "ACTIF",
                      fichierId: "",
                    });
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="space-y-1.5">
              <Label htmlFor="typeCertificat">{t("certificates.form.typeCertificat")}</Label>
              <Input id="typeCertificat" {...form.register("typeCertificat")} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="dateEmission">{t("certificates.form.dateEmission")}</Label>
                <Input id="dateEmission" type="date" {...form.register("dateEmission")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="dateExpiration">{t("certificates.form.dateExpiration")}</Label>
                <Input id="dateExpiration" type="date" {...form.register("dateExpiration")} />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="statut">{t("certificates.form.statut")}</Label>
              <Input id="statut" {...form.register("statut")} />
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpen(false)}>
                {tCommon("actions.cancel")}
              </Button>
              <Button type="submit" disabled={create.isPending}>
                {create.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("certificates.form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
