import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as profileApi from "@/server/ksm/modules/employee-profile";
import * as employeesApi from "@/server/ksm/modules/employees";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

export async function GET() {
  return authenticatedRoute(async (session) => {
    const me = await findMyEmployee(session);
    if (!me) {
      return Response.json({ ok: true, data: null });
    }

    const safe = async <T>(p: Promise<T>, fb: T): Promise<T> => p.catch(() => fb);

    const [profile, personalInfo, dependents, emergencyContacts, timeline] = await Promise.all([
      safe(profileApi.getEmployeeProfile(me.id, session), null),
      safe(profileApi.getPersonalInfo(me.id, session), null),
      safe(employeesApi.listDependents(me.id, session), []),
      safe(profileApi.getEmergencyContacts(me.id, session), []),
      safe(profileApi.getEmployeeTimeline(me.id, session), []),
    ]);

    return Response.json({
      ok: true,
      data: { employee: me, profile, personalInfo, dependents, emergencyContacts, timeline },
    });
  });
}
