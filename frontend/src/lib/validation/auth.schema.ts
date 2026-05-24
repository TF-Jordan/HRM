import { z } from "zod";
import { uuidLike } from "@/lib/validation/uuid";

// Principal can be an email (admin@hrcore.local) OR a matricule (HRC-00007).
// Strict email validation is dropped; we only require a non-empty string.
export const loginSchema = z.object({
  email: z.string().trim().min(1).max(200),
  password: z.string().min(6).max(200),
});
export type LoginInput = z.infer<typeof loginSchema>;

export const mfaConfirmSchema = z.object({
  mfaToken: z.string().min(1),
  code: z.string().min(4).max(10),
});
export type MfaConfirmInput = z.infer<typeof mfaConfirmSchema>;

export const selectContextSchema = z.object({
  tenantId: uuidLike,
  organizationId: uuidLike,
  agencyId: uuidLike.nullish(),
});
export type SelectContextInput = z.infer<typeof selectContextSchema>;
