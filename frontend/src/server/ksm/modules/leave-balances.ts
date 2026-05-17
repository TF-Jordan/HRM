import "server-only";

import { callKsm } from "../client";
import type { LeaveBalance } from "@/lib/types/hrm/leave-balance";

type KsmCtx = {
  tenantId: string;
  organizationId: string;
  bearer: string;
};

export async function ksmListLeaveBalances(
  employeeId: string,
  annee: number,
  ctx: KsmCtx,
): Promise<LeaveBalance[]> {
  return callKsm<LeaveBalance[]>(
    `/api/v1/hrm/employees/${employeeId}/leave-balances`,
    {
      method: "GET",
      bearer: ctx.bearer,
      tenantId: ctx.tenantId,
      organizationId: ctx.organizationId,
      query: { annee },
    },
  );
}
