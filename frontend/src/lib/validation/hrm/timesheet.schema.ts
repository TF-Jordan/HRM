import { z } from "zod";

export const timesheetEntrySchema = z.object({
  date: z.iso.date(),
  projet: z.string().trim().max(120).optional().nullable().or(z.literal("").transform(() => null)),
  heuresNormales: z.coerce.number().min(0).max(24),
  heuresSupplementaires: z.coerce.number().min(0).max(24),
  description: z.string().trim().max(300).optional().nullable().or(z.literal("").transform(() => null)),
});

export const createTimesheetSchema = z.object({
  periode: z.iso.date(), // first day of the period
  entries: z.array(timesheetEntrySchema).min(1).max(31),
});

export type CreateTimesheetFormValues = z.input<typeof createTimesheetSchema>;
