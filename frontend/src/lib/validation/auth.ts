import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().min(1, "validation.required").email("validation.email"),
  password: z.string().min(1, "validation.required"),
});

export type LoginFormValues = z.infer<typeof loginSchema>;

export const changePasswordSchema = z
  .object({
    currentPassword: z.string().min(1, "validation.required"),
    newPassword: z
      .string()
      .min(8, "validation.password.minLength")
      .regex(/[A-Z]/, "validation.password.uppercase")
      .regex(/[a-z]/, "validation.password.lowercase")
      .regex(/[0-9]/, "validation.password.digit")
      .regex(/[^A-Za-z0-9]/, "validation.password.special"),
    confirmPassword: z.string().min(1, "validation.required"),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    path: ["confirmPassword"],
    message: "validation.password.mismatch",
  });

export type ChangePasswordFormValues = z.infer<typeof changePasswordSchema>;

export const selectContextSchema = z.object({
  selectionToken: z.string().min(1),
  contextId: z.string().min(1),
  organizationId: z.string().uuid().optional(),
});

export type SelectContextValues = z.infer<typeof selectContextSchema>;
