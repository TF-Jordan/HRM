import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

/* Rich 360° profile endpoint — fuses Employee + Actor personal identity + manager. */

export type EmployeeProfileResponse = {
  id: string;
  organizationId: string;
  agencyId?: string | null;
  actorId: string;
  managerId?: string | null;
  matricule: string;
  numCnps?: string | null;
  categorie: number;
  echelon?: string | null;
  dateEmbauche: string;
  status: "ACTIVE" | "ON_LEAVE" | "SUSPENDED" | "TERMINATED";
  departmentCode?: string | null;
  modePaiement?: string | null;
  compteBancaire?: string | null;
  numMobileMoney?: string | null;
  operateurMm?: string | null;
  actorDisplayName?: string | null;

  // Actor personal identity
  actorFirstName?: string | null;
  actorLastName?: string | null;
  actorEmail?: string | null;
  actorPhoneNumber?: string | null;
  actorGender?: string | null;
  actorNationality?: string | null;
  actorBirthDate?: string | null;
  actorPhotoUri?: string | null;

  // Hierarchy
  managerDisplayName?: string | null;
};

export type TimelineEventResponse = {
  type: "HIRE" | "CONTRACT" | "REVIEW" | string;
  date: string;
  title: string;
  detail?: string | null;
};

export function getEmployeeProfile(employeeId: string, session: AppSession) {
  return callKsm<EmployeeProfileResponse>(
    `/api/v1/hrm/employees/${employeeId}/profile`,
    {},
    { session },
  );
}

export function getEmployeeTimeline(employeeId: string, session: AppSession) {
  return callKsm<TimelineEventResponse[]>(
    `/api/v1/hrm/employees/${employeeId}/timeline`,
    {},
    { session },
  );
}

/* ── Personal info (1:1 extended row) ─────────────────────────────────── */

export type PersonalInfoResponse = {
  id: string;
  employeeId: string;
  lieuNaissance?: string | null;
  situationMatrimoniale?: string | null;
  typePiece?: string | null;
  numeroPiece?: string | null;
  dateEmissionPiece?: string | null;
  niuFiscal?: string | null;
  permisConduire?: string | null;
  languesParlees?: string | null;
  emailPersonnel?: string | null;
  telephoneDomicile?: string | null;
  whatsapp?: string | null;
  adressePostale?: string | null;
  adresseDomicile?: string | null;
  ville?: string | null;
  region?: string | null;
  codePostal?: string | null;
};

export type UpsertPersonalInfoRequest = Partial<Omit<PersonalInfoResponse, "id" | "employeeId">>;

export function getPersonalInfo(employeeId: string, session: AppSession) {
  return callKsm<PersonalInfoResponse | null>(
    `/api/v1/hrm/employees/${employeeId}/personal-info`,
    {},
    { session },
  );
}

export function upsertPersonalInfo(
  employeeId: string,
  body: UpsertPersonalInfoRequest,
  session: AppSession,
) {
  return callKsm<PersonalInfoResponse>(
    `/api/v1/hrm/employees/${employeeId}/personal-info`,
    { method: "PUT", body },
    { session },
  );
}

/* Self-service: the caller edits their OWN record (guarded by user context, no HR permission). */
export function upsertMyPersonalInfo(body: UpsertPersonalInfoRequest, session: AppSession) {
  return callKsm<PersonalInfoResponse>(
    `/api/v1/hrm/employees/me/personal-info`,
    { method: "PUT", body },
    { session },
  );
}

/* ── Emergency contacts ────────────────────────────────────────────────── */

export type EmergencyContactResponse = {
  id: string;
  employeeId: string;
  nom: string;
  prenom?: string | null;
  relation?: string | null;
  telephone?: string | null;
  email?: string | null;
  priorite: number;
};

export type AddEmergencyContactRequest = {
  nom: string;
  prenom?: string;
  relation?: string;
  telephone?: string;
  email?: string;
  priorite: number;
};

export type UpdateEmergencyContactRequest = Partial<AddEmergencyContactRequest>;

export function getEmergencyContacts(employeeId: string, session: AppSession) {
  return callKsm<EmergencyContactResponse[]>(
    `/api/v1/hrm/employees/${employeeId}/emergency-contacts`,
    {},
    { session },
  );
}

export function addEmergencyContact(
  employeeId: string,
  body: AddEmergencyContactRequest,
  session: AppSession,
) {
  return callKsm<EmergencyContactResponse>(
    `/api/v1/hrm/employees/${employeeId}/emergency-contacts`,
    { method: "POST", body },
    { session },
  );
}

export function updateEmergencyContact(
  employeeId: string,
  contactId: string,
  body: UpdateEmergencyContactRequest,
  session: AppSession,
) {
  return callKsm<EmergencyContactResponse>(
    `/api/v1/hrm/employees/${employeeId}/emergency-contacts/${contactId}`,
    { method: "PATCH", body },
    { session },
  );
}

export function deleteEmergencyContact(
  employeeId: string,
  contactId: string,
  session: AppSession,
) {
  return callKsm<void>(
    `/api/v1/hrm/employees/${employeeId}/emergency-contacts/${contactId}`,
    { method: "DELETE" },
    { session },
  );
}

/* Self-service emergency contacts: scoped to the caller's own employee record. */
export function addMyEmergencyContact(body: AddEmergencyContactRequest, session: AppSession) {
  return callKsm<EmergencyContactResponse>(
    `/api/v1/hrm/employees/me/emergency-contacts`,
    { method: "POST", body },
    { session },
  );
}

export function updateMyEmergencyContact(
  contactId: string,
  body: UpdateEmergencyContactRequest,
  session: AppSession,
) {
  return callKsm<EmergencyContactResponse>(
    `/api/v1/hrm/employees/me/emergency-contacts/${contactId}`,
    { method: "PATCH", body },
    { session },
  );
}

export function deleteMyEmergencyContact(contactId: string, session: AppSession) {
  return callKsm<void>(
    `/api/v1/hrm/employees/me/emergency-contacts/${contactId}`,
    { method: "DELETE" },
    { session },
  );
}
