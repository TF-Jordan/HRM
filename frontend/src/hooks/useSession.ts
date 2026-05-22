"use client";

import { useQuery } from "@tanstack/react-query";

type SessionUser = {
  userId: string;
  actorId: string | null;
  email: string;
  displayName: string;
};

type SessionContext = {
  tenantId: string;
  organizationId: string;
  agencyId?: string | null;
};

type SessionPayload = {
  user: SessionUser;
  context: SessionContext;
  permissions: string[];
  expiresAt: string;
};

async function fetchSession(): Promise<SessionPayload | null> {
  const res = await fetch("/api/auth/me", {
    credentials: "include",
    headers: { Accept: "application/json" },
  });
  if (res.status === 401) return null;
  if (!res.ok) throw new Error(`Session fetch failed: ${res.status}`);
  const body = await res.json();
  return body.data as SessionPayload;
}

export function useSession() {
  return useQuery({
    queryKey: ["session"],
    queryFn: fetchSession,
    retry: false,
    staleTime: 30_000,
  });
}

export function usePermissions(): string[] {
  const { data } = useSession();
  return data?.permissions ?? [];
}

export function useHasPermission(permission: string | string[]): boolean {
  const perms = usePermissions();
  if (Array.isArray(permission)) {
    return permission.some((p) => perms.includes(p));
  }
  return perms.includes(permission);
}

export function hasAnyPermission(perms: string[], required: string[]): boolean {
  if (required.length === 0) return true;
  return required.some((r) => perms.includes(r));
}
