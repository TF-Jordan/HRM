import { z } from "zod";

export const createSkillSchema = z.object({
  name: z.string().trim().min(1).max(160),
  categorie: z.string().trim().max(80).optional().nullable().or(z.literal("").transform(() => null)),
  description: z.string().trim().max(500).optional().nullable().or(z.literal("").transform(() => null)),
});

export type CreateSkillFormValues = z.input<typeof createSkillSchema>;

export const createEmployeeSkillSchema = z.object({
  employeeId: z.uuid(),
  skillId: z.uuid(),
  niveauActuel: z.coerce.number().int().min(0).max(5),
  niveauAttendu: z.coerce.number().int().min(0).max(5),
  dateEvaluation: z.iso.date(),
});

export type CreateEmployeeSkillFormValues = z.input<typeof createEmployeeSkillSchema>;
