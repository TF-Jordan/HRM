import "server-only";

import { requirePermissionRoute } from "@/server/handlers";
import * as adminApi from "@/server/ksm/modules/admin";

export async function POST() {
  return requirePermissionRoute("administration:roles:write", async (session) => {
    const data = await adminApi.provisionDefaultRoles(session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
