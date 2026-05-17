export type ExpenseStatus = "DRAFT" | "SUBMITTED" | "APPROVED" | "REJECTED" | "REIMBURSED";

export type ExpenseReport = {
  id: string;
  employeeId: string;
  periode: string;
  totalMontant: number;
  motif: string | null;
  status: ExpenseStatus;
};

export type ExpenseLine = {
  id: string;
  expenseReportId: string;
  description: string | null;
  montant: number;
  categorie: string;
  justificatifFileId: string | null;
};

export type CreateExpenseInput = {
  employeeId: string;
  periode: string;
  motif?: string | null;
};

export type AddExpenseLineInput = {
  description?: string | null;
  montant: number;
  categorie: string;
  justificatifFileId?: string | null;
};
