import type { PayrollRunResponse, PayrollRunStatus } from "@/server/ksm/modules/payroll";

export function payrollStatusTone(s: PayrollRunStatus | string): "warning" | "info" | "success" | "gray" {
  switch (s) {
    case "CALCULATED":
      return "warning";
    case "VALIDATED":
      return "info";
    case "PAID":
      return "success";
    default:
      return "gray";
  }
}

export function payrollStatusProgress(s: PayrollRunStatus | string): number {
  switch (s) {
    case "CALCULATED":
      return 0.4;
    case "VALIDATED":
      return 0.8;
    case "PAID":
      return 1;
    default:
      return 0;
  }
}

export type PayrollStepKey = "variables" | "calculation" | "review" | "validation" | "payment";

export function payrollStepperState(
  status: PayrollRunStatus | string,
): Record<PayrollStepKey, "done" | "active" | "pending"> {
  if (status === "PAID") {
    return {
      variables: "done",
      calculation: "done",
      review: "done",
      validation: "done",
      payment: "done",
    };
  }
  if (status === "VALIDATED") {
    return {
      variables: "done",
      calculation: "done",
      review: "done",
      validation: "done",
      payment: "active",
    };
  }
  if (status === "CALCULATED") {
    return {
      variables: "done",
      calculation: "done",
      review: "active",
      validation: "pending",
      payment: "pending",
    };
  }
  return {
    variables: "pending",
    calculation: "pending",
    review: "pending",
    validation: "pending",
    payment: "pending",
  };
}

export function formatPeriodFr(periode: string): string {
  const [y, m] = periode.split("-").map(Number);
  if (!y || !m) return periode;
  const d = new Date(y, m - 1, 1);
  return d.toLocaleDateString("fr-FR", { month: "long", year: "numeric" });
}

// ── Payroll cycle window ──────────────────────────────────────────────────────

export type PayrollWindowState =
  | { canRun: false; reason: "already_run" }
  | { canRun: false; reason: "too_early"; daysUntilOpen: number }
  | { canRun: true; daysUntilMonthEnd: number };

/**
 * Returns whether the "Run payroll" button should be active, and why if not.
 *
 * Rules:
 *  - If a run already exists for the current month → disabled (already_run)
 *  - If more than 5 days remain until month-end → disabled (too_early)
 *  - Otherwise → active (last 5 calendar days of the month)
 */
export function getPayrollWindowState(
  runs: Pick<PayrollRunResponse, "periode">[],
  now = new Date(),
): PayrollWindowState {
  const currentPeriode = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;

  if (runs.some((r) => r.periode === currentPeriode)) {
    return { canRun: false, reason: "already_run" };
  }

  // Last day of the current month
  const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  // Days remaining (inclusive of today → ceil)
  const msRemaining = lastDay.getTime() - now.getTime() + 1;
  const daysRemaining = Math.max(0, Math.ceil(msRemaining / (1000 * 60 * 60 * 24)));

  if (daysRemaining > 5) {
    return { canRun: false, reason: "too_early", daysUntilOpen: daysRemaining - 5 };
  }

  return { canRun: true, daysUntilMonthEnd: daysRemaining };
}

export function formatPeriodShort(periode: string, locale: "fr" | "en"): string {
  const [y, m] = periode.split("-").map(Number);
  if (!y || !m) return periode;
  const d = new Date(y, m - 1, 1);
  return d.toLocaleDateString(locale === "fr" ? "fr-FR" : "en-US", {
    month: "short",
    year: "numeric",
  });
}
