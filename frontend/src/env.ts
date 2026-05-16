import { z } from "zod";

const serverSchema = z.object({
  NODE_ENV: z.enum(["development", "test", "production"]).default("development"),
  LOG_LEVEL: z.enum(["fatal", "error", "warn", "info", "debug", "trace"]).default("info"),
  KSM_BASE_URL: z.url().default("http://localhost:8080"),
  KSM_CLIENT_ID: z.string().min(1).default("dev-platform-backend"),
  KSM_API_KEY: z.string().min(1).default("dev-api-key"),
  KSM_REQUEST_TIMEOUT_MS: z.coerce.number().int().positive().default(15000),
  SESSION_SECRET: z
    .string()
    .min(32, "SESSION_SECRET must be at least 32 bytes (base64) — generate via openssl rand -base64 48")
    .default("dev-only-secret-please-replace-in-prod-dev-only-secret-please-replace"),
  SESSION_COOKIE_NAME: z.string().default("hrm_session"),
  WORKSPACE_COOKIE_NAME: z.string().default("hrm_workspace"),
  CSRF_COOKIE_NAME: z.string().default("hrm_csrf"),
  SESSION_TTL_SECONDS: z.coerce.number().int().positive().default(3600),
});

const publicSchema = z.object({
  NEXT_PUBLIC_APP_NAME: z.string().default("HR Core"),
  NEXT_PUBLIC_DEFAULT_CURRENCY: z.string().default("XAF"),
  NEXT_PUBLIC_DEFAULT_LOCALE: z.string().default("fr"),
  NEXT_PUBLIC_SUPPORTED_LOCALES: z.string().default("fr,en"),
});

const parsedServer = serverSchema.safeParse({
  NODE_ENV: process.env.NODE_ENV,
  LOG_LEVEL: process.env.LOG_LEVEL,
  KSM_BASE_URL: process.env.KSM_BASE_URL,
  KSM_CLIENT_ID: process.env.KSM_CLIENT_ID,
  KSM_API_KEY: process.env.KSM_API_KEY,
  KSM_REQUEST_TIMEOUT_MS: process.env.KSM_REQUEST_TIMEOUT_MS,
  SESSION_SECRET: process.env.SESSION_SECRET,
  SESSION_COOKIE_NAME: process.env.SESSION_COOKIE_NAME,
  WORKSPACE_COOKIE_NAME: process.env.WORKSPACE_COOKIE_NAME,
  CSRF_COOKIE_NAME: process.env.CSRF_COOKIE_NAME,
  SESSION_TTL_SECONDS: process.env.SESSION_TTL_SECONDS,
});

if (!parsedServer.success) {
  console.error("[env] Invalid server environment variables:", z.treeifyError(parsedServer.error));
  throw new Error("Invalid environment configuration — see logs above");
}

const parsedPublic = publicSchema.safeParse({
  NEXT_PUBLIC_APP_NAME: process.env.NEXT_PUBLIC_APP_NAME,
  NEXT_PUBLIC_DEFAULT_CURRENCY: process.env.NEXT_PUBLIC_DEFAULT_CURRENCY,
  NEXT_PUBLIC_DEFAULT_LOCALE: process.env.NEXT_PUBLIC_DEFAULT_LOCALE,
  NEXT_PUBLIC_SUPPORTED_LOCALES: process.env.NEXT_PUBLIC_SUPPORTED_LOCALES,
});

if (!parsedPublic.success) {
  console.error("[env] Invalid public environment variables:", z.treeifyError(parsedPublic.error));
  throw new Error("Invalid public environment configuration");
}

export const serverEnv = parsedServer.data;
export const publicEnv = parsedPublic.data;
