import "server-only";

import type { NextRequest } from "next/server";

import { fail } from "@/server/api-response";
import { requirePermissionRoute } from "@/server/handlers";
import * as payrollApi from "@/server/ksm/modules/payroll";

export async function GET(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:read", async (session) => {
    const countryCode = request.nextUrl.searchParams.get("countryCode");
    if (!countryCode) {
      return fail(400, "MISSING_COUNTRY", "Query parameter 'countryCode' is required.");
    }
    const data = await payrollApi.listLookupTables(session, countryCode);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const body = (await request.json()) as Partial<payrollApi.CreateLookupTableRequest>;
    if (!body.code || !body.label || !body.countryCode || !body.effectiveFrom) {
      return fail(400, "INVALID_BODY", "'code', 'label', 'countryCode' and 'effectiveFrom' are required.");
    }
    if (!Array.isArray(body.entries) || body.entries.length === 0) {
      return fail(400, "INVALID_BODY", "At least one entry is required.");
    }
    const payload: payrollApi.CreateLookupTableRequest = {
      code: body.code,
      label: body.label,
      countryCode: body.countryCode,
      effectiveFrom: body.effectiveFrom,
      effectiveTo: body.effectiveTo ?? null,
      entries: body.entries,
    };
    const data = await payrollApi.createLookupTable(payload, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
