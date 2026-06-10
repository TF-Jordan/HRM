import "server-only";

import type { NextRequest } from "next/server";

import { requirePermissionRoute } from "@/server/handlers";
import * as api from "@/server/ksm/modules/payroll-employees";

/**
 * CSV import — accepts either raw text (Content-Type text/csv) or JSON {"csv": "..."}.
 * The first successful import flips the organization's payroll data source to LOCAL.
 */
export async function POST(request: NextRequest) {
  return requirePermissionRoute("hrm:payroll:run", async (session) => {
    const contentType = request.headers.get("content-type") ?? "";
    let csv: string;
    if (contentType.includes("application/json")) {
      const body = (await request.json()) as { csv?: string };
      csv = String(body.csv ?? "");
    } else {
      csv = await request.text();
    }
    if (!csv.trim()) {
      return Response.json(
        { ok: false, status: 400, errorCode: "VALIDATION_ERROR", message: "CSV content is empty" },
        { status: 400 },
      );
    }
    const data = await api.importPayrollEmployeesCsv(csv, session);
    return Response.json({ ok: true, data });
  });
}
