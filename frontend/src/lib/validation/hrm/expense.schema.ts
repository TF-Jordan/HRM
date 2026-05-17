import { z } from "zod";

export const expenseCategoryEnum = z.enum([
  "TRANSPORT",
  "MEAL",
  "HOTEL",
  "SUPPLIES",
  "MEDICAL",
  "OTHER",
]);

export const createExpenseSchema = z.object({
  periode: z.iso.date(),
  motif: z.string().trim().min(1).max(160),
});

export type CreateExpenseFormValues = z.input<typeof createExpenseSchema>;

export const addExpenseLineSchema = z.object({
  categorie: expenseCategoryEnum,
  description: z.string().trim().max(500).optional().nullable().or(z.literal("").transform(() => null)),
  montant: z.coerce.number().positive(),
});

export type AddExpenseLineFormValues = z.input<typeof addExpenseLineSchema>;
