import "server-only";

import { HttpError, type ApiResponse } from "@/lib/types/api";

/**
 * Convert a KSM HTTP Response into either a successful payload or a thrown HttpError.
 * Always expects an `ApiResponse<T>` envelope.
 */
export async function unwrapKsm<T>(res: Response, requestId?: string): Promise<T> {
  const text = await res.text();
  let envelope: ApiResponse<T> | null = null;
  if (text) {
    try {
      envelope = JSON.parse(text) as ApiResponse<T>;
    } catch {
      // Non-JSON response — treat as raw error
    }
  }

  if (!res.ok) {
    throw new HttpError({
      status: res.status,
      errorCode: envelope?.errorCode ?? null,
      message: envelope?.message ?? res.statusText ?? "Request failed",
      requestId,
    });
  }

  if (!envelope) {
    throw new HttpError({
      status: 500,
      errorCode: "INVALID_RESPONSE",
      message: "Backend returned an empty or non-JSON response",
      requestId,
    });
  }

  if (!envelope.success) {
    throw new HttpError({
      status: res.status,
      errorCode: envelope.errorCode,
      message: envelope.message,
      requestId,
    });
  }

  return envelope.data as T;
}
