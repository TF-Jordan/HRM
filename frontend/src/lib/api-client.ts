import { HttpError, type ApiResponse } from "@/lib/types/api";

/**
 * Client-side fetcher for BFF routes. All BFF responses follow the
 * { success, data, message, errorCode } envelope.
 */
export async function bffFetch<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const res = await fetch(path, {
    credentials: "include",
    ...init,
    headers: {
      Accept: "application/json",
      ...(init.body && !(init.body instanceof FormData)
        ? { "Content-Type": "application/json" }
        : {}),
      ...(init.headers ?? {}),
    },
  });
  let body: unknown = null;
  const text = await res.text();
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = text;
    }
  }
  if (!res.ok) {
    const env = body as Partial<ApiResponse<unknown>> & { errorCode?: string | null };
    throw new HttpError({
      status: res.status,
      message: env?.message ?? `HTTP ${res.status}`,
      errorCode: env?.errorCode ?? null,
      upstream: body,
    });
  }
  const env = body as ApiResponse<T>;
  return env.data as T;
}

export const queryKeys = {
  hrm: {
    dashboard: () => ["hrm", "dashboard"] as const,
    employees: () => ["hrm", "employees"] as const,
    employee: (id: string) => ["hrm", "employees", id] as const,
    contracts: (employeeId: string) =>
      ["hrm", "employees", employeeId, "contracts"] as const,
    dependents: (employeeId: string) =>
      ["hrm", "employees", employeeId, "dependents"] as const,
    leaveBalances: (employeeId: string, annee: number) =>
      ["hrm", "employees", employeeId, "leave-balances", annee] as const,
  },
};
