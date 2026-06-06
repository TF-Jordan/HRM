import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type TrainingStatus = "PLANNED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";
export type EnrollmentStatus = "ENROLLED" | "COMPLETED" | "CANCELLED";
export type TrainingRequestStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";

export type TrainingResponse = {
  id: string;
  organizationId: string;
  agencyId: string | null;
  intitule: string;
  organisme: string | null;
  dateDebut: string | null;
  dateFin: string | null;
  cout: number | string | null;
  nbPlaces: number | null;
  lieu: string | null;
  status: TrainingStatus;
};

export type EnrollmentResponse = {
  id: string;
  trainingId: string;
  employeeId: string;
  status: EnrollmentStatus;
  noteEvaluation: number | string | null;
  attestationFileId: string | null;
};

export type TrainingRequestResponse = {
  id: string;
  organizationId: string;
  employeeId: string;
  trainingId: string;
  motivation: string | null;
  status: TrainingRequestStatus;
  decisionReason: string | null;
  enrollmentId: string | null;
  decidedAt: string | null;
};

export type PlanTrainingRequest = {
  agencyId?: string | null;
  intitule: string;
  organisme?: string | null;
  dateDebut?: string | null;
  dateFin?: string | null;
  cout?: number | string | null;
  nbPlaces?: number | null;
  lieu?: string | null;
};

export function listTrainings(session: AppSession, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  return callKsm<TrainingResponse[]>(`/api/v1/hrm/trainings?${params}`, {}, { session });
}

export function getTraining(id: string, session: AppSession) {
  return callKsm<TrainingResponse>(`/api/v1/hrm/trainings/${id}`, {}, { session });
}

export function planTraining(body: PlanTrainingRequest, session: AppSession) {
  return callKsm<TrainingResponse>(
    "/api/v1/hrm/trainings",
    { method: "POST", body },
    { session },
  );
}

export function startTraining(id: string, session: AppSession) {
  return callKsm<TrainingResponse>(
    `/api/v1/hrm/trainings/${id}/start`,
    { method: "PUT" },
    { session },
  );
}

export function completeTraining(id: string, session: AppSession) {
  return callKsm<TrainingResponse>(
    `/api/v1/hrm/trainings/${id}/complete`,
    { method: "PUT" },
    { session },
  );
}

export function cancelTraining(id: string, session: AppSession) {
  return callKsm<TrainingResponse>(
    `/api/v1/hrm/trainings/${id}/cancel`,
    { method: "PUT" },
    { session },
  );
}

export function enrollEmployee(trainingId: string, employeeId: string, session: AppSession) {
  return callKsm<EnrollmentResponse>(
    `/api/v1/hrm/trainings/${trainingId}/enrollments`,
    { method: "POST", body: { employeeId } },
    { session },
  );
}

export function listEnrollmentsByTraining(trainingId: string, session: AppSession) {
  return callKsm<EnrollmentResponse[]>(
    `/api/v1/hrm/trainings/${trainingId}/enrollments`,
    {},
    { session },
  );
}

export function listEnrollmentsByEmployee(employeeId: string, session: AppSession) {
  return callKsm<EnrollmentResponse[]>(
    `/api/v1/hrm/trainings/enrollments/employee/${employeeId}`,
    {},
    { session },
  );
}

export function completeEnrollment(
  enrollmentId: string,
  body: { note: number | string; attestationId?: string | null },
  session: AppSession,
) {
  return callKsm<EnrollmentResponse>(
    `/api/v1/hrm/trainings/enrollments/${enrollmentId}/complete`,
    { method: "PUT", body },
    { session },
  );
}

export function cancelEnrollment(enrollmentId: string, session: AppSession) {
  return callKsm<EnrollmentResponse>(
    `/api/v1/hrm/trainings/enrollments/${enrollmentId}/cancel`,
    { method: "PUT" },
    { session },
  );
}

// --- Self-service training requests (employee → manager/DRH approval) ---

export function requestTraining(
  body: { trainingId: string; employeeId: string; motivation?: string | null },
  session: AppSession,
) {
  return callKsm<TrainingRequestResponse>(
    "/api/v1/hrm/trainings/requests",
    { method: "POST", body },
    { session },
  );
}

export function cancelTrainingRequest(requestId: string, session: AppSession) {
  return callKsm<TrainingRequestResponse>(
    `/api/v1/hrm/trainings/requests/${requestId}/cancel`,
    { method: "PUT" },
    { session },
  );
}

export function approveTrainingRequest(requestId: string, session: AppSession) {
  return callKsm<TrainingRequestResponse>(
    `/api/v1/hrm/trainings/requests/${requestId}/approve`,
    { method: "PUT" },
    { session },
  );
}

export function rejectTrainingRequest(requestId: string, reason: string, session: AppSession) {
  return callKsm<TrainingRequestResponse>(
    `/api/v1/hrm/trainings/requests/${requestId}/reject`,
    { method: "PUT", body: { reason } },
    { session },
  );
}

export function listTrainingRequestsByEmployee(employeeId: string, session: AppSession) {
  const params = new URLSearchParams({ employeeId });
  return callKsm<TrainingRequestResponse[]>(
    `/api/v1/hrm/trainings/requests?${params}`,
    {},
    { session },
  );
}

export function listTrainingRequestsByOrganization(
  session: AppSession,
  opts: { organizationId?: string; status?: TrainingRequestStatus } = {},
) {
  const orgId = opts.organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  if (opts.status) params.set("status", opts.status);
  return callKsm<TrainingRequestResponse[]>(
    `/api/v1/hrm/trainings/requests?${params}`,
    {},
    { session },
  );
}
