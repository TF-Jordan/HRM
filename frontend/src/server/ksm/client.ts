import "server-only";

import { randomUUID } from "node:crypto";
import { serverEnv } from "@/env";
import { logger } from "@/lib/log";
import type { ApiResponse } from "@/lib/types/api";
import { ksmErrorFromResponse } from "./errors";

export type KsmCallOptions = {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
  bearer?: string | null;
  tenantId?: string | null;
  organizationId?: string | null;
  agencyId?: string | null;
  query?: Record<string, string | number | boolean | null | undefined>;
  signal?: AbortSignal;
  /** Override timeout in ms. */
  timeoutMs?: number;
  /** Used for tracing — propagated as X-Request-Id. */
  requestId?: string;
};

function buildUrl(path: string, query?: KsmCallOptions["query"]): string {
  const base = serverEnv.KSM_BASE_URL.replace(/\/$/, "");
  const prefixed = path.startsWith("/") ? path : "/" + path;
  if (!query) return base + prefixed;
  const sp = new URLSearchParams();
  for (const [k, v] of Object.entries(query)) {
    if (v !== undefined && v !== null && v !== "") sp.set(k, String(v));
  }
  const qs = sp.toString();
  return qs ? `${base}${prefixed}?${qs}` : base + prefixed;
}

/**
 * Single entry-point to call any KSM endpoint.
 * - Injects X-Client-Id, X-Api-Key (server secrets).
 * - Injects X-Tenant-Id, X-Organization-Id, X-Agency-Id when provided.
 * - Injects Bearer token when provided.
 * - Validates ApiResponse envelope and unwraps `data` on success.
 * - Maps non-2xx and `success:false` to typed HttpError.
 */
export async function callKsm<T>(path: string, opts: KsmCallOptions = {}): Promise<T> {
  const requestId = opts.requestId ?? randomUUID();
  const url = buildUrl(path, opts.query);
  const method = opts.method ?? "GET";

  const headers: Record<string, string> = {
    Accept: "application/json",
    "X-Client-Id": serverEnv.KSM_CLIENT_ID,
    "X-Api-Key": serverEnv.KSM_API_KEY,
    "X-Request-Id": requestId,
  };
  if (opts.bearer) headers["Authorization"] = `Bearer ${opts.bearer}`;
  if (opts.tenantId) headers["X-Tenant-Id"] = opts.tenantId;
  if (opts.organizationId) headers["X-Organization-Id"] = opts.organizationId;
  if (opts.agencyId) headers["X-Agency-Id"] = opts.agencyId;

  const init: RequestInit = {
    method,
    headers,
    cache: "no-store",
  };
  if (opts.body !== undefined && method !== "GET") {
    init.body = JSON.stringify(opts.body);
    headers["Content-Type"] = "application/json";
  }

  const controller = new AbortController();
  const timeoutMs = opts.timeoutMs ?? serverEnv.KSM_REQUEST_TIMEOUT_MS;
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  if (opts.signal) opts.signal.addEventListener("abort", () => controller.abort());
  init.signal = controller.signal;

  const start = performance.now();
  let response: Response;
  try {
    response = await fetch(url, init);
  } catch (err) {
    clearTimeout(timer);
    const aborted = controller.signal.aborted;
    logger.error(
      { requestId, url, method, err: String(err), aborted },
      "KSM call failed (network)",
    );
    throw aborted
      ? new Error("KSM request timed out")
      : (err as Error);
  }
  clearTimeout(timer);

  const elapsedMs = Math.round(performance.now() - start);
  const text = await response.text();
  let parsed: unknown = null;
  if (text) {
    try {
      parsed = JSON.parse(text);
    } catch {
      parsed = text;
    }
  }

  logger.debug(
    { requestId, url, method, status: response.status, elapsedMs },
    "KSM call",
  );

  if (!response.ok) {
    throw ksmErrorFromResponse({
      status: response.status,
      body: parsed,
      fallbackMessage: `KSM ${method} ${path} → ${response.status}`,
    });
  }

  if (parsed && typeof parsed === "object" && "success" in parsed) {
    const env = parsed as ApiResponse<T>;
    if (!env.success) {
      throw ksmErrorFromResponse({
        status: response.status,
        body: env,
        fallbackMessage: env.message || "KSM returned success=false",
      });
    }
    return env.data as T;
  }

  return parsed as T;
}

/**
 * Health probe — calls a public app endpoint that KSM always exposes.
 * KSM rejects with 400 (TENANT_CONTEXT_REQUIRED) when reachable but
 * missing context, which is good enough to confirm the upstream is alive.
 * Any 2xx / 4xx response is treated as "reachable" ; 5xx / network errors
 * are treated as down.
 */
export async function ksmHealth(): Promise<{ ok: boolean; status?: number; error?: string }> {
  try {
    const res = await fetch(`${serverEnv.KSM_BASE_URL}/api/v1/hrm/employees`, {
      method: "GET",
      cache: "no-store",
      headers: {
        "X-Client-Id": serverEnv.KSM_CLIENT_ID,
        "X-Api-Key": serverEnv.KSM_API_KEY,
      },
      signal: AbortSignal.timeout(5_000),
    });
    return { ok: res.status < 500, status: res.status };
  } catch (err) {
    return { ok: false, error: (err as Error).message };
  }
}
