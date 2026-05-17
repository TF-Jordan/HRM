import { z } from "zod";

export const dependentRelationshipEnum = z.enum([
  "SPOUSE",
  "CHILD",
  "PARENT",
  "SIBLING",
  "OTHER",
]);

export const addDependentSchema = z.object({
  nom: z.string().trim().min(1).max(120),
  prenom: z.string().trim().min(1).max(120),
  dateNaissance: z.iso.date(),
  lienParente: dependentRelationshipEnum,
});

export type AddDependentFormValues = z.input<typeof addDependentSchema>;
