"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import {
  Pause,
  Play,
  XCircle,
  AlertTriangle,
  ChevronLeft,
  CalendarDays,
  Building2,
  ShieldCheck,
  Pencil,
  MoreHorizontal,
} from "lucide-react";
import { useEmployee, useContracts, useDependents, useLeaveBalances } from "@/hooks/modules/useEmployees";
import { Link } from "@/i18n/navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge, type BadgeProps } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { StatCard } from "@/components/ui-tokens/StatCard";
import { Avatar } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { useFormat } from "@/hooks/useFormat";
import { IdentityTab } from "./tabs/identity-tab";
import { ContractsTab } from "./tabs/contracts-tab";
import { DependentsTab } from "./tabs/dependents-tab";
import { LeavesTab } from "./tabs/leaves-tab";
import { TerminateDialog } from "./dialogs/terminate-dialog";
import { SuspendDialog } from "./dialogs/suspend-dialog";
import { ReactivateDialog } from "./dialogs/reactivate-dialog";
import { EditEmployeeDialog } from "./dialogs/edit-employee-dialog";

const CONTRACT_TONE: Record<string, BadgeProps["tone"]> = {
  CDI: "green",
  CDD: "blue",
  STAGE: "violet",
  INTERIM: "amber",
};

function seniorityLabel(iso: string): string {
  const start = new Date(iso);
  if (Number.isNaN(start.getTime())) return "—";
  const now = new Date();
  const m = (now.getFullYear() - start.getFullYear()) * 12 + (now.getMonth() - start.getMonth());
  const years = Math.floor(m / 12);
  const months = m % 12;
  if (years <= 0 && months <= 0) return "< 1m";
  return `${years > 0 ? `${years}a ` : ""}${months}m`.trim();
}

