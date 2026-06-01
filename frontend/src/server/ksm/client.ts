import "server-only";

import { serverEnv } from "@/env";
import type { AppSession } from "@/lib/types/auth";
import { logger } from "@/server/logger";

import { unwrapKsm } from "./errors";

type CallOptions = {
  method?: "GET" | "POST" | "PUT" | "DELETE" | "PATCH";
  body?: unknown;
  /** Pass when an authenticated user session must be attached (default true). */
  authenticated?: boolean;
  /** Override the active workspace headers — usually null to use session workspace. */
  organizationId?: string | null;
  agencyId?: string | null;
  /** Extra headers (override defaults). */
  headers?: Record<string, string>;
  /** Skip ApiResponse<T> unwrapping (e.g. binary downloads). */
  raw?: boolean;
  signal?: AbortSignal;
};

type CallContext = {
  session?: AppSession | null;
};

/**
 * Centralised KSM client used by all BFF Route Handlers + Server Components.
 * Injects X-Client-Id + X-Api-Key (server-only secrets) + workspace headers
 * + Authorization Bearer when a session is provided.
 */
export async function callKsm<T>(
  path: string,
  options: CallOptions = {},
  ctx: CallContext = {},
): Promise<T> {
  if (!serverEnv) throw new Error("callKsm must be called server-side");

  const requestId = crypto.randomUUID();
  const url = `${serverEnv.KSM_BASE_URL}${path}`;
  const method = options.method ?? "GET";
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "application/json",
    "X-Client-Id": serverEnv.KSM_CLIENT_ID,
    "X-Api-Key": serverEnv.KSM_API_KEY,
    "X-Request-Id": requestId,
    ...(options.headers ?? {}),
  };

  const authenticated = options.authenticated ?? true;
  const session = ctx.session ?? null;
  if (authenticated && session) {
    headers.Authorization = `Bearer ${session.accessToken}`;
    if (session.workspace) {
      headers["X-Tenant-Id"] = session.workspace.tenantId;
      const orgId = options.organizationId ?? session.workspace.organizationId;
      if (orgId) headers["X-Organization-Id"] = orgId;
      const agId = options.agencyId ?? session.workspace.agencyId;
      if (agId) headers["X-Agency-Id"] = agId;
    } else if (session.user.tenantId) {
      headers["X-Tenant-Id"] = session.user.tenantId;
    }
  }

  const init: RequestInit = {
    method,
    headers,
    cache: "no-store",
    signal: options.signal,
  };
  if (options.body !== undefined) {
    init.body = typeof options.body === "string" ? options.body : JSON.stringify(options.body);
  }

  const started = Date.now();
  let res: Response;
  try {
    res = await fetch(url, init);
  } catch (cause) {
    logger.error({ requestId, path, method, cause }, "ksm.fetch_failed");
    throw cause;
  }
  const durationMs = Date.now() - started;
  logger.debug(
    { requestId, path, method, status: res.status, durationMs },
    "ksm.call",
  );

  if (options.raw) {
    return res as unknown as T;
  }
  return unwrapKsm<T>(res, requestId);
}
