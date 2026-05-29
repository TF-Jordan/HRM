import "server-only";

import type { AppSession } from "@/lib/types/auth";

/**
 * KSM serialises a user's granted permissions with a `#SCOPE:<id>` suffix when
 * the grant is scoped (e.g. `hrm:expense:read#ORGANIZATION:<uuid>`). Server-side
 * code only needs to know whether the user holds the permission at all — the
 * scope check itself is enforced by KSM. Compare on the bare code.
 */
export function hasPermission(session: AppSession, code: string | string[]): boolean {
  const owned = new Set(
    (session.user.permissions ?? []).map((p) => p.split("#")[0] ?? p),
  );
  const list = Array.isArray(code) ? code : [code];
  return list.some((c) => owned.has(c));
}
