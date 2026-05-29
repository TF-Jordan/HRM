import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as budgetApi from "@/server/ksm/modules/training-budgets";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:budget:read", async (session) => {
    const sp = request.nextUrl.searchParams;
    const annee = Number(sp.get("annee") ?? new Date().getFullYear());
    const data = await budgetApi.listBudgets(session, annee, sp.get("organizationId") ?? undefined);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:budget:create", async (session) => {
    const body = (await request.json()) as budgetApi.CreateTrainingBudgetRequest;
    const data = await budgetApi.createBudget(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
