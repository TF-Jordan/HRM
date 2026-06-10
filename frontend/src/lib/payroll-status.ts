import type { PayrollRunResponse, PayrollRunStatus } from "@/server/ksm/modules/payroll";

/**
 * Linear ordering of the payroll-core run lifecycle (9 states). Used to derive
 * tone, progress and stepper state generically so every backend status is
 * handled — not just CALCULATED / VALIDATED / PAID.
 */
export const PAYROLL_RUN_ORDER: Record<PayrollRunStatus, number> = {
  DRAFT: 0,
  VARIABLES_LOCKED: 1,
  // REJECTED sits back at the calculation step: the payroll manager must recalculate before
  // the HR admin can validate again.
  REJECTED: 1,
  CALCULATED: 2,
  REVIEW: 3,
  VALIDATED: 4,
  APPROVED: 5,
  PAYMENT_INITIATED: 6,
  PAID: 7,
  CLOSED: 8,
};

function runOrder(s: PayrollRunStatus | string): number {
  return PAYROLL_RUN_ORDER[s as PayrollRunStatus] ?? 0;
}

/** Terminal states (no further action possible). */
export function isPayrollRunTerminal(s: PayrollRunStatus | string): boolean {
  return s === "PAID" || s === "CLOSED";
}

export function payrollStatusTone(
  s: PayrollRunStatus | string,
): "warning" | "info" | "success" | "gray" | "danger" {
  switch (s) {
    case "REJECTED":
      return "danger";
    case "CALCULATED":
    case "REVIEW":
      return "warning";
    case "VALIDATED":
    case "APPROVED":
    case "PAYMENT_INITIATED":
      return "info";
    case "PAID":
    case "CLOSED":
      return "success";
    default:
      return "gray";
  }
}

export function payrollStatusProgress(s: PayrollRunStatus | string): number {
  const max = PAYROLL_RUN_ORDER.CLOSED;
  return Math.min(1, runOrder(s) / max);
}

export type PayrollStepKey = "variables" | "calculation" | "review" | "validation" | "payment";

/**
 * Maps a run status to the 5-step business stepper
 * (Variables → Calcul → Vérif. DRH → Validation DG → Paiement).
 *
 * Each step carries the order at which it becomes "done"; it is "active" while
 * the run sits within the step's own range and "pending" before it is reached.
 */
const STEP_DONE_AT: Record<PayrollStepKey, number> = {
  variables: PAYROLL_RUN_ORDER.VARIABLES_LOCKED, // 1
  calculation: PAYROLL_RUN_ORDER.CALCULATED, // 2
  review: PAYROLL_RUN_ORDER.VALIDATED, // 4 (validate = HR review)
  validation: PAYROLL_RUN_ORDER.APPROVED, // 5 (approve = CEO/DG approval)
  payment: PAYROLL_RUN_ORDER.PAID, // 7
};

const STEP_KEYS: PayrollStepKey[] = [
  "variables",
  "calculation",
  "review",
  "validation",
  "payment",
];

export function payrollStepperState(
  status: PayrollRunStatus | string,
): Record<PayrollStepKey, "done" | "active" | "pending"> {
  const order = runOrder(status);
  const result = {} as Record<PayrollStepKey, "done" | "active" | "pending">;
  let previousDoneAt = 0;
  for (const key of STEP_KEYS) {
    const doneAt = STEP_DONE_AT[key];
    if (order >= doneAt) {
      result[key] = "done";
    } else if (order >= previousDoneAt) {
      result[key] = "active";
    } else {
      result[key] = "pending";
    }
    previousDoneAt = doneAt;
  }
  return result;
}

/**
 * The next lifecycle transition a payroll officer can trigger from a given
 * status, or {@code null} when the run is awaiting an async step (e.g. payment
 * processing) or is terminal. Each action maps to a BFF route under
 * {@code /api/hrm/payroll/[id]/<action>} and requires {@code hrm:payroll:validate}.
 */
export type PayrollRunAction = "validate" | "approve" | "initiate-payment" | "close";

export function nextPayrollAction(
  status: PayrollRunStatus | string,
): PayrollRunAction | null {
  switch (status) {
    case "CALCULATED":
    case "REVIEW":
      return "validate";
    case "VALIDATED":
      return "approve";
    case "APPROVED":
      return "initiate-payment";
    case "PAID":
      return "close";
    default:
      return null;
  }
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
 * Returns whether the "Run payroll" button should be active.
 * The button is always active — the backend validates business rules.
 */
export function getPayrollWindowState(
  _runs: Pick<PayrollRunResponse, "periode">[],
  now = new Date(),
): PayrollWindowState {
  const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  const msRemaining = lastDay.getTime() - now.getTime() + 1;
  const daysRemaining = Math.max(0, Math.ceil(msRemaining / (1000 * 60 * 60 * 24)));

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
