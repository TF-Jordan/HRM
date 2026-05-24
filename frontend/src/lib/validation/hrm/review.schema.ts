import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";

export const createReviewSchema = z.object({
  employeeId: uuidLike,
  periode: z.string().trim().min(4).max(20),
});

export type CreateReviewFormValues = z.input<typeof createReviewSchema>;

export const addObjectiveSchema = z.object({
  description: z.string().trim().min(1).max(500),
  poids: z.coerce.number().min(1).max(100),
});

export type AddObjectiveFormValues = z.input<typeof addObjectiveSchema>;

export const submitReviewSchema = z.object({
  noteGlobale: z.coerce.number().min(0).max(20),
  commentaires: z.string().trim().min(1).max(2000),
  planAction: z
    .string()
    .trim()
    .max(2000)
    .optional()
    .nullable()
    .or(z.literal("").transform(() => null)),
});

export type SubmitReviewFormValues = z.input<typeof submitReviewSchema>;
