import "server-only";

import { callKsm } from "../client";
import type { AddDependentInput, Dependent } from "@/lib/types/hrm/dependent";

type KsmCtx = {
  tenantId: string;
  organizationId: string;
  bearer: string;
};

export async function ksmListDependents(employeeId: string, ctx: KsmCtx): Promise<Dependent[]> {
  return callKsm<Dependent[]>(`/api/v1/hrm/employees/${employeeId}/dependents`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmAddDependent(
  employeeId: string,
  body: AddDependentInput,
  ctx: KsmCtx,
): Promise<Dependent> {
  return callKsm<Dependent>(`/api/v1/hrm/employees/${employeeId}/dependents`, {
    method: "POST",
    body,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
