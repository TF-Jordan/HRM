import "server-only";

import { requireSession } from "@/server/session";

export type KsmCallContext = {
  tenantId: string;
  organizationId: string;
  agencyId: string | null;
  bearer: string;
};

/**
 * Reads the BFF session and produces the headers needed to call any KSM
 * endpoint scoped to the current tenant + organisation.
 * Throws if the session is missing or has no organisation context.
 */
export async function getKsmContext(): Promise<KsmCallContext> {
  const session = await requireSession();
  if (!session.context.organizationId) {
    throw new Error("No organisation context");
  }
  return {
    tenantId: session.context.tenantId,
    organizationId: session.context.organizationId,
    agencyId: session.context.agencyId ?? null,
    bearer: session.accessToken,
  };
}
