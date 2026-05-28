import "server-only";

import { HttpError } from "@/lib/types/api";
import { logger } from "@/server/logger";

export type BffSuccess<T> = { ok: true; data: T };
export type BffError = {
  ok: false;
  status: number;
  errorCode: string | null;
  message: string;
  fields?: Record<string, string[]>;
};

export function ok<T>(data: T, init?: ResponseInit): Response {
  return Response.json({ ok: true, data } satisfies BffSuccess<T>, init);
}

export function created<T>(data: T): Response {
  return ok(data, { status: 201 });
}

export function noContent(): Response {
  return new Response(null, { status: 204 });
}

export function fail(
  status: number,
  errorCode: string | null,
  message: string,
  fields?: Record<string, string[]>,
): Response {
  return Response.json(
    { ok: false, status, errorCode, message, fields } satisfies BffError,
    { status },
  );
}

export function fromHttpError(error: HttpError): Response {
  return fail(error.status, error.errorCode, error.message, error.validationErrors);
}

export function unexpected(cause: unknown, requestId?: string): Response {
  logger.error({ cause, requestId }, "bff.unexpected_error");
  const message = cause instanceof Error ? cause.message : "Unexpected error";
  return fail(500, "INTERNAL_ERROR", message);
}

export async function handleRoute<T>(
  handler: () => Promise<T>,
): Promise<Response> {
  try {
    const result = await handler();
    if (result instanceof Response) return result;
    return ok(result);
  } catch (cause) {
    if (cause instanceof HttpError) {
      return fromHttpError(cause);
    }
    return unexpected(cause);
  }
}
