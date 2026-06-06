import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as profileApi from "@/server/ksm/modules/employee-profile";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

export async function GET() {
  return authenticatedRoute(async (session) => {
    const me = await findMyEmployee(session);
    if (!me) return Response.json({ ok: true, data: [] });
    const data = await profileApi.getEmergencyContacts(me.id, session);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: Request) {
  return authenticatedRoute(async (session) => {
    const body = await request.json();
    const data = await profileApi.addMyEmergencyContact(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
