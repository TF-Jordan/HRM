import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as budgetApi from "@/server/ksm/modules/training-budgets";

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return requirePermissionRoute("hrm:budget:manage", async (session) => {
    const body = (await request.json()) as { montant?: number | string };
    const montant = Number(body.montant ?? 0);
    if (!(montant > 0)) {
      return Response.json(
        { ok: false, status: 400, errorCode: "BAD_REQUEST", message: "montant must be > 0" },
        { status: 400 },
      );
    }
    const data = await budgetApi.engageBudget(id, montant, session);
    return Response.json({ ok: true, data });
  });
}
