import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as profileApi from "@/server/ksm/modules/employee-profile";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

export async function PUT(request: Request) {
  return authenticatedRoute(async (session) => {
    const me = await findMyEmployee(session);
    if (!me) {
      return Response.json({ ok: false, status: 404, errorCode: "NOT_FOUND", message: "Employee not found" }, { status: 404 });
    }
    const body = await request.json();
    const data = await profileApi.upsertPersonalInfo(me.id, body, session);
    return Response.json({ ok: true, data });
  });
}
