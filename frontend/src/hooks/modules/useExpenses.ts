"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { bffFetch, queryKeys } from "@/lib/api-client";
import type {
  AddExpenseLineInput,
  CreateExpenseInput,
  ExpenseLine,
  ExpenseReport,
} from "@/lib/types/hrm/expense";

export function useEmployeeExpenses(employeeId: string | undefined) {
  return useQuery({
    queryKey: employeeId
      ? queryKeys.hrm.employeeExpenses(employeeId)
      : ["hrm", "expenses", "_none"],
    queryFn: () => bffFetch<ExpenseReport[]>(`/api/hrm/expenses?employeeId=${employeeId}`),
    enabled: !!employeeId,
  });
}

export function useExpense(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.expense(id) : ["hrm", "expense", "_none"],
    queryFn: () => bffFetch<ExpenseReport>(`/api/hrm/expenses/${id}`),
    enabled: !!id,
  });
}

export function useExpenseLines(id: string | undefined) {
  return useQuery({
    queryKey: id ? queryKeys.hrm.expenseLines(id) : ["hrm", "expense-lines", "_none"],
    queryFn: () => bffFetch<ExpenseLine[]>(`/api/hrm/expenses/${id}/lines`),
    enabled: !!id,
  });
}

export function useCreateExpense(employeeId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateExpenseInput) =>
      bffFetch<ExpenseReport>("/api/hrm/expenses", {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      if (employeeId) qc.invalidateQueries({ queryKey: queryKeys.hrm.employeeExpenses(employeeId) });
    },
  });
}

export function useAddExpenseLine(expenseId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: AddExpenseLineInput) =>
      bffFetch<ExpenseLine>(`/api/hrm/expenses/${expenseId}/lines`, {
        method: "POST",
        body: JSON.stringify(input),
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.expense(expenseId) });
      qc.invalidateQueries({ queryKey: queryKeys.hrm.expenseLines(expenseId) });
    },
  });
}

export function useSubmitExpense(employeeId: string | undefined) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (expenseId: string) =>
      bffFetch<ExpenseReport>(`/api/hrm/expenses/${expenseId}/submit`, { method: "PUT" }),
    onSuccess: (_data, expenseId) => {
      qc.invalidateQueries({ queryKey: queryKeys.hrm.expense(expenseId) });
      if (employeeId) qc.invalidateQueries({ queryKey: queryKeys.hrm.employeeExpenses(employeeId) });
    },
  });
}
