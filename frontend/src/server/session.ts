import "server-only";

import { cookies } from "next/headers";
import { SignJWT, jwtVerify } from "jose";
import { serverEnv } from "@/env";
import type { Session } from "@/lib/types/auth";

const issuer = "hrm-frontend";
const audience = "hrm-session";

function getKey(): Uint8Array {
  return new TextEncoder().encode(serverEnv.SESSION_SECRET);
}

export async function signSession(session: Session): Promise<string> {
  return await new SignJWT({ session })
    .setProtectedHeader({ alg: "HS256" })
    .setIssuer(issuer)
    .setAudience(audience)
    .setIssuedAt()
    .setExpirationTime(Math.floor(session.expiresAt / 1000))
    .sign(getKey());
}

export async function verifySession(token: string): Promise<Session | null> {
  try {
    const { payload } = await jwtVerify(token, getKey(), { issuer, audience });
    return (payload as { session: Session }).session;
  } catch {
    return null;
  }
}

export async function getSession(): Promise<Session | null> {
  const store = await cookies();
  const cookie = store.get(serverEnv.SESSION_COOKIE_NAME);
  if (!cookie?.value) return null;
  return verifySession(cookie.value);
}

export async function requireSession(): Promise<Session> {
  const session = await getSession();
  if (!session) throw new Error("Not authenticated");
  if (session.expiresAt < Date.now()) throw new Error("Session expired");
  return session;
}

export async function setSession(session: Session): Promise<void> {
  const token = await signSession(session);
  const store = await cookies();
  store.set(serverEnv.SESSION_COOKIE_NAME, token, {
    httpOnly: true,
    secure: serverEnv.NODE_ENV !== "development",
    sameSite: "lax",
    path: "/",
    maxAge: Math.max(1, Math.floor((session.expiresAt - Date.now()) / 1000)),
  });
}

export async function clearSession(): Promise<void> {
  const store = await cookies();
  store.delete(serverEnv.SESSION_COOKIE_NAME);
  store.delete(serverEnv.WORKSPACE_COOKIE_NAME);
}

/**
 * Generates a CSRF token, stores it in a non-HttpOnly cookie so JS can echo it
 * back in X-CSRF-Token on mutations.
 */
export async function ensureCsrfToken(): Promise<string> {
  const store = await cookies();
  const existing = store.get(serverEnv.CSRF_COOKIE_NAME);
  if (existing?.value) return existing.value;
  const token = crypto.randomUUID();
  store.set(serverEnv.CSRF_COOKIE_NAME, token, {
    httpOnly: false,
    secure: serverEnv.NODE_ENV !== "development",
    sameSite: "lax",
    path: "/",
  });
  return token;
}