export function EmployeeDetailClient({ employeeId }: { employeeId: string }) {
  const t = useTranslations("employees");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const year = new Date().getFullYear();

  const { data, isLoading, isError, error } = useEmployee(employeeId);
  const contracts = useContracts(employeeId);
  const dependents = useDependents(employeeId);
  const balances = useLeaveBalances(employeeId, year);

  const [editOpen, setEditOpen] = React.useState(false);
  const [terminateOpen, setTerminateOpen] = React.useState(false);
  const [suspendOpen, setSuspendOpen] = React.useState(false);
  const [reactivateOpen, setReactivateOpen] = React.useState(false);

  const activeContract = React.useMemo(
    () => contracts.data?.find((c) => c.status === "ACTIVE") ?? null,
    [contracts.data],
  );
  const soldeConges = React.useMemo(
    () => (balances.data ?? []).reduce((sum, b) => sum + Number(b.soldeRestant ?? 0), 0),
    [balances.data],
  );

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-9 w-40" />
        <Skeleton className="h-44 w-full rounded-[20px]" />
        <Skeleton className="h-24 w-full rounded-[20px]" />
        <Skeleton className="h-60 w-full rounded-[20px]" />
      </div>
    );
  }

  if (isError || !data) {
    return (
      <Card>
        <CardContent className="flex items-start gap-3">
          <AlertTriangle className="size-5 shrink-0 text-status-red-500" />
          <div className="text-sm">
            <div className="font-semibold text-ink">Failed to load employee</div>
            <div className="text-ink-3">{(error as Error | null)?.message ?? "Unknown error"}</div>
          </div>
        </CardContent>
      </Card>
    );
  }

  const status = data.status;
  const canSuspend = status === "ACTIVE";
  const canReactivate = status === "SUSPENDED";
  const canTerminate = status !== "TERMINATED";
  const contractType = activeContract?.type ?? data.contractType ?? null;

  return (
    <div className="space-y-5 animate-fade-up">
      <Link
        href="/employees"
        className="inline-flex items-center gap-1 text-[13px] font-semibold text-ink-3 hover:text-brand-600"
      >
        <ChevronLeft className="size-4" />
        {t("detail.back")}
        <span className="ml-2 font-normal text-ink-4">
          {tNav("items.employees")} / {data.actorDisplayName}
        </span>
      </Link>

      {/* Profile header card */}
      <Card>
        <CardContent className="flex flex-col gap-5 p-5 lg:flex-row lg:items-start lg:justify-between">
          <div className="flex items-start gap-4">
            <Avatar name={data.actorDisplayName} tone="orange" shape="square" size="xl" />
            <div className="min-w-0">
              <div className="flex flex-wrap items-center gap-2">
                {contractType && (
                  <Badge tone={CONTRACT_TONE[contractType] ?? "gray"}>{contractType}</Badge>
                )}
                <StatusBadge kind="employee" status={status} />
              </div>
              <h1 className="mt-1.5 font-display text-[26px] font-extrabold leading-tight tracking-tight text-ink">
                {data.actorDisplayName}
              </h1>
              <p className="text-[14px] text-ink-3">
                {data.poste ?? data.echelon ?? `Cat. ${data.categorie}`}
                {data.departmentCode ? ` · ${data.departmentCode}` : ""}
              </p>
              <div className="mt-2 flex flex-wrap items-center gap-x-5 gap-y-1.5 text-[12.5px] text-ink-3">
                <span className="inline-flex items-center gap-1.5">
                  <CalendarDays className="size-3.5 text-ink-4" />
                  {fmt.date(data.dateEmbauche)}
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <Building2 className="size-3.5 text-ink-4" />
                  {data.matricule}
                </span>
                {data.numCnps && (
                  <span className="inline-flex items-center gap-1.5">
                    <ShieldCheck className="size-3.5 text-ink-4" />
                    CNPS {data.numCnps}
                  </span>
                )}
              </div>
            </div>
          </div>

          <div className="flex shrink-0 items-center gap-2">
            <Button onClick={() => setEditOpen(true)}>
              <Pencil className="size-4" />
              {t("detail.actions.edit")}
            </Button>
            {(canSuspend || canReactivate || canTerminate) && (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="secondary" size="icon" aria-label="Actions">
                    <MoreHorizontal className="size-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  {canSuspend && (
                    <DropdownMenuItem onSelect={() => setSuspendOpen(true)}>
                      <Pause className="size-4" />
                      {t("detail.actions.suspend")}
                    </DropdownMenuItem>
                  )}
                  {canReactivate && (
                    <DropdownMenuItem onSelect={() => setReactivateOpen(true)}>
                      <Play className="size-4" />
                      {t("detail.actions.reactivate")}
                    </DropdownMenuItem>
                  )}
                  {canTerminate && (
                    <DropdownMenuItem
                      onSelect={() => setTerminateOpen(true)}
                      className="text-status-red-600"
                    >
                      <XCircle className="size-4" />
                      {t("detail.actions.terminate")}
                    </DropdownMenuItem>
                  )}
                </DropdownMenuContent>
              </DropdownMenu>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Stat cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          tone="orange"
          label={t("detail.stats.salaireBrut")}
          value={activeContract ? fmt.money(activeContract.salaireBase) : "—"}
          footer={t("detail.stats.salaireBrutFooter")}
        />
        <StatCard
          tone="green"
          label={t("detail.stats.soldeConges")}
          value={balances.isLoading ? "…" : soldeConges.toFixed(1)}
          footer={t("detail.stats.soldeCongesFooter", { year })}
        />
        <StatCard
          tone="blue"
          label={t("detail.stats.anciennete")}
          value={seniorityLabel(data.dateEmbauche)}
          footer={t("detail.stats.ancienneteFooter")}
        />
        <StatCard
          tone="violet"
          label={t("detail.stats.dependents")}
          value={dependents.isLoading ? "…" : (dependents.data?.length ?? 0)}
          footer={t("detail.stats.dependentsFooter")}
        />
      </div>

      <Tabs defaultValue="overview">
        <TabsList>
          <TabsTrigger value="overview">{t("detail.tabs.overview")}</TabsTrigger>
          <TabsTrigger value="contracts">{t("detail.tabs.contracts")}</TabsTrigger>
          <TabsTrigger value="dependents">{t("detail.tabs.dependents")}</TabsTrigger>
          <TabsTrigger value="leaves">{t("detail.tabs.leaves")}</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <IdentityTab employee={data} />
        </TabsContent>
        <TabsContent value="contracts">
          <ContractsTab employeeId={employeeId} />
        </TabsContent>
        <TabsContent value="dependents">
          <DependentsTab employeeId={employeeId} />
        </TabsContent>
        <TabsContent value="leaves">
          <LeavesTab employeeId={employeeId} />
        </TabsContent>
      </Tabs>

      <EditEmployeeDialog employee={data} open={editOpen} onOpenChange={setEditOpen} />
      <TerminateDialog employeeId={employeeId} open={terminateOpen} onOpenChange={setTerminateOpen} />
      <SuspendDialog employeeId={employeeId} open={suspendOpen} onOpenChange={setSuspendOpen} />
      <ReactivateDialog employeeId={employeeId} open={reactivateOpen} onOpenChange={setReactivateOpen} />
    </div>
  );
}
