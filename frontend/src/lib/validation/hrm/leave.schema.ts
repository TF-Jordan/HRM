import { z } from "zod";

export const leaveTypeEnum = z.enum([
  "ANNUAL",
  "SICK",
  "MATERNITY",
  "PATERNITY",
  "UNPAID",
  "SPECIAL",
]);

export const submitLeaveSchema = z
  .object({
    type: leaveTypeEnum,
    dateDebut: z.iso.date(),
    dateFin: z.iso.date(),
    motif: z.string().trim().max(500).optional().nullable().or(z.literal("").transform(() => null)),
  })
  .refine((v) => v.dateFin >= v.dateDebut, {
    path: ["dateFin"],
    message: "End date must be after start date",
  });

export type SubmitLeaveFormValues = z.input<typeof submitLeaveSchema>;

export const rejectLeaveSchema = z.object({
  commentaire: z.string().trim().min(1).max(500),
});
