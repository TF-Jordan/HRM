export type EmployeeStatus = "ACTIVE" | "ON_LEAVE" | "SUSPENDED" | "TERMINATED";

export type PaymentMode = "BANK_TRANSFER" | "MOBILE_MONEY" | "CASH" | "CHECK";

export type MobileMoneyOperator = "MTN" | "ORANGE" | "EU_MOBILE" | "YOOMEE";

export type EmployeeAccountInfo = {
  userId: string;
  username: string;
  email: string;
  temporaryPassword: string;
  emailSent: boolean;
  roleAssigned: string | null;
  membershipCreated: boolean;
};

export type EmployeeCreatedWithAccount = Employee & {
  account: EmployeeAccountInfo | null;
  accountError: string | null;
};

export type Employee = {
  id: string;
  organizationId: string;
  agencyId: string | null;
  actorId: string;
  matricule: string;
  numCnps: string | null;
  categorie: number;
  echelon: string | null;
  dateEmbauche: string; // ISO date
  status: EmployeeStatus;
  departmentCode: string | null;
  modePaiement: PaymentMode;
  compteBancaire: string | null;
  numMobileMoney: string | null;
  operateurMm: MobileMoneyOperator | null;
  actorDisplayName: string;
};

export type CreateEmployeeInput = {
  // Actor data (BFF will create the actor first)
  firstName: string;
  lastName: string;
  email?: string | null;
  phoneNumber?: string | null;
  // HRM data
  numCnps?: string | null;
  categorie: number;
  echelon?: string | null;
  dateEmbauche: string;
  departmentCode?: string | null;
  modePaiement: PaymentMode;
  compteBancaire?: string | null;
  numMobileMoney?: string | null;
  operateurMm?: MobileMoneyOperator | null;
  // First contract (optional)
  contractType?: "CDD" | "CDI" | "STAGE" | "INTERIM" | null;
  contractDateDebut?: string | null;
  contractDateFin?: string | null;
  salaireBase?: number | null;
  avantagesNature?: number | null;
  periodeEssai?: number | null;
};

export type UpdateEmployeeInput = {
  numCnps?: string | null;
  categorie: number;
  echelon?: string | null;
  departmentCode?: string | null;
  modePaiement: PaymentMode;
  compteBancaire?: string | null;
  numMobileMoney?: string | null;
  operateurMm?: MobileMoneyOperator | null;
};

export type TerminateEmployeeInput = {
  terminationDate: string;
  reason: string;
};

export type SuspendEmployeeInput = {
  reason: string;
};
