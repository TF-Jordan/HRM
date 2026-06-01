import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as employeesApi from "@/server/ksm/modules/employees";
import * as loansApi from "@/server/ksm/modules/loans";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

export async function GET() {
  return authenticatedRoute(async (session) => {
    const [loans, me] = await Promise.all([
      loansApi.listMyLoans(session),
      findMyEmployee(session),
    ]);

    let salaireBase: number | null = null;
    if (me) {
      try {
        const contracts = await employeesApi.listContracts(me.id, session);
        const active = contracts.find((c) => c.status === "ACTIVE");
        if (active) {
          const v = active.salaireBase;
          salaireBase = typeof v === "string" ? parseFloat(v) : v;
          if (isNaN(salaireBase as number)) salaireBase = null;
        }
      } catch {
        // salary info is optional — continue without it
      }
    }

    return Response.json({ ok: true, data: { loans, salaireBase } });
  });
}

export async function POST(request: NextRequest) {
  return authenticatedRoute(async (session) => {
    const body = await request.json();
    const data = await loansApi.requestMyLoan(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
