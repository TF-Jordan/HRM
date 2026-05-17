import "server-only";

import { callKsm } from "../client";
import type {
  Employee,
  TerminateEmployeeInput,
  UpdateEmployeeInput,
} from "@/lib/types/hrm/employee";

type KsmCtx = {
  tenantId: string;
  organizationId: string;
  agencyId?: string | null;
  bearer: string;
};

const BASE = "/api/v1/hrm/employees";

export async function ksmListEmployees(ctx: KsmCtx): Promise<Employee[]> {
  return callKsm<Employee[]>(BASE, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    agencyId: ctx.agencyId,
    query: { organizationId: ctx.organizationId, agencyId: ctx.agencyId ?? undefined },
  });
}

export async function ksmGetEmployee(employeeId: string, ctx: KsmCtx): Promise<Employee> {
  return callKsm<Employee>(`${BASE}/${employeeId}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export type KsmCreateEmployeeBody = {
  actorId: string;
  numCnps: string | null;
  categorie: number;
  echelon: string | null;
  dateEmbauche: string;
  departmentCode: string | null;
  modePaiement: string;
  compteBancaire: string | null;
  numMobileMoney: string | null;
  operateurMm: string | null;
  contractType: string | null;
  contractDateDebut: string | null;
  contractDateFin: string | null;
  salaireBase: number | null;
  avantagesNature: number | null;
  periodeEssai: number | null;
};

export async function ksmCreateEmployee(
  body: KsmCreateEmployeeBody,
  ctx: KsmCtx,
): Promise<Employee> {
  return callKsm<Employee>(BASE, {
    method: "POST",
    body,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    agencyId: ctx.agencyId,
  });
}

export async function ksmUpdateEmployee(
  employeeId: string,
  body: UpdateEmployeeInput,
  ctx: KsmCtx,
): Promise<Employee> {
  return callKsm<Employee>(`${BASE}/${employeeId}`, {
    method: "PUT",
    body,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmTerminateEmployee(
  employeeId: string,
  body: TerminateEmployeeInput,
  ctx: KsmCtx,
): Promise<Employee> {
  return callKsm<Employee>(`${BASE}/${employeeId}/terminate`, {
    method: "PUT",
    body,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmSuspendEmployee(
  employeeId: string,
  reason: string,
  ctx: KsmCtx,
): Promise<Employee> {
  return callKsm<Employee>(`${BASE}/${employeeId}/suspend`, {
    method: "PUT",
    body: { reason },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmReactivateEmployee(
  employeeId: string,
  ctx: KsmCtx,
): Promise<Employee> {
  return callKsm<Employee>(`${BASE}/${employeeId}/reactivate`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
