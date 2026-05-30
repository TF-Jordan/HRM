import { z } from "zod";

const serverSchema = z.object({
  KSM_BASE_URL: z.string().url(),
  KSM_CLIENT_ID: z.string().min(1),
  KSM_API_KEY: z.string().min(1),

  SESSION_COOKIE_NAME: z.string().default("hrm_session"),
  SESSION_SECRET: z.string().min(32, "SESSION_SECRET must be at least 32 chars"),
  SESSION_TTL_SECONDS: z.coerce.number().int().positive().default(3600),

  EMAIL_PROVIDER: z.enum(["resend", "smtp", "none"]).default("none"),
  RESEND_API_KEY: z.string().optional(),
  SMTP_HOST: z.string().optional(),
  SMTP_PORT: z.coerce.number().int().positive().optional(),
  SMTP_SECURE: z
    .union([z.literal("true"), z.literal("false"), z.boolean()])
    .transform((v) => v === true || v === "true")
    .default(false),
  SMTP_USER: z.string().optional(),
  SMTP_PASS: z.string().optional(),
  EMAIL_FROM: z.string().default("noreply@hrcore.local"),
  EMAIL_REPLY_TO: z.string().optional(),

  LOG_LEVEL: z.enum(["fatal", "error", "warn", "info", "debug", "trace"]).default("info"),
  NODE_ENV: z.enum(["development", "test", "production"]).default("development"),
});

const clientSchema = z.object({
  NEXT_PUBLIC_APP_NAME: z.string().default("HR Core"),
  NEXT_PUBLIC_APP_URL: z.string().url(),
  NEXT_PUBLIC_DEFAULT_CURRENCY: z.string().default("XAF"),
  NEXT_PUBLIC_DEFAULT_LOCALE: z.enum(["fr", "en"]).default("fr"),
  NEXT_PUBLIC_SUPPORTED_LOCALES: z.string().default("fr,en"),
});

const isServer = typeof window === "undefined";

const parseServer = () => {
  if (!isServer) return null;
  const parsed = serverSchema.safeParse(process.env);
  if (!parsed.success) {
    console.error("❌ Invalid server env vars:", parsed.error.flatten().fieldErrors);
    throw new Error("Invalid server environment variables");
  }
  return parsed.data;
};

const parseClient = () => {
  const parsed = clientSchema.safeParse({
    NEXT_PUBLIC_APP_NAME: process.env.NEXT_PUBLIC_APP_NAME,
    NEXT_PUBLIC_APP_URL: process.env.NEXT_PUBLIC_APP_URL,
    NEXT_PUBLIC_DEFAULT_CURRENCY: process.env.NEXT_PUBLIC_DEFAULT_CURRENCY,
    NEXT_PUBLIC_DEFAULT_LOCALE: process.env.NEXT_PUBLIC_DEFAULT_LOCALE,
    NEXT_PUBLIC_SUPPORTED_LOCALES: process.env.NEXT_PUBLIC_SUPPORTED_LOCALES,
  });
  if (!parsed.success) {
    console.error("❌ Invalid client env vars:", parsed.error.flatten().fieldErrors);
    throw new Error("Invalid client environment variables");
  }
  return parsed.data;
};

export const serverEnv = parseServer();
export const clientEnv = parseClient();

export const SUPPORTED_LOCALES = clientEnv.NEXT_PUBLIC_SUPPORTED_LOCALES.split(",").map((s) =>
  s.trim(),
) as ("fr" | "en")[];
export const DEFAULT_LOCALE = clientEnv.NEXT_PUBLIC_DEFAULT_LOCALE;
