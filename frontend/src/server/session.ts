import "server-only";

import { cookies } from "next/headers";
import { getIronSession } from "iron-session";

import { serverEnv } from "@/env";
import type { AppSession } from "@/lib/types/auth";

const SESSION_OPTIONS = {
  cookieName: serverEnv?.SESSION_COOKIE_NAME ?? "hrm_session",
  password: serverEnv?.SESSION_SECRET ?? "fallback-secret-do-not-use-in-production-12345678",
  cookieOptions: {
    httpOnly: true,
    sameSite: "lax" as const,
    secure: serverEnv?.NODE_ENV === "production",
    maxAge: serverEnv?.SESSION_TTL_SECONDS ?? 3600,
    path: "/",
  },
};

export async function getSession() {
  const cookieStore = await cookies();
  return getIronSession<Partial<AppSession>>(cookieStore, SESSION_OPTIONS);
}

export async function readSession(): Promise<AppSession | null> {
  const session = await getSession();
  if (!session.accessToken || !session.user) return null;
  // Verify expiration
  if (session.expiresAt && session.expiresAt * 1000 < Date.now()) {
    return null;
  }
  return session as AppSession;
}

export async function requireSession(): Promise<AppSession> {
  const session = await readSession();
  if (!session) {
    throw new Error("UNAUTHORIZED");
  }
  return session;
}

export async function destroySession(): Promise<void> {
  const session = await getSession();
  session.destroy();
}
