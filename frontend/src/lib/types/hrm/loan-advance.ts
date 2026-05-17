export type LoanAdvanceStatus = "PENDING" | "REJECTED" | "IN_REPAYMENT" | "FULLY_REPAID";

export type LoanAdvance = {
  id: string;
  employeeId: string;
  montant: number;
  soldeRestant: number;
  mensualite: number;
  status: LoanAdvanceStatus;
  dateDebut: string;
  nbEcheances: number;
  motif: string | null;
  approvedBy: string | null;
};

export type RequestLoanInput = {
  employeeId: string;
  montant: number;
  nbEcheances: number;
  motif?: string | null;
};
