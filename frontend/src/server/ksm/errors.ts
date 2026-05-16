import "server-only";

import { HttpError, type ApiResponse } from "@/lib/types/api";

/**
 * Maps a KSM ApiResponse + HTTP status to a typed HttpError.
 * Always preserves errorCode for downstream UI mapping.
 */
export function ksmErrorFromResponse(opts: {
  status: number;
  body: unknown;
  fallbackMessage?: string;
}): HttpError {
  const { status, body, fallbackMessage = "Upstream error" } = opts;

  if (
    body &&
    typeof body === "object" &&
    "success" in body &&
    "message" in body &&
    "errorCode" in body
  ) {
    const env = body as ApiResponse<unknown>;
    return new HttpError({
      status,
      message: env.message || fallbackMessage,
      errorCode: env.errorCode ?? null,
      upstream: env,
    });
  }

  return new HttpError({
    status,
    message: fallbackMessage,
    errorCode: null,
    upstream: body,
  });
}

/**
 * Lookup table mapping known KSM errorCode tokens to i18n keys.
 * Used by the BFF/server to surface friendly client messages.
 */
export const KSM_ERROR_CODE_TO_I18N: Record<string, string> = {
  ORGANIZATION_CONTEXT_REQUIRED: "errors.organizationContextRequired",
  ORGANIZATION_SERVICE_NOT_SUBSCRIBED: "errors.organizationServiceNotSubscribed",
  INSUFFICIENT_LEAVE_BALANCE: "errors.insufficientLeaveBalance",
  DUPLICATE_EMPLOYEE: "errors.duplicateEmployee",
  INVALID_STATE: "errors.invalidState",
};
