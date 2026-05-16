import { z } from "zod";

export const loginSchema = z.object({
  email: z.email(),
  password: z.string().min(6).max(200),
});
export type LoginInput = z.infer<typeof loginSchema>;

export const mfaConfirmSchema = z.object({
  mfaToken: z.string().min(1),
  code: z.string().min(4).max(10),
});
export type MfaConfirmInput = z.infer<typeof mfaConfirmSchema>;

export const selectContextSchema = z.object({
  tenantId: z.uuid(),
  organizationId: z.uuid(),
  agencyId: z.uuid().nullish(),
});
export type SelectContextInput = z.infer<typeof selectContextSchema>;
