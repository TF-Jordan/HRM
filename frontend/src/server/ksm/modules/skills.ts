import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";

export type SkillResponse = {
  id: string;
  name: string;
  categorie?: string | null;
  description?: string | null;
};

export type EmployeeSkillResponse = {
  id: string;
  employeeId: string;
  skillId: string;
  niveauActuel: number;
  niveauAttendu: number;
  dateEvaluation?: string | null;
};

/** Employee skill enriched with the referential name + category (BFF-side join). */
export type EmployeeSkillView = EmployeeSkillResponse & {
  skillName: string;
  skillCategory?: string | null;
};

export type ReviewResponse = {
  id: string;
  employeeId: string;
  evaluateurDisplayName?: string | null;
  periode: string;
  noteGlobale?: number | string | null;
  status: string;
};

export type EnrollmentResponse = {
  id: string;
  trainingId: string;
  employeeId: string;
  status: string;
  noteEvaluation?: number | string | null;
  attestationFileId?: string | null;
};

export function listSkills(session: AppSession) {
  return callKsm<SkillResponse[]>("/api/v1/hrm/skills", {}, { session });
}

export function listEmployeeSkills(employeeId: string, session: AppSession) {
  return callKsm<EmployeeSkillResponse[]>(
    `/api/v1/hrm/skills/employees/${employeeId}/skills`,
    {},
    { session },
  );
}

/** Join employee skills with the skills referential to expose human names. */
export async function listEmployeeSkillsEnriched(
  employeeId: string,
  session: AppSession,
): Promise<EmployeeSkillView[]> {
  const [employeeSkills, referential] = await Promise.all([
    listEmployeeSkills(employeeId, session),
    listSkills(session).catch(() => [] as SkillResponse[]),
  ]);
  const byId = new Map(referential.map((s) => [s.id, s]));
  return employeeSkills.map((es) => {
    const skill = byId.get(es.skillId);
    return {
      ...es,
      skillName: skill?.name ?? es.skillId.slice(0, 8),
      skillCategory: skill?.categorie ?? null,
    };
  });
}

export function listEmployeeReviews(employeeId: string, session: AppSession) {
  return callKsm<ReviewResponse[]>(
    `/api/v1/hrm/reviews/employee/${employeeId}`,
    {},
    { session },
  );
}

export function listEmployeeEnrollments(employeeId: string, session: AppSession) {
  return callKsm<EnrollmentResponse[]>(
    `/api/v1/hrm/trainings/enrollments/employee/${employeeId}`,
    {},
    { session },
  );
}
