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
    const data = await payrollApi.listPayElements(session, countryCode);
    return Response.json({ ok: true, data });
  });
}

export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const body = (await request.json()) as Partial<payrollApi.CreatePayElementRequest>;
    if (!body.code || !body.label || !body.category || !body.method || !body.countryCode || !body.effectiveFrom) {
      return fail(
        400,
        "INVALID_BODY",
        "'code', 'label', 'category', 'method', 'countryCode' and 'effectiveFrom' are required.",
      );
    }
    const payload: payrollApi.CreatePayElementRequest = {
      code: body.code,
      label: body.label,
      category: body.category,
      method: body.method,
      baseReference: body.baseReference ?? null,
      rate: body.rate ?? null,
      ceiling: body.ceiling ?? null,
      floor: body.floor ?? null,
      exemptionThreshold: body.exemptionThreshold ?? null,
      flatAmount: body.flatAmount ?? null,
      bracketTableCode: body.bracketTableCode ?? null,
      lookupTableCode: body.lookupTableCode ?? null,
      taxable: body.taxable ?? false,
      socialContributable: body.socialContributable ?? false,
      countryCode: body.countryCode,
      displayOrder: body.displayOrder ?? 0,
      effectiveFrom: body.effectiveFrom,
      effectiveTo: body.effectiveTo ?? null,
    };
    const data = await payrollApi.createPayElement(payload, session);
    return Response.json({ ok: true, data }, { status: 201 });
  });
}
