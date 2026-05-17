import "server-only";

import { callKsm } from "../client";
import type { AddContractInput, Contract } from "@/lib/types/hrm/contract";

type KsmCtx = {
  tenantId: string;
  organizationId: string;
  bearer: string;
};

export async function ksmListContracts(employeeId: string, ctx: KsmCtx): Promise<Contract[]> {
  return callKsm<Contract[]>(`/api/v1/hrm/employees/${employeeId}/contracts`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmAddContract(
  employeeId: string,
  body: AddContractInput,
  ctx: KsmCtx,
): Promise<Contract> {
  return callKsm<Contract>(`/api/v1/hrm/employees/${employeeId}/contracts`, {
    method: "POST",
    body,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
