import "server-only";

import { callKsm } from "@/server/ksm/client";
import type { AppSession } from "@/lib/types/auth";
import type { UserAccountResponse } from "@/server/ksm/modules/auth";

export type RegisterUserBody = {
  actorId: string;
  username: string;
  email: string;
  phoneNumber?: string;
  /** When omitted, auth-core generates a "bootstrap-<uuid>" placeholder.
   *  The BFF should always send a real temp password and set forcePasswordChange. */
  password: string;
  authProvider?: string;
  forcePasswordChange?: boolean;
};

/**
 * Register a brand-new user under the current tenant via auth-core's admin endpoint.
 * Requires the caller to hold canManageIdentity (system/iam/tenant:admin).
 */
export function registerUser(body: RegisterUserBody, session: AppSession) {
  return callKsm<UserAccountResponse>(
    "/api/auth/register",
    {
      method: "POST",
      body: {
        actorId: body.actorId,
        username: body.username,
        email: body.email,
        phoneNumber: body.phoneNumber ?? null,
        password: body.password,
        authProvider: body.authProvider ?? "LOCAL",
        forcePasswordChange: body.forcePasswordChange ?? false,
      },
    },
    { session },
  );
}

/**
 * Cryptographically random temporary password meeting the strong-policy
 * required by the change-password schema (≥8 chars, upper+lower+digit+special).
 */
export function generateTemporaryPassword(length = 14): string {
  const upper = "ABCDEFGHJKLMNPQRSTUVWXYZ"; // no I, O
  const lower = "abcdefghijkmnopqrstuvwxyz"; // no l
  const digits = "23456789"; // no 0, 1
  const specials = "!@#$%^&*_-+=?";
  const all = upper + lower + digits + specials;

  function pick(pool: string): string {
    const idx = Math.floor((globalThis.crypto?.getRandomValues(new Uint32Array(1))[0] ?? 0) / (0xffffffff / pool.length));
    return pool[Math.min(pool.length - 1, idx)]!;
  }

  // Guarantee at least one char from each class, then fill the rest randomly.
  const required = [pick(upper), pick(lower), pick(digits), pick(specials)];
  const rest: string[] = [];
  for (let i = required.length; i < length; i++) rest.push(pick(all));

  // Fisher–Yates shuffle
  const chars = [...required, ...rest];
  for (let i = chars.length - 1; i > 0; i--) {
    const j = Math.floor((globalThis.crypto?.getRandomValues(new Uint32Array(1))[0] ?? 0) / (0xffffffff / (i + 1)));
    [chars[i], chars[j]] = [chars[j]!, chars[i]!];
  }
  return chars.join("");
}
