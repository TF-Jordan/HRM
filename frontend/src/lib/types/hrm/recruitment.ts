export type JobOfferStatus = "DRAFT" | "PUBLISHED" | "CLOSED";

export type JobOffer = {
  id: string;
  organizationId: string;
  agencyId: string | null;
  poste: string;
  departement: string | null;
  localisation: string | null;
  competencesRequises: string | null;
  dateLimite: string | null;
  packageSalarial: string | null;
  status: JobOfferStatus;
};

export type ApplicationStatus =
  | "NEW"
  | "SHORTLISTED"
  | "INTERVIEWING"
  | "OFFERED"
  | "HIRED"
  | "REJECTED";

export type Application = {
  id: string;
  jobOfferId: string;
  candidatNom: string;
  candidatPrenom: string;
  candidatEmail: string;
  candidatTelephone: string | null;
  cvFileId: string | null;
  lettreMotivationFileId: string | null;
  status: ApplicationStatus;
};

export type InterviewType = "PHONE" | "TECHNICAL" | "MANAGERIAL" | "HR" | "FINAL";
export type InterviewResult = "PENDING" | "PASS" | "FAIL";

export type Interview = {
  id: string;
  applicationId: string;
  type: InterviewType;
  dateHeure: string;
  lieu: string | null;
  interviewerPartyId: string;
  interviewerDisplayName: string;
  notes: string | null;
  resultat: InterviewResult;
};

export type OnboardingTaskStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";

export type OnboardingTask = {
  id: string;
  employeeId: string;
  titre: string;
  description: string | null;
  assignedToPartyId: string | null;
  echeance: string | null;
  status: OnboardingTaskStatus;
};

export type CreateJobOfferInput = {
  agencyId?: string | null;
  poste: string;
  departement?: string | null;
  localisation?: string | null;
  competencesRequises?: string | null;
  dateLimite?: string | null;
  packageSalarial?: string | null;
};

export type CreateApplicationInput = {
  jobOfferId: string;
  candidatNom: string;
  candidatPrenom: string;
  candidatEmail: string;
  candidatTelephone?: string | null;
  cvFileId?: string | null;
  lettreMotivationFileId?: string | null;
};

export type ScheduleInterviewInput = {
  applicationId: string;
  type: InterviewType;
  dateHeure: string;
  lieu?: string | null;
  interviewerPartyId: string;
  interviewerDisplayName: string;
};

export type CompleteInterviewInput = {
  notes?: string | null;
  resultat: InterviewResult;
};
