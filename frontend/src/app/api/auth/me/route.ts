import "server-only";

import { handleRoute } from "@/server/api-response";
import { readSession } from "@/server/session";

export async function GET() {
  return handleRoute(async () => {
    const session = await readSession();
    if (!session) {
      return Response.json(
        { ok: false, status: 401, errorCode: "UNAUTHORIZED", message: "Not authenticated" },
        { status: 401 },
      );
    }
    return Response.json({
      ok: true,
      data: {
        user: session.user,
        workspace: session.workspace,
        forcePasswordChange: session.forcePasswordChange === true,
        expiresAt: session.expiresAt,
      },
    });
  });
}
