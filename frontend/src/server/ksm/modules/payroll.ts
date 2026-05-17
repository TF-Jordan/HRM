import "server-only";

import { callKsm } from "../client";
import type {
  PayrollEntry,
  PayrollRun,
  PayslipLine,
  RunPayrollInput,
} from "@/lib/types/hrm/payroll";

type KsmCtx = { tenantId: string; organizationId: string; agencyId?: string | null; bearer: string };

export async function ksmListPayrollRuns(ctx: KsmCtx): Promise<PayrollRun[]> {
  return callKsm<PayrollRun[]>(`/api/v1/hrm/payroll/runs`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: { organizationId: ctx.organizationId },
  });
}

export async function ksmGetPayrollRun(runId: string, ctx: KsmCtx): Promise<PayrollRun> {
  return callKsm<PayrollRun>(`/api/v1/hrm/payroll/runs/${runId}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmRunPayroll(input: RunPayrollInput, ctx: KsmCtx): Promise<PayrollRun> {
  return callKsm<PayrollRun>(`/api/v1/hrm/payroll/run`, {
    method: "POST",
    body: { periode: input.periode, agencyId: input.agencyId ?? null },
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    agencyId: ctx.agencyId,
  });
}

export async function ksmValidatePayrollRun(runId: string, ctx: KsmCtx): Promise<PayrollRun> {
  return callKsm<PayrollRun>(`/api/v1/hrm/payroll/runs/${runId}/validate`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListPayrollEntries(runId: string, ctx: KsmCtx): Promise<PayrollEntry[]> {
  return callKsm<PayrollEntry[]>(`/api/v1/hrm/payroll/runs/${runId}/entries`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmGetPayslipLines(entryId: string, ctx: KsmCtx): Promise<PayslipLine[]> {
  return callKsm<PayslipLine[]>(`/api/v1/hrm/payroll/entries/${entryId}/payslip`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
