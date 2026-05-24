"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  CalendarDays,
  CalendarCheck,
  AlertTriangle,
  DollarSign,
  RefreshCw,
  X,
  Loader2,
  FileText,
  Upload,
  ExternalLink,
} from "lucide-react";
import { toast } from "sonner";
import { z } from "zod";
import {
  useAllContracts,
  useRenewContract,
  useTerminateContract,
  useCreateContractWithDocument,
} from "@/hooks/modules/useContracts";
import { useFormat } from "@/hooks/useFormat";
import { addContractSchema } from "@/lib/validation/hrm/contract.schema";
import { PageHeader } from "@/components/shell/PageHeader";
import { KpiCard } from "@/components/ui-tokens/KpiCard";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
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
  DialogDescription,
} from "@/components/ui/dialog";
import type { ContractType, ContractWithEmployee } from "@/lib/types/hrm/contract";

export function ContractDetailClient({ id }: { id: string }) {
  const t = useTranslations("employees.contractsPage.detail");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const { data: contracts, isLoading } = useAllContracts();
  const contract = contracts?.find((c) => c.id === id);

  const [renewOpen, setRenewOpen] = React.useState(false);
  const [terminateOpen, setTerminateOpen] = React.useState(false);

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-12 w-1/2" />
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="h-28" />
          ))}
        </div>
        <Skeleton className="h-64" />
      </div>
    );
  }

  if (!contract) {
    return (
      <Card>
        <CardContent className="flex items-start gap-3">
          <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
          <div className="text-sm">Contrat introuvable</div>
        </CardContent>
      </Card>
    );
  }

  const c = contract;
  const now = new Date();
  const daysRemaining =
    c.dateFin
      ? Math.max(0, Math.ceil((new Date(c.dateFin).getTime() - now.getTime()) / 86_400_000))
      : null;

  const typeLabel: Record<string, string> = {
    CDI: "Contrat CDI",
    CDD: "Contrat CDD",
    STAGE: "Contrat Stage",
    INTERIM: "Contrat Intérim",
  };

  const subtitle =
    c.type === "CDI"
      ? t("cdiNotice")
      : daysRemaining !== null
        ? t("cddNotice", { days: daysRemaining })
        : "";

  const canRenew = c.status === "ACTIVE" && c.type !== "CDI";
  const canTerminate = c.status === "ACTIVE";

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-04"
        crumbs={[
          { label: tNav("items.contracts"), href: "/contracts" },
          { label: `${c.type} — ${c.employeeName}` },
        ]}
        title={
          <div className="flex flex-wrap items-center gap-3">
            <span>
              {typeLabel[c.type] ?? c.type} — {c.employeeName}
            </span>
            <StatusBadge kind="contract" status={c.status} />
          </div>
        }
        subtitle={subtitle}
        actions={
          <>
            {canRenew && (
              <Button variant="secondary" onClick={() => setRenewOpen(true)}>
                <RefreshCw className="size-4" />
                Renouveler
              </Button>
            )}
            {canTerminate && (
              <Button variant="destructive" onClick={() => setTerminateOpen(true)}>
                <X className="size-4" />
                Résilier
              </Button>
            )}
          </>
        }
      />

      {/* KPI cards */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <KpiCard icon={CalendarDays} label={t("kpi.dateDebut")} value={fmt.date(c.dateDebut)} />
        <KpiCard
          icon={CalendarCheck}
          label={t("kpi.dateFin")}
          value={c.dateFin ? fmt.date(c.dateFin) : "—"}
        />
        <KpiCard
          icon={AlertTriangle}
          label={t("kpi.remaining")}
          value={daysRemaining !== null ? daysRemaining : "∞"}
          tone={daysRemaining !== null && daysRemaining <= 30 ? "amber" : "orange"}
        />
        <KpiCard
          icon={DollarSign}
          label={t("kpi.salary")}
          value={fmt.moneyShort(c.salaireBase)}
          tone="green"
        />
      </div>

      {/* Contract details */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardContent>
            <h3 className="mb-4 border-b border-line-soft pb-3 font-display text-base font-bold text-ink">
              {t("detailsTitle")}
            </h3>
            <dl className="divide-y divide-line-soft">
              <DetailRow
                label={t("fields.type")}
                value={
                  <Badge className="bg-brand-100 text-brand-700 text-[11px]">
                    {c.type}
                  </Badge>
                }
              />
              <DetailRow
                label={t("fields.periodeEssai")}
                value={
                  c.periodeEssai ? `${c.periodeEssai} ${t("days")}` : "—"
                }
              />
              <DetailRow
                label={t("fields.avantages")}
                value={c.avantagesNature ? fmt.money(c.avantagesNature) : "—"}
              />
              <DetailRow
                label={t("fields.status")}
                value={<StatusBadge kind="contract" status={c.status} />}
              />
              {c.motifFin && (
                <DetailRow label={t("fields.motifFin")} value={c.motifFin} />
              )}
              <DetailRow
                label="Document"
                value={
                  c.documentFileId ? (
                    <a
                      href={`/api/hrm/files/${c.documentFileId}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1.5 text-brand-600 hover:text-brand-700 hover:underline font-medium"
                    >
                      <FileText className="size-4" />
                      Voir le contrat PDF
                      <ExternalLink className="size-3" />
                    </a>
                  ) : (
                    <span className="text-ink-3">Aucun document</span>
                  )
                }
              />
            </dl>
          </CardContent>
        </Card>

        {/* Employee info sidebar */}
        <Card>
          <CardContent>
            <h3 className="mb-4 border-b border-line-soft pb-3 font-display text-base font-bold text-ink">
              Employé
            </h3>
            <dl className="divide-y divide-line-soft">
              <DetailRow label="Nom" value={c.employeeName} />
              <DetailRow label="Matricule" value={c.employeeMatricule} />
              {c.employeeDepartment && (
                <DetailRow label="Département" value={c.employeeDepartment} />
              )}
            </dl>
          </CardContent>
        </Card>
      </div>

      {/* Renew dialog — full form */}
      <RenewDialog
        open={renewOpen}
        onOpenChange={setRenewOpen}
        contract={c}
      />

      {/* Terminate dialog */}
      <TerminateDialog
        open={terminateOpen}
        onOpenChange={setTerminateOpen}
        employeeId={c.employeeId}
        contractId={c.id}
      />
    </div>
  );
}

/* ------------------------------------------------------------------ */
/*  Renewal dialog — full creation-like form pre-filled                */
/* ------------------------------------------------------------------ */

const CONTRACT_TYPES: ContractType[] = ["CDI", "CDD", "STAGE", "INTERIM"];

type RenewFormValues = z.input<typeof addContractSchema>;

function RenewDialog({
  open,
  onOpenChange,
  contract,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  contract: ContractWithEmployee;
}) {
  const renew = useRenewContract(contract.employeeId, contract.id);
  const createNew = useCreateContractWithDocument(contract.employeeId);
  const [pdfFile, setPdfFile] = React.useState<File | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  const [submitting, setSubmitting] = React.useState(false);

  const defaultDateFin = React.useMemo(() => {
    if (!contract.dateFin) return null;
    const d = new Date(contract.dateFin);
    d.setFullYear(d.getFullYear() + 1);
    return d.toISOString().slice(0, 10);
  }, [contract.dateFin]);

  const form = useForm<RenewFormValues>({
    resolver: zodResolver(addContractSchema),
    defaultValues: {
      type: contract.type,
      dateDebut: contract.dateFin ?? new Date().toISOString().slice(0, 10),
      dateFin: defaultDateFin,
      salaireBase: contract.salaireBase,
      avantagesNature: contract.avantagesNature,
      periodeEssai: contract.periodeEssai,
    },
  });

  React.useEffect(() => {
    if (open) {
      form.reset({
        type: contract.type,
        dateDebut: contract.dateFin ?? new Date().toISOString().slice(0, 10),
        dateFin: defaultDateFin,
        salaireBase: contract.salaireBase,
        avantagesNature: contract.avantagesNature,
        periodeEssai: contract.periodeEssai,
      });
      setPdfFile(null);
    }
  }, [open, contract, defaultDateFin, form]);

  const selectedType = form.watch("type");

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.type !== "application/pdf") {
      toast.error("Seuls les fichiers PDF sont acceptés");
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      toast.error("Le fichier ne doit pas dépasser 10 Mo");
      return;
    }
    setPdfFile(file);
  };

  const handleSubmit = async (values: RenewFormValues) => {
    setSubmitting(true);
    try {
      // 1. Mark old contract as RENEWED
      const newEndDate = values.dateFin ?? contract.dateFin ?? new Date().toISOString().slice(0, 10);
      await renew.mutateAsync(newEndDate);

      // 2. Create new contract with modified values
      await createNew.mutateAsync({
        contract: {
          type: values.type,
          dateDebut: values.dateDebut,
          dateFin: values.dateFin ?? null,
          salaireBase: Number(values.salaireBase),
          avantagesNature: values.avantagesNature ? Number(values.avantagesNature) : null,
          periodeEssai: values.periodeEssai ? Number(values.periodeEssai) : null,
        },
        file: pdfFile ?? undefined,
      });

      toast.success("Contrat renouvelé — nouveau contrat créé");
      onOpenChange(false);
    } catch (err) {
      toast.error((err as Error).message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Renouveler le contrat</DialogTitle>
          <DialogDescription>
            L&apos;ancien contrat sera marqué comme renouvelé et un nouveau contrat actif sera créé
            avec les valeurs ci-dessous. Modifiez les champs si nécessaire.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-5">
          {/* Contract type */}
          <div className="space-y-2">
            <Label>Type de contrat</Label>
            <Controller
              control={form.control}
              name="type"
              render={({ field }) => (
                <div className="flex gap-3">
                  {CONTRACT_TYPES.map((tp) => (
                    <button
                      key={tp}
                      type="button"
                      onClick={() => field.onChange(tp)}
                      className={`rounded-lg border-2 px-5 py-2 text-sm font-semibold transition-colors ${
                        field.value === tp
                          ? "border-brand-500 bg-brand-50 text-brand-700"
                          : "border-line bg-white text-ink-2 hover:border-brand-300"
                      }`}
                    >
                      {tp}
                    </button>
                  ))}
                </div>
              )}
            />
          </div>

          {/* Dates */}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <RenewField id="r-dateDebut" label="Date de début" required error={form.formState.errors.dateDebut?.message}>
              <Input id="r-dateDebut" type="date" {...form.register("dateDebut")} />
            </RenewField>
            <RenewField id="r-dateFin" label="Date de fin" error={form.formState.errors.dateFin?.message}>
              <Input
                id="r-dateFin"
                type="date"
                disabled={selectedType === "CDI"}
                {...form.register("dateFin")}
              />
              {selectedType === "CDI" && (
                <p className="text-[11px] text-ink-3 mt-0.5">CDI — pas de date de fin</p>
              )}
            </RenewField>
            <RenewField id="r-periodeEssai" label="Période d'essai (jours)">
              <Input id="r-periodeEssai" type="number" min={0} {...form.register("periodeEssai")} />
            </RenewField>
          </div>

          {/* Remuneration */}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <RenewField id="r-salaireBase" label="Salaire de base (XAF)" required error={form.formState.errors.salaireBase?.message}>
              <Input id="r-salaireBase" type="number" min={0} step="1" {...form.register("salaireBase")} />
            </RenewField>
            <RenewField id="r-avantagesNature" label="Avantages en nature (XAF)">
              <Input id="r-avantagesNature" type="number" min={0} step="1" {...form.register("avantagesNature")} />
            </RenewField>
          </div>

          {/* Document upload */}
          <div className="space-y-2">
            <Label className="flex items-center gap-1">Document de contrat <span className="text-brand-500">*</span></Label>
            <input
              ref={fileInputRef}
              type="file"
              accept="application/pdf"
              className="hidden"
              onChange={handleFileChange}
            />
            {pdfFile ? (
              <div className="flex items-center gap-3 rounded-lg border border-line bg-cream-dim/50 px-4 py-3">
                <FileText className="size-5 text-brand-500 shrink-0" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-ink truncate">{pdfFile.name}</p>
                  <p className="text-[12px] text-ink-3">
                    {(pdfFile.size / 1024 / 1024).toFixed(2)} Mo
                  </p>
                </div>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  onClick={() => {
                    setPdfFile(null);
                    if (fileInputRef.current) fileInputRef.current.value = "";
                  }}
                >
                  <X className="size-4" />
                </Button>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="flex w-full flex-col items-center justify-center rounded-lg border-2 border-dashed border-line bg-cream-dim/50 py-6 transition-colors hover:border-brand-300 hover:bg-brand-50/30 cursor-pointer"
              >
                <Upload className="mb-1 size-5 text-ink-3" />
                <p className="text-sm font-medium text-ink-2">Téléverser le contrat signé</p>
                <p className="text-[12px] text-ink-3">PDF — max 10 Mo</p>
              </button>
            )}
          </div>

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={submitting || !pdfFile}>
              {submitting && <Loader2 className="size-4 animate-spin" />}
              Confirmer le renouvellement
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

/* ------------------------------------------------------------------ */
/*  Terminate dialog                                                   */
/* ------------------------------------------------------------------ */

function TerminateDialog({
  open,
  onOpenChange,
  employeeId,
  contractId,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  employeeId: string;
  contractId: string;
}) {
  const [motif, setMotif] = React.useState("");
  const terminate = useTerminateContract(employeeId, contractId);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!motif.trim()) return;
    terminate.mutate(motif, {
      onSuccess: () => {
        toast.success("Contrat résilié");
        onOpenChange(false);
      },
      onError: (err) => toast.error((err as Error).message),
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Résilier le contrat</DialogTitle>
          <DialogDescription>
            Cette action est définitive. Le contrat sera marqué comme résilié.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="motif">Motif de résiliation</Label>
            <Input
              id="motif"
              value={motif}
              onChange={(e) => setMotif(e.target.value)}
              placeholder="Ex: Fin de mission, démission..."
              required
            />
          </div>
          <DialogFooter>
            <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
              Annuler
            </Button>
            <Button type="submit" variant="destructive" disabled={terminate.isPending || !motif.trim()}>
              {terminate.isPending && <Loader2 className="size-4 animate-spin" />}
              Confirmer la résiliation
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

/* ------------------------------------------------------------------ */
/*  Helpers                                                            */
/* ------------------------------------------------------------------ */

function DetailRow({
  label,
  value,
}: {
  label: string;
  value: React.ReactNode;
}) {
  return (
    <div className="flex items-center justify-between py-3">
      <dt className="text-[13px] text-ink-3">{label}</dt>
      <dd className="text-[14px] font-medium text-ink">{value}</dd>
    </div>
  );
}

function RenewField({
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
