import { z } from "zod";

export const declarationTypeEnum = z.enum(["CNPS", "DIPE", "IRPP_CAC", "FNE", "CFC"]);
export const declarationFormatEnum = z.enum(["CSV", "XML", "EDI", "PDF"]);

export const createDeclarationSchema = z.object({
  type: declarationTypeEnum,
  periode: z.string().regex(/^\d{4}-\d{2}$/, "Format: YYYY-MM"),
  format: declarationFormatEnum,
});

export type CreateDeclarationFormValues = z.input<typeof createDeclarationSchema>;
