import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";

export const createMedicalVisitSchema = z
  .object({
    employeeId: uuidLike,
    dateVisite: z.iso.date(),
    medecin: z.string().trim().min(1).max(200),
    resultatAptitude: z.enum(["APTE", "APTE_AVEC_RESTRICTIONS", "INAPTE_TEMPORAIRE"]),
    restrictions: z.string().trim().max(1000).optional().nullable().or(z.literal("").transform(() => null)),
    prochaineEcheance: z.iso.date().nullable().optional(),
    certificatFileId: uuidLike.optional().nullable().or(z.literal("").transform(() => null)),
  })
  .refine(
    (v) => !v.prochaineEcheance || v.prochaineEcheance > v.dateVisite,
    { path: ["prochaineEcheance"], message: "Next checkup date must be after visit date" },
  );

export type CreateMedicalVisitFormValues = z.input<typeof createMedicalVisitSchema>;

export const createMedicalCertificateSchema = z
  .object({
    employeeId: uuidLike,
    typeCertificat: z.string().trim().min(1).max(120),
    dateEmission: z.iso.date(),
    dateExpiration: z.iso.date().nullable().optional(),
    statut: z.string().trim().min(1).max(60),
    fichierId: uuidLike.optional().nullable().or(z.literal("").transform(() => null)),
  })
  .refine(
    (v) => !v.dateExpiration || v.dateExpiration > v.dateEmission,
    { path: ["dateExpiration"], message: "Expiration date must be after emission date" },
  );

export type CreateMedicalCertificateFormValues = z.input<typeof createMedicalCertificateSchema>;
