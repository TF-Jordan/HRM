/**
 * Client-side helper around fetch() that talks to the BFF (/api/*) and unwraps
 * the BffSuccess / BffError envelope.
 */

export type BffSuccess<T> = { ok: true; data: T };
export type BffError = {
  ok: false;
  status: number;
  errorCode: string | null;
  message: string;
  fields?: Record<string, string[]>;
};

export class BffApiError extends Error {
  status: number;
  errorCode: string | null;
  fields?: Record<string, string[]>;

  constructor(error: BffError) {
    super(error.message);
    this.name = "BffApiError";
    this.status = error.status;
    this.errorCode = error.errorCode;
    this.fields = error.fields;
  }
}

export type ClientRequestInit = Omit<RequestInit, "body"> & {
  body?: unknown;
  json?: boolean;
};

export async function apiFetch<T>(path: string, init: ClientRequestInit = {}): Promise<T> {
  const { body, json = true, headers, ...rest } = init;
  const finalHeaders: Record<string, string> = {
    Accept: "application/json",
    ...((headers ?? {}) as Record<string, string>),
  };
  if (json && body !== undefined) {
    finalHeaders["Content-Type"] = "application/json";
  }
  const response = await fetch(path, {
    credentials: "include",
    ...rest,
    headers: finalHeaders,
    body:
      body === undefined
        ? undefined
        : typeof body === "string" || body instanceof FormData
          ? (body as BodyInit)
          : JSON.stringify(body),
  });

  const text = await response.text();
  let payload: unknown = null;
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      // non-JSON body
    }
  }

  if (!response.ok || (payload && typeof payload === "object" && (payload as { ok?: boolean }).ok === false)) {
    if (payload && typeof payload === "object" && (payload as { ok?: boolean }).ok === false) {
      throw new BffApiError(payload as BffError);
    }
    throw new BffApiError({
      ok: false,
      status: response.status,
      errorCode: null,
      message: response.statusText || "Request failed",
    });
  }

  if (payload && typeof payload === "object" && (payload as BffSuccess<T>).ok === true) {
    return (payload as BffSuccess<T>).data;
  }
  return payload as T;
}
