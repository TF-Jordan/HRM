"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Check, Loader2 } from "lucide-react";
import { toast } from "sonner";
import {
  useOrgTimesheets,
  useValidateTimesheet,
} from "@/hooks/modules/useOrgTimesheets";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";

export function TimesheetsClient() {
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const [periode, setPeriode] = React.useState<string>(
    new Date().toISOString().slice(0, 7),
  );
  const list = useOrgTimesheets(periode || null);
  const employees = useEmployees();
  const validate = useValidateTimesheet();

  const employeeName = (id: string) => {
    const e = employees.data?.find((x) => x.id === id);
    return e ? `${e.matricule} — ${e.actorDisplayName}` : id;
  };

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-22"
        crumbs={[{ label: tNav("items.timesheets") }]}
        title={tNav("items.timesheets")}
        subtitle="Pointages soumis pour validation"
      />

      <Card>
        <CardContent className="flex items-center gap-3 p-4">
          <Label className="shrink-0">Période</Label>
          <Input
            type="month"
            value={periode}
            onChange={(e) => setPeriode(e.target.value)}
            className="max-w-[180px] tabular"
          />
        </CardContent>
      </Card>

      {list.isLoading && <Skeleton className="h-32 w-full" />}

      {!list.isLoading && list.data && list.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">
            Aucun pointage pour cette période
          </CardContent>
        </Card>
      )}

      {!list.isLoading && list.data && list.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {["Employé", "Période", "Heures", "H. supp.", "Statut", ""].map(
                    (h, i) => (
                      <th
                        key={i}
                        className="border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3"
                      >
                        {h}
                      </th>
                    ),
                  )}
                </tr>
              </thead>
              <tbody>
                {list.data.map((t) => (
                  <tr key={t.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink">
                      {employeeName(t.employeeId)}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {t.periode}
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.number(t.totalHeures)} h
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">
                      {fmt.number(t.totalHeuresSup)} h
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]">
                      <StatusBadge kind="timesheet" status={t.status} />
                    </td>
                    <td className="border-b border-line-soft px-4 py-3 text-right">
                      {t.status === "SUBMITTED" && (
                        <Button
                          size="sm"
                          onClick={() =>
                            validate.mutate(t.id, {
                              onSuccess: () => toast.success("Pointage validé"),
                              onError: (err) => toast.error((err as Error).message),
                            })
                          }
                        >
                          {validate.isPending ? (
                            <Loader2 className="size-4 animate-spin" />
                          ) : (
                            <Check className="size-4" />
                          )}
                          Valider
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
