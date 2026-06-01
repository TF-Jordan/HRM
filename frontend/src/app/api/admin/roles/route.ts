import "server-only";

import { requirePermissionRoute } from "@/server/handlers";
import * as adminApi from "@/server/ksm/modules/admin";

export async function GET() {
  return requirePermissionRoute(
    ["administration:roles:read", "administration:roles:write"],
    async (session) => {
      const data = await adminApi.listRoles(session);
      return Response.json({ ok: true, data });
    },
  );
}
