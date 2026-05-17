export type PayrollRunStatus = "CALCULATED" | "VALIDATED" | "PAID";

export type PayrollRun = {
  id: string;
  periode: string;
  status: PayrollRunStatus;
  totalBrut: number;
  totalNet: number;
  totalCnpsEmploye: number;
  totalCnpsEmployeur: number;
  totalIrpp: number;
  nbEmployes: number;
  calculatedAt: string;
  validatedBy: string | null;
  validatedAt: string | null;
};

export type PaymentStatus = "PENDING" | "PROCESSING" | "PAID" | "FAILED";

export type PayrollEntry = {
  id: string;
  employeeId: string;
  salaireBase: number;
  brut: number;
  net: number;
  cnpsEmploye: number;
  cnpsEmployeur: number;
  irpp: number;
  cac: number;
  primes: number;
  retenues: number;
  avancesDeduites: number;
  paymentStatus: PaymentStatus;
  paymentChannel: string | null;
};

export type PayslipLineType =
  | "EARNING"
  | "DEDUCTION"
  | "EMPLOYER_CHARGE"
  | "TAX"
  | "INFO";

export type PayslipLine = {
  id: string;
  libelle: string;
  type: PayslipLineType;
  base: number | null;
  taux: number | null;
  montant: number;
  ordreAffichage: number;
};

export type RunPayrollInput = {
  periode: string;
  agencyId?: string | null;
};
