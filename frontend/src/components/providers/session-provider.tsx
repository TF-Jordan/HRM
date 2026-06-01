"use client";

import * as React from "react";

import type { AppSession, SessionUser, WorkspaceContext } from "@/lib/types/auth";

export type ClientSession = {
  user: SessionUser;
  workspace?: WorkspaceContext;
  forcePasswordChange: boolean;
  expiresAt: number;
};

export type SessionContextValue = {
  session: ClientSession | null;
  setSession: (session: ClientSession | null) => void;
  refresh: () => Promise<void>;
};

const SessionContext = React.createContext<SessionContextValue | null>(null);

export function SessionProvider({
  initialSession,
  children,
}: {
  initialSession: ClientSession | null;
  children: React.ReactNode;
}) {
  const [session, setSession] = React.useState<ClientSession | null>(initialSession);

  const refresh = React.useCallback(async () => {
    try {
      const res = await fetch("/api/auth/me", { credentials: "include", cache: "no-store" });
      if (!res.ok) {
        setSession(null);
        return;
      }
      const body = (await res.json()) as { ok: boolean; data: ClientSession | null };
      setSession(body.ok && body.data ? body.data : null);
    } catch {
      setSession(null);
    }
  }, []);

  const value = React.useMemo<SessionContextValue>(
    () => ({ session, setSession, refresh }),
    [session, refresh],
  );

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession(): SessionContextValue {
  const ctx = React.useContext(SessionContext);
  if (!ctx) {
    throw new Error("useSession must be used within a SessionProvider");
  }
  return ctx;
}

export function useSessionUser(): SessionUser | null {
  return useSession().session?.user ?? null;
}

export function useWorkspace(): WorkspaceContext | null {
  return useSession().session?.workspace ?? null;
}

/** AppSession is server-only — this helper exists to align the client shape with the server. */
export type { AppSession };
