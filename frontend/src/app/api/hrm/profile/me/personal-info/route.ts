import "server-only";

import { authenticatedRoute } from "@/server/handlers";
import * as profileApi from "@/server/ksm/modules/employee-profile";

export async function PUT(request: Request) {
  return authenticatedRoute(async (session) => {
    const body = await request.json();
    const data = await profileApi.upsertMyPersonalInfo(body, session);
    return Response.json({ ok: true, data });
  });
}
