import "server-only";

import type { AppSession } from "@/lib/types/auth";
import { logger } from "@/server/logger";
import { sendMail } from "@/server/email/mailer";
import { welcomeMail } from "@/server/email/templates";
import { createActor } from "@/server/ksm/modules/actors";
import { assignRole, listRoles } from "@/server/ksm/modules/admin";
import {
  addDependent,
  createEmployee,
  type EmployeeResponse,
  type MobileOperator,
  type PaymentChannel,
} from "@/server/ksm/modules/employees";
import { upsertPersonalInfo } from "@/server/ksm/modules/employee-profile";
import { listDocumentSequences, upsertDocumentSequence } from "@/server/ksm/modules/settings";
import { generateTemporaryPassword, registerUser } from "@/server/ksm/modules/users";

/** Document-numbering type the payroll/HR matricule allocator reads (settings-core). */
const MATRICULE_DOCUMENT_TYPE = "HRM_MATRICULE";

/**
 * Ensure the organisation has an HRM_MATRICULE document sequence before creating an
 * employee. KSM allocates the employee code via `settingsPort.generateMatricule`, which
 * throws `DocumentSequenceNotFoundException` (HTTP 500) when no sequence exists — the case
 * for any freshly-created organisation (the demo seed only provisions it for the demo org).
 *
 * Idempotent: we only create the sequence when absent (an unconditional upsert would reset
 * `nextNumber` and cause matricule collisions). Best-effort — if it fails, employee creation
 * proceeds and surfaces the original error.
 */
async function ensureMatriculeSequence(session: AppSession, organizationId: string): Promise<void> {
  try {
    const sequences = await listDocumentSequences(organizationId, session);
    if (sequences.some((s) => s.documentType === MATRICULE_DOCUMENT_TYPE)) return;
    await upsertDocumentSequence(
      {
        organizationId,
        agencyId: null,
        documentType: MATRICULE_DOCUMENT_TYPE,
        prefix: "EMP",
        suffix: null,
        paddingWidth: 6,
        nextNumber: 1,
      },
      session,
    );
    logger.info({ organizationId }, "orchestration.matricule_sequence_provisioned");
  } catch (cause) {
    logger.error(
      { organizationId, cause: String(cause) },
      "orchestration.matricule_sequence_ensure_failed",
    );
  }
}

/** Marital status codes understood by the payroll engine (IRPP family quotient). */
export type MaritalStatus = "SINGLE" | "MARRIED" | "DIVORCED" | "WIDOWED";

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
  /** Marital status — feeds the IRPP family quotient on payroll. */
  situationMatrimoniale?: MaritalStatus;
  /** Dependents captured at hire — feed the IRPP family quotient on payroll. */
  dependents?: Array<{
    prenom: string;
    nom: string;
    dateNaissance: string;
    lienParente: string;
  }>;
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
  /** Login account info — only present when a login was actually provisioned. */
  login?: {
    userId: string;
    username: string;
    email: string;
    temporaryPassword: string;
    rolesAssigned: number;
    welcomeMailSent: boolean;
    welcomeMailProvider: string;
  };
  /**
   * Set when login provisioning was requested but deliberately skipped because
   * the caller lacks the identity privilege (`tenant:admin`). The employee record
   * is created; the SuperAdmin must create the login separately. This is the
   * platform's intended split: only a tenant admin provisions credentials.
   */
  loginSkipped?: "forbidden";
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

  // Ensure the organisation can allocate a matricule (provisions the HRM_MATRICULE
  // sequence on first hire for freshly-created orgs). Best-effort.
  if (session.workspace?.organizationId) {
    await ensureMatriculeSequence(session, session.workspace.organizationId);
  }

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

    // 2.b Personal info (marital status) — drives the IRPP family quotient on
    // payroll. Best-effort: a failure must not roll back the hire.
    if (input.situationMatrimoniale) {
      try {
        await upsertPersonalInfo(
          employee.id,
          { situationMatrimoniale: input.situationMatrimoniale },
          session,
        );
      } catch (cause) {
        logger.error(
          { employeeId: employee.id, cause: String(cause) },
          "orchestration.employee_personal_info_failed",
        );
        warnings.push(`Marital status not saved: ${errorMessage(cause)}`);
      }
    }

    // 2.c Dependents — also feed the IRPP family quotient. Best-effort: a
    // failure on one dependent must not roll back the hire.
    if (input.dependents && input.dependents.length > 0) {
      for (const dep of input.dependents) {
        try {
          await addDependent(
            employee.id,
            {
              prenom: dep.prenom.trim(),
              nom: dep.nom.trim(),
              dateNaissance: dep.dateNaissance,
              lienParente: dep.lienParente,
            },
            session,
          );
        } catch (cause) {
          logger.error(
            { employeeId: employee.id, cause: String(cause) },
            "orchestration.employee_dependent_failed",
          );
          warnings.push(
            `Dependent ${dep.prenom} ${dep.nom} not saved: ${errorMessage(cause)}`,
          );
        }
      }
    }
  } catch (cause) {
    logger.error(
      { actorId: actor.id, cause: String(cause) },
      "orchestration.employee_creation_failed",
    );
    throw cause;
  }

  // 3. Login provisioning (best-effort).
  // Only a tenant admin (SUPER_ADMIN) may create login accounts — `/api/auth/register`
  // is gated by `canManageIdentity` = {system:admin, iam:admin, tenant:admin}. An HR
  // admin (DRH) can create the employee record but NOT its credentials, so we skip the
  // step cleanly instead of letting it 403 mid-flow. The SuperAdmin provisions the login.
  const provisionLogin = input.provisionLogin ?? true;
  const sendWelcomeEmail = input.sendWelcomeEmail ?? true;
  let login: CreateEmployeeOrchestratedResult["login"];
  let loginSkipped: CreateEmployeeOrchestratedResult["loginSkipped"];

  if (provisionLogin && !canProvisionLogins(session)) {
    loginSkipped = "forbidden";
    logger.info({ actorId: actor.id }, "orchestration.employee_login_skipped_forbidden");
  } else if (provisionLogin) {
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
    loginSkipped,
    warnings,
  };
}

/** Identity permissions that authorise creating a login account (`/api/auth/register`). */
const IDENTITY_PERMISSIONS = ["tenant:admin", "system:admin", "iam:admin"];

function canProvisionLogins(session: AppSession): boolean {
  const owned = new Set((session.user.permissions ?? []).map((p) => p.split("#")[0] ?? p));
  return IDENTITY_PERMISSIONS.some((p) => owned.has(p));
}

function errorMessage(cause: unknown): string {
  if (cause instanceof Error) return cause.message;
  return String(cause);
}
