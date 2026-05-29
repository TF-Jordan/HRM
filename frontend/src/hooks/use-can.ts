"use client";

import { useSession } from "@/components/providers/session-provider";

/**
 * Returns true if the current session holds the given HRM/admin permission(s).
 * Pass a single permission code (e.g. "hrm:employee:create") or an array of codes
 * where ANY match grants access.
 *
 * Permissions are also enforced server-side by KSM — this hook only governs UI
 * (hiding buttons / nav items / sections).
 */
/** Strip the `#SCOPE:<id>` suffix KSM appends to scoped permissions. */
function basePerm(p: string): string {
  return p.split("#")[0] ?? p;
}

export function useCan(required: string | string[]): boolean {
  const { session } = useSession();
  if (!session) return false;
  const owned = new Set((session.user.permissions ?? []).map(basePerm));
  const list = Array.isArray(required) ? required : [required];
  return list.some((perm) => owned.has(perm));
}

/** True only if the user holds ALL the listed permissions. */
export function useCanAll(required: string[]): boolean {
  const { session } = useSession();
  if (!session) return false;
  const owned = new Set((session.user.permissions ?? []).map(basePerm));
  return required.every((perm) => owned.has(perm));
}

/** True if the user has at least one of the listed role codes. */
export function useHasRole(roles: string | string[]): boolean {
  const { session } = useSession();
  if (!session) return false;
  const owned = new Set(session.user.roles ?? []);
  const list = Array.isArray(roles) ? roles : [roles];
  return list.some((role) => owned.has(role));
}
