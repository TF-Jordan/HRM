import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as trainingsApi from "@/server/ksm/modules/trainings";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/** Employee self-service: submit a training request for manager/DRH approval. */
export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:training:request", async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json(
        {
          ok: false,
          status: 404,
          errorCode: "NO_EMPLOYEE",
          message: "No employee profile bound to this account.",
        },
        { status: 404 },
      );
    }
    const body = (await request.json()) as { trainingId: string; motivation?: string | null };
    const data = await trainingsApi.requestTraining(
      { trainingId: body.trainingId, employeeId: employee.id, motivation: body.motivation ?? null },
      session,
    );
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
