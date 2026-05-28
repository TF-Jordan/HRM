import "server-only";

import { handleRoute } from "@/server/api-response";
import { readSession } from "@/server/session";
import type { AppSession } from "@/lib/types/auth";

/**
 * Wraps a Route Handler with: authentication guard + standard error mapping.
 * The inner function receives the live session, guaranteed non-null.
 *
 *   export async function GET() {
 *     return authenticatedRoute(async (session) => myModule.something(session));
 *   }
 */
export function authenticatedRoute<T>(
  handler: (session: AppSession) => Promise<T> | T,
): Promise<Response> {
  return handleRoute(async () => {
    const session = await readSession();
    if (!session) {
      return Response.json(
        {
          ok: false,
          status: 401,
          errorCode: "UNAUTHORIZED",
          message: "Not authenticated",
        },
        { status: 401 },
      );
    }
    return handler(session);
  });
}

/**
 * Same as authenticatedRoute but also asserts the session holds at least one of
 * the required permission codes (the same check is enforced server-side by KSM).
 */
export function requirePermissionRoute<T>(
  required: string | string[],
  handler: (session: AppSession) => Promise<T> | T,
): Promise<Response> {
  const list = Array.isArray(required) ? required : [required];
  return authenticatedRoute(async (session) => {
    const owned = new Set(session.user.permissions);
    if (!list.some((perm) => owned.has(perm))) {
      return Response.json(
        {
          ok: false,
          status: 403,
          errorCode: "FORBIDDEN",
          message: `Missing one of: ${list.join(", ")}`,
        },
        { status: 403 },
      );
    }
    return handler(session);
  });
}
