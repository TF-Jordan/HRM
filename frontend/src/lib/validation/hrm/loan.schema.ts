import { z } from "zod";

export const requestLoanSchema = z.object({
  montant: z.coerce.number().positive().max(100_000_000),
  motif: z.string().trim().max(500).optional().nullable().or(z.literal("").transform(() => null)),
  nbEcheances: z.coerce.number().int().min(1).max(60),
});

export type RequestLoanFormValues = z.input<typeof requestLoanSchema>;
