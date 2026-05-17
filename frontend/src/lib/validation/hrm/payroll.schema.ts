import { z } from "zod";

export const runPayrollSchema = z.object({
  periode: z.string().regex(/^\d{4}-\d{2}$/, "Format: YYYY-MM"),
  agencyId: z.uuid().nullish(),
});

export type RunPayrollFormValues = z.input<typeof runPayrollSchema>;
