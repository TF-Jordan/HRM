import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";

export const createJobOfferSchema = z
  .object({
    poste: z.string().trim().min(1).max(160),
    departement: z.string().trim().max(80).optional().nullable().or(z.literal("").transform(() => null)),
    localisation: z.string().trim().max(120).optional().nullable().or(z.literal("").transform(() => null)),
    competencesRequises: z.string().trim().max(1000).optional().nullable().or(z.literal("").transform(() => null)),
    dateLimite: z.iso.date().nullable().optional(),
    packageSalarial: z.string().trim().max(200).optional().nullable().or(z.literal("").transform(() => null)),
  })
  .refine(
    (v) => !v.dateLimite || v.dateLimite >= new Date().toISOString().slice(0, 10),
    { path: ["dateLimite"], message: "Application deadline must not be in the past" },
  );

export type CreateJobOfferFormValues = z.input<typeof createJobOfferSchema>;

export const createApplicationSchema = z.object({
  jobOfferId: uuidLike,
  candidatNom: z.string().trim().min(1).max(120),
  candidatPrenom: z.string().trim().min(1).max(120),
  candidatEmail: z.email(),
  candidatTelephone: z.string().trim().max(30).optional().nullable().or(z.literal("").transform(() => null)),
});

export type CreateApplicationFormValues = z.input<typeof createApplicationSchema>;
