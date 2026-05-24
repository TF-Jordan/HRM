import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";

export const runPayrollSchema = z.object({
  periode: z.string().regex(/^\d{4}-\d{2}$/, "Format: YYYY-MM"),
  agencyId: uuidLike.nullish(),
});

export type RunPayrollFormValues = z.input<typeof runPayrollSchema>;
