import "server-only";

import { handleRoute } from "@/server/api-response";
import { logAuthEvent } from "@/server/auth-flow";
import { readSession, destroySession } from "@/server/session";

export async function POST() {
  return handleRoute(async () => {
    const session = await readSession();
    if (session) {
      logAuthEvent("logout", { userId: session.user.userId });
    }
    await destroySession();
    return Response.json({ ok: true, data: { ok: true } });
  });
}
