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
