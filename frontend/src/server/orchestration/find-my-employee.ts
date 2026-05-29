import "server-only";

import type { AppSession } from "@/lib/types/auth";
import { listEmployees, type EmployeeResponse } from "@/server/ksm/modules/employees";

/**
 * Find the Employee record that belongs to the currently authenticated actor.
 *
 * Required for every self-service screen (/leaves/my, /payroll/my, etc.):
 * the session carries the actorId of the human behind the user account, but
 * HRM endpoints are keyed by employeeId. We resolve the link on demand by
 * scanning the tenant's employees and matching on actorId.
 *
 * Returns null when the authenticated user has no Employee record yet
 * (e.g. SuperAdmin who is purely administrative).
 */
export async function findMyEmployee(session: AppSession): Promise<EmployeeResponse | null> {
  const orgId = session.workspace?.organizationId;
  if (!orgId || !session.user.actorId) return null;
  try {
    const all = await listEmployees(session, { organizationId: orgId });
    return all.find((e) => e.actorId === session.user.actorId) ?? null;
  } catch {
    return null;
  }
}
