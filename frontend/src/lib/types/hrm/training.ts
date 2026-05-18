export type TrainingStatus = "PLANNED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";

export type Training = {
  id: string;
  organizationId: string;
  agencyId: string | null;
  intitule: string;
  organisme: string | null;
  dateDebut: string;
  dateFin: string;
  cout: string;
  nbPlaces: number;
  lieu: string | null;
  status: TrainingStatus;
};

export type TrainingEnrollmentStatus = "ENROLLED" | "COMPLETED" | "CANCELLED";

export type TrainingEnrollment = {
  id: string;
  trainingId: string;
  employeeId: string;
  status: TrainingEnrollmentStatus;
  noteEvaluation: string | null;
  attestationFileId: string | null;
};

export type PlanTrainingInput = {
  agencyId?: string | null;
  intitule: string;
  organisme?: string | null;
  dateDebut: string;
  dateFin: string;
  cout: string;
  nbPlaces: number;
  lieu?: string | null;
};

export type EnrollEmployeeInput = {
  employeeId: string;
};

export type CompleteEnrollmentInput = {
  note?: string | null;
  attestationId?: string | null;
};

export type TrainingBudget = {
  id: string;
  organizationId: string;
  agencyId: string | null;
  annee: number;
  montantAlloue: string;
  montantEngage: string;
  montantRealise: string;
};

export type CreateTrainingBudgetInput = {
  agencyId?: string | null;
  annee: number;
  montantAlloue: string;
};

export type MontantInput = {
  montant: string;
};
