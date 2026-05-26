"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Loader2, Send, X as CloseIcon } from "lucide-react";
import { toast } from "sonner";
import {
  useJobOffers,
  useCreateJobOffer,
  useJobOfferTransition,
  useApplications,
  useCreateApplication,
  useApplicationTransition,
} from "@/hooks/modules/useRecruitment";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
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
import { useQuery } from "@tanstack/react-query";
import { bffFetch } from "@/lib/api-client";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { StatCard } from "@/components/ui-tokens/StatCard";
import {
  createJobOfferSchema,
  createApplicationSchema,
  type CreateJobOfferFormValues,
  type CreateApplicationFormValues,
} from "@/lib/validation/hrm/recruitment.schema";
import type { ApplicationStatus, Application } from "@/lib/types/hrm/recruitment";

const COLUMNS: ApplicationStatus[] = [
  "NEW",
  "SHORTLISTED",
  "INTERVIEWING",
  "OFFERED",
  "HIRED",
  "REJECTED",
];

export function RecruitmentClient() {
  const t = useTranslations("recruitment");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const offers = useJobOffers();
  const createOffer = useCreateJobOffer();
  const offerTx = useJobOfferTransition();
  const [openOffer, setOpenOffer] = React.useState(false);
  const [selectedOffer, setSelectedOffer] = React.useState<string>("");

  const offerForm = useForm<CreateJobOfferFormValues>({
    resolver: zodResolver(createJobOfferSchema),
    defaultValues: {
      poste: "",
      departement: "",
      localisation: "",
      competencesRequises: "",
      dateLimite: null,
      packageSalarial: "",
    },
  });

  const effectiveOffer = selectedOffer || (offers.data?.[0]?.id ?? "");

  const allApps = useQuery({
    queryKey: ["hrm", "applications", "all"],
    enabled: !!offers.data,
    queryFn: async () => {
      const list = offers.data ?? [];
      const res = await Promise.all(
        list.map((o) =>
          bffFetch<Application[]>(`/api/hrm/job-offers/${o.id}/applications`).catch(() => [] as Application[]),
        ),
      );
      return res.flat();
    },
  });

  const apps = allApps.data ?? [];
  const openOffers = (offers.data ?? []).filter((o) => o.status === "PUBLISHED").length;
  const inProgress = apps.filter((a) => a.status !== "HIRED" && a.status !== "REJECTED").length;
  const interviewing = apps.filter((a) => a.status === "INTERVIEWING").length;
  const newApps = apps.filter((a) => a.status === "NEW").length;

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        crumbs={[{ label: tNav("items.recruitment") }]}
        title={t("title")}
        subtitle={t("subtitle")}
        actions={
          <Button onClick={() => setOpenOffer(true)}>
            <Plus className="size-4" />
            {t("offers.newButton")}
          </Button>
        }
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {offers.isLoading ? (
          Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-[104px] rounded-[18px]" />)
        ) : (
          <>
            <StatCard tone="orange" label={t("stats.openOffers")} value={openOffers} footer={t("stats.openOffersFooter", { total: offers.data?.length ?? 0 })} />
            <StatCard tone="blue" label={t("stats.inProgress")} value={inProgress} footer={t("stats.inProgressFooter", { count: newApps })} />
            <StatCard tone="amber" label={t("stats.interviews")} value={interviewing} footer={t("stats.interviewsFooter")} />
            <StatCard tone="green" label={t("stats.totalApplications")} value={apps.length} footer={t("stats.totalApplicationsFooter")} />
          </>
        )}
      </div>

      <Tabs defaultValue="offers">
        <TabsList>
          <TabsTrigger value="offers">{t("offers.tab")}</TabsTrigger>
          <TabsTrigger value="applications">{t("applications.tab")}</TabsTrigger>
        </TabsList>

        <TabsContent value="offers">
          <div className="mb-3 flex justify-end">
            <Button onClick={() => setOpenOffer(true)}>
              <Plus className="size-4" />
              {t("offers.newButton")}
            </Button>
          </div>

          {offers.isLoading && <Skeleton className="h-32 w-full" />}

          {!offers.isLoading && offers.data && offers.data.length === 0 && (
            <Card>
              <CardContent className="py-8 text-center text-sm text-ink-3">{t("offers.empty")}</CardContent>
            </Card>
          )}

          {!offers.isLoading && offers.data && offers.data.length > 0 && (
            <Card>
              <CardContent className="p-0">
                <table className="w-full border-separate border-spacing-0">
                  <thead>
                    <tr>
                      {[
                        t("offers.table.poste"),
                        t("offers.table.departement"),
                        t("offers.table.localisation"),
                        t("offers.table.dateLimite"),
                        t("offers.table.status"),
                        "",
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
                    {offers.data.map((o) => (
                      <tr key={o.id}>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">{o.poste}</td>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">{o.departement ?? "—"}</td>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2">{o.localisation ?? "—"}</td>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">{o.dateLimite ? fmt.date(o.dateLimite) : "—"}</td>
                        <td className="border-b border-line-soft px-4 py-3 text-[13.5px]"><StatusBadge kind="jobOffer" status={o.status} /></td>
                        <td className="border-b border-line-soft px-4 py-3 text-right">
                          <div className="flex justify-end gap-1.5">
                            {o.status === "DRAFT" && (
                              <Button
                                size="sm"
                                onClick={() =>
                                  offerTx.mutate(
                                    { id: o.id, action: "publish" },
                                    {
                                      onSuccess: () => toast.success(t("offers.actions.publish")),
                                      onError: (err) => toast.error((err as Error).message),
                                    },
                                  )
                                }
                              >
                                <Send className="size-4" />
                                {t("offers.actions.publish")}
                              </Button>
                            )}
                            {o.status === "PUBLISHED" && (
                              <Button
                                size="sm"
                                variant="secondary"
                                onClick={() =>
                                  offerTx.mutate(
                                    { id: o.id, action: "close" },
                                    {
                                      onSuccess: () => toast.success(t("offers.actions.close")),
                                      onError: (err) => toast.error((err as Error).message),
                                    },
                                  )
                                }
                              >
                                <CloseIcon className="size-4" />
                                {t("offers.actions.close")}
                              </Button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="applications">
          <Card>
            <CardContent className="flex items-center gap-3">
              <Label className="shrink-0">{t("applications.selectOffer")}</Label>
              <Select value={effectiveOffer} onValueChange={setSelectedOffer}>
                <SelectTrigger className="max-w-lg">
                  <SelectValue placeholder="—" />
                </SelectTrigger>
                <SelectContent>
                  {offers.data?.map((o) => (
                    <SelectItem key={o.id} value={o.id}>
                      {o.poste} {o.status === "PUBLISHED" ? "(publiée)" : `(${o.status})`}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </CardContent>
          </Card>

          {effectiveOffer && <ApplicationsKanban jobOfferId={effectiveOffer} />}
        </TabsContent>
      </Tabs>

      <Dialog open={openOffer} onOpenChange={setOpenOffer}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("offers.newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={offerForm.handleSubmit((values) =>
              createOffer.mutate(
                {
                  ...values,
                  departement: values.departement ?? null,
                  localisation: values.localisation ?? null,
                  competencesRequises: values.competencesRequises ?? null,
                  dateLimite: values.dateLimite ?? null,
                  packageSalarial: values.packageSalarial ?? null,
                } as never,
                {
                  onSuccess: () => {
                    toast.success(t("offers.form.submit"));
                    setOpenOffer(false);
                    offerForm.reset();
                  },
                  onError: (err) => toast.error((err as Error).message),
                },
              ),
            )}
            className="space-y-4"
          >
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5 col-span-2">
                <Label htmlFor="poste">{t("offers.form.poste")}</Label>
                <Input id="poste" {...offerForm.register("poste")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="departement">{t("offers.form.departement")}</Label>
                <Input id="departement" {...offerForm.register("departement")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="localisation">{t("offers.form.localisation")}</Label>
                <Input id="localisation" {...offerForm.register("localisation")} />
              </div>
              <div className="space-y-1.5 col-span-2">
                <Label htmlFor="competencesRequises">{t("offers.form.competencesRequises")}</Label>
                <Input id="competencesRequises" {...offerForm.register("competencesRequises")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="dateLimite">{t("offers.form.dateLimite")}</Label>
                <Input id="dateLimite" type="date" min={new Date().toISOString().slice(0, 10)} {...offerForm.register("dateLimite")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="packageSalarial">{t("offers.form.packageSalarial")}</Label>
                <Input id="packageSalarial" {...offerForm.register("packageSalarial")} />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpenOffer(false)}>
                Annuler
              </Button>
              <Button type="submit" disabled={createOffer.isPending}>
                {createOffer.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("offers.form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function ApplicationsKanban({ jobOfferId }: { jobOfferId: string }) {
  const t = useTranslations("recruitment");
  const apps = useApplications(jobOfferId);
  const tx = useApplicationTransition(jobOfferId);
  const createApp = useCreateApplication(jobOfferId);
  const [openApp, setOpenApp] = React.useState(false);

  const form = useForm<CreateApplicationFormValues>({
    resolver: zodResolver(createApplicationSchema),
    defaultValues: {
      jobOfferId,
      candidatNom: "",
      candidatPrenom: "",
      candidatEmail: "",
      candidatTelephone: "",
    },
  });

  React.useEffect(() => {
    form.setValue("jobOfferId", jobOfferId);
  }, [jobOfferId, form]);

  if (apps.isLoading) return <Skeleton className="h-48 w-full" />;

  const grouped = new Map<ApplicationStatus, typeof apps.data>();
  for (const c of COLUMNS) grouped.set(c, []);
  for (const a of apps.data ?? []) grouped.get(a.status)?.push(a);

  return (
    <div className="space-y-3">
      <div className="flex justify-end">
        <Button onClick={() => setOpenApp(true)}>
          <Plus className="size-4" />
          {t("applications.newButton")}
        </Button>
      </div>

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-6">
        {COLUMNS.map((col) => {
          const items = grouped.get(col) ?? [];
          return (
            <div key={col} className="flex flex-col gap-2">
              <div className="flex items-center justify-between rounded-lg bg-cream-soft px-2 py-1.5">
                <span className="text-[11px] font-semibold uppercase tracking-wide text-ink-3">
                  {t(`applications.columns.${col}` as never)}
                </span>
                <span className="text-[11px] font-semibold text-ink-3 tabular">{items.length}</span>
              </div>
              <div className="flex flex-col gap-2">
                {items.map((a) => (
                  <Card key={a!.id}>
                    <CardContent className="space-y-2 p-3">
                      <div className="text-[13px] font-medium text-ink">
                        {a!.candidatPrenom} {a!.candidatNom}
                      </div>
                      <div className="truncate text-[11.5px] text-ink-3">{a!.candidatEmail}</div>
                      <ActionButtons
                        status={a!.status}
                        onAction={(action) =>
                          tx.mutate(
                            { id: a!.id, action },
                            {
                              onSuccess: () =>
                                toast.success(t(`applications.actions.${action}` as never)),
                              onError: (err) => toast.error((err as Error).message),
                            },
                          )
                        }
                      />
                    </CardContent>
                  </Card>
                ))}
                {items.length === 0 && (
                  <div className="rounded-lg border border-dashed border-line py-3 text-center text-[11px] text-ink-4">—</div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <Dialog open={openApp} onOpenChange={setOpenApp}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("applications.newButton")}</DialogTitle>
          </DialogHeader>
          <form
            onSubmit={form.handleSubmit((values) =>
              createApp.mutate(
                {
                  jobOfferId: values.jobOfferId,
                  candidatNom: values.candidatNom,
                  candidatPrenom: values.candidatPrenom,
                  candidatEmail: values.candidatEmail,
                  candidatTelephone: values.candidatTelephone ?? null,
                } as never,
                {
                  onSuccess: () => {
                    toast.success(t("applications.form.submit"));
                    setOpenApp(false);
                    form.reset({
                      jobOfferId,
                      candidatNom: "",
                      candidatPrenom: "",
                      candidatEmail: "",
                      candidatTelephone: "",
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
                <Label htmlFor="candidatPrenom">{t("applications.form.candidatPrenom")}</Label>
                <Input id="candidatPrenom" {...form.register("candidatPrenom")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="candidatNom">{t("applications.form.candidatNom")}</Label>
                <Input id="candidatNom" {...form.register("candidatNom")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="candidatEmail">{t("applications.form.candidatEmail")}</Label>
                <Input id="candidatEmail" type="email" {...form.register("candidatEmail")} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="candidatTelephone">{t("applications.form.candidatTelephone")}</Label>
                <Input id="candidatTelephone" {...form.register("candidatTelephone")} />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => setOpenApp(false)}>
                Annuler
              </Button>
              <Button type="submit" disabled={createApp.isPending}>
                {createApp.isPending && <Loader2 className="size-4 animate-spin" />}
                {t("applications.form.submit")}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function ActionButtons({
  status,
  onAction,
}: {
  status: ApplicationStatus;
  onAction: (action: "shortlist" | "interview" | "offer" | "hire" | "reject") => void;
}) {
  const t = useTranslations("recruitment.applications.actions");
  if (status === "HIRED" || status === "REJECTED") return null;
  const next: Record<ApplicationStatus, "shortlist" | "interview" | "offer" | "hire" | null> = {
    NEW: "shortlist",
    SHORTLISTED: "interview",
    INTERVIEWING: "offer",
    OFFERED: "hire",
    HIRED: null,
    REJECTED: null,
  };
  const action = next[status];
  return (
    <div className="flex gap-1">
      {action && (
        <Button size="sm" className="grow" onClick={() => onAction(action)}>
          {t(action)}
        </Button>
      )}
      <Button
        size="sm"
        variant="secondary"
        className="grow"
        onClick={() => onAction("reject")}
      >
        {t("reject")}
      </Button>
    </div>
  );
}
