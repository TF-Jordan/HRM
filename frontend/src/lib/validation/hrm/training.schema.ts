import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";

export const planTrainingSchema = z
  .object({
    intitule: z.string().trim().min(1).max(200),
    organisme: z.string().trim().max(160).optional().nullable().or(z.literal("").transform(() => null)),
    dateDebut: z.iso.date(),
    dateFin: z.iso.date(),
    cout: z.string().trim().regex(/^\d+(\.\d{1,2})?$/, "Montant invalide"),
    nbPlaces: z.coerce.number().int().min(1).max(1000),
    lieu: z.string().trim().max(200).optional().nullable().or(z.literal("").transform(() => null)),
  })
  .refine((v) => v.dateFin >= v.dateDebut, {
    path: ["dateFin"],
    message: "End date must be on or after start date",
  });

export type PlanTrainingFormValues = z.input<typeof planTrainingSchema>;

export const enrollEmployeeSchema = z.object({
  employeeId: uuidLike,
});

export type EnrollEmployeeFormValues = z.input<typeof enrollEmployeeSchema>;

export const completeEnrollmentSchema = z.object({
  note: z.string().trim().regex(/^\d+(\.\d{1,2})?$/).optional().nullable().or(z.literal("").transform(() => null)),
  attestationId: uuidLike.optional().nullable().or(z.literal("").transform(() => null)),
});

export type CompleteEnrollmentFormValues = z.input<typeof completeEnrollmentSchema>;

export const createTrainingBudgetSchema = z.object({
  annee: z.coerce.number().int().min(2000).max(2100),
  montantAlloue: z.string().trim().regex(/^\d+(\.\d{1,2})?$/, "Montant invalide"),
});

export type CreateTrainingBudgetFormValues = z.input<typeof createTrainingBudgetSchema>;

export const montantSchema = z.object({
  montant: z.string().trim().regex(/^\d+(\.\d{1,2})?$/, "Montant invalide"),
});

export type MontantFormValues = z.input<typeof montantSchema>;
