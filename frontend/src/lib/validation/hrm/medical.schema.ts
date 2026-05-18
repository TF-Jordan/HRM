import { z } from "zod";

export const createMedicalVisitSchema = z.object({
  employeeId: z.uuid(),
  dateVisite: z.iso.date(),
  medecin: z.string().trim().min(1).max(200),
  resultatAptitude: z.enum(["APTE", "APTE_AVEC_RESTRICTIONS", "INAPTE_TEMPORAIRE"]),
  restrictions: z.string().trim().max(1000).optional().nullable().or(z.literal("").transform(() => null)),
  prochaineEcheance: z.iso.date().nullable().optional(),
  certificatFileId: z.uuid().optional().nullable().or(z.literal("").transform(() => null)),
});

export type CreateMedicalVisitFormValues = z.input<typeof createMedicalVisitSchema>;

export const createMedicalCertificateSchema = z.object({
  employeeId: z.uuid(),
  typeCertificat: z.string().trim().min(1).max(120),
  dateEmission: z.iso.date(),
  dateExpiration: z.iso.date().nullable().optional(),
  statut: z.string().trim().min(1).max(60),
  fichierId: z.uuid().optional().nullable().or(z.literal("").transform(() => null)),
});

export type CreateMedicalCertificateFormValues = z.input<typeof createMedicalCertificateSchema>;
