import type { ExpenseReportStatus } from "@/server/ksm/modules/expenses";

export type BadgeTone =
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "orange"
  | "violet"
  | "teal"
  | "gray";

export function expenseStatusTone(status: ExpenseReportStatus): BadgeTone {
  switch (status) {
    case "DRAFT":
      return "gray";
    case "SUBMITTED":
      return "warning";
    case "APPROVED":
      return "info";
    case "REJECTED":
      return "danger";
    case "REIMBURSED":
      return "success";
    default:
      return "gray";
  }
}

/** Expense line category → badge tone, mirroring the design palette. */
export function categoryTone(categorie: string | null | undefined): BadgeTone {
  switch ((categorie ?? "").toUpperCase()) {
    case "TRANSPORT":
      return "info";
    case "REPAS":
    case "MEALS":
      return "orange";
    case "HEBERGEMENT":
    case "LODGING":
      return "violet";
    case "MATERIEL":
    case "SUPPLIES":
      return "success";
    default:
      return "gray";
  }
}

export const EXPENSE_CATEGORIES = [
  "TRANSPORT",
  "REPAS",
  "HEBERGEMENT",
  "MATERIEL",
  "AUTRES",
] as const;

export type ExpenseCategory = (typeof EXPENSE_CATEGORIES)[number];
