import type { BadgeTone } from "@/lib/expense-status";
import type { LoanAdvanceStatus } from "@/server/ksm/modules/loans";

export function loanStatusTone(status: LoanAdvanceStatus): BadgeTone {
  switch (status) {
    case "PENDING":
      return "warning";
    case "APPROVED":
      return "info";
    case "IN_REPAYMENT":
      return "orange";
    case "FULLY_REPAID":
      return "success";
    case "REJECTED":
      return "danger";
    default:
      return "gray";
  }
}

export type LoanKind = "ADVANCE" | "PERSONAL" | "VEHICLE" | "HOUSING";

export function loanKindOf(nbEcheances: number): LoanKind {
  if (nbEcheances <= 1) return "ADVANCE";
  if (nbEcheances <= 6) return "PERSONAL";
  if (nbEcheances <= 36) return "VEHICLE";
  return "HOUSING";
}

export function loanKindTone(kind: LoanKind): BadgeTone {
  switch (kind) {
    case "ADVANCE":
      return "warning";
    case "PERSONAL":
      return "violet";
    case "VEHICLE":
      return "info";
    case "HOUSING":
      return "success";
  }
}

export function loanProgressPct(montant: number, soldeRestant: number): number {
  if (montant <= 0) return 100;
  const repaid = Math.max(0, montant - soldeRestant);
  return Math.min(100, Math.round((repaid / montant) * 100));
}

export function shortLoanRef(id: string, kind: LoanKind): string {
  const prefix = kind === "ADVANCE" ? "AV" : "PR";
  return `${prefix}-${id.slice(0, 4).toUpperCase()}-${id.slice(4, 8).toUpperCase()}`;
}
