import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type JobOfferStatus = "DRAFT" | "PUBLISHED" | "CLOSED";
export type ApplicationStatus =
  | "NEW"
  | "SHORTLISTED"
  | "INTERVIEWING"
  | "OFFERED"
  | "REJECTED"
  | "HIRED";
export type InterviewType = "RH" | "TECHNIQUE" | "FINAL";
export type InterviewResult = "PENDING" | "PASS" | "FAIL";
export type OnboardingTaskStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";

export type JobOfferResponse = {
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

export type ApplicationResponse = {
  id: string;
  jobOfferId: string;
  candidatNom: string;
  candidatPrenom: string;
  candidatEmail: string | null;
  candidatTelephone: string | null;
  cvFileId: string | null;
  lettreMotivationFileId: string | null;
  status: ApplicationStatus;
};

export type InterviewResponse = {
  id: string;
  applicationId: string;
  type: InterviewType;
  dateHeure: string;
  lieu: string | null;
  interviewerPartyId: string | null;
  interviewerDisplayName: string | null;
  notes: string | null;
  resultat: InterviewResult;
};

export type OnboardingTaskResponse = {
  id: string;
  employeeId: string;
  titre: string;
  description: string | null;
  assignedToPartyId: string | null;
  echeance: string | null;
  status: OnboardingTaskStatus;
};

export type CreateJobOfferRequest = {
  agencyId?: string | null;
  poste: string;
  departement?: string | null;
  localisation?: string | null;
  competencesRequises?: string | null;
  dateLimite?: string | null;
  packageSalarial?: string | null;
};

export type CreateApplicationRequest = {
  jobOfferId: string;
  candidatNom: string;
  candidatPrenom: string;
  candidatEmail?: string | null;
  candidatTelephone?: string | null;
  cvFileId?: string | null;
  lettreMotivationFileId?: string | null;
};

export type ScheduleInterviewRequest = {
  applicationId: string;
  type: InterviewType;
  dateHeure: string;
  lieu?: string | null;
  interviewerPartyId?: string | null;
  interviewerDisplayName?: string | null;
};

export type CompleteInterviewRequest = { notes: string; resultat: InterviewResult };

export type CreateOnboardingTaskRequest = {
  employeeId: string;
  titre: string;
  description?: string | null;
  assignedToPartyId?: string | null;
  echeance?: string | null;
};

/* -------- Job offers -------- */

export function listJobOffers(session: AppSession, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  return callKsm<JobOfferResponse[]>(`/api/v1/hrm/job-offers?${params}`, {}, { session });
}

export function getJobOffer(id: string, session: AppSession) {
  return callKsm<JobOfferResponse>(`/api/v1/hrm/job-offers/${id}`, {}, { session });
}

export function createJobOffer(body: CreateJobOfferRequest, session: AppSession) {
  return callKsm<JobOfferResponse>(
    "/api/v1/hrm/job-offers",
    { method: "POST", body },
    { session },
  );
}

export function publishJobOffer(id: string, session: AppSession) {
  return callKsm<JobOfferResponse>(
    `/api/v1/hrm/job-offers/${id}/publish`,
    { method: "PUT" },
    { session },
  );
}

export function closeJobOffer(id: string, session: AppSession) {
  return callKsm<JobOfferResponse>(
    `/api/v1/hrm/job-offers/${id}/close`,
    { method: "PUT" },
    { session },
  );
}

/* -------- Applications -------- */

export function listApplicationsByJobOffer(jobOfferId: string, session: AppSession) {
  return callKsm<ApplicationResponse[]>(
    `/api/v1/hrm/job-offers/${jobOfferId}/applications`,
    {},
    { session },
  );
}

export function getApplication(id: string, session: AppSession) {
  return callKsm<ApplicationResponse>(`/api/v1/hrm/applications/${id}`, {}, { session });
}

export function createApplication(body: CreateApplicationRequest, session: AppSession) {
  return callKsm<ApplicationResponse>(
    "/api/v1/hrm/applications",
    { method: "POST", body },
    { session },
  );
}

export function transitionApplication(
  id: string,
  to: "shortlist" | "interview" | "offer" | "reject" | "hire",
  session: AppSession,
) {
  return callKsm<ApplicationResponse>(
    `/api/v1/hrm/applications/${id}/${to}`,
    { method: "PUT" },
    { session },
  );
}

export type ConvertApplicationRequest = {
  managerId?: string | null;
  numCnps?: string | null;
  categorie: number;
  echelon?: string | null;
  dateEmbauche: string;
  departmentCode?: string | null;
  modePaiement: "BANK_TRANSFER" | "MTN_MOBILE_MONEY" | "ORANGE_MONEY" | "CASH";
  compteBancaire?: string | null;
  numMobileMoney?: string | null;
  operateurMm?: "MTN" | "ORANGE" | null;
  contractType: "CDI" | "CDD" | "STAGE" | "INTERIM";
  contractDateDebut: string;
  contractDateFin?: string | null;
  salaireBase: number | string;
  avantagesNature?: number | string | null;
  periodeEssai?: number | null;
};

export type ConvertedEmployeeResponse = {
  id: string;
  organizationId: string;
  actorId: string;
  matricule: string;
  categorie: number;
  echelon: string | null;
  dateEmbauche: string;
  status: string;
  departmentCode: string | null;
  actorDisplayName: string | null;
};

/**
 * Hire-and-provision: turns an OFFERED application into a real Employee.
 * Returns the freshly created employee snapshot.
 */
export function convertApplicationToEmployee(
  applicationId: string,
  body: ConvertApplicationRequest,
  session: AppSession,
) {
  return callKsm<ConvertedEmployeeResponse>(
    `/api/v1/hrm/applications/${applicationId}/convert-to-employee`,
    { method: "POST", body },
    { session },
  );
}

/* -------- Interviews -------- */

export function listInterviews(applicationId: string, session: AppSession) {
  return callKsm<InterviewResponse[]>(
    `/api/v1/hrm/applications/${applicationId}/interviews`,
    {},
    { session },
  );
}

export function scheduleInterview(body: ScheduleInterviewRequest, session: AppSession) {
  return callKsm<InterviewResponse>(
    "/api/v1/hrm/interviews",
    { method: "POST", body },
    { session },
  );
}

export function completeInterview(
  id: string,
  body: CompleteInterviewRequest,
  session: AppSession,
) {
  return callKsm<InterviewResponse>(
    `/api/v1/hrm/interviews/${id}/complete`,
    { method: "PUT", body },
    { session },
  );
}

/* -------- Onboarding -------- */

export function listOnboardingTasks(employeeId: string, session: AppSession) {
  return callKsm<OnboardingTaskResponse[]>(
    `/api/v1/hrm/onboarding-tasks/employee/${employeeId}`,
    {},
    { session },
  );
}

export function createOnboardingTask(body: CreateOnboardingTaskRequest, session: AppSession) {
  return callKsm<OnboardingTaskResponse>(
    "/api/v1/hrm/onboarding-tasks",
    { method: "POST", body },
    { session },
  );
}

export function startOnboardingTask(id: string, session: AppSession) {
  return callKsm<OnboardingTaskResponse>(
    `/api/v1/hrm/onboarding-tasks/${id}/start`,
    { method: "PUT" },
    { session },
  );
}

export function completeOnboardingTask(id: string, session: AppSession) {
  return callKsm<OnboardingTaskResponse>(
    `/api/v1/hrm/onboarding-tasks/${id}/complete`,
    { method: "PUT" },
    { session },
  );
}
