import "server-only";

import type { AppSession } from "@/lib/types/auth";
import { logger } from "@/server/logger";
import { createActor } from "@/server/ksm/modules/actors";
import {
  createEmployee,
  type ContractType,
  type EmployeeResponse,
  type MobileOperator,
  type PaymentChannel,
} from "@/server/ksm/modules/employees";

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
  // Initial contract
  contractType: ContractType;
  contractDateDebut: string;
  contractDateFin?: string;
  salaireBase: number;
  avantagesNature?: number;
  periodeEssai?: number;
};

export type CreateEmployeeOrchestratedResult = {
  actorId: string;
  employeeId: string;
  matricule: string;
  status: string;
  employee: EmployeeResponse;
};

/**
 * Multi-core orchestration for hiring a new employee:
 *
 *   POST /api/actors                          → actor-core (identity)
 *   POST /api/v1/hrm/employees                → hrm-core (employee + initial contract
 *                                                + ANNUAL and SICK leave balances)
 *
 * HRM rejects creating two Employees pointing at the same Actor in the same
 * tenant (DuplicateEmployeeException). When that happens the Actor we just
 * created is orphaned — the caller is warned.
 */
export async function createEmployeeOrchestrated(
  input: CreateEmployeeOrchestratedInput,
  session: AppSession,
): Promise<CreateEmployeeOrchestratedResult> {
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
  try {
    const employee = await createEmployee(
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
        contractType: input.contractType,
        contractDateDebut: input.contractDateDebut,
        contractDateFin: input.contractDateFin,
        salaireBase: input.salaireBase,
        avantagesNature: input.avantagesNature,
        periodeEssai: input.periodeEssai,
      },
      session,
    );
    logger.info(
      { employeeId: employee.id, actorId: actor.id, matricule: employee.matricule },
      "orchestration.employee_created",
    );
    return {
      actorId: actor.id,
      employeeId: employee.id,
      matricule: employee.matricule,
      status: employee.status,
      employee,
    };
  } catch (cause) {
    logger.error(
      { actorId: actor.id, cause: String(cause) },
      "orchestration.employee_creation_failed",
    );
    throw cause;
  }
}
