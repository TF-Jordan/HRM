import "server-only";

import { decodeJwt } from "jose";
import { getSession } from "@/server/session";
import { getKsmContext } from "@/server/ksm/context";
import { ksmListEmployees } from "@/server/ksm/modules/employees";
import { inferRoleCode, type RoleCode } from "@/lib/roles";

export type ProfileSummary = {
  userId: string;
  actorId: string | null;
  email: string;
  displayName: string;
  firstName: string;
  lastName: string;
  matricule: string | null;
  employeeId: string | null;
  roleCode: RoleCode;
  permissions: string[];
};

/**
 * Server-side variant of GET /api/hrm/me/profile-summary. Used by layout + page
 * components to avoid an extra HTTP round-trip in RSCs.
 */
export async function getProfileSummary(): Promise<ProfileSummary | null> {
  const session = await getSession();
  if (!session) return null;

  let permissions: string[] = [];
  try {
    permissions = (decodeJwt(session.accessToken) as { permissions?: string[] }).permissions ?? [];
  } catch {
    permissions = [];
  }
  const roleCode = inferRoleCode(permissions);

  let firstName = "";
  let lastName = "";
  let matricule: string | null = null;
  let employeeId: string | null = null;

  try {
    const ctx = await getKsmContext();
    const employees = await ksmListEmployees(ctx);
    const me = employees.find((e) => e.actorId === session.user.actorId);
    if (me) {
      const parts = me.actorDisplayName.trim().split(/\s+/);
      firstName = parts[0] ?? "";
      lastName = parts.slice(1).join(" ");
      matricule = me.matricule;
      employeeId = me.id;
    }
  } catch {
    // Admin without employee — fall back to session.
  }

  if (!firstName) {
    const fallback = session.user.displayName || session.user.email || "";
    const local = fallback.includes("@") ? fallback.split("@")[0]! : fallback;
    firstName = local;
  }

  return {
    userId: session.user.userId,
    actorId: session.user.actorId,
    email: session.user.email,
    displayName: session.user.displayName,
    firstName,
    lastName,
    matricule,
    employeeId,
    roleCode,
    permissions,
  };
}
