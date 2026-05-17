import { z } from "zod";

export const contractTypeEnum = z.enum(["CDD", "CDI", "STAGE", "INTERIM"]);

export const addContractSchema = z
  .object({
    type: contractTypeEnum,
    dateDebut: z.iso.date(),
    dateFin: z.iso.date().nullable().optional(),
    salaireBase: z.coerce.number().positive(),
    avantagesNature: z.coerce.number().nonnegative().nullable().optional(),
    periodeEssai: z.coerce.number().int().nonnegative().nullable().optional(),
  })
  .refine(
    (v) => v.type !== "CDD" || !!v.dateFin,
    { path: ["dateFin"], message: "End date required for CDD" },
  )
  .refine(
    (v) => !v.dateFin || v.dateFin >= v.dateDebut,
    { path: ["dateFin"], message: "End date must be after start date" },
  );

export type AddContractFormValues = z.input<typeof addContractSchema>;
