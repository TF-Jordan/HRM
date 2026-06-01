import "server-only";

import { decodeJwt } from "jose";

export type DecodedKsmClaims = {
  sub?: string;
  userId?: string;
  actorId?: string;
  tenantId?: string;
  organizationId?: string;
  agencyId?: string;
  email?: string;
  permissions?: string[];
  roles?: string[];
  exp?: number;
  iat?: number;
};

/**
 * Decode a KSM JWT without verifying the signature.
 * The signature is already verified by KSM on every API call —
 * the BFF only needs the claims for context propagation.
 */
export function decodeKsmJwt(token: string): DecodedKsmClaims {
  try {
    return decodeJwt(token) as DecodedKsmClaims;
  } catch {
    return {};
  }
}
