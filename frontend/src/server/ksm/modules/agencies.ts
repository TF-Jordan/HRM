import "server-only";

import { callKsm } from "../client";
import type { KsmCallContext } from "../context";
import type { Agency } from "@/lib/types/hrm/agency";

/**
 * Read-only proxy to the organization service's agency listing.
 * Used to resolve agencyId → human-readable site name in HRM views.
 */
export async function ksmListAgencies(ctx: KsmCallContext): Promise<Agency[]> {
  return callKsm<Agency[]>(`/api/organizations/${ctx.organizationId}/agencies`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
