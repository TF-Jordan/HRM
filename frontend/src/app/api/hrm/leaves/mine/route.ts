import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";
import * as leavesApi from "@/server/ksm/modules/leaves";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/**
 * Leave self-service payload for the authenticated employee: their leave requests plus the
 * per-type balances for the requested year (defaults to the current year). The employeeId is
 * resolved from session.actorId so the client never handles the underlying HRM id.
 */
export async function GET(request: NextRequest) {
  const year = Number(
    request.nextUrl.searchParams.get("year") ?? new Date().getFullYear(),
  );
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({
        ok: true,
        data: { employee: null, leaves: [], balances: [], year },
      });
    }

    const [leaves, balances] = await Promise.all([
      leavesApi.listLeavesByEmployee(employee.id, session),
      // Balances are optional — degrade gracefully if the read is not permitted.
      employeesApi
        .listLeaveBalances(employee.id, year, session)
        .catch(() => [] as employeesApi.LeaveBalanceResponse[]),
    ]);

    return Response.json({ ok: true, data: { employee, leaves, balances, year } });
  });
}
