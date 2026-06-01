import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type AptitudeResult = "APTE" | "APTE_AVEC_RESTRICTIONS" | "INAPTE_TEMPORAIRE";

export type MedicalVisitResponse = {
  id: string;
  employeeId: string;
  dateVisite: string;
  medecin: string;
  resultatAptitude: AptitudeResult;
  restrictions: string | null;
  prochaineEcheance: string;
  certificatFileId: string | null;
};

export type MedicalCertificateResponse = {
  id: string;
  employeeId: string;
  typeCertificat: string;
  dateEmission: string;
  dateExpiration: string;
  statut: string;
  fichierId: string | null;
};

export type CreateMedicalVisitRequest = {
  employeeId: string;
  dateVisite: string;
  medecin: string;
  resultatAptitude: AptitudeResult;
  restrictions?: string | null;
  prochaineEcheance: string;
  certificatFileId?: string | null;
};

export type CreateMedicalCertificateRequest = {
  employeeId: string;
  typeCertificat: string;
  dateEmission: string;
  dateExpiration: string;
  statut: string;
  fichierId?: string | null;
};

/* -------- Visits -------- */

export function listVisits(session: AppSession, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  return callKsm<MedicalVisitResponse[]>(
    `/api/v1/hrm/medical/visits?${params}`,
    {},
    { session },
  );
}

export function listVisitsByEmployee(employeeId: string, session: AppSession) {
  return callKsm<MedicalVisitResponse[]>(
    `/api/v1/hrm/medical/employees/${employeeId}/visits`,
    {},
    { session },
  );
}

export function getVisit(id: string, session: AppSession) {
  return callKsm<MedicalVisitResponse>(`/api/v1/hrm/medical/visits/${id}`, {}, { session });
}

export function createVisit(body: CreateMedicalVisitRequest, session: AppSession) {
  return callKsm<MedicalVisitResponse>(
    "/api/v1/hrm/medical/visits",
    { method: "POST", body },
    { session },
  );
}

/* -------- Certificates -------- */

export function listCertificates(session: AppSession, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId });
  return callKsm<MedicalCertificateResponse[]>(
    `/api/v1/hrm/medical/certificates?${params}`,
    {},
    { session },
  );
}

export function listCertificatesByEmployee(employeeId: string, session: AppSession) {
  return callKsm<MedicalCertificateResponse[]>(
    `/api/v1/hrm/medical/employees/${employeeId}/certificates`,
    {},
    { session },
  );
}

export function getCertificate(id: string, session: AppSession) {
  return callKsm<MedicalCertificateResponse>(
    `/api/v1/hrm/medical/certificates/${id}`,
    {},
    { session },
  );
}

export function createCertificate(
  body: CreateMedicalCertificateRequest,
  session: AppSession,
) {
  return callKsm<MedicalCertificateResponse>(
    "/api/v1/hrm/medical/certificates",
    { method: "POST", body },
    { session },
  );
}
