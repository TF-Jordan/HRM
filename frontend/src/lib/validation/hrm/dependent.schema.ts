import { z } from "zod";

export const dependentRelationshipEnum = z.enum([
  "SPOUSE",
  "CHILD",
  "PARENT",
  "SIBLING",
  "OTHER",
]);

export const addDependentSchema = z
  .object({
    nom: z.string().trim().min(1).max(120),
    prenom: z.string().trim().min(1).max(120),
    dateNaissance: z.iso.date(),
    lienParente: dependentRelationshipEnum,
  })
  .refine(
    (v) => v.dateNaissance <= new Date().toISOString().slice(0, 10),
    { path: ["dateNaissance"], message: "Birth date must be in the past" },
  );

export type AddDependentFormValues = z.input<typeof addDependentSchema>;
