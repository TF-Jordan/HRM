import { z } from "zod";

export const createMissionSchema = z
  .object({
    employeeId: z.uuid(),
    destination: z.string().trim().min(1).max(160),
    objet: z.string().trim().min(1).max(500),
    dateDebut: z.iso.date(),
    dateFin: z.iso.date(),
    montantAvance: z.coerce.number().nonnegative(),
    centreCout: z
      .string()
      .trim()
      .max(60)
      .optional()
      .nullable()
      .or(z.literal("").transform(() => null)),
  })
  .refine((v) => v.dateFin >= v.dateDebut, {
    path: ["dateFin"],
    message: "End date must be after start date",
  });

export type CreateMissionFormValues = z.input<typeof createMissionSchema>;
