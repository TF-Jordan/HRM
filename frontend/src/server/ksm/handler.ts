import "server-only";

import { NextResponse } from "next/server";
import { ZodError, type ZodType } from "zod";
import { HttpError } from "@/lib/types/api";
import { logger } from "@/lib/log";

type Json = Record<string, unknown> | unknown[];

/**
 * Wraps a route-handler action with consistent error mapping:
 *  - HttpError (KSM upstream) -> mirror status + errorCode
 *  - ZodError -> 400 VALIDATION with issues
 *  - other -> 500 INTERNAL
 */
export async function withKsmHandler<T extends Json | undefined>(
  fn: () => Promise<T>,
  options?: { status?: number },
): Promise<NextResponse> {
  try {
    const data = await fn();
    return NextResponse.json(
      { success: true, data: data ?? null },
      { status: options?.status ?? 200 },
    );
  } catch (err) {
    if (err instanceof ZodError) {
      return NextResponse.json(
        {
          success: false,
          message: "Validation failed",
          errorCode: "VALIDATION",
          details: err.issues,
        },
        { status: 400 },
      );
    }
    if (err instanceof HttpError) {
      logger.warn(
        { status: err.status, errorCode: err.errorCode, message: err.message },
        "KSM call failed via BFF",
      );
      return NextResponse.json(
        {
          success: false,
          message: err.message,
          errorCode: err.errorCode ?? "UPSTREAM",
        },
        { status: mapStatus(err.status) },
      );
    }
    if (err instanceof Error && err.message === "Not authenticated") {
      return NextResponse.json(
        { success: false, message: "Unauthorized", errorCode: "UNAUTHORIZED" },
        { status: 401 },
      );
    }
    if (err instanceof Error && err.message === "No organisation context") {
      return NextResponse.json(
        {
          success: false,
          message: "No organisation context",
          errorCode: "ORGANIZATION_CONTEXT_REQUIRED",
        },
        { status: 400 },
      );
    }
    logger.error({ err: String(err) }, "Unexpected BFF error");
    return NextResponse.json(
      { success: false, message: "Unexpected error", errorCode: "INTERNAL" },
      { status: 500 },
    );
  }
}

function mapStatus(upstream: number): number {
  if (upstream === 401) return 401;
  if (upstream === 403) return 403;
  if (upstream === 404) return 404;
  if (upstream === 409) return 409;
  if (upstream === 422) return 422;
  if (upstream >= 400 && upstream < 500) return 400;
  return 502;
}

/**
 * Parses + validates JSON body against a Zod schema. Throws ZodError on miss.
 */
export async function parseBody<T>(request: Request, schema: ZodType<T>): Promise<T> {
  let raw: unknown;
  try {
    raw = await request.json();
  } catch {
    throw new HttpError({ status: 400, message: "Invalid JSON body", errorCode: "BAD_REQUEST" });
  }
  return schema.parse(raw);
}
