import "server-only";

import type { NextRequest } from "next/server";

import { authenticatedRoute } from "@/server/handlers";
import * as medicalApi from "@/server/ksm/modules/medical";
import { findMyEmployee } from "@/server/orchestration/find-my-employee";

/**
 * Self-service: medical visits and certificates for the current employee.
 */
export async function GET() {
  return authenticatedRoute(async (session) => {
    const employee = await findMyEmployee(session);
    if (!employee) {
      return Response.json({ ok: true, data: { employee: null, visits: [], certificates: [] } });
    }
    const [visits, certificates] = await Promise.all([
      medicalApi.listVisitsByEmployee(employee.id, session),
      medicalApi.listCertificatesByEmployee(employee.id, session),
    ]);
    return Response.json({ ok: true, data: { employee, visits, certificates } });
  });
}

/**
 * Self-service: the current employee files their own medical certificate. The KSM endpoint resolves
 * the employee from the caller's actor and forces the status, so no employeeId is trusted here.
 */
export async function POST(request: NextRequest) {
  return authenticatedRoute(async (session) => {
    const body = (await request.json()) as medicalApi.SubmitMyCertificateRequest;
    const data = await medicalApi.submitMyCertificate(body, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
