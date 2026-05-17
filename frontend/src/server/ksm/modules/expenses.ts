import "server-only";

import { callKsm } from "../client";
import type {
  AddExpenseLineInput,
  CreateExpenseInput,
  ExpenseLine,
  ExpenseReport,
} from "@/lib/types/hrm/expense";

type KsmCtx = { tenantId: string; organizationId: string; bearer: string };

export async function ksmListExpenses(employeeId: string, ctx: KsmCtx): Promise<ExpenseReport[]> {
  return callKsm<ExpenseReport[]>(`/api/v1/hrm/expenses`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
    query: { employeeId },
  });
}

export async function ksmGetExpense(id: string, ctx: KsmCtx): Promise<ExpenseReport> {
  return callKsm<ExpenseReport>(`/api/v1/hrm/expenses/${id}`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmListExpenseLines(id: string, ctx: KsmCtx): Promise<ExpenseLine[]> {
  return callKsm<ExpenseLine[]>(`/api/v1/hrm/expenses/${id}/lines`, {
    method: "GET",
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmCreateExpense(input: CreateExpenseInput, ctx: KsmCtx): Promise<ExpenseReport> {
  return callKsm<ExpenseReport>(`/api/v1/hrm/expenses`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmAddExpenseLine(
  id: string,
  input: AddExpenseLineInput,
  ctx: KsmCtx,
): Promise<ExpenseLine> {
  return callKsm<ExpenseLine>(`/api/v1/hrm/expenses/${id}/lines`, {
    method: "POST",
    body: input,
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmSubmitExpense(id: string, ctx: KsmCtx): Promise<ExpenseReport> {
  return callKsm<ExpenseReport>(`/api/v1/hrm/expenses/${id}/submit`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmApproveExpense(id: string, ctx: KsmCtx): Promise<ExpenseReport> {
  return callKsm<ExpenseReport>(`/api/v1/hrm/expenses/${id}/approve`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmRejectExpense(id: string, ctx: KsmCtx): Promise<ExpenseReport> {
  return callKsm<ExpenseReport>(`/api/v1/hrm/expenses/${id}/reject`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}

export async function ksmReimburseExpense(id: string, ctx: KsmCtx): Promise<ExpenseReport> {
  return callKsm<ExpenseReport>(`/api/v1/hrm/expenses/${id}/reimburse`, {
    method: "PUT",
    body: {},
    bearer: ctx.bearer,
    tenantId: ctx.tenantId,
    organizationId: ctx.organizationId,
  });
}
