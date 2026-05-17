"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Pause, Play, XCircle, AlertTriangle } from "lucide-react";
import { useEmployee } from "@/hooks/modules/useEmployees";
import { PageHeader } from "@/components/shell/PageHeader";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import { Avatar } from "@/components/ui/avatar";
import { useFormat } from "@/hooks/useFormat";
import { IdentityTab } from "./tabs/identity-tab";
import { ContractsTab } from "./tabs/contracts-tab";
import { DependentsTab } from "./tabs/dependents-tab";
import { LeavesTab } from "./tabs/leaves-tab";
import { TerminateDialog } from "./dialogs/terminate-dialog";
import { SuspendDialog } from "./dialogs/suspend-dialog";
import { ReactivateDialog } from "./dialogs/reactivate-dialog";

export function EmployeeDetailClient({ employeeId }: { employeeId: string }) {
  const t = useTranslations("employees");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const { data, isLoading, isError, error } = useEmployee(employeeId);
  const [terminateOpen, setTerminateOpen] = React.useState(false);
  const [suspendOpen, setSuspendOpen] = React.useState(false);
  const [reactivateOpen, setReactivateOpen] = React.useState(false);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-16 w-1/2" />
        <Skeleton className="h-40 w-full" />
        <Skeleton className="h-60 w-full" />
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

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-02..05"
        crumbs={[
          { label: tNav("items.employees"), href: "/employees" },
          { label: data.actorDisplayName },
        ]}
        title={
          <div className="flex flex-wrap items-center gap-3">
            <Avatar name={data.actorDisplayName} size="lg" />
            <div>
              <div>{data.actorDisplayName}</div>
              <div className="mt-1 flex items-center gap-2 font-sans text-[13.5px] font-normal text-ink-3">
                <span className="font-mono text-ink-2">{data.matricule}</span>
                <span aria-hidden>•</span>
                <StatusBadge kind="employee" status={status} />
                <span aria-hidden>•</span>
                <span>{t("detail.fields.hireDate")}: {fmt.date(data.dateEmbauche)}</span>
              </div>
            </div>
          </div>
        }
        actions={
          <>
            {canSuspend && (
              <Button variant="secondary" onClick={() => setSuspendOpen(true)}>
                <Pause className="size-4" />
                {t("detail.actions.suspend")}
              </Button>
            )}
            {canReactivate && (
              <Button variant="secondary" onClick={() => setReactivateOpen(true)}>
                <Play className="size-4" />
                {t("detail.actions.reactivate")}
              </Button>
            )}
            {canTerminate && (
              <Button variant="destructive" onClick={() => setTerminateOpen(true)}>
                <XCircle className="size-4" />
                {t("detail.actions.terminate")}
              </Button>
            )}
          </>
        }
      />

      <Tabs defaultValue="identity">
        <TabsList>
          <TabsTrigger value="identity">{t("detail.tabs.identity")}</TabsTrigger>
          <TabsTrigger value="contracts">{t("detail.tabs.contracts")}</TabsTrigger>
          <TabsTrigger value="dependents">{t("detail.tabs.dependents")}</TabsTrigger>
          <TabsTrigger value="leaves">{t("detail.tabs.leaves")}</TabsTrigger>
        </TabsList>

        <TabsContent value="identity">
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

      <TerminateDialog
        employeeId={employeeId}
        open={terminateOpen}
        onOpenChange={setTerminateOpen}
      />
      <SuspendDialog
        employeeId={employeeId}
        open={suspendOpen}
        onOpenChange={setSuspendOpen}
      />
      <ReactivateDialog
        employeeId={employeeId}
        open={reactivateOpen}
        onOpenChange={setReactivateOpen}
      />
    </div>
  );
}
