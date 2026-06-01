import "server-only";
import { pino } from "pino";

import { serverEnv } from "@/env";

const isDev = serverEnv?.NODE_ENV === "development";

export const logger = pino({
  level: serverEnv?.LOG_LEVEL ?? "info",
  ...(isDev
    ? {
        transport: {
          target: "pino-pretty",
          options: { colorize: true, translateTime: "SYS:HH:MM:ss" },
        },
      }
    : {}),
  redact: {
    paths: [
      "*.password",
      "*.accessToken",
      "*.refreshToken",
      "headers.authorization",
      'headers["x-api-key"]',
      "user.email",
    ],
    remove: true,
  },
});
