"use client";

import { useTranslations } from "next-intl";
import { useQuery } from "@tanstack/react-query";
import { Check, X, Banknote } from "lucide-react";
import { toast } from "sonner";
import { bffFetch } from "@/lib/api-client";
import { useEmployees } from "@/hooks/modules/useEmployees";
import { useExpenseTransition } from "@/hooks/modules/useExpenses";
import { useFormat } from "@/hooks/useFormat";
import { PageHeader } from "@/components/shell/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/ui-tokens/StatusBadge";
import type { ExpenseReport } from "@/lib/types/hrm/expense";

export function ApproveExpensesClient() {
  const t = useTranslations("manager.expensesApprove");
  const tNav = useTranslations("navigation");
  const fmt = useFormat();
  const employees = useEmployees();
  const transition = useExpenseTransition();

  // Fetch expenses for all employees (paginated/N+1 acceptable on small org)
  const expenses = useQuery({
    queryKey: ["hrm", "expenses", "all-pending"],
    queryFn: async () => {
      if (!employees.data) return [];
      const all = await Promise.all(
        employees.data.map((e) =>
          bffFetch<ExpenseReport[]>(`/api/hrm/expenses?employeeId=${e.id}`).catch(() => []),
        ),
      );
      return all.flat().filter((r) => r.status === "SUBMITTED" || r.status === "APPROVED");
    },
    enabled: !!employees.data,
  });

  const run = (id: string, action: "approve" | "reject" | "reimburse") =>
    transition.mutate(
      { id, action },
      {
        onSuccess: () => {
          toast.success(t(action as never));
          expenses.refetch();
        },
        onError: (err) => toast.error((err as Error).message),
      },
    );

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        ucBadge="UC-22"
        crumbs={[{ label: tNav("items.expenses") }, { label: t("title") }]}
        title={t("title")}
        subtitle={t("subtitle")}
      />

      {(employees.isLoading || expenses.isLoading) && <Skeleton className="h-32 w-full" />}

      {!expenses.isLoading && expenses.data && expenses.data.length === 0 && (
        <Card>
          <CardContent className="py-8 text-center text-sm text-ink-3">{t("empty")}</CardContent>
        </Card>
      )}

      {!expenses.isLoading && expenses.data && expenses.data.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-separate border-spacing-0">
              <thead>
                <tr>
                  {["Motif", "Période", "Total", "Statut", ""].map((h, i) => (
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
                {expenses.data.map((e) => (
                  <tr key={e.id}>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] font-medium text-ink">{e.motif ?? "—"}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular">{fmt.date(e.periode)}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px] text-ink-2 tabular text-right">{fmt.money(e.totalMontant)}</td>
                    <td className="border-b border-line-soft px-4 py-3 text-[13.5px]"><StatusBadge kind="expense" status={e.status} /></td>
                    <td className="border-b border-line-soft px-4 py-3 text-right">
                      <div className="flex justify-end gap-1.5">
                        {e.status === "SUBMITTED" && (
                          <>
                            <Button size="sm" onClick={() => run(e.id, "approve")}>
                              <Check className="size-4" />
                              {t("approve")}
                            </Button>
                            <Button size="sm" variant="secondary" onClick={() => run(e.id, "reject")}>
                              <X className="size-4" />
                              {t("reject")}
                            </Button>
                          </>
                        )}
                        {e.status === "APPROVED" && (
                          <Button size="sm" onClick={() => run(e.id, "reimburse")}>
                            <Banknote className="size-4" />
                            {t("reimburse")}
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
    </div>
  );
}
