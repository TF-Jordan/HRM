import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";

const optionalTrimmed = z
  .string()
  .trim()
  .max(200)
  .optional()
  .nullable()
  .transform((v) => (v === "" ? null : (v ?? null)));

const paymentModeEnum = z.enum(["BANK_TRANSFER", "MTN_MOBILE_MONEY", "ORANGE_MONEY", "CASH"]);
const operatorEnum = z.enum(["MTN", "ORANGE", "EU_MOBILE", "YOOMEE"]).nullable().optional();
const contractTypeEnum = z.enum(["CDD", "CDI", "STAGE", "INTERIM"]);

export const createEmployeeSchema = z
  .object({
    firstName: z.string().trim().min(1).max(120),
    lastName: z.string().trim().min(1).max(120),
    email: z.email(),
    phoneNumber: optionalTrimmed,
    photoFileId: uuidLike.optional().nullable().or(z.literal("").transform(() => null)),
    createAccount: z.boolean().optional().default(true),
    sendWelcomeEmail: z.boolean().optional().default(true),
    numCnps: optionalTrimmed,
    categorie: z.coerce.number().int().min(1).max(20),
    echelon: optionalTrimmed,
    poste: optionalTrimmed,
    dateEmbauche: z.iso.date(),
    departmentCode: optionalTrimmed,
    modePaiement: paymentModeEnum,
    compteBancaire: optionalTrimmed,
    numMobileMoney: optionalTrimmed,
    operateurMm: operatorEnum,
    contractType: contractTypeEnum.nullable().optional(),
    contractDateDebut: z.iso.date().nullable().optional(),
    contractDateFin: z.iso.date().nullable().optional(),
    salaireBase: z.coerce.number().nonnegative().nullable().optional(),
    avantagesNature: z.coerce.number().nonnegative().nullable().optional(),
    periodeEssai: z.coerce.number().int().nonnegative().nullable().optional(),
  })
  .refine(
    (v) => (v.modePaiement !== "BANK_TRANSFER" ? true : !!v.compteBancaire),
    { path: ["compteBancaire"], message: "Bank account required for BANK_TRANSFER" },
  )
  .refine(
    (v) =>
      v.modePaiement === "MTN_MOBILE_MONEY" || v.modePaiement === "ORANGE_MONEY"
        ? !!v.numMobileMoney
        : true,
    { path: ["numMobileMoney"], message: "Mobile money number required" },
  )
  .refine(
    (v) =>
      !v.contractDateDebut || !v.contractDateFin || v.contractDateFin >= v.contractDateDebut,
    { path: ["contractDateFin"], message: "Contract end date must be on or after start date" },
  )
  .refine(
    (v) =>
      !v.contractDateDebut || v.contractDateDebut >= v.dateEmbauche,
    { path: ["contractDateDebut"], message: "Contract start date must be on or after hire date" },
  );

export type CreateEmployeeFormValues = z.input<typeof createEmployeeSchema>;
export type CreateEmployeeParsed = z.output<typeof createEmployeeSchema>;

export const updateEmployeeSchema = z.object({
  numCnps: optionalTrimmed,
  categorie: z.coerce.number().int().min(1).max(20),
  echelon: optionalTrimmed,
  poste: optionalTrimmed,
  departmentCode: optionalTrimmed,
  modePaiement: paymentModeEnum,
  compteBancaire: optionalTrimmed,
  numMobileMoney: optionalTrimmed,
  operateurMm: operatorEnum,
});

export type UpdateEmployeeFormValues = z.input<typeof updateEmployeeSchema>;

export const terminateEmployeeSchema = z.object({
  terminationDate: z.iso.date(),
  reason: z.string().trim().min(3).max(500),
});

export const suspendEmployeeSchema = z.object({
  reason: z.string().trim().min(3).max(500),
});
