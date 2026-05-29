import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type TimesheetStatus = "DRAFT" | "SUBMITTED" | "VALIDATED";

export type TimesheetResponse = {
  id: string;
  employeeId: string;
  periode: string; // YYYY-MM
  heuresNormales: number | string;
  heuresSupplementaires: number | string;
  heuresNuit: number | string;
  heuresWeekend: number | string;
  absencesNonJustifiees: number | string;
  status: TimesheetStatus;
};

export type CreateTimesheetRequest = {
  employeeId: string;
  periode: string;
  heuresNormales: number;
  heuresSupplementaires: number;
  heuresNuit: number;
  heuresWeekend: number;
  absencesNonJustifiees: number;
};

export function createTimesheet(body: CreateTimesheetRequest, session: AppSession) {
  return callKsm<TimesheetResponse>(
    "/api/v1/hrm/timesheets",
    { method: "POST", body },
    { session },
  );
}

export function getTimesheet(timesheetId: string, session: AppSession) {
  return callKsm<TimesheetResponse>(`/api/v1/hrm/timesheets/${timesheetId}`, {}, { session });
}

export function submitTimesheet(timesheetId: string, session: AppSession) {
  return callKsm<TimesheetResponse>(
    `/api/v1/hrm/timesheets/${timesheetId}/submit`,
    { method: "PUT" },
    { session },
  );
}

export function validateTimesheet(timesheetId: string, session: AppSession) {
  return callKsm<TimesheetResponse>(
    `/api/v1/hrm/timesheets/${timesheetId}/validate`,
    { method: "PUT" },
    { session },
  );
}

export function listByEmployee(employeeId: string, periode: string, session: AppSession) {
  return callKsm<TimesheetResponse[]>(
    `/api/v1/hrm/timesheets/employee/${employeeId}?periode=${encodeURIComponent(periode)}`,
    {},
    { session },
  );
}

export function listByOrganization(periode: string, session: AppSession, organizationId?: string) {
  const orgId = organizationId ?? session.workspace?.organizationId;
  if (!orgId) throw new Error("organizationId is required");
  const params = new URLSearchParams({ organizationId: orgId, periode });
  return callKsm<TimesheetResponse[]>(`/api/v1/hrm/timesheets?${params}`, {}, { session });
}
