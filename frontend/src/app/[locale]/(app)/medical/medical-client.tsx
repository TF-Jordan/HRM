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
import { useQuery } from "@tanstack/react-query";
import { bffFetch } from "@/lib/api-client";
import { PageHeader } from "@/components/shell/PageHeader";
import { StatCard } from "@/components/ui-tokens/StatCard";
import { Avatar } from "@/components/ui/avatar";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import type { AptitudeResult, MedicalVisit } from "@/lib/types/hrm/medical";

const APTITUDES: AptitudeResult[] = ["APTE", "APTE_AVEC_RESTRICTIONS", "INAPTE_TEMPORAIRE"];
const APTITUDE_TONE: Record<AptitudeResult, "green" | "amber" | "red"> = {
  APTE: "green",
  APTE_AVEC_RESTRICTIONS: "amber",
  INAPTE_TEMPORAIRE: "red",
};

export type MedicalClientProps = {
  mode: "manager" | "self";
  selfEmployeeId: string | null;
};

export function MedicalClient({ mode, selfEmployeeId }: MedicalClientProps) {
  const t = useTranslations("medical");
  const tNav = useTranslations("navigation");

  if (mode === "self") {
    return (
      <div className="space-y-6 animate-fade-up">
        <PageHeader
          ucBadge="UC-23"
          crumbs={[{ label: tNav("items.medical") }]}
          title={t("title")}
          subtitle={t("subtitle")}
        />
        <SelfMedicalView employeeId={selfEmployeeId} />
      </div>
    );
  }

  return <ManagerMedicalView />;
}

function SelfMedicalView({ employeeId }: { employeeId: string | null }) {
  const t = useTranslations("medical");
  if (!employeeId) {
    return (
      <Card>
        <CardContent className="py-8 text-center text-sm text-ink-3">
          {t("noEmployee")}
        </CardContent>
      </Card>
    );
  }
  return (
    <Tabs defaultValue="visits">
      <TabsList>
        <TabsTrigger value="visits">{t("tabs.visits")}</TabsTrigger>
        <TabsTrigger value="certificates">{t("tabs.certificates")}</TabsTrigger>
      </TabsList>
      <TabsContent value="visits">
        <VisitsPanel employeeId={employeeId} readOnly />
      </TabsContent>
      <TabsContent value="certificates">
        <CertificatesPanel employeeId={employeeId} readOnly />
      </TabsContent>
    </Tabs>
  );
}

const APT_HEX: Record<AptitudeResult, string> = {
  APTE: "#10b981",
  APTE_AVEC_RESTRICTIONS: "#f59e0b",
  INAPTE_TEMPORAIRE: "#ef4444",
};

type MedEmp = { id: string; actorDisplayName: string; matricule: string };

