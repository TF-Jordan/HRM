import "server-only";

import type { AppSession } from "@/lib/types/auth";
import { logger } from "@/server/logger";
import { sendMail } from "@/server/email/mailer";
import { welcomeMail } from "@/server/email/templates";
import { createActor } from "@/server/ksm/modules/actors";
import { assignRole, listRoles } from "@/server/ksm/modules/admin";
import {
  createEmployee,
  type EmployeeResponse,
  type MobileOperator,
  type PaymentChannel,
} from "@/server/ksm/modules/employees";
import { generateTemporaryPassword, registerUser } from "@/server/ksm/modules/users";

export type CreateEmployeeOrchestratedInput = {
  // Identity (Actor)
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  gender?: string;
  nationality?: string;
  birthDate?: string;
  // HR core data
  numCnps?: string;
  categorie: number;
  echelon?: string;
  dateEmbauche: string;
  departmentCode?: string;
  // Payment
  modePaiement?: PaymentChannel;
  compteBancaire?: string;
  numMobileMoney?: string;
  operateurMm?: MobileOperator;
  // Login provisioning (defaults: provision = true, sendWelcome = true)
  provisionLogin?: boolean;
  sendWelcomeEmail?: boolean;
  /** Locale used to render the welcome email. Defaults to "fr". */
  locale?: "fr" | "en";
};

export type CreateEmployeeOrchestratedResult = {
  actorId: string;
  employeeId: string;
  matricule: string;
  status: string;
  employee: EmployeeResponse;
  /** Login account info — only present when provisionLogin = true. */
  login?: {
    userId: string;
    username: string;
    email: string;
    temporaryPassword: string;
    rolesAssigned: number;
    welcomeMailSent: boolean;
    welcomeMailProvider: string;
  };
  warnings: string[];
};

/**
 * Multi-core orchestration for hiring a new employee:
 *
 *   POST /api/actors                          → actor-core (identity)
 *   POST /api/v1/hrm/employees                → hrm-core (employee + initial contract
 *                                                + ANNUAL and SICK leave balances)
 *   POST /api/auth/register                   → auth-core (login account, temp password)
 *   POST /api/administration/users/{id}/roles → roles-core (EMPLOYEE @ organization)
 *   sendMail(welcome)                         → BFF mailer (smtp / resend / none)
 *
 * Steps after employee creation are best-effort: failure to provision a login or
 * send the welcome mail does NOT roll back the employee, it just surfaces in
 * `warnings` so the caller can retry from the admin UI.
 */
export async function createEmployeeOrchestrated(
  input: CreateEmployeeOrchestratedInput,
  session: AppSession,
): Promise<CreateEmployeeOrchestratedResult> {
  const warnings: string[] = [];

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
  logger.info({ actorId: actor.id }, "orchestration.employee_actor_created");

  // 2. Employee
  let employee: EmployeeResponse;
  try {
    employee = await createEmployee(
      {
        actorId: actor.id,
        numCnps: input.numCnps?.trim() || undefined,
        categorie: input.categorie,
        echelon: input.echelon?.trim() || undefined,
        dateEmbauche: input.dateEmbauche,
        departmentCode: input.departmentCode?.trim() || undefined,
        modePaiement: input.modePaiement,
        compteBancaire: input.compteBancaire?.trim() || undefined,
        numMobileMoney: input.numMobileMoney?.trim() || undefined,
        operateurMm: input.operateurMm,
      },
      session,
    );
    logger.info(
      { employeeId: employee.id, actorId: actor.id, matricule: employee.matricule },
      "orchestration.employee_created",
    );
  } catch (cause) {
    logger.error(
      { actorId: actor.id, cause: String(cause) },
      "orchestration.employee_creation_failed",
    );
    throw cause;
  }

  // 3. Login provisioning (best-effort).
  const provisionLogin = input.provisionLogin ?? true;
  const sendWelcomeEmail = input.sendWelcomeEmail ?? true;
  let login: CreateEmployeeOrchestratedResult["login"];

  if (provisionLogin) {
    const username = input.email.trim().toLowerCase();
    const temporaryPassword = generateTemporaryPassword();

    try {
      // NOTE: the phone number is intentionally NOT forwarded to the login
      // account. It already lives on the Actor (identity), and auth-core enforces
      // phone uniqueness on accounts — forwarding it would make account
      // provisioning fail with "Phone number is already used." whenever a number
      // is reused (common in dev/testing). The login is keyed by email/username.
      const user = await registerUser(
        {
          actorId: actor.id,
          username,
          email: username,
          password: temporaryPassword,
          authProvider: "LOCAL",
          forcePasswordChange: true,
        },
        session,
      );
      logger.info({ userId: user.id, actorId: actor.id }, "orchestration.employee_user_registered");

      // 4. Assign EMPLOYEE role @ ORGANIZATION
      let rolesAssigned = 0;
      const orgId = session.workspace?.organizationId;
      if (orgId) {
        try {
          const roles = await listRoles(session);
          const employeeRole = roles.find((r) => r.code === "EMPLOYEE");
          if (employeeRole) {
            await assignRole(
              user.id,
              {
                roleId: employeeRole.id,
                scope: `ORGANIZATION:${orgId}`,
                scopeType: "ORGANIZATION",
                scopeId: orgId,
              },
              session,
            );
            rolesAssigned = 1;
          } else {
            warnings.push("EMPLOYEE role not found in tenant — login account created without role.");
          }
        } catch (cause) {
          logger.error(
            { userId: user.id, cause: String(cause) },
            "orchestration.employee_role_assignment_failed",
          );
          warnings.push(`Role assignment failed: ${errorMessage(cause)}`);
        }
      } else {
        warnings.push("No organization in session — EMPLOYEE role not assigned.");
      }

      // 5. Welcome email (best-effort).
      let welcomeMailSent = false;
      let welcomeMailProvider = "skipped";
      if (sendWelcomeEmail) {
        const mail = welcomeMail({
          firstName: input.firstName.trim(),
          lastName: input.lastName.trim(),
          email: username,
          matricule: employee.matricule,
          temporaryPassword,
          organizationName: session.workspace?.organizationName ?? "HR Core",
          locale: input.locale ?? "fr",
        });
        const result = await sendMail({ to: username, ...mail });
        welcomeMailSent = result.ok;
        welcomeMailProvider = result.provider;
        if (!result.ok) {
          warnings.push(`Welcome email failed (${result.provider}): ${result.error}`);
        }
      }

      login = {
        userId: user.id,
        username,
        email: username,
        temporaryPassword,
        rolesAssigned,
        welcomeMailSent,
        welcomeMailProvider,
      };
    } catch (cause) {
      logger.error(
        { actorId: actor.id, cause: String(cause) },
        "orchestration.employee_login_provisioning_failed",
      );
      warnings.push(`Login provisioning failed: ${errorMessage(cause)}`);
    }
  }

  return {
    actorId: actor.id,
    employeeId: employee.id,
    matricule: employee.matricule,
    status: employee.status,
    employee,
    login,
    warnings,
  };
}

function errorMessage(cause: unknown): string {
  if (cause instanceof Error) return cause.message;
  return String(cause);
}
