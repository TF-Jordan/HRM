import "server-only";

import pino from "pino";
import { serverEnv } from "@/env";

const isDev = serverEnv.NODE_ENV === "development";

export const logger = pino({
  level: serverEnv.LOG_LEVEL,
  base: { service: "hrm-frontend" },
  redact: {
    paths: [
      "*.password",
      "*.accessToken",
      "*.refreshToken",
      "*.apiKey",
      "headers.authorization",
      "headers['x-api-key']",
      "headers['x-client-id']",
    ],
    censor: "[REDACTED]",
  },
  transport: isDev
    ? {
        target: "pino-pretty",
        options: {
          colorize: true,
          translateTime: "SYS:HH:MM:ss.l",
          ignore: "pid,hostname",
        },
      }
    : undefined,
});
