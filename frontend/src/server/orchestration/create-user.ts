import "server-only";

import type { AppSession } from "@/lib/types/auth";
import { logger } from "@/server/logger";
import { createActor } from "@/server/ksm/modules/actors";
import { assignRole } from "@/server/ksm/modules/admin";
import { generateTemporaryPassword, registerUser } from "@/server/ksm/modules/users";

export type CreateUserInput = {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  gender?: string;
  nationality?: string;
  birthDate?: string;
  /** Role IDs to assign with their target scope. */
  assignments: Array<{
    roleId: string;
    scope: string;
    scopeType?: "SYSTEM" | "TENANT" | "ORGANIZATION" | "AGENCY";
    scopeId?: string | null;
  }>;
  /** Force the user to change the auto-generated temp password at first login. */
  forcePasswordChange?: boolean;
};

export type CreateUserResult = {
  actorId: string;
  userId: string;
  username: string;
  email: string;
  temporaryPassword: string;
  rolesAssigned: number;
  warnings: string[];
};

/**
 * Orchestrate the multi-core flow that materialises a brand-new application user.
 *
 *   POST /api/actors                       → create the human identity (actor-core)
 *   POST /api/auth/register                → create the credentials (auth-core),
 *                                            tagged forcePasswordChange = true
 *   POST /api/administration/users/{id}/roles  → attach every requested role
 *
 * The temporary password is returned to the caller exactly once. The caller is
 * responsible for delivering it (mail, SMS, manual handover).
 *
 * If a step beyond actor + auth creation fails, the user still exists and is
 * functional — we surface a `warnings` array describing what was skipped.
 */
export async function createUserOrchestrated(
  input: CreateUserInput,
  session: AppSession,
): Promise<CreateUserResult> {
  const warnings: string[] = [];
  const temporaryPassword = generateTemporaryPassword();
  const forcePasswordChange = input.forcePasswordChange ?? true;
  const username = input.email.trim().toLowerCase();

  // 1. Actor
  const actor = await createActor(
    {
      firstName: input.firstName.trim(),
      lastName: input.lastName.trim(),
      email: input.email.trim().toLowerCase(),
      phoneNumber: input.phoneNumber?.trim() || undefined,
      gender: input.gender,
      nationality: input.nationality,
      birthDate: input.birthDate,
    },
    session,
  );
  logger.info({ actorId: actor.id }, "orchestration.actor_created");

  // 2. User account
  const user = await registerUser(
    {
      actorId: actor.id,
      username,
      email: input.email.trim().toLowerCase(),
      phoneNumber: input.phoneNumber?.trim() || undefined,
      password: temporaryPassword,
      authProvider: "LOCAL",
      forcePasswordChange,
    },
    session,
  );
  logger.info({ userId: user.id, actorId: actor.id }, "orchestration.user_registered");

  // 3. Roles (best-effort — failure here is reported but doesn't unwind the user)
  let rolesAssigned = 0;
  for (const assignment of input.assignments) {
    try {
      await assignRole(
        user.id,
        {
          roleId: assignment.roleId,
          scope: assignment.scope,
          scopeType: assignment.scopeType,
          scopeId: assignment.scopeId,
        },
        session,
      );
      rolesAssigned += 1;
    } catch (cause) {
      logger.error(
        { userId: user.id, roleId: assignment.roleId, cause: String(cause) },
        "orchestration.role_assignment_failed",
      );
      warnings.push(`Role assignment failed for ${assignment.roleId}: ${(cause as Error).message}`);
    }
  }

  return {
    actorId: actor.id,
    userId: user.id,
    username,
    email: input.email.trim().toLowerCase(),
    temporaryPassword,
    rolesAssigned,
    warnings,
  };
}
