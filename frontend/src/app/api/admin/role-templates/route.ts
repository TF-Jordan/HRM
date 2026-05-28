import "server-only";

import { requirePermissionRoute } from "@/server/handlers";
import * as adminApi from "@/server/ksm/modules/admin";

export async function GET() {
  return requirePermissionRoute(
    ["administration:roles:read", "administration:read"],
    async (session) => {
      const data = await adminApi.listRoleTemplates(session);
      return Response.json({ ok: true, data });
    },
  );
}
