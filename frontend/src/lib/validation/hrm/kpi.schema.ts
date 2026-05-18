import { z } from "zod";

export const createKpiSnapshotSchema = z.object({
  periode: z.string().trim().regex(/^\d{4}-\d{2}$/, "Format attendu YYYY-MM"),
  effectifTotal: z.coerce.number().int().min(0),
  effectifActif: z.coerce.number().int().min(0),
  tauxTurnover: z.string().trim().regex(/^\d+(\.\d{1,4})?$/, "Taux invalide"),
  tauxAbsenteisme: z.string().trim().regex(/^\d+(\.\d{1,4})?$/, "Taux invalide"),
  masseSalariale: z.string().trim().regex(/^\d+(\.\d{1,2})?$/, "Montant invalide"),
  couvertureCompetences: z.string().trim().regex(/^\d+(\.\d{1,4})?$/, "Taux invalide"),
});

export type CreateKpiSnapshotFormValues = z.input<typeof createKpiSnapshotSchema>;
