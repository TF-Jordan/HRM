export type PayrollRunStatus = "CALCULATED" | "VALIDATED" | "PAID";

export type PayrollRun = {
  id: string;
  organizationId: string;
  agencyId: string | null;
  periode: string;
  status: PayrollRunStatus;
  totalBrut: number;
  totalNet: number;
  totalEmployeur: number;
  dateCalcul: string;
  dateValidation: string | null;
};

export type PayrollEntry = {
  id: string;
  payrollRunId: string;
  employeeId: string;
  matricule: string;
  actorDisplayName: string;
  salaireBase: number;
  brut: number;
  totalCotisations: number;
  totalImpots: number;
  net: number;
};

export type PayslipLine = {
  id: string;
  code: string;
  libelle: string;
  base: number | null;
  taux: number | null;
  montant: number;
  category: string;
};

export type PayslipView = {
  entry: PayrollEntry;
  lines: PayslipLine[];
};