function MedicalOverview({
  employees,
  t,
  fmt,
}: {
  employees: MedEmp[];
  t: (k: string) => string;
  fmt: ReturnType<typeof useFormat>;
}) {
  const [nowTs] = React.useState(() => Date.now());
  const visits = useQuery({
    queryKey: ["hrm", "medical", "visits", "all"],
    enabled: employees.length > 0,
    queryFn: async () => {
      const res = await Promise.all(
        employees.map((e) =>
          bffFetch<MedicalVisit[]>(`/api/hrm/medical/employees/${e.id}/visits`)
            .then((v) => v.map((x) => ({ ...x, _emp: e })))
            .catch(() => [] as (MedicalVisit & { _emp: MedEmp })[]),
        ),
      );
      return res.flat();
    },
  });

  const rows = visits.data ?? [];
  const planning90 = rows.filter((v) => {
    if (!v.prochaineEcheance) return false;
    const d = (new Date(v.prochaineEcheance).getTime() - nowTs) / 86_400_000;
    return d >= 0 && d <= 90;
  }).length;
  const byApt: Record<string, number> = {};
  for (const v of rows) byApt[v.resultatAptitude] = (byApt[v.resultatAptitude] ?? 0) + 1;
  const restrictions = (byApt.APTE_AVEC_RESTRICTIONS ?? 0) + (byApt.INAPTE_TEMPORAIRE ?? 0);
  const total = rows.length || 1;
  const aptePct = Math.round(((byApt.APTE ?? 0) / total) * 100);

  let acc = 0;
  const parts: string[] = [];
  for (const a of ["APTE", "APTE_AVEC_RESTRICTIONS", "INAPTE_TEMPORAIRE"] as AptitudeResult[]) {
    const n = byApt[a] ?? 0;
    if (!n) continue;
    const start = (acc / total) * 100;
    acc += n;
    parts.push(`${APT_HEX[a]} ${start}% ${(acc / total) * 100}%`);
  }
  const donut = parts.length ? `conic-gradient(${parts.join(", ")})` : "conic-gradient(var(--color-cream-2) 0 100%)";

  const recent = [...rows]
    .sort((a, b) => new Date(b.dateVisite).getTime() - new Date(a.dateVisite).getTime())
    .slice(0, 8);

  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <div className="space-y-4 lg:col-span-2">
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          {visits.isLoading ? (
            Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-[104px] rounded-[18px]" />)
          ) : (
            <>
              <StatCard tone="orange" label="À planifier (90j)" value={planning90} footer="échéances proches" />
              <StatCard tone="blue" label="Visites" value={rows.length} footer="enregistrées" />
              <StatCard tone="green" label="Aptes" value={byApt.APTE ?? 0} footer={`${aptePct}%`} />
              <StatCard tone="red" label="Restrictions" value={restrictions} footer="à suivre" />
            </>
          )}
        </div>

        {!visits.isLoading && recent.length > 0 && (
          <Card className="overflow-hidden p-0">
            <div className="border-b border-line-soft p-3">
              <h3 className="px-1 font-display text-[15px] font-bold text-ink">Visites médicales récentes</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full border-collapse text-[13px]">
                <thead>
                  <tr className="border-b border-line-soft text-[10.5px] uppercase tracking-[0.12em] text-ink-4">
                    <th className="px-4 py-2.5 text-left font-semibold">Employé</th>
                    <th className="px-3 py-2.5 text-left font-semibold">Date</th>
                    <th className="px-3 py-2.5 text-left font-semibold">Prochaine</th>
                    <th className="px-3 py-2.5 text-left font-semibold">Médecin</th>
                    <th className="px-3 py-2.5 text-left font-semibold">Résultat</th>
                  </tr>
                </thead>
                <tbody>
                  {recent.map((v) => (
                    <tr key={v.id} className="border-b border-line-soft/70 last:border-0">
                      <td className="px-4 py-2.5">
                        <div className="flex items-center gap-2.5">
                          <Avatar name={v._emp.actorDisplayName} size="sm" />
                          <span className="font-semibold text-ink">{v._emp.actorDisplayName}</span>
                        </div>
                      </td>
                      <td className="px-3 py-2.5 text-ink-2 tabular">{fmt.date(v.dateVisite)}</td>
                      <td className="px-3 py-2.5 text-ink-2 tabular">{v.prochaineEcheance ? fmt.date(v.prochaineEcheance) : "—"}</td>
                      <td className="px-3 py-2.5 text-ink-2">{v.medecin}</td>
                      <td className="px-3 py-2.5">
                        <span
                          className="inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11px] font-semibold"
                          style={{ background: `${APT_HEX[v.resultatAptitude]}1a`, color: APT_HEX[v.resultatAptitude] }}
                        >
                          {t(`visits.aptitudes.${v.resultatAptitude}`)}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Statut d&apos;aptitude</CardTitle>
        </CardHeader>
        <CardContent className="flex items-center gap-5">
          <div className="relative grid size-28 shrink-0 place-items-center rounded-full" style={{ background: donut }}>
            <div className="grid size-20 place-items-center rounded-full bg-white text-center">
              <div>
                <div className="font-display text-[20px] font-extrabold leading-none text-ink tabular">{aptePct}%</div>
                <div className="text-[9px] text-ink-4">aptes</div>
              </div>
            </div>
          </div>
          <ul className="flex-1 space-y-1.5 text-[12.5px]">
            {(["APTE", "APTE_AVEC_RESTRICTIONS", "INAPTE_TEMPORAIRE"] as AptitudeResult[]).map((a) => (
              <li key={a} className="flex items-center justify-between">
                <span className="flex items-center gap-2 text-ink-2">
                  <span className="size-2.5 rounded-full" style={{ background: APT_HEX[a] }} />
                  {t(`visits.aptitudes.${a}`)}
                </span>
                <span className="font-semibold text-ink tabular">{byApt[a] ?? 0}</span>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}

function ManagerMedicalView() {
  const t = useTranslations("medical");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const employees = useEmployees();
  const [employeeId, setEmployeeId] = React.useState<string>("");

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        crumbs={[{ label: tNav("items.medical") }]}
        title={t("title")}
        subtitle={t("subtitle")}
      />

      <MedicalOverview employees={employees.data ?? []} t={t} fmt={fmt} />

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

function VisitsPanel({ employeeId, readOnly = false }: { employeeId: string; readOnly?: boolean }) {
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
      {!readOnly && (
        <div className="flex justify-end">
          <Button onClick={() => setOpen(true)}>
            <Plus className="size-4" />
            {t("visits.newButton")}
          </Button>
        </div>
      )}

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
                  min={new Date().toISOString().slice(0, 10)}
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

function CertificatesPanel({
  employeeId,
  readOnly = false,
}: {
  employeeId: string;
  readOnly?: boolean;
}) {
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
      {!readOnly && (
        <div className="flex justify-end">
          <Button onClick={() => setOpen(true)}>
            <Plus className="size-4" />
            {t("certificates.newButton")}
          </Button>
        </div>
      )}

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
                <Input id="dateExpiration" type="date" min={new Date().toISOString().slice(0, 10)} {...form.register("dateExpiration")} />
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
